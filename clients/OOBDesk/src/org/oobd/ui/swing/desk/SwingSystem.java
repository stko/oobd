/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.ui.swing.desk;

import java.awt.Desktop;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import org.oobd.base.*;
import org.oobd.base.OOBDConstants;
import java.net.URL;
import java.net.URLClassLoader;
import org.oobd.base.port.ComPort_Win;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.crypt.AES.EncodeDecodeAES;

//import java.io.FileInputStream;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import org.json.JSONException;
import static org.oobd.base.OOBDConstants.DP_ACTIVE_ARCHIVE;
import static org.oobd.base.OOBDConstants.DP_LIST_OF_SCRIPTS;
import static org.oobd.base.OOBDConstants.MANIFEST_STARTPAGE;
import org.oobd.base.archive.Archive;
import org.oobd.base.support.Onion;
import org.oobd.base.port.ComPort_Kadaver;
import org.oobd.base.port.ComPort_Telnet;
import org.oobd.crypt.AES.PassPhraseProvider;
import javax.jmdns.*;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.support.OnionWrongTypeException;

/**
 * This class is the connection between the generic oobd system and the
 * enviroment for e.g. IO operations
 *
 * @author steffen
 */
public class SwingSystem implements IFsystem, OOBDConstants {

    Core core;
    JmDNS jmdns;
    static SwingSystem thisInstance = null;

    @Override
    public void registerOobdCore(Core thisCore) {
        core = thisCore;

        final HashMap<String, String> values = new HashMap<String, String>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    jmdns = JmDNS.create();
                    values.put("host-name", "OOBD-" + jmdns.getHostName());
                    // ServiceInfo service_info =  ServiceInfo.create("_http._tcp.local.", "foo._http._tcp.local.", 1234, 0, 0, "path=index.html")
                    //  ServiceInfo service_info = ServiceInfo.create("_http._tcp.", "OOBDesk", 8080, "path=/")
                    // ServiceInfo service_info =   ServiceInfo.create("_http._tcp.", "OOBDesk", 8080, 0,0,values)
                    String service_type = "_http._tcp.";
                    //       String service_name = "http://www.mycompany.com/xyz.html";
                    //String service_name = "OOBD-" + jmdns.getHostName();
                    //String service_name = Core.getSingleInstance().getSystemIF().getOobdURL();
                    String service_name = "OOBD DaaS (" + core.getMACAddress() + ")";
                    int service_port = (int) Settings.readDataPool(DP_HTTP_PORT, 8080);
                    ServiceInfo service_info = ServiceInfo.create(service_type, service_name, service_port, "");
                    jmdns.registerService(service_info);
                } catch (IOException ex) {
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

    }

    public  SwingSystem() {
        thisInstance = this;
    }

    /**
     * a static help routine which returns the actual running Instance of the
     * Core class
     *
     * @return Core
     */
    public static SwingSystem getSingleInstance() {
        return thisInstance;
    }

    public void openBrowser() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(core.getOobdURL()));
            } catch (IOException ex) {
            } catch (URISyntaxException ex) {
            }
        }
    }

    @Override
    public String getSystemDefaultDirectory(boolean privateDir, String fileName){
        if (privateDir){
                return System.getProperty("user.home") + "/" + fileName;
        }else{
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
    public Preferences loadPreferences(int pathID, String filename) {
        Preferences myPrefs = null;
        Preferences prefsRoot;

        try {
            prefsRoot = Preferences.userRoot();
            prefsRoot.sync();
            myPrefs = prefsRoot.node("com.oobd.preference." + filename);
            String sysKeys[];
            if (myPrefs.keys().length == 0) { //no settings found? Then try to read system prefs
                Preferences sysPrefsRoot;
                Preferences mySysPrefs = null;
                sysPrefsRoot = Preferences.systemRoot();
                sysPrefsRoot.sync();
                mySysPrefs = sysPrefsRoot.node("com.oobd.preference." + filename);
                sysKeys = mySysPrefs.keys();
                for (int i = 0; i < sysKeys.length; i++) { //copy system settings, if any exist
                    myPrefs.put(sysKeys[i], mySysPrefs.get(sysKeys[i], ""));
                }
            }
            return myPrefs;
        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "could not load property id " + filename, e);
        }
        return myPrefs;
    }

    @Override
    public boolean savePreferences(int pathID, String filename, Preferences properties) {
        try {
            properties.flush();
            return true;

        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "could not load property id " + filename, e);

            return false;
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

}
