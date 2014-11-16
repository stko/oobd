package org.oobd.ui.android.application;

import java.util.HashMap;
import java.util.prefs.Preferences;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;
import org.oobd.base.IFsystem;
import org.oobd.base.IFui;
import org.oobd.base.archive.*;

import org.oobd.base.support.Onion;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.crypt.AES.EncodeDecodeAES;
import org.oobd.crypt.AES.PassPhraseProvider;
import org.oobd.ui.android.Diagnose;
import org.oobd.ui.android.bus.ComPort;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;


/**
 * @author Andreas Budde, Peter Mayer Base class to maintain global application
 *         state. This activity is Initialised before all others. Store here
 *         e.g. list of bluetooth devices,...
 */
public class OOBDApp extends Application implements IFsystem, OOBDConstants {

	// Constants that are global for the Android App
	public static final String VISUALIZER_UPDATE = "OOBD Broadcast_UI_Update";
	public static final String UPDATE_LEVEL = "OOBD Update Level";
	public static final int REQUEST_ENABLE_BT = 10;
	public Core core;
	public IFui androidGui;
	// make it singleton
	private static OOBDApp mInstance;
	private Toast mToast;
	private ComPort myComPort;
	private String userPassPhrase = "";
	
	public static OOBDApp getInstance() {
		return mInstance;
	}

	public String generateUIFilePath(int pathID, String fileName) {
		switch (pathID) {

		case FT_DATABASE:
			return fileName;
		case FT_KEY:
			return getFilesDir() + "/" + fileName;

		default:
			return Environment.getExternalStorageDirectory().getPath()
					+ "/OOBD/" + fileName;
			// return null;
		}
	}

	public void displayToast(CharSequence text) {
		mToast.setText(text);
		mToast.show();
	}

	public InputStream generateResourceStream(int pathID, String resourceName)
			throws java.util.MissingResourceException {
		Log.v(this.getClass().getSimpleName(), "Try to load: " + resourceName
				+ " with path ID : " + pathID);
		InputStream resource = null;
		switch (pathID) {
		case OOBDConstants.FT_PROPS:
			try {
				resource = new FileInputStream(generateUIFilePath(pathID,
						resourceName));
				Log.v(this.getClass().getSimpleName(), "File " + resourceName
						+ " loaded from /sdcard/oobd");
			} catch (FileNotFoundException e) {
				Log.v(this.getClass().getSimpleName(), "File " + resourceName
						+ " could not loaded from /sdcard/oobd", e);
			}
			return resource;
		case OOBDConstants.FT_DATABASE:
		case OOBDConstants.FT_SCRIPT:
			try {
				String filePath = generateUIFilePath(pathID, resourceName);
				Archive achive = Factory.getArchive(filePath);
				achive.bind(filePath);
				resource = achive.getInputStream("");
				Log.v(this.getClass().getSimpleName(), "File " + resourceName
						+ " loaded from /sdcard/oobd");
			} catch (Exception e) {
				Log.v(this.getClass().getSimpleName(), "File " + resourceName
						+ " could not loaded from /sdcard/oobd", e);
			}
			return resource;
		case OOBDConstants.FT_KEY:
			try {
				resource = openFileInput(resourceName);
				Log.v(this.getClass().getSimpleName(), "Key File "
						+ resourceName + " loaded");
			} catch (Exception e) {
				Log.v(this.getClass().getSimpleName(), "Key File "
						+ resourceName + " could not loaded", e);
			}
			return resource;

		default:
			throw new java.util.MissingResourceException("Resource not known",
					"OOBDApp", resourceName);
		}

	}

