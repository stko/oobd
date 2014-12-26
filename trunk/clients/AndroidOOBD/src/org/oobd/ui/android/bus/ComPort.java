package org.oobd.ui.android.bus;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.oobd.base.bus.OobdBus;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.port.PortInfo;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import org.oobd.base.support.Onion;
import org.oobd.ui.android.application.OOBDApp;

public class ComPort implements OOBDPort {
	InputStream inputStream;
	OutputStream outputStream;
	static ComPort myInstance = null;
	Activity callingActivity;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice obdDevice;
	BluetoothSocket serialPort;
	String BTAddress = null;
	Thread myThread;
	OobdBus msgReceiver;

	public static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

//	public InputStream getInputStream() {
//		if (serialPort != null) {
//			return inputStream;
//		} else {
//			return null;
//		}
//	}
//
//	public OutputStream getOutputStream() {
//		if (serialPort != null) {
//			return outputStream;
//		} else {
//			return null;
//		}
//	}

	public ComPort(Activity callingActivity, String BTAddress) {
		myInstance = this;
		this.BTAddress = BTAddress;
		this.callingActivity = callingActivity;
		// Looper.prepare();
	}

	public boolean connect(Onion options, OobdBus receiveListener) {
		msgReceiver = receiveListener;
		System.out.println("Starting Bluetooth Detection and Device Pairing");
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Log.w(this.getClass().getSimpleName(),
						"Bluetooth not supported.");
				return false;
			}
			if (!mBluetoothAdapter.isEnabled()) {
				Log.w(this.getClass().getSimpleName(),
						"Bluetooth switched off.");
				return false;
			}
		}

		obdDevice = mBluetoothAdapter.getRemoteDevice(BTAddress);

		if (obdDevice != null) { // Get a BluetoothSocket to connect
			// with the given BluetoothDevice
			try {
				Log.v(this.getClass().getSimpleName(),
						"Device " + obdDevice.getName());
				java.lang.reflect.Method m = obdDevice.getClass().getMethod(
						"createRfcommSocket", new Class[] { int.class });
				// "createInsecureRfcommSocket", new Class[] { int.class });
				serialPort = null;
				serialPort = (BluetoothSocket) m.invoke(obdDevice, 1);

				if (serialPort != null) {
					try {
						mBluetoothAdapter.cancelDiscovery();
						serialPort.connect();
						Log.d("OOBD:Bluetooth", "Bluetooth connected");
						inputStream = serialPort.getInputStream();
						outputStream = serialPort.getOutputStream();

						OOBDApp.getInstance().displayToast(
								"Bluetooth connected");

						myThread = new Thread() {

							@Override
							public void run() {
								byte[] buffer = new byte[1024]; // buffer store
																// for the
																// stream
								int bytes; // bytes returned from read()

								// Keep listening to the InputStream until an
								// exception occurs
								Log.d("OOBD:Bluetooth", "receiver task runs");
								while (true) {
									if (inputStream != null) {
										try {
											// Read from the InputStream
											bytes = inputStream.read(buffer);
											if (bytes > 0) {
												Log.v(this.getClass().getSimpleName(),
														"Debug: received something");
												msgReceiver
														.receiveString(new String(
																buffer));
											}
										} catch (IOException e) {
											break;
										}
									} else {
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}

						};
						myThread.start();
						return true;
					} catch (IOException ex) {
						Log.e(this.getClass().getSimpleName(),
								"Error: Could not connect to socket.", ex);
						OOBDApp.getInstance().displayToast(
								"Bluetooth NOT connected!");
					}
				} else {
					Log.e("OOBD:Bluetooth", "Bluetooth NOT connected!");
					OOBDApp.getInstance().displayToast(
							"Bluetooth NOT connected!");
					if (serialPort != null) {
						try {
							serialPort.close();
						} catch (IOException closeEx) {
						}
					}
					return false;
				}
				// do not yet connect. Connect before calling the
				// socket.
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

//	public boolean available() {
//		try {
//			return inputStream != null && inputStream.available() > 0;
//		} catch (IOException ex) {
//			// broken socket: Close it..
//			close();
//			return false;
//		}
//	}

    public String connectInfo(){
    	if (inputStream != null){
    		return "BT Connect to "+BTAddress;
    	}else{
    		return null;
    	}
    }

	
	public void close() {
		if (serialPort != null) {
			try {
				inputStream.close();
				inputStream = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				outputStream.close();
				outputStream = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				serialPort.close();
				serialPort = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public PortInfo[] getPorts() {
		System.out.println("Starting Bluetooth Detection and Device Pairing");

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.w(this.getClass().getSimpleName(), "Bluetooth not supported.");
			PortInfo[] BTDeviceSet = new PortInfo[1];
			BTDeviceSet[0] = new PortInfo("", "No Devices paired :-(");
			return BTDeviceSet;
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		PortInfo[] BTDeviceSet = new PortInfo[pairedDevices.size()];
		Log.v(this.getClass().getSimpleName(), "Anzahl paired devices: "
				+ pairedDevices.size());

		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			int i = 0;

			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				Log.d("OOBD:BluetoothIntiWorker", "Found Bluetooth Device: "
						+ device.getName() + "=" + device.getAddress());
				BTDeviceSet[i] = new PortInfo(device.getAddress(),
						device.getName());
				i++;
			}
		}
		return BTDeviceSet;
	}

	public void attachShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				System.out.println("Inside Add Shutdown Hook");
				close();
				System.out.println("Serial line closed");
			}
		});
		System.out.println("Shut Down Hook Attached.");

	}

	public synchronized void write(String s) {
		Log.d("OOBD:Comport", "Write something ");
		if (serialPort != null) {
			try {
				serialPort.getOutputStream().write(s.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.err.println("NO comhandle:" + s);

		}
	}

}
