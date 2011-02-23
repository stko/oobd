/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.mobile;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.*;
import javax.microedition.rms.RecordStoreException;
import se.krka.kahlua.vm.*;

/**
 * @author Axel
 */
public class OOBD_MEv2 extends MIDlet implements CommandListener {

    BTSerial btComm;
    Form mainwindow;
    ConfigForm configwindow;
    Spacer confSpacer;
    TextField btConf;
    TextField scriptConf;
    Command confCom;
    Command back2mainCom;
    Command startCom;
    Display display;
    Preferences mPreferences;
    boolean initialized=false;
    private static final String prefsURL = "BTMAC";
    private static final String prefsScript = "SCRIPT";
    private static final String scriptDefault = "/OOBD.lbc";
    private String actScript = scriptDefault;
    private LuaScript scriptEngine;
    private List cellList;


    public void startApp() {

        if (!initialized){
            initialized=true;

            btComm = new BTSerial();

            try {
                mPreferences = new Preferences("preferences");

                actScript = mPreferences.get(prefsScript);
                btComm.URL = mPreferences.get(prefsURL);
                if (actScript == null) {
                    actScript = scriptDefault;
                }
            } catch (RecordStoreException rse) {
            }
            display = Display.getDisplay(this);

            mainwindow = new Form("OOBD-MEv2",null);
            mainwindow.setCommandListener(this);

            confCom = new Command("Config", Command.SCREEN,0);
            mainwindow.addCommand(confCom);
            startCom = new Command("Start", Command.OK,0);
            mainwindow.addCommand(startCom);

            display.setCurrent(mainwindow);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        
    }

    public void commandAction(Command c, Displayable d) {
        if (d == mainwindow) {                                           
            if (c == confCom) {                                         
                if (configwindow == null){
                    configwindow = new ConfigForm(mainwindow, btComm, this);

                }
                                          
                display.setCurrent(configwindow);
            }                                           
        }
        if (d == configwindow) {
            if (c == back2mainCom) {
                display.setCurrent(mainwindow);
            }
        }
        
    }

    public Display getDisplay() {
        return display;
    }

    public void showAlert(String text){
        Alert check = new Alert("Debug Message",text,null,AlertType.CONFIRMATION);
        display.setCurrent(check);
    }

//    private void initiateScriptEngine(){
//        scriptEngine = new LuaScript();
//        scriptEngine.Script();
//
//        scriptEngine.register("openPageCall", new JavaFunction() {
//            private String actPageName;
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls openPage");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                cellList = new List("Scriptliste", List.MULTIPLE);
//                actPageName=scriptEngine.getString(0);
//                //scriptEngine.finishRPC(callFrame, nArguments);
//                System.out.println("Lua leaves openPage");
//                return 1;
//            }
//        });
//        scriptEngine.register("addElementCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                System.out.println("Lua calls addElement");
//                scriptEngine.initRPC(callFrame, nArguments);
//                cellList.addItem(new ScriptCell(
//                        scriptEngine.getString(0), //String title
//                        scriptEngine.getString(1), //String function
//                        scriptEngine.getString(2), //String initalValue
//                        scriptEngine.getInt(3), //int OOBDElementFlags
//                        scriptEngine.getString(4) //String id
//                        ));
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("pageDoneCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls pageDone");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                writeForm = new ScriptForm(f, cellList, actPageName, scriptEngine, myDisplay);
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("serReadLnCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls serReadLn");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                System.out.println("timeout value:" + Integer.toString(scriptEngine.getInt(0)));
//                if (scriptEngine.getBoolean(1) == true) {
//                    System.out.println("ignore value: true");
//                } else {
//                    if (scriptEngine.getBoolean(1) == false) {
//                        System.out.println("ignore value: false");
//                    } else {
//                        System.out.println("ignore value: undefined");
//                    }
//                }
//                String result = "";
//                if (btComm != null) {
//                    result = btComm.readln(scriptEngine.getInt(0), scriptEngine.getBoolean(1));
//                    //result = btComm.readln(2000, true);
//                }
//                callFrame.push(result.intern());
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("serWaitCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls serWait");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                int result = 0;
//                if (btComm != null) {
//                    result = btComm.wait(scriptEngine.getString(0), scriptEngine.getInt(1));
//                }
//                callFrame.push(new Integer(result));
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("serSleepCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls serSleep");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                try {
//                    Thread.sleep(scriptEngine.getInt(0));
//                } catch (InterruptedException e) {
//                    // the VM doesn't want us to sleep anymore,
//                    // so get back to work
//                }
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("serWriteCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls serWrite");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                if (btComm != null) {
//                    btComm.write(scriptEngine.getString(0));
//                }
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("serFlushCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls serFlush");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                if (btComm != null) {
//                    btComm.flush();
//                }
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//        scriptEngine.register("serDisplayWriteCall", new JavaFunction() {
//
//            public int call(LuaCallFrame callFrame, int nArguments) {
//                System.out.println("Lua calls serDisplayWrite");
//                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                scriptEngine.initRPC(callFrame, nArguments);
//                prepareDisplayWrite(scriptEngine.getString(0));
//                scriptEngine.finishRPC(callFrame, nArguments);
//                return 1;
//            }
//        });
//
//    }
//
    
}