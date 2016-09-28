/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.scriptengine;

import org.oobd.core.support.Onion;
import org.oobd.core.OOBDConstants;
import org.oobd.core.Message;
import org.oobd.core.OobdPlugin;
import org.oobd.core.Base64Coder;
import org.oobd.core.IFsystem;
import org.oobd.core.Core;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 * generic abstract for the implementation of scriptengines
 *
 * @author steffen
 */
abstract public class OobdScriptengine extends OobdPlugin implements OOBDConstants {

    protected Onion myStartupParam;
    protected File myTempInputFile = null;
    protected FileInputStream actualOobdInputStream = null;
    protected File myTempOutputFile = null;
    protected OobdScriptengine myself;
    private BufferedReader actualOobdInputStreamReader = null;

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
     * \brief tells the Scriptengine, which actual tempfile the systemIF has
     * reserved
     *
     * @param the temp file path
     */
    public void setTempInputFile(File newFile) {
        myTempInputFile = newFile;
    }

    /**
     * \brief reports which actual tempfile the Scriptengine is using
     *
     * @return the actual temp file path
     */
    public File getTempInputFile() {
        return myTempInputFile;
    }

    /**
     * \brief deletes the actual temporary input file, if exists
     *
     */
    public void removeTempInputFile() {
        try {
            if (actualOobdInputStream != null) {
                actualOobdInputStream.close();
                actualOobdInputStream = null;
            }
            if (myTempInputFile != null) {
                myTempInputFile.delete();
                myTempInputFile = null;
            }
            if (actualOobdInputStreamReader != null) {
                actualOobdInputStreamReader.close();
                actualOobdInputStreamReader = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(OobdScriptengine.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public boolean fillTempInputFile(InputStreamReader in) {
        boolean res = false;
        if (myTempInputFile != null) {
            try {
                FileOutputStream myTempFileOutput = new FileOutputStream(myTempInputFile);
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

    /**
     * \brief reads "lua like" from an stored input file
     *
     * @param format defines the kind of returned string
     * @return string, nil if EOL
     */
    public String readTempInputFile(String format) {
        String res = null;
        if (myTempInputFile != null) {
            try {
                if (actualOobdInputStream == null) {
                    actualOobdInputStream = new FileInputStream(myTempInputFile);
                }
                format = format.toLowerCase().trim();
                if ("*all".equals(format) || "*json".equals(format)) {
                    res = org.apache.commons.io.IOUtils.toString(actualOobdInputStream);
                    actualOobdInputStream.close();
                    actualOobdInputStream = null;
                }
                if ("*sha256".equals(format)) {
                    res = org.apache.commons.io.IOUtils.toString(actualOobdInputStream);
                    actualOobdInputStream.close();
                    actualOobdInputStream = null;
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.update(res.getBytes("UTF-8")); // Change this to "UTF-16" if needed
                    byte[] digest = md.digest();
                    res = String.format("%064x", new java.math.BigInteger(1, digest));
                }
                if ("*line".equals(format)) {
                    if (actualOobdInputStreamReader == null) {
                        actualOobdInputStreamReader = new BufferedReader(new InputStreamReader(actualOobdInputStream));
                    }

                    res = actualOobdInputStreamReader.readLine();
                    if (res == null) {
                        actualOobdInputStreamReader.close();
                        actualOobdInputStreamReader = null;
                        actualOobdInputStream.close();
                        actualOobdInputStream = null;
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(OobdScriptengine.class.getName()).log(Level.SEVERE, null, ex);

            } catch (IOException ex) {
                Logger.getLogger(OobdScriptengine.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(OobdScriptengine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return res;
    }

}
