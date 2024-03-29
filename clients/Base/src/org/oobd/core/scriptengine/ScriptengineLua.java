/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.scriptengine;

import org.oobd.core.Message;
import org.oobd.core.Settings;
import org.oobd.core.Base64Coder;
import org.oobd.core.IFsystem;
import org.oobd.core.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Iterator;

import org.oobd.core.support.Onion;

import java.io.InputStream;
import java.io.IOException;

import java.io.*;
import java.io.File.*;
import java.util.MissingResourceException;
import org.oobd.core.support.OnionNoEntryException;

import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.MathLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.stdlib.TableLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;
import se.krka.kahlua.vm.LuaTable;
import se.krka.kahlua.vm.LuaTableImpl;
import org.json.JSONException;
import static org.oobd.core.OOBDConstants.DP_ACTIVE_ARCHIVE;
import org.oobd.core.archive.Archive;

/**
 *
 * @author steffen
 */
public class ScriptengineLua extends OobdScriptengine {

    public static final String ENG_LUA_DEFAULT = "OOBD.lbc";
    public static final String ENG_LUA_STDLIB = "stdlib.lbc";
    private LuaState state;
    private LuaCallFrame callFrame;
    private String scriptDir;
    private String connectURL;

    public ScriptengineLua(String id, Core myCore, IFsystem mySystem) {
        super(id, myCore, mySystem, "ScriptengineLua id " + id);
        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.CONFIG,
                "Construct ScriptengineLua instance " + id);
        myself = this;
        Settings.writeDataPool(DP_LAST_CREATED_SCRIPTENGINE, this);
    }

    @Override
    public String getPluginName() {
        return "se:Lua";
    }

    public static String publicName() {
        return "Script";
    }

    /**
     * @todo open & write files MUST go through an generic stream supported by
     * core to handle different sources and encrypted files
     */
    public void run() {
        state = new LuaState(System.out);
        BaseLib.register(state);
        MathLib.register(state);
        StringLib.register(state);
        CoroutineLib.register(state);
        TableLib.register(state);

        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.CONFIG,
                "Try to initialize Lua engine");
        register("openChannelCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                // cellList = new List();
                String connectURLParameter = getString(0);
                if (connectURLParameter != null && !"".equals(connectURLParameter)) {
                    connectURL = Base64Coder.encodeString(connectURLParameter);
                }
                try {
                    myself.getMsgPort().sendAndWait(
                            new Message(myself, CoreMailboxName, new Onion(""
                                            + "{'type':'" + CM_CHANNEL + "'"
                                            + ",'owner':'" + myself.getId() + "'"
                                            + ",'command':'connect'"
                                            + ",'connecturl':'" + connectURL + "'" //just as reminder : connectURL comes already base64 coded
                                            + "}")), -1);
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                // finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("openPageCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                // cellList = new List();
                try {
                    core.transferMsg(new Message(myself, UIHandlerMailboxName,
                            new Onion("" + "{'type':'" + CM_PAGE + "',"
                                    + "'owner':'" + myself.getId() + "',"
                                    + "'name':'" + getString(0) + "'}")));
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                // finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("addElementCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                try {
                    String updevent = "";
                    int oobdElementFlags = getInt(3);
                    if (oobdElementFlags > 0) {
                        updevent = "'" + FN_UPDATEOPS + "':" + oobdElementFlags
                                + ",";
                    }
                    String optid = getString(4); // String id
                    // Android: String.isEmpty() not available
                    // if (!optid.isEmpty()) {
                    if (optid.length() != 0) {
                        optid = "'optid':'" + optid + "',";
                    }
                    Onion myOnion = new Onion("" + "{'type':'" + CM_VISUALIZE + "',"
                            + "'owner':" + "{'name':'" + myself.getId()
                            + "'}," + updevent + optid + "'tooltip':'"
                            + Base64Coder.encodeString(getString(0))
                            + "'," + "'value':'"
                            + Base64Coder.encodeString(getString(2))
                            + "'," + "'name':'" + getString(1) + ":"
                            + getString(4) + "'}");
                    Onion optTable = getLuaTable(5);
                    if (optTable != null) {
                        myOnion.setValue("opts", optTable);
                    }
                    core.transferMsg(new Message(myself, UIHandlerMailboxName,
                            myOnion));

                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("openXCVehicleDataCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                Onion openXCJson = getLuaTable(0);
                if (openXCJson != null) {
                    core.getSystemIF().openXCVehicleData(openXCJson);
                }
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("pageDoneCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                // cellList = new List();
                try {
                    core.transferMsg(new Message(myself, UIHandlerMailboxName,
                            new Onion("" + "{'type':'" + CM_PAGEDONE + "',"
                                    + "'owner':'" + myself.getId() + "',"
                                    + "'name':'Canvastest_1'}")));
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                // finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("serReadLnCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                String result = "";
                Message answer = null;
                try {
                    answer = myself.getMsgPort().sendAndWait(
                            new Message(myself, BusMailboxName, new Onion(""
                                            + "{'type':'" + CM_BUSTEST + "',"
                                            + "'owner':" + "{'name':'" + myself.getId()
                                            + "'}," + "'command':'serReadLn',"
                                            + "'timeout':'" + getInt(0) + "',"
                                            + "'ignore':'"
                                            + Boolean.toString(getBoolean(1)) + "'}")),
                            +(getInt(0) * 12) / 10);
                    if (answer != null) {
                        result = answer.getContent().getString("result");
                        if (result != null && result.length() > 0) {
                            result = new String(Base64Coder.decodeString(result));
                        }
                        // } catch (IOException ex) {
                        // Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE,
                        // null, ex);
                        // }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                callFrame.push(result.intern());
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("serWaitCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                initRPC(callFrame, nArguments);
                Logger.getLogger(ScriptengineLua.class.getName()).log(
                        Level.INFO,
                        "Lua calls serWait with string data:>" + getString(0)
                        + "<");
                double result = 0;
                if (getString(0) != null) { // send only if string contains
                    // anything to wait for
                    // BaseLib.luaAssert(nArguments >0, "not enough args");
                    Message answer = null;
                    try {
                        answer = myself.getMsgPort().sendAndWait(
                                new Message(
                                        myself,
                                        BusMailboxName,
                                        new Onion(
                                                ""
                                                + "{'type':'"
                                                + CM_BUSTEST
                                                + "',"
                                                + "'owner':"
                                                + "{'name':'"
                                                + myself.getId()
                                                + "'},"
                                                + "'command':'serWait',"
                                                + "'timeout':'"
                                                + getInt(1)
                                                + "',"
                                                + "'data':'"
                                                + Base64Coder.encodeString(getString(0))
                                                + "'}")),
                                +(getInt(1) * 12) / 10);
                        if (answer != null) {
                            Logger.getLogger(ScriptengineLua.class.getName()).log(Level.INFO,
                                    "Lua calls serWait returns with onion:"
                                    + answer.getContent().toString());
                            result = answer.getContent().getInt("result");

                        }

                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
               callFrame.push(result);
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("serSleepCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                try {
                    Thread.sleep(getInt(0));
                } catch (InterruptedException e) {
                    // the VM doesn't want us to sleep anymore,
                    // so get back to work
                }
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("serWriteCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {

                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                try {
                    core.transferMsg(new Message(myself, BusMailboxName,
                            new Onion("{" + "'type':'" + CM_BUSTEST + "',"
                                    + "'owner':" + "{'name':'" + myself.getId()
                                    + "'}," + "'command':'serWrite',"
                                    + "'data':'"
                                    + Base64Coder.encodeString(getString(0))
                                    + "'" + "}")));
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                // if (btComm != null) {
                // btComm.write(getString(0));
                // }
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("serFlushCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                try {
                    core.transferMsg(new Message(myself, BusMailboxName,
                            new Onion("" + "{'type':'" + CM_BUSTEST + "',"
                                    + "'owner':" + "{'name':'" + myself.getId()
                                    + "'}," + "'command':'serFlush'}")));
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                // if (btComm != null) {
                // btComm.flush();
                // }
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("serDisplayWriteCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {

                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                try {

                    String msg = "{" + "'type':'" + CM_WRITESTRING + "',"
                            + "'owner':" + "{'name':'" + myself.getId()
                            + "'}," + "'command':'serDisplayWrite',"
                            + "'data':'"
                            + Base64Coder.encodeString(getString(0))
                            + "'";
                    String cmd = getString(1);
                    if (cmd != null) {
                        msg = msg + " , 'modifier':'"
                                + Base64Coder.encodeString(cmd)
                                + "'";
                    }
                    msg = msg + "}";
                    core.transferMsg(new Message(myself, UIHandlerMailboxName,
                            new Onion(msg)));

                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("msgBoxCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {

                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                String result = "";
                String typeOfBox = getString(0).toLowerCase();
                if ("alert".equals(typeOfBox)) {
                    core.userAlert(getString(2), getString(1));
                } else {
                    try {
                        String msg = "{'" + CM_PARAM + "' : { "
                                + "'type':'String'," + "'title':'"
                                + Base64Coder.encodeString(getString(1))
                                + "'," + "'default':'"
                                + Base64Coder.encodeString(getString(3))
                                + "',"
                                + "'text':'"
                                + Base64Coder.encodeString(getString(2))
                                + "'";
                        if ("confirm".equals(typeOfBox)) {
                            msg = msg + " , 'confirm':'yes'";
                        }
                        msg = msg + "}}";
                        Onion answer = core.requestParamInput(myself, new Onion(msg));
                        Onion answerOnion = answer.getOnion("answer");
                        if (answerOnion != null) {
                            result = answerOnion.getString("answer");
                            if (result != null && result.length() > 0) {
                                result = Base64Coder.decodeString(result);
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                }
                callFrame.push(result.intern());
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("onionMsgCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                Onion result = new Onion();
                Message answer = null;
                try {
                    answer = myself.getMsgPort().sendAndWait(
                            new Message(myself, DBName, new Onion(""
                                            + "{'type':'"
                                            + CM_DBLOOKUP
                                            + "',"
                                            + "'owner':"
                                            + "{'name':'"
                                            + myself.getId()
                                            + "'},"
                                            + "'command':'lookup',"
                                            + "'dbfilename':'"
                                            + Base64Coder.encodeString(scriptDir
                                                    + getString(0))
                                            + "'," + "'key':'" + getString(1) + "'}")),
                            -1);
                    if (answer != null) {
                        result = answer.getContent().getOnion("result");
                        // } catch (IOException ex) {
                        // Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE,
                        // null, ex);
                        // }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                // Object tab = callFrame.get(0);
                // System.err.println(tab.getClass().getName());
                LuaTableImpl newTable = new LuaTableImpl();
                // try {
                // newTable = onion2Lua(new Onion("{" + "'type':'" +
                // CM_WRITESTRING + "'," + "'owner':" + "{'name':'" +
                // myself.getId() + "'}," + "'command':'serDisplayWrite'," +
                // "'data':'" + "blablub" + "'" + "}"));
                newTable = onion2Lua(result);
                /*
                 * } catch (JSONException ex) {
                 * Logger.getLogger(ScriptengineLua.
                 * class.getName()).log(Level.SEVERE, null, ex); }
                 */
                // newTable.rawset("Testkey", "Testvalue");
                callFrame.push(newTable);
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("dbLookupCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                Onion result = new Onion();
                Message answer = null;
                try {
                    answer = myself.getMsgPort().sendAndWait(
                            new Message(myself, DBName, new Onion(""
                                            + "{'type':'"
                                            + CM_DBLOOKUP
                                            + "',"
                                            + "'owner':"
                                            + "{'name':'"
                                            + myself.getId()
                                            + "'},"
                                            + "'command':'lookup',"
                                            + "'dbfilename':'"
                                            + Base64Coder.encodeString(
                                                    //                                                    scriptDir +
                                                    //                                                    "/" + 
                                                    getString(0)
                                            )
                                            + "',"
                                            // + "'dbfilename':'" + getString(0) + "',"
                                            + "'key':'"
                                            + Base64Coder.encodeString(getString(1))
                                            + "'}")), -1);
                    if (answer != null) {
                        result = answer.getContent().getOnion("result");
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }

                LuaTableImpl newTable = new LuaTableImpl();
                newTable = onion2Lua(result);
                callFrame.push(newTable);
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("ioInputCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                initRPC(callFrame, nArguments);
                String potentialJSONParam;
                Onion optTable = null;
                Logger.getLogger(ScriptengineLua.class.getName()).log(
                        Level.INFO,
                        "Lua calls ioInputCall with string data:>" + getString(0)
                        + "<");
                try {
                    optTable = getLuaTable(1);
                } catch (ClassCastException ex) {
                    optTable = null;
                }
                if (optTable != null) {
                    potentialJSONParam = optTable.toString();
                } else {
                    potentialJSONParam = getString(1);
                }

                String result = createInputTempFile(getString(0), potentialJSONParam, getString(2));
                callFrame.push(result.intern());
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });
        register("ioReadCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {

                // BaseLib.luaAssert(nArguments >0, "not enough args");
                initRPC(callFrame, nArguments);
                Logger.getLogger(ScriptengineLua.class.getName()).log(
                        Level.INFO,
                        "Lua calls ioReadCall with string data:>" + getString(0)
                        + "<");

                String result = readTempInputFile(getString(0));

                if ("*json".equalsIgnoreCase(getString(0))) {
                    Onion answer = null;
                    try {
                        answer = new Onion(result);
                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }

                    LuaTableImpl newTable = new LuaTableImpl();
                    newTable = onion2Lua(answer);
                    callFrame.push(newTable);
                } else {
                    if (result == null) {
                        callFrame.push(null);
                    } else {
                        callFrame.push(result.intern());
                    }
                }
                finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        String scriptFileName = null;
        if (myStartupParam != null) {
            scriptFileName = myStartupParam.getOnionString("scriptpath",null);
            connectURL = myStartupParam.getOnionString("connecturl","");
        }
        // given filename overrides config settings
        if (scriptFileName == null) {
            scriptFileName = ENG_LUA_DEFAULT;
        }
        try {
            try {
                scriptDir = (new File(UISystem.getSystemDefaultDirectory(false,
                        scriptFileName))).getParentFile().getAbsolutePath() + "/";
            } catch (Exception ex) {
                scriptDir = "";
            }
            if (!doScript(scriptFileName)) { //fire an page done event 
                keepRunning = false;
/*                try {
                    core.transferMsg(new Message(myself, UIHandlerMailboxName,
                            new Onion("" + "{'type':'" + CM_PAGEDONE + "',"
                                    + "'owner':'" + myself.getId() + "',"
                                    + "'name':'Canvastest_1'}")));
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineLua.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
  */    
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE,
                    "couldn't run script engine", ex);
        }
        while (keepRunning == true) {
            Message msg = msgPort.getMsg(10);
            if (msg != null) {
                Onion on = msg.getContent();
                String vis = on.getOnionString("vis","");
                Logger.getLogger(ScriptengineLua.class.getName()).log(Level.INFO,
                        "Msg received:" + msg.getContent().toString());
                try {
                    if (CM_UPDATE.equals(on.get("type"))) {
                        core.transferMsg(new Message(
                                this,
                                UIHandlerMailboxName,
                                new Onion(
                                        ""
                                        + "{'type':'"
                                        + CM_VALUE
                                        + "',"
                                        + "'owner':"
                                        + "{'name':'"
                                        + this.id
                                        + "'},"
                                        + "'to':"
                                        + "{'name':'"
                                        + vis
                                        + "'},"
                                        // + "'value':'" +
                                        // Integer.toString(i)
                                        // adds a preformed JSON string, containing a value string plus an optional "dataset" sub- table
                                        + callFunction(
                                                vis,
                                                new Object[]{
                                                    Base64Coder.decodeString(on.getOnionString("actValue","")),
                                                    on.getOnionString("optid",""),
                                                    (double) on.optInt("updType")})
                                        + "}")));
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ScriptengineTerminal.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        }
        // end of objects lifetime
        System.out.println("End of Scriptengine" + this.toString());
        Settings.writeDataPool(DP_RUNNING_SCRIPTENGINE, null);

    }

    public void close() {
        System.out.println("Send Scriptengine close request" + this.toString());
        getMsgPort().interuptWait();
        keepRunning = false;
        /*
         try {
         core.transferMsg(
         new Message(myself, BusMailboxName, new Onion(""
         + "{'type':'" + CM_BUSTEST + "'"
         + ",'owner':'" + myself.getId() + "'"
         + ",'command':'close'"
         + "}")));
         } catch (JSONException ex) {
         Logger.getLogger(ScriptengineLua.class.getName()).log(
         Level.SEVERE, null, ex);
         }
         try { // send a message to myself to make sure that the receive loop ends

         getMsgPort().receive(new Message(myself, myself.getId(), new Onion(""
         + "{'type':'" + CM_CHANNEL + "'"
         + ",'owner':'" + myself.getId() + "'"
         + ",'command':'connect'"
         + "}")));
         } catch (JSONException ex) {
         Logger.getLogger(ScriptengineLua.class.getName()).log(
         Level.SEVERE, null, ex);
         }
         */ super.close();
    }

    public boolean doScript(String fileName) throws IOException {
         Archive scriptArchive = (Archive) Settings.readDataPool(DP_ACTIVE_ARCHIVE, null);
        Settings.writeDataPool(DP_RUNNING_SCRIPT_NAME, scriptArchive.getID() + "/" + scriptArchive.getProperty(MANIFEST_SCRIPTNAME, fileName));
        System.out.println("Scriptengine: Waiting for WS to connect " + this.toString());
        while (keepRunning && (!(Boolean) Settings.readDataPool(DP_WEBUI_WS_READY_SIGNAL, false) || (Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null) != null && Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null) != this))) {
            try {
                Thread.sleep(100);
                OobdScriptengine oldEngine = (OobdScriptengine) Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null);
                if (oldEngine != null) {
                    System.out.println("doScript: Old Scriptengine not finished yet:" + oldEngine.toString());
                }
            } catch (InterruptedException ex) {
            }
        }
        if (keepRunning) {
            System.out.println("Scriptengine: WS connected " + this.toString());
            Settings.writeDataPool(DP_RUNNING_SCRIPTENGINE, this);
            InputStream resource = core.generateResourceStream(FT_SCRIPT,
                    fileName);
            if (resource == null) {
                return false;
            }
            LuaClosure callback = LuaPrototype.loadByteCode(resource,
                    state.getEnvironment());
            try{
            state.call(callback, null, null, null);
            Logger.getLogger(ScriptengineLua.class.getName()).log(Level.CONFIG,
                    "Start Lua Script" + fileName);
            return true;
            }catch(java.lang.RuntimeException ex){
                keepRunning=false;
                Logger.getLogger(ScriptengineLua.class.getName()).log(Level.WARNING,"Lua script crashed during initialisation: Please go back to main page",
                     fileName);
               core.userAlert(
                        "Lua script crashed during initialisation", "Script Failure");
            return false;
            }
        } else {
            System.out.println("Scriptengine: Aborting doScript");
        }
        return false;
    }

    public String callFunction(String functionName, Object[] params) {
        functionName = functionName.substring(0, functionName.lastIndexOf(':')); // removing
        // the
        // index
        // behind
        // the
        // :
        // seperator
        String res = "'value':'";
        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.INFO,
                "function to call in Lua:" + functionName);
        try {
            LuaClosure fObject = (LuaClosure) state.getEnvironment().rawget(
                    functionName);
            Object[] results = state.pcall(fObject, params);
            if (results[0] != Boolean.TRUE) {
                Object errorMessage = results[1];
                Logger.getLogger(ScriptengineLua.class.getName()).log(Level.INFO,
                        "Lua Crash: " + errorMessage);
                Logger.getLogger(ScriptengineLua.class.getName()).log(Level.INFO,
                        results[2].toString());
                /*
                 Throwable stacktrace = (Throwable) (results[3]);
                
                 if (stacktrace != null) {
                 stacktrace.printStackTrace();
                 }
                 */
            }

            // String response = BaseLib.rawTostring(fObject.env.rawget(1));
            // fObject.push(response.intern());
            if (results.length > 1) {
                res += Base64Coder.encodeString((String) results[1]);
            }
            res += "'";
            if (results[0] == Boolean.TRUE && results.length > 2 && results[2] instanceof LuaTableImpl) {
                res += ", 'dataset':" + lua2Onion((LuaTableImpl) results[2]);
            }
            return res;

        } catch (Exception ex) {
            return "'value':'" + Base64Coder.encodeString(ex.toString()) + "'";
        }
    }

    public String GetVarString(String var) {
        return "";
    }

    public String GetArgAsString(int index) {
        return "";
    }

    public int Unload() {
        return 0;
    }

    public void register(Object key, Object value) {
        state.getEnvironment().rawset(key, value);
    }

    /*
     * Tja, leider hat das mit dem Kapseln des Script- Interpreters zwar fast,
     * aber nicht ganz funktioniert. MLDP scheint noch nicht Java 1.5 zu
     * unterstützen, und damit auch keinen variablen Übergabeparameter und kein
     * Autoboxing für elementare Datentypen wie Integer. Und damit ist das
     * Interface dann doch wieder Scriptengine-abhängig..
     */
    public void initRPC(Object key, int nArgs) {
        callFrame = (LuaCallFrame) key;

    }

    public void finishRPC(Object key, int nArgs) {
        // für Lua passiert hier nix...
    }

    public String getString(int index) {
        String response = BaseLib.rawTostring(callFrame.get(index));
        // callFrame.push(response.intern());
        return response;
    }

    public boolean getBoolean(int index) {
        Boolean response = (Boolean) callFrame.get(index);
        // callFrame.push(response);
        return response;
    }

    public int getInt(int index) {
        Double response = BaseLib.rawTonumber(callFrame.get(index));
        // callFrame.push(response);
        return response.intValue();
    }

    public Onion getLuaTable(int index) {
        LuaTableImpl lTable = (LuaTableImpl) callFrame.get(index);
        if (lTable != null) {
            return lua2Onion(lTable);
        } else {
            return null;
        }
    }

    LuaTableImpl onion2Lua(Onion input) {
        LuaTableImpl newLuaTable = new LuaTableImpl();
        for (Iterator iter = input.keys(); iter.hasNext();) {
            String key = (String) iter.next();
            Object value;
            try {
                value = input.getOnionObject(key);
                if (value == null) {
                    newLuaTable.rawset(key, null);
                } else if (value instanceof String) {
                    newLuaTable.rawset(key, value);
                } else if (value instanceof Boolean) {
                    newLuaTable.rawset(key, value);
                } else if (value instanceof Double) {
                    newLuaTable.rawset(key, value);
                } else if (value instanceof Integer) {
                    newLuaTable.rawset(key, ((Integer) value).doubleValue());
                } else if (value instanceof Long) {
                    newLuaTable.rawset(key, ((Long) value).doubleValue());
                } else if (value instanceof Onion) {
                    newLuaTable.rawset(key, onion2Lua((Onion) value));
                } else {
                }

            } catch (OnionNoEntryException ex) {
                Logger.getLogger(ScriptengineLua.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        return newLuaTable;
    }

    Onion lua2Onion(LuaTableImpl input) {
        Onion newOnion = new Onion();
        Object hash = input.next(null);
        while (hash != null) {
            String key = hash.toString();
            Object value = input.rawget(hash);
            try {
                if (value instanceof String) {
                    newOnion.setValue(key, (String) value);
                } else if (value instanceof Boolean) {
                    newOnion.put(key, value);
                } else if (value instanceof Double) {
                    newOnion.put(key, value);
                } else if (value instanceof Integer) {
                    newOnion.put(key, ((Integer) value));
                } else if (value instanceof Long) {
                    newOnion.put(key, ((Long) value));
                } else if (value instanceof LuaTableImpl) {
                    newOnion.put(key, lua2Onion((LuaTableImpl) value));
                } else {
                }

            } catch (JSONException ex) {
                Logger.getLogger(ScriptengineLua.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
            hash = input.next(hash);
        }
        return newOnion;
    }
}
