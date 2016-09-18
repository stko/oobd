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

/**
 *
 * @author steffen
 */
public class Oobdd implements IFsystem, OOBDConstants {
    Preferences myPrefs = null;

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

}
