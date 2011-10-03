package org.oobd.ui.android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import android.app.Activity;

import java.util.Set;

/**
 * @author Andreas Budde, Peter Mayer Settings activity that allows users to
 *         configure the app (Bluetooth OBD connection, Lua script file
 *         selection and simulation mode.
 */
public class Settings extends Activity {

	public static final String KEY_LIST_SELECT_PAIRED_OBD2_DEVICE = "PREF_OOBD_BT_DEVICE";

	private Spinner mDeviceSpinner;
	private String BTDeviceName;
	SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mDeviceSpinner = (Spinner) findViewById(R.id.BTDeviceSpinner);
		mDeviceSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						BTDeviceName = ((FriendlyBTName) parent
								.getItemAtPosition(pos)).getDevice();
						if (BTDeviceName != null
								&& !BTDeviceName.equalsIgnoreCase("")) {
							preferences.edit().putString("BTDEVICE",
									BTDeviceName).commit();
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						preferences.edit().putString("BTDEVICE", "").commit();
					}
				});
		createList();
	}

	protected void onRestart(){
		super.onRestart();
		createList();		
	}
	
	
	private void createList() {
		preferences = this.getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		if (preferences != null) {
			BTDeviceName = preferences.getString("BTDEVICE",
					"00:12:6F:07:27:25");
		}

		ArrayAdapter<FriendlyBTName> adapter = new ArrayAdapter<FriendlyBTName>(
				this, android.R.layout.simple_spinner_item, getBTDevices());
		mDeviceSpinner.setAdapter(adapter);
		for (int i = 0; i < adapter.getCount(); i++) {
			if (BTDeviceName.equals(adapter.getItem(i).getDevice())) {
				mDeviceSpinner.setSelection(i);
				break;
			}
		}

	}

	private synchronized FriendlyBTName[] getBTDevices() {
		System.out.println("Starting Bluetooth Detection and Device Pairing");

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.w(this.getClass().getSimpleName(), "Bluetooth not supported.");
			FriendlyBTName[] BTDeviceSet = new FriendlyBTName[1];
			BTDeviceSet[0] = new FriendlyBTName("", "No Devices paired :-(");
			return BTDeviceSet;
		}
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		FriendlyBTName[] BTDeviceSet = new FriendlyBTName[pairedDevices.size()];
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
				BTDeviceSet[i] = new FriendlyBTName(device.getAddress(), device
						.getName());
				i++;
			}
		}
		return BTDeviceSet;

	}

}

class FriendlyBTName {
	String deviceName;
	String friendlyName;

	public FriendlyBTName(String device, String name) {
		deviceName = device;
		friendlyName = name;
	}

	public String getDevice() {
		return deviceName;
	}

	public String toString() {
		return "(" + deviceName + ") " + friendlyName;
	}
}
