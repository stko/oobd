package org.oobd.ui.android.application;

import java.util.ArrayList;
import java.util.List;  
import java.util.HashMap;
import java.util.Set;
import java.io.*;

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;
import org.oobd.base.IFsystem;
import org.oobd.base.IFui;

import org.oobd.base.support.Onion;
import org.oobd.ui.android.Diagnose;
import org.oobd.ui.android.DiagnoseItem;
import org.oobd.ui.android.MainActivity;
import org.oobd.ui.android.R;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.*;
import android.content.Intent;
import 	android.widget.ArrayAdapter;

/**
 * @author Andreas Budde, Peter Mayer
 * Base class to maintain global application state. This activity is Initialised before all others. Store here e.g. list of bluetooth devices,...
 */
public class OOBDApp extends Application implements IFsystem {

    // Constants that are global for the Android App
    public static final String VISUALIZER_UPDATE = "OOBD Broadcast_UI_Update";
    public static final String UPDATE_LEVEL = "OOBD Update Level";
    public static final int REQUEST_ENABLE_BT = 10;
    public Core core;
    public IFui androidGui;
    // make it singleton
    private static OOBDApp mInstance;
    private Toast mToast;


    public static OOBDApp getInstance() {
        return mInstance;
    }

        public String generateUIFilePath(int pathID, String fileName) {
        switch (pathID) {
            case OOBDConstants.FT_PROPS:
                return "/"+fileName;

            default:
                return null;
        }
    }

        public void displayToast(CharSequence text) {
            mToast.setText(text);
            mToast.show();
        }


    public InputStream generateResourceStream(int pathID, String resourceName) throws java.util.MissingResourceException {
    	Log.v(this.getClass().getSimpleName(), "Try to load: " + resourceName + " with path ID : " + pathID);
    	InputStream resource = null;
        if (pathID == OOBDConstants.FT_PROPS ) {  // Achtung: Hier wird der ResourceName nicht weiter beachtet, weil nur hardcoded der oobdcore verwendet wird. Ist das so richtig
 /*       		if (resourceName.contains("oobdcore"))
        			resource = OOBDApp.getInstance().getApplicationContext().getResources().openRawResource(R.raw.oobdcore);
        		else if (resourceName.contains("enginelua"))
        			resource = OOBDApp.getInstance().getApplicationContext().getResources().openRawResource(R.raw.enginelua);
        		else if (resourceName.contains("buscom"))
        			resource = OOBDApp.getInstance().getApplicationContext().getResources().openRawResource(R.raw.buscom);
        		
                Log.v(this.getClass().getSimpleName(), "File " + resourceName + " could be loaded from /res/raw");
*/
    		try {
				resource = new FileInputStream("/sdcard/oobd/" + resourceName);
   		Log.v(this.getClass().getSimpleName(), "File " + resourceName + " could be loaded from /sdcard/oobd");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				Log.v(this.getClass().getSimpleName(), "File " + resourceName + " could not loaded from /sdcard/oobd",e);
			}
         	return resource;
        } else if (pathID == OOBDConstants.FT_SCRIPT) {
        	try {
        		//InputStream resource = new FileInputStream("/sdcard/stdlib.lbc");
        		resource = new FileInputStream("/sdcard/oobd/" + resourceName);
        		Log.v(this.getClass().getSimpleName(), "File " + resourceName + " could be loaded from /sdcard/oobd");
        		return resource;
        	} catch (IOException e) {
        		Log.e(this.getClass().getSimpleName(), "Script not found on SDCard or SDCard not available. Try to read from /assets");
            	try {
            		resource = OOBDApp.getInstance().getAssets().open(resourceName);
            		Log.v(this.getClass().getSimpleName(), "File " + resourceName + ": default file loaded from /assets instead of sdcard.");
            		
            		// inform user in Dialog Box:
            		
                    MainActivity.getMyMainActivity().runOnUiThread(new Runnable() {
                        public void run() {
                        	Toast.makeText(MainActivity.getMyMainActivity().getApplicationContext(), "Skript on SDCard not found. Loading default Script.", Toast.LENGTH_LONG).show();
                        }
                    });
                    
            		return resource;
            	} catch (IOException ex) {
            		Log.e(this.getClass().getSimpleName(), "Script also not found in local directory /assets");
                    throw new java.util.MissingResourceException("Resource not found", "OOBDApp", resourceName);
                }
        	}
	
        } else {
            throw new java.util.MissingResourceException("Resource not known", "OOBDApp", resourceName);
        }
    }

    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
        androidGui = new AndroidGui();
        new Core(androidGui, this,"Core");
        Log.v(this.getClass().getSimpleName(), "Core creation finalized");
    }

    public CharSequence[] getAvailableLuaScript() {
        // TODO read what scripts are available on the device (e.g. from harddisc?)
        return new CharSequence[]{"test.lua", "init.lua", "ford.lua"};
    }

    public ArrayList<String> getAvailableBluetoothDevices() {
        // TODO find out what devices are bluetooth coupled
    	ArrayList<String> mArrayAdapter = new ArrayList<String>();
        return mArrayAdapter;
 /*   	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
             	if (mBluetoothAdapter != null) {
   		if (!mBluetoothAdapter.isEnabled()) {
    		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    		}
   	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	// If there are paired devices
    	if (pairedDevices.size() > 0) {
    	    // Loop through paired devices
    	    for (BluetoothDevice device : pairedDevices) {
    	        // Add the name and address to an array adapter to show in a ListView
    	        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    	    }
    	}
    	}
        return mArrayAdapter;
*/     }

    public void registerOobdCore(Core core) {
        this.core = core;
        Log.v(this.getClass().getSimpleName(), "Core registered in IFsystem");

    }


    public HashMap <String, Class<?>> loadOobdClasses(String path, String classPrefix, Class<?> classType) {
        HashMap<String, Class<?>> myInstances = new HashMap<String, Class<?>>();

        Class<?> tempClass = null;
        try {
            tempClass = Class.forName(path);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        myInstances.put(tempClass.getSimpleName(), tempClass);

        return myInstances;
    }

    public Core getCore() {
        return core;
    }
}
