/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core;

/**
 *
 * @author steffen
 */


import java.util.ArrayList;
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
import org.oobd.OOBD.IllegalSettingsException;
import static org.oobd.core.OOBDConstants.DP_ACTUAL_CONNECTION_TYPE;
import static org.oobd.core.OOBDConstants.DP_ARRAY_SIZE;
import static org.oobd.core.OOBDConstants.FT_PROPS;
import org.oobd.core.archive.Archive;
import org.oobd.core.archive.Factory;
import org.oobd.core.support.Onion;
import org.oobd.core.support.OnionNoEntryException;
import org.oobd.core.support.OnionWrongTypeException;

public class Settings {

    static Onion prefs;
    static Preferences pref;
    static boolean validAdmin = false;
    static String lockExt = "_lock";
    static String probNamePassword = "Password";
    static int propOrder;
    static IFsystem  savePrefsCallback;
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
            + "  \"ConnectType\": {\"type\" : \"string\" , \"title\": \"Connection Type\" , \"description\" : \"How to connect to the dongle\"},\n"
            + "  \"LibraryDir\": {\"type\" : \"string\" , \"title\": \"HTML Library Directory\" , \"description\" : \"where the HTML library is stored\"},\n"
            + "}";
    static String lockTemplateString = "{\"type\" : \"boolean\" , \"title\": \"Protect '##'\" , \"description\" : \"only changable by admin, if selected\"}";
    static final ArrayList dataPoolList = new ArrayList(OOBDConstants.DP_ARRAY_SIZE);

    static public void transferPreferences2System(String localConnectTypeName) {
        if (localConnectTypeName != null && !localConnectTypeName.equalsIgnoreCase("")) {
            writeDataPool(OOBDConstants.DP_ACTUAL_REMOTECONNECT_SERVER, Settings.getString(localConnectTypeName + "_" + OOBDConstants.PropName_ConnectServerURL, ""));
            writeDataPool(OOBDConstants.DP_ACTUAL_PROXY_HOST, Settings.getString(localConnectTypeName + "_" + OOBDConstants.PropName_ProxyHost, ""));
            writeDataPool(OOBDConstants.DP_ACTUAL_PROXY_PORT, Settings.getInt(localConnectTypeName + "_" + OOBDConstants.PropName_ProxyPort, 0));
            writeDataPool(OOBDConstants.DP_ACTUAL_CONNECT_ID, Settings.getString(localConnectTypeName + "_" + OOBDConstants.PropName_SerialPort, ""));
        }
        writeDataPool(OOBDConstants.DP_ACTUAL_UIHANDLER, Settings.getString(OOBDConstants.PropName_UIHander, OOBDConstants.UIHANDLER_WS_NAME));
        String actualScriptDir = Settings.getString(OOBDConstants.PropName_ScriptDir, ".");
        writeDataPool(OOBDConstants.DP_SCRIPTDIR, actualScriptDir);
        writeDataPool(OOBDConstants.DP_WWW_LIB_DIR, Settings.getString(OOBDConstants.PropName_LibraryDir, "."));
        ArrayList<Archive> files = Factory.getDirContent(actualScriptDir);
        writeDataPool(OOBDConstants.DP_LIST_OF_SCRIPTS, files);
        writeDataPool(OOBDConstants.DP_HTTP_HOST, Core.getSingleInstance().getSystemIP());
        writeDataPool(OOBDConstants.DP_HTTP_PORT, 8080);
        writeDataPool(OOBDConstants.DP_WSOCKET_PORT, 8443);
    }

    /**
     * @brief gets an object to the global data pool, used for most of the
     * variables used in OOBD
     *
     * @param id a numeric identifier, defined in OOBDConstants in DP_ (Data
     * Pool) section
     * @param defaultObject object returned, if object is null
     * @return Object
     */
    public static Object readDataPool(int id, Object defaultObject) {
        synchronized (dataPoolList) {
            try {
                Object data = dataPoolList.get(id);
                if (data == null) {
                    return defaultObject;
                }
                return data;
            } catch (IndexOutOfBoundsException ex) {
                return defaultObject;
            }
        }
    }

