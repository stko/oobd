package org.oobd.ui.android;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.port.PortInfo;
import org.oobd.ui.android.application.OOBDApp;

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
	private OOBDPort portListGenerator;
	SharedPreferences preferences;


//	protected void onCreate(Bundle savedInstanceState,OOBDPort comPort) {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		preferences = this.getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		portListGenerator=((OOBDPort)OOBDApp.getInstance().getCore().supplyHardwareHandle(null));
		mDeviceSpinner = (Spinner) findViewById(R.id.BTDeviceSpinner);
		mDeviceSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						BTDeviceName = ((PortInfo) parent
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
		if (preferences != null) {
			BTDeviceName = preferences.getString("BTDEVICE",
					"00:12:6F:07:27:25");
		}

		ArrayAdapter<PortInfo> adapter = new ArrayAdapter<PortInfo>(
				this, android.R.layout.simple_spinner_item, portListGenerator.getPorts());
		mDeviceSpinner.setAdapter(adapter);
		for (int i = 0; i < adapter.getCount(); i++) {
			if (BTDeviceName.equals(adapter.getItem(i).getDevice())) {
				mDeviceSpinner.setSelection(i);
				break;
			}
		}

	}

	

}
