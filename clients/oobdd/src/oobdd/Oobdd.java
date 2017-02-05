/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oobdd;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.oobd.core.Core;
import org.oobd.core.IFsystem;
import org.oobd.core.OOBDConstants;
import org.oobd.core.Settings;
import org.oobd.core.port.ComPort_Win;
import org.oobd.core.support.Onion;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonController;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonUserSignal;

/**
 *
 * @author steffen
 */
public class Oobdd implements IFsystem, OOBDConstants, Daemon, Runnable, DaemonUserSignal {

    private Thread thread = null;
    private DaemonController controller = null;
    private volatile boolean stopping = false;
    private boolean softReloadSignalled;
    private static Core core;
    private static OptionSet options;
    private static OptionParser parser=null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try {
        
            loadOptions(args);
 
          core = new Core(new Oobdd(), "Core");
            // TODO code application logic here
         } catch (Settings.IllegalSettingsException ex) {
            Logger.getLogger(Oobdd.class.getName()).log(Level.SEVERE, "Illegal preferences", ex);
        }
    }

    @Override
    public String getSystemDefaultDirectory(boolean privateDir, String fileName) {
        if (privateDir) {
            return System.getProperty("user.home") + "/" + fileName;
        } else {
            File myFile = new File(fileName);
            if (myFile.exists()) {
                return myFile.getAbsolutePath();
            } else {
                myFile = new File(Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + fileName);
                if (myFile.exists()) {
                    return myFile.getAbsolutePath();
                }
                return fileName;
            }

        }

    }

    @Override
    public Object supplyHardwareHandle(Onion typ) {

        try {
            return new ComPort_Win();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void openXCVehicleData(Onion onion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String doFileSelector(String path, final String extension, String message, boolean save) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String loadPreferences() {
        System.out.println("Start to load preferences");
        if (options.has("settings") && options.hasArgument("settings")) {

            try {
                       System.out.println("Preferences path:"+options.valueOf("settings"));
 
                byte[] encoded = Files.readAllBytes(Paths.get((String) options.valueOf("settings")));
                System.out.println("Preferences String:\n"+new String(encoded, "UTF8")+"\n");
                return new String(encoded, "UTF8");
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean savePreferences(String json) {
        if (options.has("settings") && options.hasArgument("settings")) {

            try (PrintWriter out = new PrintWriter((String) options.valueOf("settings"))) {
                out.print(json);
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }

    //++++++++++++++++++
    /**
     * handle the given options
     *
     * @param args
     * @return true, if options ok and ok to proceed, false in case of wrong
     * options
     */
    static boolean loadOptions(String[] args) {
       parser = new OptionParser();
         parser.accepts("settings").withRequiredArg();;

        options = parser.parse(args);
        return true;
    }

    protected void finalize() {
        System.err.println("oobdd: instance " + this.hashCode()
                + " garbage collected");
    }

    /**
     * init and destroy were added in jakarta-tomcat-daemon.
     */
    public void init(DaemonContext context)
            throws Exception {
        System.err.println("oobdd: instance " + this.hashCode()
                + " init");

        parser = new OptionParser();
        parser.accepts("settings");

        loadOptions(context.getArguments());

        /* Dump a message */
        System.err.println("oobdd: loaded");

        /* Set up this simple daemon */
        this.controller = context.getController();
        this.thread = new Thread(this);
    }

    public void start() {
        /* Dump a message */
        System.err.println("oobdd: starting");

        /* Start */
        this.thread.start();
    }

    public void stop()
            throws IOException, InterruptedException {
        /* Dump a message */
        System.err.println("oobdd: stopping");

        /* Close the ServerSocket. This will make our thread to terminate */
        this.stopping = true;

        /* Wait for the main thread to exit and dump a message */
        this.thread.join(5000);
        System.err.println("oobdd: stopped");
    }

    public void destroy() {
        System.err.println("oobdd: instance " + this.hashCode()
                + " destroy");
    }

    public void run() {
        int number = 0;

        System.err.println("oobdd: started acceptor loop");
        while (!this.stopping) {
            checkForReload();
            Thread thisCoreThread;
            try {
                core = new Core(this, "Core");

                thisCoreThread = core.getThread();
                while (thisCoreThread.isAlive()) {
                    try {
                        thisCoreThread.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Oobdd.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (Settings.IllegalSettingsException ex) {
                Logger.getLogger(Oobdd.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*
             Handler handler = new Handler( this, this.controller);
             handler.setConnectionNumber(number++);
             Thread handlerThread=
             new Thread(handler);
             handlerThread.start();
                
  
                
                
                
             while(handlerThread.isAlive()) {
             try {
             handlerThread.join();
             } catch (InterruptedException ex) {
             Logger.getLogger(Oobdd.class.getName()).log(Level.SEVERE, null, ex);
             }
             }
           
             */
            /*
             out.println("Attempting a shutdown...");
             try {
             this.controller.shutdown();
             } catch (IllegalStateException e) {
             out.println();
             out.println("Can't shutdown now");
             e.printStackTrace(out);
             }
             out.println("Attempting a reload...");
             try {
             this.controller.reload();
             } catch (IllegalStateException e) {
             out.println();
             out.println("Can't reload now");
             e.printStackTrace(out);
             }
                
             // Disconnect
             out.println("Reloading configuration...");
             this.parent.signal();
             return;
                
             */

        }

        System.err.println("oobdd: exiting acceptor loop");
    }

    public void signal() {
        /* In this example we are using soft reload on
         * custom signal.
         */
        this.softReloadSignalled = true;
        core.close();
    }

    private void checkForReload() {
        if (this.softReloadSignalled) {
            System.err.println("oobdd: all connections have finished, pretending to reload");
            this.softReloadSignalled = false;
        }
    }

}
