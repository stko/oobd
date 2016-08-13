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
import org.json.JSONException;
import org.oobd.base.support.Onion;


public class Settings {
    
    Onion prefs;

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
    }
    
    public int getInt(String prefix, String name, int defaultValue){
        return prefs.getOnionInt(name, defaultValue);
    }
}
