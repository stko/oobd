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

import org.oobd.core.port.ComPort_Win;

//import java.io.FileInputStream;
import java.io.*;
import java.net.URI;
import org.oobd.OOBD;
import org.oobd.core.IFsystem;
import org.oobd.core.support.Onion;

/**
 * This class is the connection between the generic oobd system and the
 * enviroment for e.g. IO operations
 *
 * @author steffen
 */
public class SwingSystem implements IFsystem {

     static SwingSystem thisInstance = null;
    Preferences myPrefs = null;


    public SwingSystem() {
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
                Desktop.getDesktop().browse(new URI(OOBD.getOobdURL()));
            } catch (IOException | URISyntaxException ex) {
            }
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
                myFile = new File(OOBD.getScriptDir() + "/" + fileName);
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
    public String loadPreferences() {
        Preferences prefsRoot;
        Onion prefs = new Onion();
        try {
            prefsRoot = Preferences.userRoot();
            prefsRoot.sync();
            myPrefs = prefsRoot.node("OOBDesk");
            String sysKeys[];
            if (myPrefs.keys().length == 0) { //no settings found? Then try to read system prefs
                Preferences sysPrefsRoot;
                Preferences mySysPrefs = null;
                sysPrefsRoot = Preferences.systemRoot();
                sysPrefsRoot.sync();
                mySysPrefs = sysPrefsRoot.node("OOBDesk");
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
            Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "could not load preferenves for OOBDesk", e);
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
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "could not save preferenves for OOBDesk", e);

            return false;
        }

    }

    @Override
    public String doFileSelector(String path, String extension, String message, boolean Save) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
