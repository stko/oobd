/*
 * This interface contains the program constants in an interface format for direct implementation
 */
package org.oobd.base;

/**
 *
 * @author steffen
 */
public interface OOBDConstants {
    // Type of the different onion  messages, which are send through the system (the so called Core- messages)

    public static final String CM_VISUALIZE = "VISUALIZE";
    public static final String CM_CANVAS = "CANVAS";
    public static final String CM_VALUE = "VALUE";
    // IDs used for internal core list handling
    public static final String CL_PANE = "pane";
    public static final String CL_CANVAS = "canvas";
    // constants for the debug message handling
    public static final Integer DEBUG_BORING = 0;
    public static final Integer DEBUG_INFO = 1;
    public static final Integer DEBUG_WARNING = 2;
    public static final Integer DEBUG_ERROR = 3;
    public static final Integer DEBUG_FATAL = 4;
    public static final Integer DEBUG_DEFAULTLEVEL = DEBUG_BORING;
    // IDs used as field names in the onions
    public static final String FN_NAME = "name";
    public static final String FN_OWNER = "owner";
    public static final String FN_VALUESTRING = "ValueString";
    // Types of the different visualizer update requests
    public static final Integer UR_USER = 0;
    public final static String CorePrefsFileName = "oobdcore.props";
}
