package org.oobd.ui.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;

import org.oobd.base.OOBDConstants;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.port.PortInfo;
import org.oobd.ui.android.application.OOBDApp;

import android.app.Activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private CheckBox pgpEnabled;
	private TextView pgpStatus;
	private Button pgpImportKeys;

	// protected void onCreate(Bundle savedInstanceState,OOBDPort comPort) {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		preferences = this.getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		portListGenerator = ((OOBDPort) OOBDApp.getInstance().getCore()
				.supplyHardwareHandle(null));
		mDeviceSpinner = (Spinner) findViewById(R.id.BTDeviceSpinner);
		mDeviceSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						BTDeviceName = ((PortInfo) parent
								.getItemAtPosition(pos)).getDevice();
						if (BTDeviceName != null
								&& !BTDeviceName.equalsIgnoreCase("")) {
							preferences.edit()
									.putString("BTDEVICE", BTDeviceName)
									.commit();
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						preferences.edit().putString("BTDEVICE", "").commit();
					}
				});
		pgpEnabled = (CheckBox) findViewById(R.id.PGPCheckBox);
		pgpEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton parent,
					boolean isChecked) {
				preferences.edit().putBoolean("PGPENABLED", isChecked).commit();

			}
		});
		pgpStatus = (TextView) findViewById(R.id.pgpStatustextView);
		pgpImportKeys = (Button) findViewById(R.id.pgpImportKeysbutton);
		pgpImportKeys.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				importKeyFiles();
				pgpStatus.setText("PGP Key Status: " + checkKeyFiles());

			}});

		updateUI();
	}

	protected void onRestart() {
		super.onRestart();
		updateUI();
	}

	private void updateUI() {
		if (preferences != null) {
			BTDeviceName = preferences.getString("BTDEVICE",
					"00:12:6F:07:27:25");
			pgpEnabled.setChecked(preferences.getBoolean("PGPENABLED", false));
		}

		ArrayAdapter<PortInfo> adapter = new ArrayAdapter<PortInfo>(this,
				android.R.layout.simple_spinner_item,
				portListGenerator.getPorts());
		mDeviceSpinner.setAdapter(adapter);
		for (int i = 0; i < adapter.getCount(); i++) {
			if (BTDeviceName.equals(adapter.getItem(i).getDevice())) {
				mDeviceSpinner.setSelection(i);
				break;
			}
		}
		pgpStatus.setText("PGP Key Status: " + checkKeyFiles());
	}

	private String checkKeyFiles() {
		Boolean userKeyExist;
		Boolean groupKeyExist;
		try {
			FileInputStream keyfile = openFileInput(OOBDConstants.PGP_USER_KEYFILE_NAME);
			userKeyExist = keyfile != null;
			keyfile.close();
		} catch (Exception e) {
			userKeyExist = false;
		}
		try {
			FileInputStream keyfile = openFileInput(OOBDConstants.PGP_GROUP_KEYFILE_NAME);
			groupKeyExist = keyfile != null;
			keyfile.close();
		} catch (Exception e) {
			groupKeyExist = false;
		}
		if (userKeyExist && groupKeyExist) {
			return "All Keys available";
		} else {
			if (userKeyExist) {
				return "Missing Group Key File";
			} else {
				if (userKeyExist) {
					return "Missing User Key File";
				} else {
					return "Not any Key found";

				}
			}
		}
	}

	private void importKeyFiles() {
		importsingleKeyFile(OOBDConstants.PGP_USER_KEYFILE_NAME,
				OOBDConstants.PGP_USER_KEYFILE_NAME);
		importsingleKeyFile(OOBDConstants.PGP_GROUP_KEYFILE_NAME,
				OOBDConstants.PGP_GROUP_KEYFILE_NAME);
	}

	private void importsingleKeyFile(String from, String to) {
		FileOutputStream fos;
		InputStream inFile = OOBDApp.getInstance().generateResourceStream(
				OOBDConstants.FT_SCRIPT, from);
		if (inFile != null) {
			try {
				fos = openFileOutput(to, Context.MODE_PRIVATE);
				org.apache.commons.io.IOUtils.copy(inFile, fos);
				inFile.close();
				fos.close();
			} catch (IOException e) {
				// e.printStackTrace(); no stacktrace needed
			}
		}

	}

}
