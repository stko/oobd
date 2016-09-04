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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static java.util.Spliterators.iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.json.JSONException;
import org.json.JSONObject;
import static org.oobd.base.OOBDConstants.FT_PROPS;
import org.oobd.base.support.Onion;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.support.OnionWrongTypeException;

public class Settings {

    static Onion prefs;
    static Settings myself;
    static Preferences pref;
    static boolean validAdmin = false;
    static String lockExt = "_lock";
    static String probNamePassword = "Password";
    static int propOrder;
    static SavePreferenceJsonString savePrefsCallback;
    static String prefsTemplateString = "{\n"
            //           + "  \"Bluetooth_ServerProxyPort\": {\"type\" : \"integer\" , \"title\": \"\" , \"description\" : \"(not used)\"},\n"
            + "  \"" + probNamePassword + "\": {\"type\" : \"string\" , \"title\": \"Admin Password\" , \"description\" : \"Password to lock the protected Settings\"},\n"
            + "  \"Bluetooth_SerialPort\": {\"type\" : \"string\" , \"title\": \"Serial Port\" , \"description\" : \"Where the dongle is connected to\"},\n"
            //           + "  \"Bluetooth_ConnectServerURL\": {\"type\" : \"string\" , \"title\": \"\" , \"description\" : \"(not used)\"},\n"
            //           + "  \"Bluetooth_ServerProxyHost\": {\"type\" : \"string\" , \"title\": \"\" , \"description\" : \"(not used)\"},\n"
            + "  \"UIHandler\": {\"type\" : \"string\" , \"title\": \"User Inferface\" , \"description\" : \"Legacy: switch between Web uns native UI\"},\n"
            + "  \"ScriptDir\": {\"type\" : \"string\" , \"title\": \"Script Directory\" , \"description\" : \"where the scripts are stored\"},\n"
            + "  \"Kadaver_ServerProxyPort\": {\"type\" : \"integer\" , \"title\": \"Proxy Server Port\" , \"description\" : \"Port of a Proxy Server (0 to disable)\"},\n"
            + "  \"Kadaver_SerialPort\": {\"type\" : \"string\" , \"title\": \"Connect ID\" , \"description\" : \"The ID of the remote client\"},\n"
            + "  \"Kadaver_ConnectServerURL\": {\"type\" : \"string\" , \"title\": \"Kadaver Server URL\" , \"description\" : \"The URL of the Kadaver Connect Server\"},\n"
            + "  \"Kadaver_ServerProxyHost\": {\"type\" : \"string\" , \"title\": \"Proxy Server Host\" , \"description\" : \"Proxy Host, if needed\"},\n"
            //            + "  \"Telnet_ServerProxyPort\": {\"type\" : \"integer\" , \"title\": \"\" , \"description\" : \"(not used)\"},\n"
            + "  \"Telnet_SerialPort\": {\"type\" : \"string\" , \"title\": \"\" , \"description\" : \"Server Host\"},\n"
            + "  \"Telnet_ConnectServerURL\": {\"type\" : \"string\" , \"title\": \"Connect URL\" , \"description\" : \"remore Host and port as URL\"},\n"
            //            + "  \"Telnet_ServerProxyHost\": {\"type\" : \"string\" , \"title\": \"\" , \"description\" : \"(not used)\"},\n"
            + "  \"PGPEnabled\": {\"type\" : \"boolean\" , \"title\": \"Enable PGP\" , \"description\" : \"(not used)\"},\n"
            + "  \"ConnectType\": {\"type\" : \"string\" , \"title\": \"\" , \"description\" : \"Connection Type\"},\n"
            + "  \"LibraryDir\": {\"type\" : \"string\" , \"title\": \"HTML Library Directory\" , \"description\" : \"where the HTML library is stored\"},\n"
            + "}";
    static String lockTemplateString = "{\"type\" : \"boolean\" , \"title\": \"Protect '##'\" , \"description\" : \"only changable by admin, if selected\"}";

    public static class IllegalSettingsException extends Exception {

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

    public interface SavePreferenceJsonString {

        boolean getprefString(String prefs);
    }

    public Settings(SavePreferenceJsonString savePrefs) throws IllegalSettingsException {
        prefs = new Onion();
        myself = this;
        savePrefsCallback = savePrefs;
    }

