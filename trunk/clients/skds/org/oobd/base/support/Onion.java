/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.support;

import java.util.Hashtable;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Onion provides the carrier of the different data arrays which have to be carried through the process
 * @author steffen
 */
//public class Onion extends Hashtable<String, Object> {
public class Onion extends JSONObject {

    /**
     * parent: points to the parent onion
     * content: contains the data of this actual level
     */
    Onion parent;
    //Hashtable<String, Object> content;
    //JSONObject content;

    /**
     * Help class to ease internal data handling
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
     * @bug parent is used in Onion, but not supplied by superclass JSONObject, so it either needs to be deleted or made functional
     * @param parentOnion
     */
    public Onion(Onion parentOnion) {
        //content = new JSONObject();
        parent = parentOnion;
    }
    /**
     * Constructor to generate Onion from JSON- String
     * @param jsonString
     * @throws JSONException
     */
    public Onion (String jsonString) throws JSONException{
        super(jsonString);
        parent=null;
    }

    /**
     * takes a fresh Onion from the pool (if available)
     * @author steffen
     * @return a new onion
     * @todo does a onion- pool needs to be implemented?
     */
    static public Onion generate(Onion parentOnion) {
        return new Onion(parentOnion);
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
     * returns the onion at the given path
     * @param path /path/to/onion
     * @return onion or null
     * @todo not yet implemented
     */
    public Onion getOnion(String path) {
        return null;
    }

    /**
     * returns the value at the given path
     * @param path /path/to/value
     * @return onion or
     * @todo not yet implemented
     */
    public String getValue(String path) {
        return null;
    }

    /**
     * set the value at the given path.
     *
     * path consist of an optional directory and the key name, seperated by /, like path/to/value, where the last value is used as key for the onion hash.
     * @param path
     * @param value
     * @return onion which contains generated key:value
     */

    public Onion setValue(String path, String value) {
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
        String[] head = path.split("/", 2);
        // does the path start with a leading /? Then move up to root onion
        if (head[0].matches("")) {
            while (actOnion.parent != null) {
                actOnion = actOnion.parent;
            }
            head[0] = ".";
        }
        while (!head[0].matches("") && !head[1].matches("")) {//as long there's still a path and a key in the string
            if (head[0].matches("..")) { // up one level?
                if (actOnion.parent != null) {
                    actOnion = actOnion.parent;
                } else {
                    return null; //there's no parent, so path is invalid...
                }
            } else {
                if (head[0].matches(".")) { // actual level?
                    // no action
                } else {
                    try {
                        if (!actOnion.has(head[0]) || (actOnion.get(head[0]) instanceof String)) { //if there's not already a value for that key
                            Onion newOnion = new Onion(actOnion);
                            // DestroyOnionPath(actOnion.put(head[0], newOnion)); //adds the new element and destroy the previous one, if any
                            actOnion.put(head[0], newOnion); //adds the new element
                            actOnion = newOnion;
                        }else{
                            actOnion=(Onion)actOnion.get(head[0]);
                        }
                    } catch (JSONException e) {
                    }
                }
            }
            if (head[1].contains("/")) {
                head = head[1].split("/", 2);
            } else {
                head[0] = head[1];
                head[1] = ""; // stop condition for surrounding while- loop
            }
        }
        if (!head[0].matches("")) {
            return new OnionData(actOnion, head[0]);
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
