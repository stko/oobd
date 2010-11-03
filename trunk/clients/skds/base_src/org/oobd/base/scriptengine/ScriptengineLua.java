/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import org.oobd.base.*;
import org.oobd.base.support.Onion;

import java.io.InputStream;
import java.io.IOException;

import java.io.*;
import java.io.File.*;


import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.MathLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;
import org.json.JSONException;

import sun.misc.*;

/**
 *
 * @author steffen
 */
public class ScriptengineLua extends OobdScriptengine {

    private LuaState state;
    private LuaCallFrame callFrame;
    private OobdScriptengine myself;

    public ScriptengineLua(String id, Core myCore) {
        super(id, myCore);
        Debug.msg("scriptenginelua", DEBUG_BORING, "Ich bin  ScriptengineLua mit der ID "+id);
        myself = this;



    }

    @Override
    public String getPluginName() {
        return "se:Lua";
    }

    public static String publicName() {
        return "Script";
    }

    /**
     * @todo open & write files MUST go through an generic stream supported by core to handle different sources and encrypted files
     */
    public void run() {
        state = new LuaState(System.out);
        BaseLib.register(state);
        MathLib.register(state);
        StringLib.register(state);
        CoroutineLib.register(state);

        InputStream resource = getClass().getResourceAsStream("/stdlib.lbc");
        try {
            state.call(LuaPrototype.loadByteCode(resource, state.getEnvironment()), null, null, null);
        } catch (IOException ex) {
            Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            register("initCellTableCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls initCellTable");
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    // cellList = new List();
                    try {
                        core.transferMsg(new Message(myself, CoreMailboxName, new Onion(""
                                + "{'type':'" + CM_CANVAS + "',"
                                + "'owner':'" + myself.getId() + "',"
                                + "'name':'Canvastest_1'}")));
                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //finishRPC(callFrame, nArguments);
                    System.out.println("Lua leaves initCellTable");
                    return 1;
                }
            });
            register("addCellCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    System.out.println("Lua calls addCell");
                    initRPC(callFrame, nArguments);
//                    cellList.addItem(new ScriptCell(
//                            getString(0), //String title
//                            getString(1), //String function
//                            getString(2), //String initalValue
//                            getBoolean(3), //boolean update
//                            getBoolean(4), //boolean timer
//                            getString(5) //String id
//                            ));
                    try {
                        String updevent = "";
                        if (getBoolean(3) /*boolean update*/) {
                            if (!updevent.isEmpty()) {
                                updevent += "|";
                            }
                            updevent += "UPDATE";
                        }
                        if (getBoolean(4) /*boolean timer*/) {
                            if (!updevent.isEmpty()) {
                                updevent += "|";
                            }
                            updevent += "timer";
                        }
                        if (!updevent.isEmpty()) {
                            updevent = "'updevent':'" + updevent + "',";
                        }
                        String optid = getString(5); //String id
                        if (!optid.isEmpty()) {
                            optid = "'optid':'" + optid + "',";
                        }
                        core.transferMsg(new Message(myself, CoreMailboxName, new Onion(""
                                + "{'type':'" + CM_VISUALIZE + "',"
                                + "'owner':"
                                + "{'name':'" + myself.getId() + "'},"
                                + "'canvas':'Canvastest_1',"
                                + updevent
                                + optid
                                + "'tooltip':'" + getString(0) + "',"
                                + "'value':'" + getString(2) + "',"
                                + "'optid':'" + getString(5) + "',"
                                + "'name':'" + getString(1)
                                + "'}")));

                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

            register("showCellTableCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls showCellTable");
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    // do nothing...

                    // writeForm = new ScriptForm(f, cellList, getString(0), scriptEngine, myDisplay);
                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

            register("serReadLnCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls serReadLn");
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    System.out.println("timeout value:" + Integer.toString(getInt(0)));
                    if (getBoolean(1) == true) {
                        System.out.println("ignore value: true");
                    } else {
                        if (getBoolean(1) == false) {
                            System.out.println("ignore value: false");
                        } else {
                            System.out.println("ignore value: undefined");
                        }
                    }
                    String result = "";
                    Message answer = null;
                    try {
                        answer = myself.getMsgPort().sendAndWait(new Message(myself, BusMailboxName, new Onion(""
                                + "{'type':'" + CM_BUSTEST + "',"
                                + "'owner':"
                                + "{'name':'" + myself.getId() + "'},"
                                + "'command':'serReadLn',"
                                + "'timeout':'" + getInt(0) + "',"
                                + "'ignore':'" + Boolean.toString(getBoolean(1)) + "'}")), 5000);
                        if (answer != null) {
                            try {
                                result = new String(new BASE64Decoder().decodeBuffer(answer.getContent().getString("result")));
                                System.out.println("Lua serReadLn result:"+result);
                            } catch (IOException ex) {
                                Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }else{
                            System.out.println("Lua serReadLn: No answer received");
                        }

                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }


//                    if (btComm != null) {
//                        result = btComm.readln(getInt(0), getBoolean(1));
//                        //result = btComm.readln(2000, true);
//                    }
                    callFrame.push(result.intern());
                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

            register("serWaitCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls serWait with string data:>" + getString(0) + "<");
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    int result = 0;
                    Message answer = null;
                    try {
                        answer = myself.getMsgPort().sendAndWait(new Message(myself, BusMailboxName, new Onion(""
                                + "{'type':'" + CM_BUSTEST + "',"
                                + "'owner':"
                                + "{'name':'" + myself.getId() + "'},"
                                + "'command':'serWait',"
                                + "'timeout':'" + getInt(1) + "',"
                                + "'data':'" + new BASE64Encoder().encode(getString(0).getBytes()) + "'}")), 500);
                        if (answer != null) {
                            System.out.println("Lua calls serWait returns with onion:" + answer.getContent().toString() );
                            result = answer.getContent().getInt("result");

                        }

                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }

//                    if (btComm != null) {
//                        result = btComm.wait(getString(0), getInt(1));
//                    }
                    callFrame.push(new Integer(result));
                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

            register("serSleepCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls serSleep");
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
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
                    System.out.println("Lua calls serWrite with string:" + getString(0));

                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    try {
                        core.transferMsg(new Message(myself, BusMailboxName, new Onion("{"
                                + "'type':'" + CM_BUSTEST + "',"
                                + "'owner':"
                                + "{'name':'" + myself.getId() + "'},"
                                + "'command':'serWrite',"
                                + "'data':'" + new BASE64Encoder().encode(getString(0).getBytes()) + "'"
                                + "}")));
                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }
//                    if (btComm != null) {
//                        btComm.write(getString(0));
//                    }
                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

            register("serFlushCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls serFlush");
                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    try {
                        core.transferMsg(new Message(myself, BusMailboxName, new Onion(""
                                + "{'type':'" + CM_BUSTEST + "',"
                                + "'owner':"
                                + "{'name':'" + myself.getId() + "'},"
                                + "'command':'serFlush'}")));
                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }
//                      if (btComm != null) {
//                        btComm.flush();
//                    }
                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

            register("serDisplayWriteCall", new JavaFunction() {

                public int call(LuaCallFrame callFrame, int nArguments) {
                    System.out.println("Lua calls serDisplayWrite with string:" + getString(0));

                    //BaseLib.luaAssert(nArguments >0, "not enough args");
                    initRPC(callFrame, nArguments);
                    try {
                        core.transferMsg(new Message(myself, BusMailboxName, new Onion("{"
                                + "'type':'" + CM_BUSTEST + "',"
                                + "'owner':"
                                + "{'name':'" + myself.getId() + "'},"
                                + "'command':'serDisplayWrite',"
                                + "'data':'" + new BASE64Encoder().encode(getString(0).getBytes()) + "'"
                                + "}")));
                    } catch (JSONException ex) {
                        Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
                    }
//                    if (btComm != null) {
//                        btComm.write(getString(0));
//                    }
                    finishRPC(callFrame, nArguments);
                    return 1;
                }
            });

        File dir1 = new File(".");
        try {
            System.out.println("ScriptengineLua Current dir : " + dir1.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties props = new Properties();
        try {
            props.load(new FileInputStream("enginelua.props"));
        } catch (IOException ignored) {
            Debug.msg("scriptengineterminal", DEBUG_WARNING, "couldn't load properties");
        }
         
        try {
            //doScript("/OOBD.lbc");
             System.out.println("Lua file" + props.getProperty("LuaDefaultScript", "OOBD.lbc"));
            doScript(props.getProperty("LuaDefaultScript", "OOBD.lbc"));
        } catch (IOException ex) {
            Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
        }
        int i = 0;
        while (keepRunning == true) {
            Debug.msg("scriptengineterminal", DEBUG_BORING, "sleeping...");
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            String vis = on.getOnionString("vis");
            Debug.msg("ScriptengineLua", DEBUG_BORING, "Msg received:" + msg.getContent().toString());
            try {
                if (CM_UPDATE.equals(on.get("type"))){
                core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                        + "{'type':'" + CM_VALUE + "',"
                        + "'owner':"
                        + "{'name':'" + this.id + "'},"
                        + "'to':"
                        + "{'name':'" + vis + "'},"
                        //+ "'value':'" + Integer.toString(i)
                        + "'value':'" + callFunction(vis, new Object[]{on.getOnionString("vis"), on.getOnionString("optid")})
                        + "'}")));
                }
            } catch (JSONException ex) {
                Logger.getLogger(ScriptengineTerminal.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;
            Debug.msg("ScriptengineLua", DEBUG_BORING, "waked up after received msg...");

        }
    }

    public void doScript(String fileName) throws IOException {
        InputStream resource = new FileInputStream(fileName);
        //InputStream resource=getClass().getResourceAsStream(fileName);
        LuaClosure callback = LuaPrototype.loadByteCode(resource, state.getEnvironment());
        state.call(callback, null, null, null);

    }

    public String callFunction(String functionName, Object[] params) {
        System.out.println("function to call in Lua:" +functionName);
        LuaClosure fObject = (LuaClosure) state.getEnvironment().rawget(functionName);
        System.err.println(fObject.getClass().toString());
        System.err.println(fObject.toString());
        Object[] results = state.pcall(fObject, params);
        if (results[0] != Boolean.TRUE) {
            Object errorMessage = results[1];
            System.out.println("Crash: " + errorMessage);
            System.out.println(results[2]);
            Throwable stacktrace = (Throwable) (results[3]);
            if (stacktrace != null) {
                stacktrace.printStackTrace();
            }
        }

        //String response = BaseLib.rawTostring(fObject.env.rawget(1));
        //fObject.push(response.intern());
        return (String) results[1];

        //return "-";
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
    /* Tja, leider hat das mit dem Kapseln des Script- Interpreters
     * zwar fast, aber nicht ganz funktioniert. MLDP scheint noch
     * nicht Java 1.5 zu unterstützen, und damit auch keinen variablen
     * Übergabeparameter und kein Autoboxing für elementare Datentypen
     * wie Integer.
     * Und damit ist das Interface dann doch wieder Scriptengine-abhängig..
     */

    public void initRPC(Object key, int nArgs) {
        callFrame = (LuaCallFrame) key;

    }

    public void finishRPC(Object key, int nArgs) {
        // für Lua passiert hier nix...
    }

    public String getString(int index) {
        System.out.println("Lua get string index " + Integer.toString(index));
        String response = BaseLib.rawTostring(callFrame.get(index));
        // callFrame.push(response.intern());
        System.out.println("Lua get string value: " + response);
        return response;
    }

    public boolean getBoolean(int index) {
        Boolean response = (Boolean) callFrame.get(index);
        //callFrame.push(response);
        return response.booleanValue();
    }

    public int getInt(int index) {
        Double response = BaseLib.rawTonumber(callFrame.get(index));
        //callFrame.push(response);
        return response.intValue();
    }
}
