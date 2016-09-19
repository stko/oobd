/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oobdd;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.oobd.base.Core;
import org.oobd.base.IFsystem;
import org.oobd.base.OOBDConstants;
import org.oobd.base.Settings;
import org.oobd.base.port.ComPort_Win;
import org.oobd.base.support.Onion;

import java.io.*;
import java.net.*;
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

    Preferences myPrefs = null;

    private ServerSocket server = null;
    private Thread thread = null;
    private DaemonController controller = null;
    private volatile boolean stopping = false;
    private String directory = null;
    private Vector handlers = new Vector();
    private boolean softReloadSignalled;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new Core(new Oobdd(), "Core");
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
    public String doFileSelector(String path, final String extension, String message, Boolean save) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String loadPreferences() {
        Preferences prefsRoot;
        Onion prefs = new Onion();
        try {
            prefsRoot = Preferences.userRoot();
            prefsRoot.sync();
            myPrefs = prefsRoot.node("com.oobd.preference." + OOBDConstants.AppPrefsFileName);
            String sysKeys[];
            if (myPrefs.keys().length == 0) { //no settings found? Then try to read system prefs
                Preferences sysPrefsRoot;
                Preferences mySysPrefs = null;
                sysPrefsRoot = Preferences.systemRoot();
                sysPrefsRoot.sync();
                mySysPrefs = sysPrefsRoot.node("com.oobd.preference." + OOBDConstants.AppPrefsFileName);
                sysKeys = mySysPrefs.keys();
                for (int i = 0; i < sysKeys.length; i++) { //copy system settings, if any exist
                    myPrefs.put(sysKeys[i], mySysPrefs.get(sysKeys[i], ""));
                }
            }
            String prefsString = myPrefs.get("json", null);
            if (prefsString != null) {
                System.out.println("Prefs loaded from JSON String!");
                return prefsString;
            } else {
                sysKeys = myPrefs.keys();
                for (int i = 0; i < sysKeys.length; i++) { //copy system settings, if any exist
                    System.out.println(sysKeys[i] + ":" + myPrefs.get(sysKeys[i], ""));
                    prefs.setValue(sysKeys[i].replaceAll("_", "/"), myPrefs.get(sysKeys[i], ""));
                }
            }
            System.out.println(prefs);

        } catch (Exception e) {
            Logger.getLogger(Oobdd.class.getName()).log(Level.CONFIG, "could not load property id " + OOBDConstants.AppPrefsFileName, e);
        }

        return prefs.toString();
    }

    @Override
    public boolean savePreferences(String json) {
        myPrefs.put("json", json);

        try {
            myPrefs.flush();
            return true;

        } catch (Exception e) {
            Logger.getLogger(Oobdd.class.getName()).log(Level.WARNING, "could not load property id " + OOBDConstants.AppPrefsFileName, e);

            return false;
        }

    }

    protected void finalize() {
        System.err.println("SimpleDaemon: instance " + this.hashCode()
                + " garbage collected");
    }

    /**
     * init and destroy were added in jakarta-tomcat-daemon.
     */
    public void init(DaemonContext context)
            throws Exception {
        System.err.println("SimpleDaemon: instance " + this.hashCode()
                + " init");

        int port = 1200;

        String[] a = context.getArguments();

        if (a.length > 0) {
            port = Integer.parseInt(a[0]);
        }
        if (a.length > 1) {
            this.directory = a[1];
        } else {
            this.directory = "/tmp";
        }

        /* Dump a message */
        System.err.println("SimpleDaemon: loading on port " + port);

        /* Set up this simple daemon */
        this.controller = context.getController();
        this.server = new ServerSocket(port);
        this.thread = new Thread(this);
    }

    public void start() {
        /* Dump a message */
        System.err.println("SimpleDaemon: starting");

        /* Start */
        this.thread.start();
    }

    public void stop()
            throws IOException, InterruptedException {
        /* Dump a message */
        System.err.println("SimpleDaemon: stopping");

        /* Close the ServerSocket. This will make our thread to terminate */
        this.stopping = true;
        this.server.close();

        /* Wait for the main thread to exit and dump a message */
        this.thread.join(5000);
        System.err.println("SimpleDaemon: stopped");
    }

    public void destroy() {
        System.err.println("SimpleDaemon: instance " + this.hashCode()
                + " destroy");
    }

    public void run() {
        int number = 0;

        System.err.println("SimpleDaemon: started acceptor loop");
        try {
            while (!this.stopping) {
                checkForReload();
                Socket socket = this.server.accept();
                checkForReload();

                Handler handler = new Handler(socket, this, this.controller);
                handler.setConnectionNumber(number++);
                handler.setDirectoryName(this.directory);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            /* Don't dump any error message if we are stopping. A IOException
             is generated when the ServerSocket is closed in stop() */
            if (!this.stopping) {
                e.printStackTrace(System.err);
            }
        }

        /* Terminate all handlers that at this point are still open */
        Enumeration openhandlers = this.handlers.elements();
        while (openhandlers.hasMoreElements()) {
            Handler handler = (Handler) openhandlers.nextElement();
            System.err.println("SimpleDaemon: dropping connection "
                    + handler.getConnectionNumber());
            handler.close();
        }

        System.err.println("SimpleDaemon: exiting acceptor loop");
    }

    public void signal() {
        /* In this example we are using soft reload on
         * custom signal.
         */
        this.softReloadSignalled = true;
    }

    private void checkForReload() {
        if (this.softReloadSignalled) {
            System.err.println("SimpleDaemon: picked up reload, waiting for connections to finish...");
            while (!this.handlers.isEmpty()) {
            }
            System.err.println("SimpleDaemon: all connections have finished, pretending to reload");
            this.softReloadSignalled = false;
        }
    }

    protected void addHandler(Handler handler) {
        synchronized (handler) {
            this.handlers.add(handler);
        }
    }

    protected void removeHandler(Handler handler) {
        synchronized (handler) {
            this.handlers.remove(handler);
        }
    }

    public static class Handler implements Runnable {

        private DaemonController controller = null;
        private Oobdd parent = null;
        private String directory = null;
        private Socket socket = null;
        private int number = 0;

        public Handler(Socket s, Oobdd p, DaemonController c) {
            super();
            this.socket = s;
            this.parent = p;
            this.controller = c;
        }

        public void run() {
            this.parent.addHandler(this);
            System.err.println("SimpleDaemon: connection " + this.number
                    + " opened from " + this.socket.getInetAddress());
            try {
                InputStream in = this.socket.getInputStream();
                OutputStream out = this.socket.getOutputStream();
                handle(in, out);
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("SimpleDaemon: connection " + this.number
                    + " closed");
            this.parent.removeHandler(this);
        }

        public void close() {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

        public void setConnectionNumber(int number) {
            this.number = number;
        }

        public int getConnectionNumber() {
            return (this.number);
        }

        public void setDirectoryName(String directory) {
            this.directory = directory;
        }

        public String getDirectoryName() {
            return (this.directory);
        }

        public void log(String name)
                throws IOException {
            OutputStream file = new FileOutputStream(name, true);
            PrintStream out = new PrintStream(file);
            SimpleDateFormat fmt = new SimpleDateFormat();

            out.println(fmt.format(new Date()));
            out.close();
            file.close();
        }

        public void handle(InputStream in, OutputStream os) {
            PrintStream out = new PrintStream(os);

            while (true) {
                try {
                    /* If we don't have data in the System InputStream, we want
                     to ask to the user for an option. */
                    if (in.available() == 0) {
                        out.println();
                        out.println("Please select one of the following:");
                        out.println("    1) Shutdown");
                        out.println("    2) Reload");
                        out.println("    3) Create a file");
                        out.println("    4) Disconnect");
                        out.println("    5) Soft reload");
                        out.print("Your choice: ");
                    }

                    /* Read an option from the client */
                    int x = in.read();

                    switch (x) {
                        /* If the socket was closed, we simply return */
                        case -1:
                            return;

                        /* Attempt to shutdown */
                        case '1':
                            out.println("Attempting a shutdown...");
                            try {
                                this.controller.shutdown();
                            } catch (IllegalStateException e) {
                                out.println();
                                out.println("Can't shutdown now");
                                e.printStackTrace(out);
                            }
                            break;

                        /* Attempt to reload */
                        case '2':
                            out.println("Attempting a reload...");
                            try {
                                this.controller.reload();
                            } catch (IllegalStateException e) {
                                out.println();
                                out.println("Can't reload now");
                                e.printStackTrace(out);
                            }
                            break;

                        /* Disconnect */
                        case '3':
                            String name = this.getDirectoryName()
                                    + "/SimpleDaemon."
                                    + this.getConnectionNumber()
                                    + ".tmp";
                            try {
                                this.log(name);
                                out.println("File '" + name + "' created");
                            } catch (IOException e) {
                                e.printStackTrace(out);
                            }
                            break;

                        /* Disconnect */
                        case '4':
                            out.println("Disconnecting...");
                            return;
                        case '5':
                            out.println("Reloading configuration...");
                            this.parent.signal();
                            return;

                        /* Discard any carriage return / newline characters */
                        case '\r':
                        case '\n':
                            break;

                        /* We got something that we weren't supposed to get */
                        default:
                            out.println("Unknown option '" + (char) x + "'");
                            break;

                    }

                    /* If we get an IOException we return (disconnect) */
                } catch (IOException e) {
                    System.err.println("SimpleDaemon: IOException in "
                            + "connection "
                            + this.getConnectionNumber());
                    return;
                }
            }
        }
    }

}
