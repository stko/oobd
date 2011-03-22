package org.oobd.ui.android.application;

import java.util.ArrayList;
import java.util.HashMap;

import org.oobd.base.Core;
import org.oobd.base.IFsystem;
import org.oobd.base.IFui;

import org.oobd.base.support.Onion;
import org.oobd.ui.android.DiagnoseItem;

import android.app.Application;
import android.util.Log;

/**
 * @author Andreas Budde, Peter Mayer
 * Base class to maintain global application state. This activity is Initialised before all others. Store here e.g. list of bluetooth devices,...
 */
public class OOBDApp extends Application implements IFsystem  {
	
	
	// Constants that are global for the Android App
	public static final String VISUALIZER_UPDATE = "OOBD Broadcast_UI_Update";
	public static final String UPDATE_LEVEL = "OOBD Update Level";
	public static final int REQUEST_ENABLE_BT = 10;
	
	public Core core;
	public IFui androidGui; 
	
	// make it singleton
	private static OOBDApp mInstance;
	
	public static OOBDApp getInstance() {
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		
		androidGui = new AndroidGui();
		new Core(androidGui, this);
		Log.v(this.getClass().getSimpleName(), "Core creation finalized");
	}

	public CharSequence[] getAvailableLuaScript() {
		// TODO read what scripts are available on the device (e.g. from harddisc?)
		return new CharSequence[] {"test.lua", "init.lua", "ford.lua" };
	}

	public CharSequence[] getAvailableBluetoothDevices() {
		// TODO find out what devices are bluetooth coupled
		return new CharSequence[] {"ODB2 device", "Laptop", "Nokia 8610", "HTC Hero" };
	}

	

	
	@Override
	public void registerOobdCore(Core core) {
		this.core = core;
		Log.v(this.getClass().getSimpleName(), "Core registered in IFsystem");
		
	}
	
	@Override
	public HashMap loadOobdClasses(String path, String classPrefix, Class<?> classType){
		HashMap<String, Class<?>> myInstances = new HashMap<String, Class<?>>();
	
		Class tempClass = null;
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
