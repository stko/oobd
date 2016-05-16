package org.oobd.ui.android.application;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import java.io.*;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;
import org.oobd.base.IFsystem;
import org.oobd.base.IFui;
import org.oobd.base.archive.*;
import org.oobd.base.port.ComPort_Kadaver;
import org.oobd.base.port.ComPort_Telnet;
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
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.content.Context;
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

	String webRootDir = "";
	String webLibraryDir = "";
	Preferences appProbs;
	String oobdMacAddress = "-";
	InetAddress oobdIPAddress = null;

	public static OOBDApp getInstance() {
		return mInstance;
	}

	@Override
	public String getOobdURL() {
		InetAddress ip;
		String hostname;
		ip = getSystemIP();
		// even when using the external address, ip.getHostName() returns just "localhost", which
		//makes the server unreachable; so on android we use the ip address as host. Not so nice, but it works.
		hostname = ip.getHostAddress();
		System.out.println("Your current Hostname : " + hostname);
		return "http://"
				+ hostname
				+ ":"
				+ ((Integer) Core.getSingleInstance().readDataPool(
						DP_HTTP_PORT, 8080)).toString();
	}

	public void openBrowser() {

		try {
			Intent myIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getOobdURL()));
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(myIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(
					this,
					"No application can handle this request."
							+ " Please install a webbrowser", Toast.LENGTH_LONG)
					.show();
			e.printStackTrace();
		}

	}

	@Override
	public String getMACAddress() {
		if (oobdMacAddress.equals("-")) {// not initialized? Then do it first
			getSystemIP();
		}
		return oobdMacAddress;
	}

	@Override
	public InetAddress getSystemIP() {
		if (oobdIPAddress != null) {
			return oobdIPAddress;
		}
		try {
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					oobdIPAddress = (InetAddress) ee.nextElement();
					System.out.println(oobdIPAddress.getHostAddress());
					if (oobdIPAddress.isSiteLocalAddress()) {
						System.out
								.println("Your current local side IP address : "
										+ oobdIPAddress);
						WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
						WifiInfo info = manager.getConnectionInfo();
						oobdMacAddress = info.getMacAddress();
						System.out.println("Your current MAC address : "
								+ oobdMacAddress);
						return oobdIPAddress;
					}
				}
			}
			oobdIPAddress = InetAddress.getLocalHost();
			System.out.println("Your current IP address : " + oobdIPAddress);
			return oobdIPAddress;

		} catch (UnknownHostException ex) {
			oobdMacAddress = "-";
			oobdIPAddress = null;
			Log.v(this.getClass().getSimpleName(), ex.getMessage());
		} catch (SocketException ex) {
			oobdMacAddress = "-";
			oobdIPAddress = null;
			Log.v(this.getClass().getSimpleName(), ex.getMessage());
		}
		return null;
	}

	public String generateUIFilePath(int pathID, String fileName) {
		System.err.println("Try to map file "+fileName);
		switch (pathID) {
		case FT_RAW:
		case FT_WEBPAGE:
		case FT_DATABASE:
			return fileName;
		case FT_KEY:
			return getFilesDir() + "/" + fileName;

		default:
			File myFile = new File(Environment.getExternalStorageDirectory()
					.getPath() + "/OOBD/" + fileName);
			if (myFile.exists()) {
				System.err.println("found as  "+myFile.getAbsolutePath());
				return myFile.getAbsolutePath();
			} else {
				System.err.println("try to find as  "+appProbs.get(
						OOBDConstants.PropName_ScriptDir, "") + "/" + fileName);
				myFile = new File(appProbs.get(
						OOBDConstants.PropName_ScriptDir, "") + "/" + fileName);
				if (myFile.exists()) {
					System.err.println("found as  "+myFile.getAbsolutePath());
					return myFile.getAbsolutePath();
				}
				System.err.println("give up, report as  "+fileName);
				return fileName;
			}

		}

	}

	public void displayToast(CharSequence text) {
		mToast.setText(text);
		mToast.show();
	}

	   /*
    replaces leading directory alias against their physical location
    */
   String mapDirectory(String[] mapDir, String path) {
       int i = 0;
       while (i < mapDir.length) {
           if (path.toLowerCase().startsWith("/" + mapDir[i].toLowerCase() + "/")) {
               path = path.substring(mapDir[i].length() + 2);
               if (mapDir[i].toLowerCase().equalsIgnoreCase("theme") && path.toLowerCase().startsWith("default/")) { //map the theme folder to  the actual theme
                   path = core.readDataPool(DP_WEBUI_ACTUAL_THEME, "default") + "/" + path.substring("default/".length());
               }
               return mapDir[i + 1] + path;
           }
           i += 2;
       }
       return null;
   }

	public InputStream generateResourceStream(int pathID, String resourceName)
			throws java.util.MissingResourceException {
		Log.v(this.getClass().getSimpleName(), "Try to load: " + resourceName
				+ " with path ID : " + pathID);
		InputStream resource = null;
	/*
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
		case OOBDConstants.FT_WEBPAGE:
			String newPath = mapDirectory("libs", resourceName);
			if (!newPath.equals("")) {
				// CAUTION: This directory mapping is only temporary, as Android
				// does not
				// handle any webapp subdirectories.
				// this is just a dirty hack
				// resourceName="/../libs/"+newPath;
				resourceName = "/libs/" + newPath;
			}
			// please notice: here's no case "break"!
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
*/
		
	       Archive scriptArchive = (Archive) core.readDataPool(DP_ACTIVE_ARCHIVE, null);
	        try {
	            switch (pathID) {
	                case OOBDConstants.FT_WEBPAGE:
	                    appProbs = core.getSystemIF().loadPreferences(FT_PROPS, OOBDConstants.AppPrefsFileName);
	                    webRootDir = (String) core.readDataPool(DP_SCRIPTDIR, "") + "/";
	                    webLibraryDir = (String) core.readDataPool(DP_WWW_LIB_DIR, "") + "/";
	                    // in case the resource name points to a "executable" scriptengine, the engine get started 
	                    // and the resourcename is corrected to the html start page to be used
	                    resourceName = core.startScriptEngineByURL(resourceName);
	                    scriptArchive = (Archive) core.readDataPool(DP_ACTIVE_ARCHIVE, null);
	                    String mapping = mapDirectory(new String[]{"libs", webLibraryDir + "libs/", "theme", webLibraryDir + "theme/"}, resourceName);
	                    if (mapping != null) { //its a mapped request
	                        core.writeDataPool(DP_LAST_OPENED_PATH, mapping);
	                        return new FileInputStream(generateUIFilePath(pathID, mapping));
	                    }
	                    // let's see, if it's a passthrough request;
	                    String[] parts = resourceName.split("/", 3); //remember that resourceName starts with /, so the first split part should be empty
	                    if (parts.length > 2) {
	                        ArrayList<Archive> files = (ArrayList<Archive>) core.readDataPool(DP_LIST_OF_SCRIPTS, null);
	                        if (files != null) {
	                            for (Archive file : files) {
	                                if (parts[1].equals(file.getFileName())) {
	                                    core.writeDataPool(DP_LAST_OPENED_PATH, parts[2]);
	                                    return file.getInputStream(parts[2]);
	                                }
	                            }
	                        }
	                    }

	                    if (scriptArchive != null) { // if everything else fails, try to load the file out of the package
	                        core.writeDataPool(DP_LAST_OPENED_PATH, resourceName);
	                        return scriptArchive.getInputStream(resourceName);
	                    }

	                    break;
	                case OOBDConstants.FT_PROPS:
	                case OOBDConstants.FT_RAW:
	                    resource = new FileInputStream(generateUIFilePath(pathID,
	                            resourceName));
	                    Log.v(this.getClass().getSimpleName(),"File " + resourceName
	                            + " loaded");
	                    break;

	                case OOBDConstants.FT_DATABASE:
	                    if (scriptArchive != null) {
	                        resource = scriptArchive.getInputStream(resourceName);
	                    }
	                    break;
	                case OOBDConstants.FT_SCRIPT:
	                    appProbs = core.getSystemIF().loadPreferences(FT_PROPS,
	                            OOBDConstants.AppPrefsFileName);
	                    // save actual script directory to buffer it for later as webroot directory
//	           resource = scriptArchive.getInputStream(scriptArchive.getProperty(OOBDConstants.MANIFEST_SCRIPTNAME, OOBDConstants.MANIFEST_SCRIPTNAME_DEFAULT));
	                    resource = scriptArchive.getInputStream(scriptArchive.getProperty(OOBDConstants.MANIFEST_SCRIPTNAME, resourceName));
	                    Log.v(this.getClass().getSimpleName(), "File " + resourceName
	                            + " loaded");
	                    break;

	                case OOBDConstants.FT_KEY:
	                    resource = new FileInputStream(System.getProperty("user.home") + "/" + resourceName);
	                    Log.v(this.getClass().getSimpleName(),"Key File "
	                            + resourceName + " loaded");
	                    break;

	                default:
	                    throw new java.util.MissingResourceException("Resource not known",
	                            "OOBDApp", resourceName);

	            }

	        } catch (Exception e) {
	        	Log.v(this.getClass().getSimpleName(), "generateResourceStream: File " + resourceName + " not loaded");
	        }
	        return resource;

		
		
		
		
		
		
		
	}

	public void onCreate() {
		super.onCreate();
		mInstance = this;
		mToast = Toast
				.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
		// Core thisCore = new Core(MainActivity.getMyMainActivity(), this,
		// "Core");
		// Log.v(this.getClass().getSimpleName(), "Core creation finalized"
		// + thisCore.toString());
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

		if (typ != null) {
			String connectURL = typ.getOnionBase64String("connecturl");
			String[] parts = connectURL.split("://");
			if (parts.length != 2) {
				return null;
			}
			String protocol = parts[0];
			String host = parts[1];

			String proxyHost = preferences.getString(
					OOBDConstants.PropName_ProxyHost, null);
			int proxyPort = preferences.getInt(
					OOBDConstants.PropName_ProxyPort, 0);
			if (protocol.toLowerCase().startsWith("ws")) {
				try {
					Proxy thisProxy = null;
					if (proxyHost != null && proxyPort != 0) {
						thisProxy = new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(proxyHost, proxyPort));

					}
					myComPort = new ComPort_Kadaver(new URI(connectURL),
							thisProxy, proxyHost, proxyPort);
					return myComPort;
				} catch (URISyntaxException ex) {
					Log.v(this.getClass().getSimpleName(),
							"could not open Websocket Interface", ex);
					return null;

				}
			} else if ("bt".equalsIgnoreCase(protocol)) {
				myComPort = new ComPort(Diagnose.getInstance(), host);
				return myComPort;

			} else if ("telnet".equalsIgnoreCase(protocol)) {
				Proxy thisProxy = null;
				if (proxyHost != null && proxyPort != 0) {
					thisProxy = new Proxy(Proxy.Type.HTTP,
							new InetSocketAddress(proxyHost, proxyPort));
				}
				myComPort = new ComPort_Telnet(connectURL);
				return myComPort;

			} else
				return null;
		} else {
			// todo Android always needs a comport object to have a list of the
			// possible devices for the settings screen
			// return new ComPort(Diagnose.getInstance(), BTDeviceName);
			return null;
		}
	}

	public String connectInfo() {
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
		Log.w(this.getClass().getSimpleName(),
				"Procedure doFileSelector called, but senseless in android! ");

		return null;
	}

	public Preferences loadPreferences(int pathID, String filename) {
		Preferences myPrefs = null;

		try {
			Preferences prefsRoot;

			prefsRoot = Preferences.userNodeForPackage(this.getClass());
			// prefsRoot.sync();
			myPrefs = prefsRoot.node("com.oobd.preference." + filename);
			/*
			 * if (myPrefs.keys().length == 0 && OOBDConstants.CorePrefsFileName
			 * .equalsIgnoreCase(filename)) { // no entries yet // generate
			 * system specific settings myPrefs.put("EngineClassPath",
			 * "org.oobd.base.scriptengine.ScriptengineLua");
			 * myPrefs.put("ProtocolClassPath",
			 * "org.oobd.base.protocol.ProtocolUDS");
			 * myPrefs.put("ConnectorClassPath",
			 * "org.oobd.base.connector.ConnectorLocal");
			 * myPrefs.put("BusClassPath", "org.oobd.base.bus.BusCom");
			 * myPrefs.put("DatabaseClassPath", "org.oobd.base.db.AVLLookup");
			 * myPrefs.put("UIHandlerClassPath",
			 * "org.oobd.ui.uihandler.UIHandler"); myPrefs.flush(); }
			 */
			return myPrefs;
		} catch (Exception e) {
			Log.v(this.getClass().getSimpleName(),
					"could not load property id " + filename);
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
		Hashtable<String, Class> connectClasses = new Hashtable<String, Class>();
		connectClasses.put(OOBDConstants.PropName_ConnectTypeBT, ComPort.class);
		connectClasses.put(OOBDConstants.PropName_ConnectTypeRemoteConnect,
				ComPort_Kadaver.class);
		connectClasses.put(OOBDConstants.PropName_ConnectTypeRemoteDiscovery,
				ComPort_Telnet.class);
		connectClasses.put(OOBDConstants.PropName_ConnectTypeTelnet,
				ComPort_Telnet.class);

		return connectClasses;
	}

	public DatagramSocket getUDPBroadcastSocket() {
		try {
			DatagramSocket socket = null;
			// WifiManager wifi = (WifiManager) getApplicationContext()
			// .getSystemService(Context.WIFI_SERVICE);
			// if (wifi != null) {
			// DhcpInfo dhcp = wifi.getDhcpInfo();
			// if (dhcp != null) {
			// // int broadcast = (dhcp.ipAddress & dhcp.netmask) |
			// // ~dhcp.netmask;
			// int broadcast = dhcp.ipAddress;
			// byte[] quads = new byte[4];
			// for (int k = 0; k < 4; k++)
			// quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
			// socket = new DatagramSocket(UDP_PORT,
			// InetAddress.getByAddress(quads));
			// }
			// }
			if (socket == null) {
				socket = new DatagramSocket(UDP_PORT);
			}
			socket.setBroadcast(true);
			return socket;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}

}
