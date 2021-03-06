/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oobd.core.Base64Coder;
import org.oobd.core.Core;
import org.oobd.core.OOBDConstants;
import org.oobd.core.support.Onion;
import org.oobd.crypt.gpg.*;
import org.spongycastle.openpgp.PGPException;

/**
 *
 * @author steffen
 */
public class FileHandlerPGP implements Archive {

    String myFileDirectory;
    String myFilePath;
    String myFileName;
    Core core;

    public FileHandlerPGP(Core c) {
        core = c;
    }

    public InputStream getInputStream(String innerPath) {
        if (myFilePath != null) {
            FileInputStream mfp = null;
            try {
                mfp = new FileInputStream(myFileDirectory + "/" + innerPath);
            } catch (FileNotFoundException e) {
                Core.getSingleInstance().userAlert(
                        "Error: Can't read PGP crypted file", "Diagnose");
            }
            InputStream userKeyFile = core.generateResourceStream(OOBDConstants.FT_KEY,
                    OOBDConstants.PGP_USER_KEYFILE_NAME);
            if (userKeyFile == null) {
                Core.getSingleInstance().userAlert(
                        "Error: Can't read PGP user key file", "Diagnose");
                return null;
            }
            InputStream groupKeyFile = core.generateResourceStream(OOBDConstants.FT_KEY,
                    OOBDConstants.PGP_GROUP_KEYFILE_NAME);
            if (groupKeyFile == null) {
                Core.getSingleInstance().userAlert(
                        "Error: Can't read PGP group key file", "Diagnose");
                return null;
            }
            return GroupDecoder.decryptGroup(mfp, userKeyFile, groupKeyFile,
                    core.getAppPassPhrase(), core.getUserPassPhrase().toCharArray());

        }
        return null;
    }

    public void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException ex) {
                Logger.getLogger(FileHandlerPGP.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }

    public boolean bind(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            filePath = filePath + ".pgp";
            file = new File(filePath);
        }
        if (file.exists()) {
            myFileDirectory = file.getParent().replace("\\", "/");;
            myFilePath = file.getAbsolutePath().replace("\\", "/");;
            myFileName = file.getName();
            return true;
        } else {
            return false;
        }
    }

    public void unBind() {
        myFilePath = null;
    }

    public String getProperty(String property, String defaultValue) {
        if (OOBDConstants.MANIFEST_SCRIPTNAME.equalsIgnoreCase(property)) {
            return myFileName;
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return myFileName;
    }

    public String getID() {
        return Base64Coder.encodeString(myFileName);
    }

    public String getFilePath() {
        return myFilePath;
    }

    public String getFileName() {
        return myFileName;
    }

    @Override
    public void relocateManifest(String luaFileName) {
        // do nothing..
    }

    @Override
    public boolean fileExist(String fileName) {
        InputStream in = getInputStream(fileName);
        if (in == null) {
            return false;
        } else {
            try {
                in.close();
            } catch (IOException ex) {
            }
            return true;
        }
    }
}