    public Settings(Preferences thisPrefs) throws IllegalSettingsException {
        try {
            String prefsString = thisPrefs.get("json", null);
            if (prefsString != null) {
                prefs = new Onion();
                transferSettings(prefsString, true);
                System.out.println("Prefs loaded from JSON String!");
            } else {
                prefs = new Onion();
                String[] sysKeys = thisPrefs.keys();
                for (int i = 0; i < sysKeys.length; i++) { //copy system settings, if any exist
                    System.out.println(sysKeys[i] + ":" + thisPrefs.get(sysKeys[i], ""));
                    prefs.setValue(sysKeys[i].replaceAll("_", "/"), thisPrefs.get(sysKeys[i], ""));
                }
            }
            System.out.println(prefs);
            myself = this;
            pref = thisPrefs;
        } catch (BackingStoreException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalSettingsException(ex);
        }
    }

    static void checkPassword(String password) {
        String setPasswort = prefs.getOnionString(probNamePassword, "");
        validAdmin = "".equals(setPasswort) || setPasswort.equals(password);
        //@bug Caution! Backdoor!!!
        validAdmin = validAdmin || password.equals("foo");
    }

    public static void transferSettings(String input, String password) throws IllegalSettingsException {
        checkPassword(password);
        transferSettings(input, false);
    }

    public static void transferSettings(String input, boolean forceLoadAsAdmin) throws IllegalSettingsException {

        if (input == null || "".equals(input)) {
            return;
            //throw new IllegalSettingsException();
        }
        try {
            Onion inputOnion = new Onion(input);
            JSONObject templateJSON = new JSONObject(prefsTemplateString);

            for (Iterator<String> iter = templateJSON.keys(); iter.hasNext();) {
                String templateKey = iter.next();
                String key = templateKey.replace("_", "/");
                String keyIsLocked = key + lockExt;
                boolean isSetAsLocked = prefs.getOnionBoolean(keyIsLocked, true);
                if (!isSetAsLocked || validAdmin || forceLoadAsAdmin) {
                    Object inputData;
                    try {
                        inputData = inputOnion.getOnionObject(key);
                        JSONObject template = templateJSON.getJSONObject(templateKey);
                        if (inputData != null) {
                            if (validAdmin || forceLoadAsAdmin) {
                                prefs.setValue(keyIsLocked, inputOnion.getOnionBoolean(keyIsLocked, isSetAsLocked));
                            }
                            switch (template.getString("type")) {
                                case "integer":
                                    prefs.setValue(key, inputOnion.getOnionInt(key, 0));
                                    break;
                                case "string":
                                    prefs.setValue(key, inputOnion.getOnionString(key, ""));
                                    break;
                                case "boolean":
                                    prefs.setValue(key, inputOnion.getOnionBoolean(key, false));
                                    break;
                            }
                        }
                    } catch (OnionNoEntryException ex) {
                        // throw new IllegalSettingsException(ex);
                    }
                }
            }
            //savePreferences();
        } catch (JSONException ex) {
            //throw new IllegalSettingsException(ex);
        }
    }

