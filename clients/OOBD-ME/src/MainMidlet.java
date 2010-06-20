/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.midlet.*;
import com.sun.lwuit.Image;
import com.sun.lwuit.Form;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.Component;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.util.Resources;
import com.sun.lwuit.events.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.*;
import javax.microedition.rms.*;




//import java.io.InputStream;
//import java.io.IOException;

import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;

/**
 * @author steffen
 */
public class MainMidlet extends MIDlet implements ActionListener, OutputDisplay/*,Runnable*/ {

    private Image image;
    private Script scriptEngine;
    /*
    private LuaState state;
     */
    private String response;
    private BTSerial btComm;
    private Form f;
    Command startCommand;
    Command blindCommand;
    Command exitCommand;
    Command configCommand;
    private String outputArea = "";
    private boolean freshDisplayOutput = false;
    private boolean blindMode = false;
    private  Form writeForm=null;
    private OutputDisplay myDisplay;
    private List cellList = null;
    Preferences mPreferences;
    private static final String prefsURL = "BTMAC";

    public MainMidlet() {
        myDisplay = this;
        scriptEngine = new LuaScript();
        scriptEngine.Script();

        scriptEngine.register("initCellTableCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls initCellTable");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                cellList = new List();
                //scriptEngine.finishRPC(callFrame, nArguments);
                System.out.println("Lua leaves initCellTable");
                return 1;
            }
        });
        scriptEngine.register("addCellCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                System.out.println("Lua calls addCell");
                scriptEngine.initRPC(callFrame, nArguments);
                cellList.addItem(new ScriptCell(
                        scriptEngine.getString(0), //String title
                        scriptEngine.getString(1), //String function
                        scriptEngine.getString(2), //String initalValue
                        scriptEngine.getBoolean(3), //boolean update
                        scriptEngine.getBoolean(4), //boolean timer
                        scriptEngine.getString(5) //String id
                        ));
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });

        scriptEngine.register("showCellTableCall", new JavaFunction() {

            public int call(LuaCallFrame callFrame, int nArguments) {
                System.out.println("Lua calls showCellTable");
                //BaseLib.luaAssert(nArguments >0, "not enough args");
                scriptEngine.initRPC(callFrame, nArguments);
                writeForm = new ScriptForm(f, cellList, scriptEngine.getString(0), scriptEngine, myDisplay);
                scriptEngine.finishRPC(callFrame, nArguments);
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
                prepareDisplayWrite(scriptEngine.getString(0));
                scriptEngine.finishRPC(callFrame, nArguments);
                return 1;
            }
        });
    }

    public void run() {
        try {
            scriptEngine.doRun();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            notifyDestroyed();
        }
    }

    public void startApp() {

        Display.init(this);
        if (f == null) {
            try {
                Resources r = Resources.open("/LWUITtheme.res");
                UIManager.getInstance().setThemeProps(r.getTheme("LWUITDefault"));
            } catch (java.io.IOException ioe) {
                System.out.println("Couldn't load theme.");
            }
            f = new Form("OOBD ME");
            btComm = new BTSerial(f);
            // try to load Prefs
            try {
                mPreferences = new Preferences("preferences");
            } catch (RecordStoreException rse) {
//      mForm = new Form("Exception");
//      mForm.append(new StringItem(null, rse.toString()));
//      mForm.addCommand(new Command("Exit", Command.EXIT, 0));
//      mForm.setCommandListener(this);
//      return;
            }

            btComm.URL = mPreferences.get(prefsURL);
            f.setLayout(new BorderLayout());
            try {
                image = Image.createImage("/oobd.PNG");
            } catch (java.io.IOException ioe) {
                ioe.printStackTrace();
            }

            Label logo = new Label(image);
            logo.setAlignment(Component.CENTER);
            logo.setText("www.oobd.org");
            logo.setTextPosition(Component.BOTTOM);


            f.addComponent(BorderLayout.CENTER, logo);

            startCommand = new Command("Start");
            configCommand = new Command("Config");
            blindCommand = new Command("Blind");
            exitCommand = new Command("Exit");
            f.addCommand(startCommand);
            f.addCommand(exitCommand);
            f.addCommand(blindCommand);
            f.addCommand(configCommand);
            f.show();
            f.addCommandListener(this);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void shutdownApp() {
        try {
            mPreferences.save();
        } catch (RecordStoreException rse) {
        }
        if (btComm != null) {
            btComm.Closeconnection();
        }
        destroyApp(true);
        notifyDestroyed();


    }

    public void actionPerformed(ActionEvent ae) {
        Command command = ae.getCommand();

        if (command == blindCommand) {
            blindMode = !blindMode;
            if (blindMode) {
                Dialog.show("BlindMode", "BlindMode is on", "ok", "ok");

            } else {
                Dialog.show("BlindMode", "BlindMode is off", "ok", "ok");
            }
        }

        if (command == exitCommand) {

            shutdownApp();
        }
        if (command == configCommand) {

            btComm.getDeviceURL();

        }

        if (command == startCommand) {
            if (blindMode || tryToConnect()) {
                try {
                    scriptEngine.doScript("/HHOpen.lbc");
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            // Form displayForm = new ScriptForm(f,cellList);
        }
    }

    boolean tryToConnect() {
        if (!btComm.isConnected()) {

            //btComm.URL = "00126F072725";
            if (btComm.URL != null) {
                btComm.Connect(btComm.URL);
                if (btComm.isConnected()) {
                    mPreferences.put(prefsURL, btComm.URL);

                    return true;
                } else {
                    Dialog.show("Error", "Not connected !", "ok", "ok");
                    return false;
                }
            } else {
                Dialog.show("Not BT configured", "Please configure your Bluetooth device first", "ok", "ok");
                return false;
            }
        } else {
            return true;
        }

    }

    public void prepareDisplayWrite(String outputLine) {
        outputArea += outputLine +"\n";
        freshDisplayOutput = true;
    }

    public void outputDisplayIfAny() {
        if (freshDisplayOutput) {
            ShowWrite("",writeForm);
        }
    }

    void ShowWrite(String content, final Form parent) {
        Form displayForm = new Form("Output");
        freshDisplayOutput = false;
        outputArea += content;
        final TextArea big = new TextArea(outputArea);
        displayForm.addComponent(big);

        displayForm.addCommand(new Command("Exit") {

            public void actionPerformed(ActionEvent evt) {
                parent.showBack();
            }
        });
        displayForm.addCommand(new Command("Clear") {

            public void actionPerformed(ActionEvent evt) {
                outputArea = "";
                big.setText("");
            }
        });
        //addCommandListener(this);
        displayForm.show();

    }
}
