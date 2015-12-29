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
import org.oobd.base.Base64Coder;

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;

/**
 *
 * @author steffen
 */
public class FileHandlerPlain implements Archive {

    String myFileDirectory;
    String myFilePath;
    String myFileName;
    Core core;

    public FileHandlerPlain(Core c) {
        core = c;
    }

    public InputStream getInputStream(String innerPath) {
        if (myFilePath != null) {
            try {
                return new FileInputStream(myFileDirectory + "/" + innerPath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileHandlerPlain.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return null;
    }

    public void closeInputStream(InputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException ex) {
                Logger.getLogger(FileHandlerPlain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean bind(String filePath) {
        File file = new File(filePath);
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
        if(OOBDConstants.MANIFEST_SCRIPTNAME.equalsIgnoreCase(property)){
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
}
