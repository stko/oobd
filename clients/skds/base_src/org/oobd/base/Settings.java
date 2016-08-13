/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

/**
 *
 * @author steffen
 */

/*

 Beispielcode für ein eingelagertes Interface

 public class Main {

 public interface Visitor{
 int doJob(int a, int b);
 }


 public static void main(String[] args) {
 Visitor adder = new Visitor(){
 public int doJob(int a, int b) {
 return a + b;
 }
 };

 Visitor multiplier = new Visitor(){
 public int doJob(int a, int b) {
 return a*b;
 }
 };

 System.out.println(adder.doJob(10, 20));
 System.out.println(multiplier.doJob(10, 20));

 }
 }

Setzen eines Nullwertes in JSON:
Try to set JSONObject.NULL instead of null:


 */


import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.json.JSONException;
import static org.oobd.base.OOBDConstants.FT_PROPS;
import org.oobd.base.support.Onion;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.support.OnionWrongTypeException;


public class Settings {
    
    static Onion prefs;
    static Settings myself;
    static Preferences pref;

    public class IllegalSettingsException extends Exception {

        public IllegalSettingsException() {
            super();
        }

        public IllegalSettingsException(String message) {
            super(message);
        }

        public IllegalSettingsException(String message, Throwable cause) {
            super(message, cause);
        }

        public IllegalSettingsException(Throwable cause) {
            super(cause);
        }
    }

    
    public interface getPreferenceJsonString {

        String getprefString();
    }

    public Settings(getPreferenceJsonString getPrefs) throws IllegalSettingsException {
        try {
            prefs= new Onion(getPrefs.getprefString());
        } catch (JSONException ex) {
             throw new IllegalSettingsException(ex);
        }
        myself=this;
    }
    
    public Settings(Preferences thisPrefs) throws IllegalSettingsException {
        try {
            prefs=new Onion();
            String[] sysKeys = thisPrefs.keys();
            for (int i = 0; i < sysKeys.length; i++) { //copy system settings, if any exist
                System.out.println(sysKeys[i] + ":" + thisPrefs.get(sysKeys[i], ""));
                prefs.setValue(sysKeys[i].replaceAll("_", "/"), thisPrefs.get(sysKeys[i], ""));
            }
            System.out.println(prefs);
            myself=this;
            pref=thisPrefs;
        } catch (BackingStoreException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalSettingsException(ex);
        }
    }
    
    public static int getInt( String path, int defaultValue){
        //temporary tweak to make it work
        path=path.replaceAll("_", "/");
        try {
            return myself.prefs.getOnionInt(path);
        } catch (ClassCastException|OnionWrongTypeException | OnionNoEntryException ex) {
            myself.prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }
    
    
    public static void setInt( String path, int value){
        //temporary tweak to make it work
        path=path.replaceAll("_", "/");
        myself.prefs.setValue(path, value);
    }
  
    public static String getString( String path, String defaultValue){
        //temporary tweak to make it work
        path=path.replaceAll("_", "/");
        try {
            return myself.prefs.getOnionString(path);
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            myself.prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }
    
    
    public static void setString( String path, String value){
        //temporary tweak to make it work
        path=path.replaceAll("_", "/");
        myself.prefs.setValue(path, value);
    }
 
    public static boolean getBoolean( String path, boolean defaultValue){
        //temporary tweak to make it work
        path=path.replaceAll("_", "/");
        try {
            return myself.prefs.getOnionBoolean(path);
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            myself.prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }
    
    
    public static void setBoolean( String path, boolean value){
        //temporary tweak to make it work
        path=path.replaceAll("_", "/");
        myself.prefs.setValue(path, value);
    }
    
    public static void savePreferences(){
        Core.getSingleInstance().getSystemIF().savePreferences(FT_PROPS,
                OOBDConstants.AppPrefsFileName, pref);
    }
}
