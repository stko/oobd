/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.Core;
import static org.oobd.base.OOBDConstants.*;
import org.oobd.base.support.Onion;

/**
 *
 * @author steffen
 */
public class FileHandlerEpa implements Archive {

    String myFileDirectory;
    String myFilePath;
    String myFileName;
    Core core;
    Onion manifest;
    ZipFile outerZipFile = null;
    boolean isDirectory = false;

    public FileHandlerEpa(Core c) {
        core = c;
    }

    public InputStream getInputStream(String innerPath) {
        if (myFilePath != null) {
            try {
                if (isDirectory) {
                    return new FileInputStream(myFilePath + java.io.File.separator + innerPath);
                } else {
                    ZipEntry zipFile = outerZipFile.getEntry(innerPath);
                    return outerZipFile.getInputStream(zipFile);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileHandlerEpa.class.getName()).log(Level.SEVERE, "Could not open EPA input file:" + myFilePath + java.io.File.separator + innerPath, ex);
                return null;
            } catch (IOException ex) {
                Logger.getLogger(FileHandlerEpa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException ex) {
                Logger.getLogger(FileHandlerEpa.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean bind(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            myFileDirectory = file.getParent();
            myFilePath = file.getAbsolutePath();
            myFileName = file.getName();
            try {
                if (!file.isDirectory()) {
                    outerZipFile = new ZipFile(file.getAbsolutePath());
                    isDirectory = false;
                } else {
                    outerZipFile = null;
                    isDirectory = true;
                }
                String manifestString = new String(org.apache.commons.io.IOUtils.toByteArray(getInputStream(MANIFEST_NAME)));
                manifest = new Onion(manifestString);

            } catch (IOException ex) {
                Logger.getLogger(FileHandlerEpa.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(FileHandlerEpa.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        } else {
            return false;
        }
    }

    public void unBind() {
        myFilePath = null;
    }

    public String getProperty(String property, String defaultValue) {
        if (manifest == null) {
            return defaultValue;
        } else {
            String res = manifest.getOnionString(property);
            return res == null ? defaultValue : res;
        }

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
}
