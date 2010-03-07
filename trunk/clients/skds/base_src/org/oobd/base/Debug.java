/*
 * Some help functions for debugging
 */
package org.oobd.base;

import java.util.HashMap;

/**
 *
 * @author steffen
 */
public class Debug implements OOBDConstants {

    public static HashMap<String, Integer> debugLevels;

    static {
        debugLevels = new HashMap<String, Integer>();
        debugLevels.put("ALL", DEBUG_DEFAULTLEVEL); //stores the debuglevels
    }

    public static void msg(String system, Integer debugLevel, String msg) {
        if (!debugLevels.containsKey(system)) { // if no level is given for that system
            if (debugLevel < debugLevels.get("ALL")) {  // if requested level is lower than the default
                return; //we just do nothing
            }
        }
        if (debugLevels.containsKey(system) && debugLevel < debugLevels.get(system)) {//if requested level is lower than the set value for that system
            return; // we do nothing
        }

        if (debugLevel == DEBUG_FATAL) {
            System.err.print("Fatal: ");
        }
        if (debugLevel == DEBUG_ERROR) {
            System.err.print("("+system+") Error: ");
        }
        if (debugLevel == DEBUG_WARNING) {
            System.out.print("("+system+") Warning: ");
        }
        if (debugLevel == DEBUG_INFO) {
            System.out.print("("+system+") Info: ");
        }
        if (debugLevel == DEBUG_BORING) {
            System.out.print("("+system+") Boring: ");
        }

        if (debugLevel >= DEBUG_ERROR) {
            System.err.println(msg);
        } else {
            System.out.println(msg);

        }
    }
}
