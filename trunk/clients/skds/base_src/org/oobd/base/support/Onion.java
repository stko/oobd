/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.support;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Onion provides the transport medium of the different data arrays which have to be carried through the process
 *
 * It is made internally as a set of nested hash arrays, but from outside it's acessed by a key/value pair, where the key is simular
 * to a folder path, where a folder may contains other folders, means other onions
 * @author steffen
 */
//public class Onion extends Hashtable<String, Object> {
public class Onion extends JSONObject {

    /**
     * Help class to ease internal data handling, used as function return data including key and onion, to which this key belongs to
     */
    class OnionData {

        public Onion onion;
        public String key;

        /**
         * stores Onion and key string to be used as bundled return value from some internal function
         * @param thisOnion
         * @param thisKey
         */
        OnionData(Onion thisOnion, String thisKey) {
            onion = thisOnion;
            key = thisKey;
        }
    }

    /**
     * Default Constructor
     */
    public Onion() {
        super();
    }

    /**
     * Constructor to generate Onion from JSON- String
     * @param jsonString
     * @throws JSONException
     */
    public Onion(String jsonString) throws JSONException {
        super(jsonString);
    }

    /**
     * takes a fresh Onion from the pool (if available)
     * @author steffen
     * @return a new onion
     * @todo does a onion- pool needs to be implemented?
     */
    static public Onion generate() {
        return new Onion();
    }

    /**
     * declares the oldunion as not longer be used
     * @param oldonion
     * @return null
     * @todo does a onion- pool needs to be implemented?
     */
    static public Onion push(Onion oldonion) {
        oldonion = null;
        return null;
    }

    /**
     * returns the object at the given path
     * @param path /path/to/object
     * @return object or null
     * @todo not yet implemented
     * @throws OnionNoEntryException
     */
    public Object getOnionObject(String path) throws OnionNoEntryException {
        Onion actOnion = this;
        String[] parts = path.split("/"); // split the path into the leading part and the rest
        int i = 0;
        while (i < parts.length - 1) { // the last entry in parts is the key for the last sub onion, so we have to handle that seperately in the end

            try {
                if (!actOnion.has(parts[i]) || !(actOnion.get(parts[i]) instanceof JSONObject)) { //if that key does not exist or is not a sub onion
                    throw new OnionNoEntryException();
                } else {
                    actOnion = new Onion(((JSONObject) actOnion.get(parts[i])).toString());
                }
            } catch (JSONException e) {
                throw new OnionNoEntryException();
            }
            i++;
        }
        try {
            return actOnion.get(parts[i]);
        } catch (JSONException e) {
            throw new OnionNoEntryException();
        }
    }

    /**
     * returns the string value at the given path
     * @param path /path/to/value
     * @return string
     * @todo not yet implemented
     * @throws OnionWrongTypeException
     */
    public String getOnionString(String path) {
        try {
            Object result = getOnionObject(path);
            if (!(result instanceof String)) {
                return null;
            }
            return (String) result;
        } catch (OnionNoEntryException e) {
            return null;
        }
    }

    /**
     * returns the string value at the given path
     * @param path /path/to/value
     * @return string
     * @todo not yet implemented
     * @throws OnionWrongTypeException
     */
    public Onion getOnion(String path) {
        try {
            Object result = getOnionObject(path);
            if (!(result instanceof JSONObject)) {
                return null;
            }

            return new Onion(((JSONObject) result).toString());

        } catch (OnionNoEntryException e) {
            return null;
        } catch (JSONException ex) {
            return null;
        }
    }

    /**
     * checks, if the onion has the specified type "type"
     * @param type name of requested type
     * @return boolean
     */
    public boolean isType(
            String type) {
        try {
            return type.matches(this.getString("type"));
        } catch (JSONException ex) {
            return false;
        }
    }

    /**
     * set the String value at the given path.
     *
     * path consist of an optional directory and the key name, seperated by /, like path/to/value, where the last value is used as key for the onion hash.
     * @param path
     * @param value
     * @return onion which contains generated key:value
     */
    public Onion setValue(
            String path, String value) {
        OnionData od = createPath(path);
        if (od != null) {
            try {
                od.onion.put(od.key, value);
            } catch (JSONException e) {
                return null;
            }

            return od.onion;
        } else {
            return null;
        }

    }

    /**
     * set the int value at the given path.
     *
     * path consist of an optional directory and the key name, seperated by /, like path/to/value, where the last value is used as key for the onion hash.
     * @param path
     * @param value
     * @return onion which contains generated key:value
     */
    public Onion setValue(
            String path, int value) {
        OnionData od = createPath(path);
        if (od != null) {
            try {
                od.onion.put(od.key, value);
            } catch (JSONException e) {
                return null;
            }

            return od.onion;
        } else {
            return null;
        }

    }

    /**
     * generates the reqested onion structure
     * @param path
     * @return the onion which points to the given path and the remaining piece of the part, which should be used as key for the hash
     * @bug the routine itself supports relative directories and a leading root- /, but this is not supplied by superclass JSONObject, so it either needs to be deleted or made functional
     */
    OnionData createPath(String path) {
        if (!path.contains("/")) { // in case there no path at all, just a key value, return this
            return new OnionData(this, path);
        }

        Onion actOnion = this;
        String lastpart = "";
        String[] head = path.split("/", 2); // split the path into the leading part and the rest
        while (!head[1].matches("") || !head[0].matches("")) {//as long there's still something in the path
            if (!head[0].matches("")) { // no leading part? That must be a typo then, let's just jump to the next
                lastpart = head[0];
                try {
                    if (!actOnion.has(lastpart) || !(actOnion.get(lastpart) instanceof Onion)) { //if that key does not exist or is just a base type
                        if (!head[1].matches("")) {// do we have to create another sub onion?
                            Onion newOnion = new Onion();
                            actOnion.put(lastpart, newOnion); //adds the new element
                            actOnion =
                                    newOnion;
                        }

                    } else {
                        actOnion = (Onion) actOnion.get(lastpart);
                    }

                } catch (JSONException e) {
                }
            }
            if (head[1].contains("/")) {
                head = head[1].split("/", 2);
            } else {
                head[0] = head[1];
                head[1] = ""; // stop condition for surrounding while- loop
            }

        }
        if (!lastpart.matches("")) {
            return new OnionData(actOnion, lastpart);
        } else {
            return null; //there's no actual key value, so path is invalid...
        }

    }

    /**
     * takes the value of an onion element and releases all subelements, if it is an onion
     * @param value
     * @bug delete behaviour of JSONObjects unclear, so usage of this routine at all needs to be made functional
     */
    void DestroyOnionPath(Object value) {
        if (value != null) {
            if (value instanceof Onion) {
                while (((Onion) value).keys().hasNext()) {
                    DestroyOnionPath(((Onion) value).keys().next());
                }

                this.push(((Onion) value));
                // ((Onion) value).clear();
            }
        }

    }
}