    /**
     * @brief add an object to the global data pool, used for most of the
     * variables used in OOBD
     *
     * @param id a numeric identifier, defined in OOBDConstants in DP_ (Data
     * Pool) section
     * @param data object reference to be stored
     */
    static public void writeDataPool(int id, Object data) {
        synchronized (dataPoolList) {
            if (id >= dataPoolList.size()) {
                dataPoolList.ensureCapacity(id + 1);
            }
            dataPoolList.set(id, data);
        }
    }

    /**
     * @brief removes an object from the global data pool, used for most of the
     * variables used in OOBD
     *
     * @param id a numeric identifier, defined in OOBDConstants in DP_ (Data
     * Pool) section
     * @return removed object
     */
    static public Object removeDataPool(int id, Object defaultObject, Core core1) {
        synchronized (dataPoolList) {
            return dataPoolList.remove(id);
        }
    }



    public static void init(IFsystem savePrefs){
               prefs = new Onion();
               savePrefsCallback=savePrefs;
         for (int i = 0; i < DP_ARRAY_SIZE; i++) {
            dataPoolList.add(null);
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
        String connectTypeName = Settings.getString(OOBDConstants.PropName_ConnectType, OOBDConstants.PropName_ConnectTypeBT);
        Settings.writeDataPool(DP_ACTUAL_CONNECTION_TYPE, connectTypeName);

        Settings.transferPreferences2System(connectTypeName);

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
            String actualConnectType = "";
            for (String templateKey : buffer.keySet()) {
                valueObject = buffer.get(templateKey);
                if (valueObject instanceof JSONObject) {
                    if (!"ConnectType".equals(templateKey)) {
                        innerResult += outputSingleValuScheme(innerResult, (JSONObject) valueObject, templateKey, templateKey);
                    } else {
                        try {
                            actualConnectType = prefs.getString(templateKey);
                        } catch (JSONException ex) {

                        }
                    }
                }
            }
            result += innerResult + "\n,";
            innerResult = "";

            boolean connectTypeLocked = prefs.getBoolean("ConnectType" + lockExt);
            JSONObject connectTypeData = (JSONObject) templateJSON.get("ConnectType");
            if (validAdmin) {
                try {
                    JSONObject lockTemplate = new JSONObject(lockTemplateString);
                    lockTemplate.put("title", ((String) lockTemplate.get("title")).replace("##", (String) connectTypeData.get("title")));
                    innerResult += formatSingleValuScheme(innerResult, lockTemplate, "ConnectType" + lockExt, "ConnectType" + lockExt);
                    propOrder++;
                } catch (JSONException ex) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                }
                //result += ",";

                result += innerResult + "\n,";
            }
            if (!connectTypeLocked || validAdmin) {
                result += "\"ConnectType\": {\n"
                        + "\"type\": \"string\",\n"
                        + "\"title\": \"" + connectTypeData.getString("title") + "\",\n"
                        + "\"description\": \"" + connectTypeData.getString("description") + "\", \"propertyOrder\": " + propOrder + " ,"
                        + "\"uniqueItems\": true,\n\"enum\": [";
                innerResult = "";
                for (String templateKey : buffer.keySet()) {
                    valueObject = buffer.get(templateKey);
                    if (valueObject instanceof HashMap) {
                        // no connecttype set already? Then use the first found type as default dummy
                        if ("".equals(actualConnectType)) {
                            actualConnectType = templateKey;
                        }
                        if (!innerResult.equals("")) {
                            innerResult += ",\n";
                        }
                        innerResult += "\"" + templateKey + "\"";
                    }
                }
                result += innerResult + "\n],\n\"default\":\"" + actualConnectType + "\"\n}\n";
                propOrder++;

            }
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
            return prefs.getOnionInt(path);
        } catch (ClassCastException | OnionWrongTypeException | OnionNoEntryException ex) {
            prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }

    public static void setInt(String path, int value) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        prefs.setValue(path, value);
    }

    public static String getString(String path, String defaultValue) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        try {
            return prefs.getOnionString(path);
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }

    public static void setString(String path, String value) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        prefs.setValue(path, value);
    }

    public static boolean getBoolean(String path, boolean defaultValue) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        try {
            return prefs.getOnionBoolean(path);
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            prefs.setValue(path, defaultValue);
            return defaultValue;
        }
    }

    public static void setBoolean(String path, boolean value) {
        //temporary tweak to make it work
        path = path.replaceAll("_", "/");
        prefs.setValue(path, value);
    }

    public static void savePreferences() {
        savePrefsCallback.savePreferences(prefs.toString());

    }

}
