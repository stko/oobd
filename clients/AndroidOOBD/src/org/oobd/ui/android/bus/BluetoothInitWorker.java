package org.oobd.ui.android.bus;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.oobd.ui.android.application.OOBDApp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;


public class BluetoothInitWorker implements Runnable {

	public static BluetoothInitWorker myInstance = null;
	
	public Activity callingActivity;
	public BluetoothAdapter mBluetoothAdapter;
	public BluetoothDevice obdDevice;
	public BluetoothSocket obdDeviceSocket;
	// TODO implement different uuids
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public boolean working = true;  //thread semaphore
	
	public BluetoothInitWorker () {
		myInstance = this;
	}
	
	public BluetoothInitWorker (Activity callingActivity) {
		myInstance = this;
		this.callingActivity = callingActivity;
	}
	
	public void run() {
		working = true;
		System.out.println("Thread BluetoothInitWorker started");
		Looper.prepare();
		//initializeBluetoothSocket();
		working = false;
		System.out.println("Thread BluetoothInitWorker finished");
	}
	
	public synchronized BluetoothSocket initializeBluetoothSocket(String BTAddress) {
    	System.out.println("Starting Bluetooth Detection and Device Pairing");
    	if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
			    Log.w(this.getClass().getSimpleName(), "Bluetooth not supported.");
			}
			else {
				if (!mBluetoothAdapter.isEnabled()) {
				    // TODO Fenster einblenden, um Bluetooth zu starten
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    callingActivity.startActivityForResult(enableBtIntent, OOBDApp.REQUEST_ENABLE_BT);
					Log.w(this.getClass().getSimpleName(), "Bluetooth not enabled.");
				}
			}
		}
    	
    	if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			Log.v(this.getClass().getSimpleName(), "Anzahl paired devices: " + pairedDevices.size());
			
			// If there are paired devices
			if (pairedDevices.size() > 0) {
			    // Loop through paired devices
			    for (BluetoothDevice device : pairedDevices) {
			        // Add the name and address to an array adapter to show in a ListView
			        Log.d("OOBD:BluetoothIntiWorker","Found Bluetooth Device: " + device.getName()+"="+device.getAddress()+" search for "+BTAddress);
			        // TODO delete following line once device selection is implemented
			        if ( BTAddress.equalsIgnoreCase(device.getAddress())) {
			        		obdDevice = device;
			        }
			    }
			    Log.v(this.getClass().getSimpleName(), "MY_UUID: " + MY_UUID);
				
				if (obdDevice!=null){ // Get a BluetoothSocket to connect with the given BluetoothDevice
		        try {
		            // MY_UUID is the app's UUID string, also used by the server code
		        	Log.v(this.getClass().getSimpleName(), "Device " + obdDevice.getName());
		            obdDeviceSocket = obdDevice.createRfcommSocketToServiceRecord(MY_UUID);
		            Log.v(this.getClass().getSimpleName(), "Device Socket for " + obdDevice.getName() + "successfully initiated");
		            // do not yet connect. Connect before calling the socket. 
		        } catch (IOException e) {
		        	e.printStackTrace();
		        	if (obdDeviceSocket != null) {
		        		try { obdDeviceSocket.close();} catch (IOException closeEx) {};
		        	}
		        }
				}
			}
			else
				System.out.println("No Paired Devices Found");
		}	
    	notify();
		return obdDeviceSocket;
    }

	public BluetoothSocket getObdDeviceSocket() {
		int i = 0;
		while (working && (i<100) ) {
			System.out.println("BluetoothInitWorker.getObdDeviceSocket() is waiting: " + i);
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				Log.e(this.getClass().getSimpleName(), "Thread Exception. Socket could not be created.");
				return null;
			}
			i++;
		}
		return obdDeviceSocket;
	}

	public static BluetoothInitWorker getMyInstance() {
		
		return myInstance;
	}

	public Activity getCallingActivity() {
		return callingActivity;
	}

	public void setCallingActivity(Activity callingActivity) {
		this.callingActivity = callingActivity;
	}



	
	
}
