/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.mobile;


import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.*;
import javax.microedition.rms.RecordStoreException;
import se.krka.kahlua.vm.*;

/**
 * @author Axel
 */
public class OOBD_MEv2 extends MIDlet implements CommandListener {

    public BTSerial btComm;
    public Form mainwindow;
    ConfigForm configwindow;
    Spacer confSpacer;
    ImageItem logoItem;
    TextField btConf;
    TextField scriptConf;
    Command confCmd;
    Command back2mainCmd;
    Command startCmd;
    Command infoCmd;
    Command logCmd;
    Display display;
    Preferences mPreferences;
    private final int btURLkey=1;
    private final int scriptpathKey=2;
    private final int adressbookKey=3;
    private final int loglevelKey=4;
    boolean initialized=false;
    private final String urlKey = "BTNAME";
    private String currentURL = "Not yet chosen...";
    private final String scriptKey = "SCRIPT";
    private String scriptDefault = "/OOBD.lbc";
    private String actScript = scriptDefault;
    private LuaScript scriptEngine;
    //private List cellList;
    private boolean blindMode=false; // TODO Remember to set BlindMode to "false"
    
    private Command exitCmd;
    Hashtable scriptTable;
    int tableID=1;
    private String actPageName;
    private ScriptForm scriptWindow;
    public MobileLogger log;
    private Calendar cal;