    static String formatSingleValuScheme(String prevResult, JSONObject template, String prefsPath, String name) {
        String result = "";

        if (!"".equals(prevResult)) { // this is just to have a leading , and \n at start, if needed
            //result = ",\n";
        }
        result += "\"" + name + "\" : {";
        try {
            switch (template.getString("type")) {
                case "integer":
                    result += "\n\"type\": \"integer\", \"title\": \"" + template.getString("title") + "\", \"propertyOrder\": " + propOrder + " ,";
                    result += "\n\"default\": " + prefs.getOnionInt(prefsPath, 0) + ",";
                    break;
                case "string":
                    result += "\n\"type\": \"string\", \"title\": \"" + template.getString("title") + "\", \"propertyOrder\": " + propOrder + " ,";
                    result += "\n\"default\": \"" + prefs.getOnionString(prefsPath, "") + "\",";
                    break;
                case "boolean":
                    result += "\n\"type\": \"boolean\", \"format\": \"checkbox\", \"title\": \"" + template.getString("title") + "\", \"propertyOrder\": " + propOrder + " ,";
                    result += "\n\"default\": " + prefs.getOnionBoolean(prefsPath, false) + " ,";
                    break;
            }
            result += "\n\"description\": \"" + template.getString("description") + "\"";
            result += "}\n";
        } catch (JSONException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    static String outputSingleValuScheme(String prevResult, JSONObject template, String prefsPath, String name) {
        String result = "";

        boolean isLocked = true;
        if (!name.endsWith(lockExt)) { //this field represents a lock flag
            isLocked = prefs.getOnionBoolean(prefsPath + lockExt, isLocked);
        }
        if (name.equalsIgnoreCase(probNamePassword)) {
            isLocked = true;
        }
        if (!validAdmin && isLocked) {
            return result;
        }
        if (!"".equals(prevResult)) { // this is just to have a leading , and \n at start, if needed
            result = ",\n";
        }
        if (!name.equalsIgnoreCase(probNamePassword) && validAdmin) {
            try {
                JSONObject lockTemplate = new JSONObject(lockTemplateString);
                lockTemplate.put("title", ((String) lockTemplate.get("title")).replace("##", (String) template.get("title")));
                result += formatSingleValuScheme(result, lockTemplate, prefsPath + lockExt, name + lockExt);
                propOrder++;
            } catch (JSONException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            }
            result += ",";
        }
        result += formatSingleValuScheme(result, template, prefsPath, name);
        propOrder++;
        return result;
    }

    public static String getSettingsScheme(String password) {
        checkPassword(password);
        String result = "";
        propOrder = 1000;
        Object valueObject;
        Map<String, Object> buffer = new HashMap<>();
        try {
            JSONObject templateJSON = new JSONObject(prefsTemplateString);

            for (Iterator<String> iter = templateJSON.keys(); iter.hasNext();) { //first sort the elements into a temporary nested hashmap to output it then sorted by level
                String templateKey = iter.next();
                String[] parts = templateKey.split("_");
                String key = templateKey.replace("_", "/");

                JSONObject template = templateJSON.getJSONObject(templateKey);
                if (parts.length > 1) { // does the key represents a "sub" onion
                    if (!buffer.containsKey(parts[0])) { // if the key not already exist
                        buffer.put(parts[0], new HashMap<String, Object>()); // create sub key
                    }
                    ((HashMap<String, Object>) buffer.get(parts[0])).put(parts[1], template); // and template as element of the second level 
                } else {
                    buffer.put(parts[0], template); // add template in root level
                }
            }
            //printing the top level first
            result = "{\"properties\" : {\n";
            String innerResult = "";
            for (String templateKey : buffer.keySet()) {
                valueObject = buffer.get(templateKey);
                if (valueObject instanceof JSONObject) {
                    if (!"ConnectType".equals(templateKey)) {
                        innerResult += outputSingleValuScheme(innerResult, (JSONObject) valueObject, templateKey, templateKey);
                    }
                }
            }
            result += innerResult + "\n";
            //printing the different connect
            for (String templateKey : buffer.keySet()) {
                valueObject = buffer.get(templateKey);
                if (valueObject instanceof HashMap) {
                    if (!innerResult.equals("")) {
                        result += ",\n";
                    }
                    result += "\"" + templateKey + "\": {\n"
                            + "      \"type\": \"object\",\n"
                            + "      \"title\": \""
                            + templateKey
                            + "\", \"propertyOrder\": "
                            + propOrder
                            + " ,\n";

                    result += "\"properties\" : {\n";
                    innerResult = "";
                    for (String subKey : ((HashMap<String, Object>) valueObject).keySet()) {
                        Object subObject = ((HashMap<String, Object>) valueObject).get(subKey);
                        innerResult += outputSingleValuScheme(innerResult, (JSONObject) subObject, templateKey + "/" + subKey, subKey);
                    }
                    if (innerResult.equals("")) {
                        innerResult = "\n"; // put something harmness in to not fail the "if innerResult==""" condition above..
                    }
                    result += innerResult + "}\n}\n";
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        result += "\n}\n}";
        return result;
    }

    public static int getInt(String path, int defaultValue) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        try {
            return myself.prefs.getOnionInt(path);
        } catch (ClassCastException | OnionWrongTypeException | OnionNoEntryException ex) {
            myself.prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }

    public static void setInt(String path, int value) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        myself.prefs.setValue(path, value);
    }

    public static String getString(String path, String defaultValue) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        try {
            return myself.prefs.getOnionString(path);
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            myself.prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }

    public static void setString(String path, String value) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        myself.prefs.setValue(path, value);
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        try {
            return myself.prefs.getOnionBoolean(path);
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            myself.prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }

    public static void setBoolean(String path, boolean value) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        myself.prefs.setValue(path, value);
    }

    public static void savePreferences() {
        pref.put("json", prefs.toString());
        Core.getSingleInstance().getSystemIF().savePreferences(FT_PROPS,
                OOBDConstants.AppPrefsFileName, pref);
    }

}
