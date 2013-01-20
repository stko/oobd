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

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;
import org.oobd.crypt.gpg.*;

/**
 * 
 * @author steffen
 */
public class FileHandlerPGP implements Archive {

	String myFilePath;
	String myFileName;
	Core core;

	public FileHandlerPGP(Core c){
		core=c;
	}
	
	
	public InputStream getInputStream(String innerPath) {
		if (myFilePath != null) {
			try {

				return GroupDecoder
						.decryptGroup(
								new FileInputStream(myFilePath),
								core.getSystemIF().generateResourceStream(OOBDConstants.FT_KEY,OOBDConstants.PGP_USER_KEYFILE_NAME),
								new FileInputStream(core.getSystemIF().generateUIFilePath(OOBDConstants.FT_KEY,OOBDConstants.PGP_GROUP_KEYFILE_NAME)),
								core.getSystemIF().getAppPassPhrase(),core.getSystemIF().getUserPassPhrase().toCharArray());

			} catch (Exception ex) {
				Logger.getLogger(FileHandlerPGP.class.getName()).log(
						Level.SEVERE, null, ex);
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
				Logger.getLogger(FileHandlerPGP.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}
	}

	public boolean bind(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			myFilePath = filePath;
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
		return defaultValue;
	}

	@Override
	public String toString() {
		return myFileName;
	}

	public String getFilePath() {
		return myFilePath;
	}
}