    public void startApp() {

        log = new MobileLogger(this);
        cal=Calendar.getInstance(TimeZone.getDefault());

        log.log("App started at: "+cal.getTime().toString());

        display = Display.getDisplay(this);
        if (!initialized){
            initialized=true;
            btComm = new BTSerial(this);
            try {
                mPreferences = new Preferences("preferences",this);                
            } catch (RecordStoreException rse) {
                showAlert("Recordstore could not be opened:\n"+rse.toString());
                rse.printStackTrace();
            }
              
            if (!mPreferences.get(scriptpathKey).equals("null")) {
                actScript = mPreferences.get(scriptpathKey);
            }
            
            
            if (!mPreferences.get(btURLkey).equals("null")){
                currentURL = mPreferences.get(btURLkey);
                btComm.deviceURL = currentURL;
            }

            if (!mPreferences.get(loglevelKey).equals("null")) {
                log.setLoglevel(Integer.parseInt(mPreferences.get(loglevelKey)));
            }
            
            mPreferences.put(adressbookKey, "Check");

            mainwindow = new Form("OOBD-MEv2",null);
            mainwindow.setCommandListener(this);

            confCmd = new Command("Config", Command.SCREEN,1);
            mainwindow.addCommand(confCmd);
            startCmd = new Command("Start", Command.OK,0);
            mainwindow.addCommand(startCmd);
//            infoCmd = new Command("Info", Command.HELP, 4);
//            mainwindow.addCommand(infoCmd);
            logCmd = new Command("Show Log", Command.SCREEN,2);
            mainwindow.addCommand(logCmd);
            exitCmd = new Command("Exit", Command.BACK,3);
            mainwindow.addCommand(exitCmd);

            Image logo=null;
            try {
                logo = Image.createImage("/oobd.PNG");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            logoItem = new ImageItem("OODB-MEv2", logo, ImageItem.LAYOUT_CENTER, "Logo not loaded");

            mainwindow.append(logoItem);
            display.setCurrent(mainwindow);
        }
    }

    public void setScript(String script){
        actScript=script;
        mPreferences.put(scriptpathKey, script);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        mPreferences.closeStore();
        log.log("Destroy App called, RecordStore closed, trying to close BT connection");
        btComm.Closeconnection();
        
        display.setCurrent(null);
            notifyDestroyed();
        
        
    }

    public void commandAction(Command c, Displayable d) {
                                                   
        if (c == confCmd) {
            if (configwindow == null){
                configwindow = new ConfigForm(mainwindow, btComm, this);
            }
            display.setCurrent(configwindow);
        }
        
        else if(c == startCmd) {
            mainwindow.setTicker(new Ticker("Establishing Bluetooth connection..."));
            if (tryToConnect()||blindMode) {
//            if (blindMode) {
                try {
                    if (scriptEngine==null){
                        initiateScriptEngine();
                    }
                    mainwindow.setTicker(new Ticker("Loading script..."));

                    System.out.println("Try to load script " + actScript);
                    scriptEngine.doScript(actScript);
                    log.log("Script: "+actScript+" loaded!");
                    System.out.println("Script loaded!");
                    if (!actScript.equals(scriptDefault)) {
                        mPreferences.put(scriptpathKey, actScript);
                    }
                    mainwindow.setTicker(new Ticker(""));

                } catch (java.io.IOException ioe) {
                    log.log(ioe.toString());
                    ioe.printStackTrace();
                }
            }
        }
        else if (c == exitCmd){
            destroyApp(true);
        }
        else if (c == infoCmd){
            showAlert("Stored values: \n \t Bluetooth-URL: "+mPreferences.get(btURLkey)+"\n\t Script: "+mPreferences.get(scriptpathKey));
        }
        else if (c == logCmd){
            log.showlogs(this);
        }
    }

    public Display getDisplay() {
        return display;
    }

    public void showAlert(String text){
        Alert check = new Alert("Debug Message",text,null,AlertType.CONFIRMATION);
        log.log("Alert shown with the Text: "+text);
        display.setCurrent(check);
    }

    boolean tryToConnect() {
        if (!btComm.isConnected()) {

            if (btComm.deviceURL != null) {
                showAlert("Trying to connect to: "+btComm.deviceURL);
                btComm.Connect(btComm.deviceURL);
                if (btComm.isConnected()) {
                    mPreferences.put(btURLkey, btComm.deviceURL);
                    

                    return true;
                } else {
                    display.setCurrent(new Alert("Start not possible", "Communication not possible", null, AlertType.WARNING));
                    return false;
                }
            } else {
                display.setCurrent(new Alert("Start not possible", "Please configure your Bluetooth device first", null, AlertType.WARNING));
                return false;
            }
        } else {
            return true;
        }

    }

    public boolean isBlindMode() {
        return blindMode;
    }

    private void initiateScriptEngine(){
        scriptEngine = new LuaScript();
        scriptEngine.Script();
        scriptWindow = new ScriptForm(this, scriptEngine, display);

        scriptEngine.register("openPageCall", new JavaFunction() {           
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls openPage");
                scriptTable = new Hashtable();
                tableID=1;
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                //alt cellList = new List("Scriptliste", List.MULTIPLE);
                actPageName=scriptEngine.getString(0);
                //scriptEngine.finishRPC(callFrame, nArguments);
                System.out.println("Lua leaves openPage");
                return 1;
            }
        });

        scriptEngine.register("addElementCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                //BaseLib.luaAssert(nArguments >0, "not enough args");
//                System.out.println("Lua calls addElement with ID: "+tableID);
                scriptEngine.initRPC(callFrame, nArguments);
                // TODO Taken counter as scriptTable-ID. Working?
                scriptTable.put(Integer.toString(tableID++),new ScriptCell(
                        scriptEngine.getString(0), //String title
                        scriptEngine.getString(1), //String function
                        scriptEngine.getString(2), //String initalValue
                        scriptEngine.getInt(3), //int OOBDElementFlags
                        scriptEngine.getString(4) //String id
                        ));
                scriptEngine.finishRPC(callFrame, nArguments);

                return 1;
            }
        });

        scriptEngine.register("pageDoneCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
//                log.log("LUA calls pageDone");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);                
                scriptEngine.finishRPC(callFrame, nArguments);
                
                scriptWindow.showForm(actPageName, scriptTable);
                
                return 1;
            }
        });
        
        scriptEngine.register("serReadLnCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls serReadLn");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                System.out.println("timeout value:" + Integer.toString(scriptEngine.getInt(0)));
                if (scriptEngine.getBoolean(1) == true) {
                    System.out.println("ignore value: true");
                } else {
                    if (scriptEngine.getBoolean(1) == false) {
                        System.out.println("ignore value: false");
                    } else {
                        System.out.println("ignore value: undefined");
                    }
                }
                String result = "";
                if (btComm != null) {
                    result = btComm.readln(scriptEngine.getInt(0), scriptEngine.getBoolean(1));
                    //result = btComm.readln(2000, true);
                }
                callFrame.push(result.intern());
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        scriptEngine.register("serWaitCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls serWait");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                int result = 0;
                if (btComm != null) {
                    result = btComm.wait(scriptEngine.getString(0), scriptEngine.getInt(1));
                }
                callFrame.push(new Integer(result));
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        scriptEngine.register("serSleepCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls serSleep");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                try {
                    Thread.sleep(scriptEngine.getInt(0));
                } catch (InterruptedException e) {
                    // the VM doesn't want us to sleep anymore,
                    // so get back to work
                }
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        scriptEngine.register("serWriteCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls serWrite");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                if (btComm != null) {
                    btComm.write(scriptEngine.getString(0));
                }
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        scriptEngine.register("serFlushCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls serFlush");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                if (btComm != null) {
                    btComm.flush();
                }
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        scriptEngine.register("serDisplayWriteCall", new JavaFunction() {
            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls serDisplayWrite");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                scriptWindow.showMessage(scriptEngine.getString(0));
//                log.log("LUA message call: "+scriptEngine.getString(0));
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });
    }

    public String getActScript() {
        return actScript;
    }

    public void switchBlindMode(boolean state){
        blindMode=state;
    }

    public void storePref(int key, String value){
        mPreferences.put(key, value);
    }

    public String getBTurl() {
        return currentURL;
    }

    public void setMmsAddress(String mmsAdress){

    }

    public void showMain(){
        display.setCurrent(mainwindow);
    }

    public MobileLogger getLog() {
        return log;
    }

    public String getScriptDefault() {
        return scriptDefault;
    }


}