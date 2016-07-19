package org.oobd.ui.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import android.content.res.AssetManager;

public class AssetInstaller {
	AssetManager assetManager;
	String  target;
	String rev;
	BufferedReader inputFileReader;

	public AssetInstaller(AssetManager am,
			String targetPath, String revision) {
		assetManager = am;
		target = targetPath;
		rev = revision;
	}

	public boolean isInstallNeeded() {
		String amName = target + "/am.rev";
		File revFile = new File(amName);
		if (!revFile.exists()) {
			return true;
		}
		String line;
		try {
			InputStream fis = new FileInputStream(amName);
			InputStreamReader isr = new InputStreamReader(fis,
					Charset.forName("UTF-8"));
			inputFileReader = new BufferedReader(isr);
			line = inputFileReader.readLine();
			inputFileReader.close();
			isr.close();
			fis.close();
			return (line==null || line.trim().equalsIgnoreCase(rev) );
		} catch (IOException e) {
			return true;
		}
	}

	public int copyAll() {
		String line;
		try {
			InputStream fis = assetManager.open("am.rev");
			InputStreamReader isr = new InputStreamReader(fis,
					Charset.forName("UTF-8"));
			this.inputFileReader = new BufferedReader(isr);

			while ((line = inputFileReader.readLine()) != null) {
				copyFile(line.trim());
			}
			copyFile("./am.rev");
			return 0;
		} catch (IOException e) {
			return 1;
		}
	}

	private boolean copyFile(String sourceFileName) {

		InputStream in = null;
		OutputStream out = null;
		try {
			String[] parts = sourceFileName.split("/", 2);
			if (parts.length != 2) {
				return false;
			}
			String fileType = parts[0];
			String newFileName = target + "/" +  parts[1];
			File tmp = new File(newFileName);
			if ("opt".equalsIgnoreCase(fileType) && tmp.exists()) { // the file
																	// is
																	// optional
																	// and exist
																	// already,
																	// so do not
																	// overwrite
																	// it
				return true;
			}
			tmp.getParentFile().mkdirs();
			if (".".equals(fileType)){
				sourceFileName=parts[1];
			}
			in = assetManager.open(sourceFileName);
			out = new FileOutputStream(newFileName);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
