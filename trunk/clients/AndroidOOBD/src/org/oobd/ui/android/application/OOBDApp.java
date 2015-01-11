package org.oobd.ui.android.application;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;
import org.oobd.base.IFsystem;
import org.oobd.base.IFui;
import org.oobd.base.archive.*;

import org.oobd.base.port.ComPort_Kadaver;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.support.Onion;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.crypt.AES.EncodeDecodeAES;
import org.oobd.crypt.AES.PassPhraseProvider;
import org.oobd.ui.android.Diagnose;
import org.oobd.ui.android.MainActivity;
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
	// make it singleton
	private static OOBDApp mInstance;
	private Toast mToast;
	private OOBDPort myComPort;
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
						+ " could not loaded from /sdcard/oobd");
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
//		Core thisCore = new Core(MainActivity.getMyMainActivity(), this, "Core");
//		Log.v(this.getClass().getSimpleName(), "Core creation finalized"
//				+ thisCore.toString());
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
		preferences = this.getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		String BTDeviceName = "00:12:6F:07:27:25";

		if (typ != null) {
			String connectURL = typ.getOnionBase64String("connecturl");
			String proxyHost = preferences.getString(
					OOBDConstants.PropName_KadaverProxyHost, null);
			int proxyPort = preferences.getInt(
					OOBDConstants.PropName_KadaverProxyPort, 0);
			if (connectURL.toLowerCase().startsWith("ws")) {
				try {
					Proxy thisProxy = null;
					if (proxyHost != null && proxyPort != 0) {
						thisProxy = new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(proxyHost, proxyPort));

					}
					myComPort= new ComPort_Kadaver(new URI(connectURL), thisProxy);
					return myComPort;
				} catch (URISyntaxException ex) {
					Log.v(this.getClass().getSimpleName(),
							"could not open Websocket Interface", ex);
					return null;

				}
			} else if (connectURL.equalsIgnoreCase("serial")) {
				if (preferences != null) {
					BTDeviceName = preferences.getString("BTDEVICE",
							"00:12:6F:07:27:25");
				}
				myComPort = new ComPort(Diagnose.getInstance(), BTDeviceName);
				return myComPort;
			}
			return null;

		} else {
			// todo Android always needs a comport object to have a list of the
			// possible devices for the settings screen
			return new ComPort(Diagnose.getInstance(), BTDeviceName);
		}
	}


	   public String connectInfo(){
			if (myComPort == null) {
				return null;
			} else {
				return (myComPort.connectInfo());
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

	public void createEngineTempInputFile(OobdScriptengine eng) {
		File f = null;

		try {
			// do we have to delete a previous first?

			eng.removeTempInputFile();
			// creates temporary file
			f = File.createTempFile("oobd", null, null);

			// deletes file when the virtual machine terminate
			f.deleteOnExit();

			eng.setTempInputFile(f);

		} catch (Exception e) {
			// if any error occurs
			Log.v(this.getClass().getSimpleName(),
					"could not create temp file! ", e);
		}

	}

	public String doFileSelector(String path, final String extension,
			String message, Boolean save) {
		File oldDir = null;
		String oldDirName = path;
		if (oldDirName != null) {
			oldDir = new File(oldDirName);
		}

		return null;
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

	public Hashtable<String, Class> getConnectorList() {
		Hashtable<String, Class> connectClasses
	     = new Hashtable<String, Class>();
		connectClasses.put(OOBDConstants.PropName_ConnectTypeBT,ComPort.class);
		connectClasses.put( OOBDConstants.PropName_ConnectTypeRemoteConnect,ComPort_Kadaver.class);
		//connectClasses.put(OOBDConstants.PropName_ConnectTypeRemoteDiscovery,);
	   
		return connectClasses;
	}

}
