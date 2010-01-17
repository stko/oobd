/*
 * This interface contains the program constants in an interface format for direct implementation
 */

package org.oobd.base;

/**
 *
 * @author steffen
 */
public interface Constants {
    public static final String ACTION = "action";




    // constants for the debug message handling
    public static final Integer DEBUG_BORING = 0;
    public static final Integer DEBUG_INFO = 1;
    public static final Integer DEBUG_WARNING = 2;
    public static final Integer DEBUG_ERROR = 3;
    public static final Integer DEBUG_FATAL = 4;
    public static final Integer DEBUG_DEFAULTLEVEL = DEBUG_BORING;
}
