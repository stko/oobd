package org.oobd.ui.android;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
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

import org.oobd.base.Core;
import org.oobd.base.OOBDConstants;
import org.oobd.base.archive.Archive;
import org.oobd.base.archive.Factory;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas Budde, Peter Mayer Settings activity that allows users to
 *         configure the app (Bluetooth OBD connection, Lua script file
 *         selection and simulation mode.
 */
public class Settings extends Activity implements org.oobd.base.OOBDConstants {

	public static final String KEY_LIST_SELECT_PAIRED_OBD2_DEVICE = "PREF_OOBD_BT_DEVICE";

	private Spinner connectTypeSpinner;
	private Spinner mDeviceSpinner;
	private String connectDeviceName;
	private String connectTypeName;
	private PortInfo[] portList;
	SharedPreferences preferences;
	private CheckBox pgpEnabled;
	private CheckBox httpEnabled;
	private TextView pgpStatus;
	private Button pgpImportKeys;
	private TextView urlEditText;
	private TextView wsProxyHostEditText;
	private TextView wsProxyPortEditText;
	private TextView deviceEditText;
	private Hashtable<String, Class> supplyHardwareConnects;
	private String oldConnectTypeName = null;
	public static Settings mySettingsActivity;

	// protected void onCreate(Bundle savedInstanceState,OOBDPort comPort) {
	protected void onCreate(Bundle savedInstanceState) {
		mySettingsActivity = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		preferences = this.getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		connectTypeSpinner = (Spinner) findViewById(R.id.connectionTypeSpinner);
		connectTypeSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						connectTypeName = (String) parent
								.getItemAtPosition(pos);
						if (connectTypeName != null
								&& !connectTypeName.equalsIgnoreCase("")) {
							Editor prefs = preferences.edit();
							prefs.putString(OOBDConstants.PropName_ConnectType,
									connectTypeName);

							Core.getSingleInstance().writeDataPool(
									OOBDConstants.DP_ACTUAL_CONNECTION_TYPE,
									connectTypeName);
							if (oldConnectTypeName != null) {

								prefs.putString(
										OOBDConstants.PropName_ConnectType,
										connectTypeName);
								// !! The value of the connection device is not
								// stored here, as this already controlled in
								// the comportComboBox change() event
								prefs.putString(
										oldConnectTypeName
												+ "_"
												+ OOBDConstants.PropName_ConnectServerURL,
										((EditText) findViewById(R.id.wsURLeditText))
												.getText().toString());
								prefs.putString(
										oldConnectTypeName
												+ "_"
												+ OOBDConstants.PropName_ProxyHost,
										((EditText) findViewById(R.id.wsProxyHostEditText))
												.getText().toString());

								String portString = ((EditText) findViewById(R.id.wsProxyPortEditText))
										.getText().toString();
								int portInt = 0;
								if (portString.length() > 0) {
									portInt = Integer.parseInt(portString);
								}
								prefs.putInt(oldConnectTypeName + "_"
										+ OOBDConstants.PropName_ProxyPort,
										portInt);

							}
							prefs.commit();
							updateUI();
						}
					}

					public void onNothingSelected(AdapterView<?> parent) {
						preferences
								.edit()
								.putString(OOBDConstants.PropName_ConnectType,
										"").commit();
					}
				});
		List<String> list = new ArrayList<String>();
		supplyHardwareConnects = OOBDApp.getInstance().getCore()
				.getConnectorList();

		Enumeration<String> e = supplyHardwareConnects.keys();

		// iterate through HashTable keys Enumeration
		while (e.hasMoreElements()) {
			list.add(e.nextElement());
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);

		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		connectTypeSpinner.setAdapter(dataAdapter);
		pgpEnabled = (CheckBox) findViewById(R.id.PGPCheckBox);
		pgpEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton parent,
					boolean isChecked) {
				preferences.edit().putBoolean("PGPENABLED", isChecked).commit();

			}
		});
		httpEnabled = (CheckBox) findViewById(R.id.HTTPCheckBox);
		httpEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton parent,
					boolean isChecked) {
				if (isChecked) {
					preferences
							.edit()
							.putString(OOBDConstants.PropName_UIHander,
									OOBDConstants.UIHANDLER_WS_NAME).commit();
				} else {
					preferences
							.edit()
							.putString(OOBDConstants.PropName_UIHander,
									OOBDConstants.UIHANDLER_LOCAL_NAME)
							.commit();

				}

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

		urlEditText = (EditText) findViewById(R.id.wsURLeditText);
		urlEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				preferences
						.edit()
						.putString(
								connectTypeName
										+ "_"
										+ OOBDConstants.PropName_ConnectServerURL,
								urlEditText.getText().toString()).commit();
				Core.getSingleInstance().writeDataPool(
						OOBDConstants.DP_ACTUAL_REMOTECONNECT_SERVER,
						urlEditText.getText().toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		wsProxyHostEditText = (EditText) findViewById(R.id.wsProxyHostEditText);
		wsProxyHostEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				preferences
						.edit()
						.putString(
								connectTypeName + "_"
										+ OOBDConstants.PropName_ProxyHost,
								wsProxyHostEditText.getText().toString())
						.commit();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		wsProxyPortEditText = (EditText) findViewById(R.id.wsProxyPortEditText);
		wsProxyPortEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				String content = wsProxyPortEditText.getText().toString();
				if (content.length() > 0) {
					preferences
							.edit()
							.putInt(connectTypeName + "_"
									+ OOBDConstants.PropName_ProxyPort,
									Integer.parseInt(wsProxyPortEditText
											.getText().toString())).commit();

				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});

		deviceEditText = (EditText) findViewById(R.id.deviceEditText);
		deviceEditText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				connectDeviceName = deviceEditText.getText().toString();
				connectDeviceName = connectDeviceName
						.replaceAll("\\(.*\\)", "").trim();
				/*
				 * Do we really have to program our own combobox to input own
				 * devices? http://stackoverflow.com/questions
				 * /3024656/how-can-i-show-a-combobox-in-android
				 * cb.getEditor().setItem(connectDeviceName);
				 */
				if (connectDeviceName != null
						&& !connectDeviceName.equalsIgnoreCase("")) {
					preferences
							.edit()
							.putString(connectTypeName + "_" + OOBDConstants.PropName_SerialPort,
									connectDeviceName).commit();
					Core.getSingleInstance().writeDataPool(
							OOBDConstants.DP_ACTUAL_CONNECT_ID,
							connectDeviceName);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		mDeviceSpinner = (Spinner) findViewById(R.id.BTDeviceSpinner);
		mDeviceSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						Object item = parent.getItemAtPosition(pos);

						if (item instanceof PortInfo) {
							connectDeviceName = ((PortInfo) item).getDevice();
						} else {
							connectDeviceName = item.toString();
							connectDeviceName = connectDeviceName.replaceAll(
									"\\(.*\\)", "").trim();
							/*
							 * Do we really have to program our own combobox to
							 * input own devices?
							 * http://stackoverflow.com/questions
							 * /3024656/how-can-i-show-a-combobox-in-android
							 * cb.getEditor().setItem(connectDeviceName);
							 */
						}
						if (connectDeviceName != null
								&& !connectDeviceName.equalsIgnoreCase("")) {
							deviceEditText.setText(connectDeviceName);

						}

					}

					public void onNothingSelected(AdapterView<?> parent) {
						deviceEditText.setText("");

					}
				});

		String actualScriptDir = preferences.getString(
				OOBDConstants.PropName_ScriptDir, Environment
						.getExternalStorageDirectory().getPath() + "/OOBD/");
		Core.getSingleInstance().writeDataPool(DP_SCRIPTDIR, actualScriptDir);
		Core.getSingleInstance().writeDataPool(
				DP_WWW_LIB_DIR,
				preferences.getString(OOBDConstants.PropName_LibraryDir,
						actualScriptDir + "lib_html"));

		connectTypeName = preferences.getString(
				OOBDConstants.PropName_ConnectType, connectTypeName);

		Core.getSingleInstance().writeDataPool(
				OOBDConstants.DP_ACTUAL_CONNECTION_TYPE, connectTypeName);

		connectDeviceName = preferences.getString(connectTypeName + "_DEVICE",
				connectDeviceName);
		Core.getSingleInstance().writeDataPool(DP_ACTUAL_CONNECT_ID,
				connectDeviceName);

		ArrayList<Archive> files = Factory.getDirContent(actualScriptDir);
		Core.getSingleInstance().writeDataPool(DP_LIST_OF_SCRIPTS, files);
		updateUI();

	}

	protected void onRestart() {
		super.onRestart();
		updateUI();
	}

	private void updateUI() {
		if (preferences != null) {
			connectTypeName = preferences.getString(
					OOBDConstants.PropName_ConnectType,
					OOBDConstants.PropName_ConnectTypeBT);
			connectDeviceName = preferences.getString(connectTypeName
					+ "_DEVICE", "");
			pgpEnabled.setChecked(preferences.getBoolean("PGPENABLED", false));
			httpEnabled.setChecked(preferences.getString(
					OOBDConstants.PropName_UIHander, "UIHandler")
					.equalsIgnoreCase(OOBDConstants.UIHANDLER_WS_NAME));
		}
		urlEditText.setText(preferences.getString(connectTypeName + "_"
				+ OOBDConstants.PropName_ConnectServerURL,
				OOBDConstants.PropName_KadaverServerDefault));
		wsProxyHostEditText.setText(preferences.getString(connectTypeName + "_"
				+ "PROXYHOST", ""));
		wsProxyPortEditText.setText(preferences.getString(connectTypeName + "_"
				+ "PROXYPORT", ""));
		deviceEditText.setText(connectDeviceName);
		Class<OOBDPort> value = supplyHardwareConnects.get(connectTypeName);
		try { // tricky: try to call a static method of an interface, where a
				// interface don't have static values by definition..
				// Class[] parameterTypes = new Class[]{};
			java.lang.reflect.Method method = value.getMethod("getPorts",
					new Class[] {}); // no parameters
			Object instance = null;
			portList = (PortInfo[]) method.invoke(instance, new Object[] {}); // no
																				// parameters

		} catch (Exception ex) {
			Logger.getLogger(Core.class.getName())
					.log(Level.WARNING,
							"can't call static method 'getPorts' of "
									+ value.getName());
			ex.printStackTrace();

		}

		for (int i = 0; i < connectTypeSpinner.getAdapter().getCount(); i++) {
			if (connectTypeName.equals(connectTypeSpinner.getAdapter().getItem(
					i))) {
				connectTypeSpinner.setSelection(i);
				break;
			}
		}

		int portListIndex = -1;

		PortInfo[] portCopyPlusOne = new PortInfo[portList.length + 1]; // needed
																		// maybe
																		// later,
																		// in
																		// case
																		// the
																		// port
																		// is
																		// not
																		// part
																		// of
																		// the
																		// port
																		// list,
																		// which
																		// was
																		// delivered
																		// by
																		// the
																		// port-getPorts()
																		// function
		for (int i = 0; i < portList.length; i++) {
			portCopyPlusOne[i + 1] = portList[i];
			if (portList[i].getDevice().equals(connectDeviceName)) {
				portListIndex = i;
			}
		}
		if (portListIndex == -1) { // now we use the List, which has space on
									// item[0] to add the port which was not
									// found in the device list
			portCopyPlusOne[0] = new PortInfo(connectDeviceName,
					connectDeviceName);

			ArrayAdapter<PortInfo> adapter = new ArrayAdapter<PortInfo>(this,
					android.R.layout.simple_spinner_item, portCopyPlusOne);
			mDeviceSpinner.setAdapter(adapter);
			mDeviceSpinner.setSelection(0);
		} else {

			ArrayAdapter<PortInfo> adapter = new ArrayAdapter<PortInfo>(this,
					android.R.layout.simple_spinner_item, portList);
			mDeviceSpinner.setAdapter(adapter);
			mDeviceSpinner.setSelection(portListIndex);
		}

		int pgp = checkKeyFiles();
		String pgpStatusText = "No PGP keys available";

		if (pgp == 0) { // all ok
			pgpStatusText = "All Keys in place";
		}
		if ((pgp & 0x04) > 0) { // no group key
			pgpStatusText = "Missing Group Key File !!";
		}
		if ((pgp & 0x01) > 0) {// new group key
			pgpStatusText = "New Group Key File is waiting for import";
		}
		if ((pgp & 0x08) > 0) { // no user key
			pgpStatusText = "Missing User Key File !!";
		}
		if ((pgp & 0x02) > 0) {// new user key
			pgpStatusText = "New User Key File is waiting for import";
		}
		if ((pgp == (0x04 + 0x04))) {// no keys
			pgpStatusText = "No PGP keys available";
		}

		pgpStatus.setText("PGP Key Status: " + pgpStatusText);
		if (pgp != 0) {
			preferences.edit()
					.putBoolean(OOBDConstants.PropName_PGPEnabled, false)
					.commit();
			pgpEnabled.setSelected(false);
			pgpEnabled.setEnabled(false);
			if ((pgp & (0x02 + 0x01)) > 0) { // any new keys there
				pgpImportKeys.setText("Import PGP keys now");
			} else {
				pgpImportKeys.setText("No PGP Keys available");
			}
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
					OOBDConstants.FT_KEY_IMPORT,
					OOBDConstants.PGP_USER_KEYFILE_NAME);
			newUserKeyExist = keyfile != null;
			keyfile.close();
		} catch (Exception e) {
			newUserKeyExist = false;
		}
		try {
			InputStream keyfile = OOBDApp.getInstance().generateResourceStream(
					OOBDConstants.FT_KEY_IMPORT,
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
					OOBDConstants.FT_KEY_IMPORT,
					OOBDConstants.PGP_USER_KEYFILE_NAME));
			f.delete();
		}
		if (importsingleKeyFile(OOBDConstants.PGP_GROUP_KEYFILE_NAME,
				OOBDConstants.PGP_GROUP_KEYFILE_NAME)) {
			File f = new File(OOBDApp.getInstance().generateUIFilePath(
					OOBDConstants.FT_KEY_IMPORT,
					OOBDConstants.PGP_GROUP_KEYFILE_NAME));
			f.delete();
		}
	}

	private boolean importsingleKeyFile(String from, String to) {
		FileOutputStream fos;
		InputStream inFile = OOBDApp.getInstance().generateResourceStream(
				OOBDConstants.FT_KEY_IMPORT, from);
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
