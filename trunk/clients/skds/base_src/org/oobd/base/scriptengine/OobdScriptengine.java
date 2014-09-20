/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.oobd.base.*;
import org.oobd.base.support.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 * generic abstract for the implementation of scriptengines
 * @author steffen
 */
abstract public class OobdScriptengine extends OobdPlugin implements OOBDConstants {

    protected Onion myStartupParam;
    protected File myTempFile = null;
    protected OobdScriptengine myself;

    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "";
    }

    public OobdScriptengine(String myID, Core myCore, IFsystem mySystem, String name) {
        super(name);
        id = myID;
        core = myCore;
        UISystem = mySystem;
        Logger.getLogger(OobdScriptengine.class.getName()).log(Level.CONFIG, "Scriptengine  object created: " + id);

    }

    public void setStartupParameter(Onion onion) {
        myStartupParam = onion;
    }

    /**
     * \brief tells the Scriptengine, which actual tempfile the systemIF has reserved
     * 
     * @param the temp file path
     */
    public void setTempFile(File newFile) {
        myTempFile = newFile;
    }

    /**
     * \brief reports which actual tempfile the Scriptengine is using
     * 
     * @return the actual temp file path
     */
    public File getTempFile() {
        return myTempFile;
    }

    /**
     * \brief tries to open an Input file
     * 
     * @return true if success
     */
    public String createInputTempFile(String filepath, String extension, String message) {
        String result = "";
        Message answer = null;
        try {
            answer = myself.getMsgPort().sendAndWait(
                    new Message(
                    myself,
                    UIHandlerMailboxName,
                    new Onion(
                    ""
                    + "{'type':'"
                    + CM_IOINPUT
                    + "',"
                    + "'owner':"
                    + "{'name':'"
                    + myself.getId()
                    + "'},"
                    + "'filepath':'"
                    + Base64Coder.encodeString(filepath)
                    + "',"
                    + "'extension':'"
                    + Base64Coder.encodeString(extension)
                    + "',"
                    + "'message':'"
                    + Base64Coder.encodeString(message)
                    + "'}")),
                    -1); // timeout -1 = wait forever
            if (answer != null) {
                Logger.getLogger(ScriptengineLua.class.getName()).log(Level.INFO,
                        "Lua calls ioInputCall returns with onion:"
                        + answer.getContent().toString());
                result = answer.getContent().getString("result");
                if (result != null && result.length() > 0) {
                    result = new String(Base64Coder.decodeString(result));
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(ScriptengineLua.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * \brief fills the temp file with data
     * 
     * @param the inputstream
     * @return true if success 
     */
    public boolean fillTempFile(InputStream in) {
        boolean res = false;
        if (myTempFile != null) {
            try {
                FileOutputStream myTempFileOutput = new FileOutputStream(myTempFile);
                org.apache.commons.io.IOUtils.copy(in, myTempFileOutput);
                res = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(OobdScriptengine.class.getName()).log(Level.SEVERE, null, ex);

            } catch (IOException ex) {
                Logger.getLogger(OobdScriptengine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return res;
    }
}