	public void onCreate() {
		super.onCreate();
		mInstance = this;
		mToast = Toast
				.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
		androidGui = new AndroidGui();
		Core thisCore = new Core(androidGui, this, "Core");
		Log.v(this.getClass().getSimpleName(), "Core creation finalized"
				+ thisCore.toString());
	}

	
	public CharSequence[] getAvailableLuaScript() {

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".lbc")
						|| name.toLowerCase().endsWith(".lbc.pgp");
			}
		};
		File dir = new File(generateUIFilePath(OOBDConstants.FT_SCRIPT, ""));
		String[] children = dir.list(filter);
		if (children == null) {
			return new String[0];
		} else {
			return children;
		}
	}

	public void registerOobdCore(Core core) {
		this.core = core;
		Log.v(this.getClass().getSimpleName(), "Core registered in IFsystem");

	}

	public HashMap<String, Class<?>> loadOobdClasses(String path,
			String classPrefix, Class<?> classType) {
		HashMap<String, Class<?>> myInstances = new HashMap<String, Class<?>>();

		Class<?> tempClass = null;
		try {
			tempClass = Class.forName(path);
			myInstances.put(tempClass.getSimpleName(), tempClass);
		} catch (ClassNotFoundException e) {
			System.out.println("Cant load dynamic class " + path);
			e.printStackTrace();
		}

		return myInstances;
	}

	public Core getCore() {
		return core;
	}

	public Object supplyHardwareHandle(Onion typ) {
		SharedPreferences preferences;
		String BTDeviceName = "00:12:6F:07:27:25";
		preferences = this.getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		if (preferences != null) {
			BTDeviceName = preferences.getString("BTDEVICE",
					"00:12:6F:07:27:25");
		}
		myComPort = new ComPort(Diagnose.getInstance(), BTDeviceName);
		return myComPort;
	}

	public boolean isConnected() {
		if (myComPort == null) {
			return false;
		} else {
			return (myComPort.getInputStream() != null);
		}
	}

	public void closeHardwareHandle() {
		if (myComPort != null) {
			myComPort.close();
			myComPort = null;
		}
	}

	public char[] getAppPassPhrase() {
		return PassPhraseProvider.getPassPhrase();
	}

	public String getUserPassPhrase() {
		if (userPassPhrase.equals("")) {
			return "";
		} else {
			try {
				return new String(EncodeDecodeAES.decrypt(new String(
						getAppPassPhrase()), userPassPhrase));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		}
	}

	public void setUserPassPhrase(String upp) {
		try {
			userPassPhrase = EncodeDecodeAES.encrypt(new String(
					getAppPassPhrase()), upp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			userPassPhrase = "";
		}

	}

	public Preferences loadPreferences(int pathID, String filename) {
		Preferences myPrefs = null;

		try {
			Preferences prefsRoot;

			prefsRoot = Preferences.userNodeForPackage(this.getClass());
			// prefsRoot.sync();
			myPrefs = prefsRoot.node("com.oobd.preference." + filename);
			if (myPrefs.keys().length == 0
					&& OOBDConstants.CorePrefsFileName
							.equalsIgnoreCase(filename)) { // no entries yet
				// generate system specific settings
				myPrefs.put("EngineClassPath",
						"org.oobd.base.scriptengine.ScriptengineLua");
				myPrefs.put("ProtocolClassPath",
						"org.oobd.base.protocol.ProtocolUDS");
				myPrefs.put("ConnectorClassPath",
						"org.oobd.base.connector.ConnectorLocal");
				myPrefs.put("BusClassPath", "org.oobd.base.bus.BusCom");
				myPrefs.put("DatabaseClassPath", "org.oobd.base.db.AVLLookup");
				myPrefs.put("UIHandlerClassPath",
						"org.oobd.ui.uihandler.UIHandler");
				myPrefs.flush();
			}
			return myPrefs;
		} catch (Exception e) {
			Log.v(this.getClass().getSimpleName(),
					"could not load property id " + filename, e);
		}
		return myPrefs;
	}

	public boolean savePreferences(int pathID, String filename,
			Preferences properties) {
		try {
			properties.flush();
			return true;
		} catch (Exception e) {
			Log.v(this.getClass().getSimpleName(),
					"could not save property id " + filename, e);
			return false;
		}
	}
	
	public void createEngineTempInputFile(OobdScriptengine eng) {
        File f = null;

        try {
            //do we have to delete a previous first?

            eng.removeTempInputFile();
            // creates temporary file
            f = File.createTempFile("oobd", null, null);

            // deletes file when the virtual machine terminate
            f.deleteOnExit();

            eng.setTempInputFile(f);

        } catch (Exception e) {
            // if any error occurs
			Log.v(this.getClass().getSimpleName(),
					"could not create temp file!", e);
         }

    }

    public String doFileSelector(String path, final String extension, String message, Boolean save) {
 
    	
    	
    	
		Intent intent = new Intent("org.openintents.action.PICK_FILE");
		intent.putExtra(
				Intent.EXTRA_TITLE,
				"Save as text");
		startActivityForResult(intent, 1);

    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	JFileChooser chooser = new JFileChooser();
        File oldDir = null;
        String oldDirName = path;
        if (oldDirName != null) {
            oldDir = new File(oldDirName);
        }
        chooser.setCurrentDirectory(oldDir);
        chooser.setSelectedFile(oldDir);
        chooser.setMultiSelectionEnabled(false);
        if (save){
        chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        }else{
            chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        }
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (extension != null) {
                    return f.getName().toLowerCase().endsWith(extension);
                } else {
                    return true;
                }
            }

            public String getDescription() {
                return extension + " Ext";
            }
        });
        if ((save && chooser.showSaveDialog(null)== JFileChooser.APPROVE_OPTION) || (!save &&chooser.showOpenDialog(null)== JFileChooser.APPROVE_OPTION)
                ) {
            try {
                return chooser.getSelectedFile().getAbsolutePath().toString();

            } catch (Exception ex) {
                Logger.getLogger(SwingSystem.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return null;
        }
    }
    
    public boolean FileselectorCallback(String selectedFilName){
		final File file = new File(filePath);
		if (file.exists()) {
			AlertDialog alertDialog = new AlertDialog.Builder(
					myOutputActivityInstance).create();
			alertDialog.setTitle("File already exist!");
			alertDialog.setMessage("OK to overwrite?");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							saveOutput(file);
						}
					});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", (DialogInterface.OnClickListener)null);
			alertDialog.show();
		} else {
			saveOutput(file);
		}
  	
    	
    }
    
	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		if (resultCode == Activity.RESULT_OK) {

			URI filePath = null;
			try {
				filePath = new URI(data.getDataString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (resultCode == Activity.RESULT_CANCELED) {

		}

	}

    
}
