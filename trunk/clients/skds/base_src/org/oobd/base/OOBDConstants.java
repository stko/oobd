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
    public static final String CM_PAGE = "PAGE";
    public static final String CM_CHANNEL="CHANNEL";
    public static final String CM_PAGEDONE = "PAGEDONE";
    public static final String CM_VALUE = "VALUE";
    public static final String CM_UPDATE = "UPDATE";
    public static final String CM_RES_BUS = "RESULT_BUS";
    public static final String CM_RES_LOOKUP = "RESULT_LOOKUP";
    public static final String CM_BUSTEST = "BUSTEST";
    public static final String CM_WRITESTRING = "WRITESTRING";
    public static final String CM_DBLOOKUP = "DBLOOKUP";
    public static final String CM_PARAM = "PARAM";
    public static final String CM_IOINPUT = "IOINPUT";
    // IDs used for internal core list handling
    public static final String CL_PANE = "pane";
    public static final String CL_OBJECTS = "objects";
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
    public static final String FN_OPTID = "optid";
    public static final String FN_VALUESTRING = "ValueString";
    public static final String FN_TOOLTIP = "tooltip";
    public static final String FN_UPDATEOPS = "updevents";
    public static final String FN_OPTTYPE = "opts/type";
    public static final String FN_OPTREGEX = "opts/regex";
    public static final String FN_OPTMIN = "opts/min";
    public static final String FN_OPTMAX = "opts/max";
    public static final String FN_OPTSTEP = "opts/step";
    public static final String FN_OPTUNIT = "opts/unit";
     // Types of the different visualizer update requests
    public static final Integer UR_USER = 0;
    public static final Integer UR_UPDATE = 1;
    public static final Integer UR_TIMER = 2;
    // file names for Disclaimer files:
    public final static String DisclaimerFileName = "disclaim.html";
    // file names for property files:
    public final static String CorePrefsFileName = "oobdcore.props";
    public final static String AppPrefsFileName = "app.props";
     //key names for properties
     public final static String PropName_SerialPort ="SerialPort";
     public final static String PropName_ScriptDir ="ScriptDir";
     public final static String PropName_ScriptName ="Script";
     public final static String PropName_OutputDir ="OutputDir";
     public final static String PropName_PGPEnabled ="PGPEnabled";
     public final static String PropName_UIHander ="UIHandler";
     //PGP key file  names
    public final static String PGP_USER_KEYFILE_NAME = "userkey.sec";
    public final static String PGP_GROUP_KEYFILE_NAME = "groupkey.sec";
    //Mailbox names
   public final static String CoreMailboxName = "core";
  public final static String UIHandlerMailboxName = "UIHandler";
   public final static String BusMailboxName = "BusCom";
    public final static String DBName = "AVLLookup";
    // definitions of the Visual Elements Handling flags (=bitpositions)
     public final static int VE_MENU = 0;
    public final static int VE_UPDATE = 1;
    public final static int VE_TIMER = 2;
    public final static int VE_LOG = 3;
    public final static int VE_BACK = 4;
    // definitions for the different directories where the UI searches standard files in
    public final static int FT_PROPS = 0;
    public final static int FT_SCRIPT = 1;
    public final static int FT_BUS = 2;
    public final static int FT_ENGINE = 3;
    public final static int FT_DATABASE = 4;
    public final static int FT_RAW = 5;
    public final static int FT_KEY = 6;
    // interval counter, after how many timer ticks the timer button items shall be refreshed
    public final static int LV_UPDATE_INTERVAL = 10;
    // loop time for timer triggered List- Updates in ms
    public final static int LV_UPDATE_UI = 10;
    // loop time for status Updates in ms
    public final static int LV_STATUS = 1000;
}
