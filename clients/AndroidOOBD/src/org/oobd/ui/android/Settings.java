package org.oobd.ui.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;

import org.oobd.base.OOBDConstants;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.port.PortInfo;
import org.oobd.ui.android.application.OOBDApp;

import android.app.Activity;
import android.app.AlertDialog;

import java.io.File;
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
	private CheckBox remoteConnect;
	private TextView wsURLeditText;
	private TextView wsProxyHostEditText;
	private TextView wsProxyPortEditText;

 	
	public static Settings mySettingsActivity;

	// protected void onCreate(Bundle savedInstanceState,OOBDPort comPort) {
	protected void onCreate(Bundle savedInstanceState) {
		mySettingsActivity = this;
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
				if (checkKeyFiles() != 0) {
					importKeyFiles();
					updateUI();
				} else {
					new AlertDialog.Builder(mySettingsActivity)
							.setTitle("Delete PGP Key Files")
							.setMessage(
									"Do you REALLY want to delete your PGP keys??")
							.setPositiveButton("Delete them!",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											try {
												deleteKeyFiles();
												updateUI();
											} catch (Exception e) {
											}
										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											// Do nothing.
										}
									}).show();
				}

			}
		});
		remoteConnect = (CheckBox) findViewById(R.id.remoteConnectCheckBox);
		remoteConnect.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton parent,
					boolean isChecked) {
				preferences.edit().putBoolean("REMOTECONNECT", isChecked).commit();

			}
		});
		remoteConnect.setChecked(preferences.getBoolean("REMOTECONNECT",false));
		
		wsURLeditText = (EditText)findViewById(R.id.wsURLeditText);
		wsURLeditText.addTextChangedListener(new TextWatcher(){
		    public void afterTextChanged(Editable s) {
		    	preferences.edit().putString(OOBDConstants.PropName_KadaverServer, wsURLeditText.getText().toString()).commit();
		    }
		    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		    public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 
		wsURLeditText.setText(preferences.getString(OOBDConstants.PropName_KadaverServer,OOBDConstants.PropName_KadaverServerDefault));
		
		
		wsProxyHostEditText = (EditText)findViewById(R.id.wsProxyHostEditText);
		wsProxyHostEditText.addTextChangedListener(new TextWatcher(){
		    public void afterTextChanged(Editable s) {
		    	preferences.edit().putString("PROXYHOST", wsProxyHostEditText.getText().toString()).commit();
		    }
		    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		    public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 
		wsProxyHostEditText.setText(preferences.getString("PROXYHOST",""));
		
		
		wsProxyPortEditText = (EditText)findViewById(R.id.wsProxyPortEditText);
		wsProxyPortEditText.addTextChangedListener(new TextWatcher(){
		    public void afterTextChanged(Editable s) {
		    	String content=wsProxyPortEditText.getText().toString();
		    	if (content.length()>0){
		    		preferences.edit().putInt("PROXYPORT", Integer.parseInt(wsProxyPortEditText.getText().toString())).commit();
		    	}
		    }
		    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
		    public void onTextChanged(CharSequence s, int start, int before, int count){}
		}); 
		wsProxyPortEditText.setText(preferences.getString("PROXYPORT",""));
		
		
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
		int pgp = checkKeyFiles();
		String pgpStatusText = "";
		if ((pgp & 0x01) > 0) {
			pgpStatusText = "New Group Key File is waiting for import";
		} else if ((pgp & 0x02) > 0) {
			pgpStatusText = "New User Key File is waiting for import";

		} else if ((pgp & 0x04) > 0) {
			pgpStatusText = "Missing Group Key File !!";

		} else if ((pgp & 0x08) > 0) {
			pgpStatusText = "Missing User Key File !!";
		} else {
			pgpStatusText = "All Keys in place";
		}
		pgpStatus.setText("PGP Key Status: " + pgpStatusText);
		if (pgp != 0) {
			preferences.edit().putBoolean("PGPENABLED", false).commit();
			pgpEnabled.setChecked(false);
			pgpEnabled.setEnabled(false);
			pgpImportKeys.setText("Import PGP keys now");
		} else {
			pgpEnabled.setEnabled(true);
			pgpImportKeys.setText("DELETE PGP keys now");
		}
	}

	private int checkKeyFiles() {
		Boolean userKeyExist;
		Boolean groupKeyExist;
		Boolean newUserKeyExist;
		Boolean newGroupKeyExist;
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
		try {
			InputStream keyfile = OOBDApp.getInstance().generateResourceStream(
					OOBDConstants.FT_SCRIPT,
					OOBDConstants.PGP_USER_KEYFILE_NAME);
			newUserKeyExist = keyfile != null;
			keyfile.close();
		} catch (Exception e) {
			newUserKeyExist = false;
		}
		try {
			InputStream keyfile = OOBDApp.getInstance().generateResourceStream(
					OOBDConstants.FT_SCRIPT,
					OOBDConstants.PGP_GROUP_KEYFILE_NAME);
			newGroupKeyExist = keyfile != null;
			keyfile.close();
		} catch (Exception e) {
			newGroupKeyExist = false;
		}
		return (userKeyExist ? 0 : 8) + (groupKeyExist ? 0 : 4)
				+ (newUserKeyExist ? 2 : 0) + (newGroupKeyExist ? 1 : 0);
	}

	private void deleteKeyFiles() {
		deleteFile(OOBDConstants.PGP_USER_KEYFILE_NAME);
		deleteFile(OOBDConstants.PGP_GROUP_KEYFILE_NAME);
	}

	private void importKeyFiles() {
		if (importsingleKeyFile(OOBDConstants.PGP_USER_KEYFILE_NAME,
				OOBDConstants.PGP_USER_KEYFILE_NAME)) {
			File f = new File(OOBDApp.getInstance().generateUIFilePath(
					OOBDConstants.FT_SCRIPT,
					OOBDConstants.PGP_USER_KEYFILE_NAME));
			f.delete();
		}
		if (importsingleKeyFile(OOBDConstants.PGP_GROUP_KEYFILE_NAME,
				OOBDConstants.PGP_GROUP_KEYFILE_NAME)) {
			File f = new File(OOBDApp.getInstance().generateUIFilePath(
					OOBDConstants.FT_SCRIPT,
					OOBDConstants.PGP_GROUP_KEYFILE_NAME));
			f.delete();
		}
	}

	private boolean importsingleKeyFile(String from, String to) {
		FileOutputStream fos;
		InputStream inFile = OOBDApp.getInstance().generateResourceStream(
				OOBDConstants.FT_SCRIPT, from);
		if (inFile != null) {
			try {
				fos = openFileOutput(to, Context.MODE_PRIVATE);
				org.apache.commons.io.IOUtils.copy(inFile, fos);
				inFile.close();
				fos.close();
				return true;
			} catch (IOException e) {
				// e.printStackTrace(); no stacktrace needed
			}
		}
		return false;

	}

}
