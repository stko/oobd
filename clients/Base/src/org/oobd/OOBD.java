/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd;

import org.oobd.core.Core;
import org.oobd.core.IFsystem;
import org.oobd.core.OOBDConstants;
import org.oobd.core.Settings;

/**
 *
 * @author steffen
 */
public class OOBD {

    static Core core=null;

    public OOBD(IFsystem system) throws IllegalSettingsException {
        core = new Core(system, "Core");
    }

    static public String getScriptDir() {
        return Settings.getString(OOBDConstants.PropName_ScriptDir, "");
    }

 
    static public String getOobdURL() {
        if (core != null) {
            return core.getOobdURL();
        }
        return "";
    }

    public Thread getCoreThread() {
        return core.getThread();
    }

    public void close() {
        if (core != null) {
            core.close();
            core = null;
        }
    }

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

}
