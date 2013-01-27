package org.oobd.ui.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.MissingResourceException;
import java.util.Set;

import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.support.Onion;
import org.oobd.base.OOBDConstants;
import org.oobd.crypt.AES.EncodeDecodeAES;
import org.oobd.ui.android.application.AndroidGui;
import org.oobd.ui.android.application.OOBDApp;

//import com.lamerman.FileDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.content.SharedPreferences;

import android.widget.EditText;

/**
 * 
 * @author Andreas Budde, Peter Mayer Activity that is launched when the app is
 *         launched.
 */
public class MainActivity extends FragmentActivity implements
		ModalDialog.NoticeDialogListener {

	private Button mDiagnoseButton;
	private Spinner mSourceSpinner;
	private String scriptName;
	private String BTDeviceName = "";
	private String lastScript = "";
	private SharedPreferences preferences;
	private BluetoothAdapter mBluetoothAdapter;

	public static MainActivity myMainActivity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		myMainActivity = this;

		mDiagnoseButton = (Button) findViewById(R.id.diagnose_button);
		mSourceSpinner = (Spinner) findViewById(R.id.scriptSpinner);

		mSourceSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						scriptName = (String) parent.getItemAtPosition(pos);
					}

					public void onNothingSelected(AdapterView<?> parent) {
						scriptName = null;
					}
				});

		ArrayAdapter<String[]> adapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, OOBDApp.getInstance()
						.getAvailableLuaScript());
		mSourceSpinner.setAdapter(adapter);

		preferences = getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
		if (preferences != null) {
			BTDeviceName = preferences.getString("BTDEVICE", "");
			lastScript = preferences.getString("SCRIPT", "");
			if (!lastScript.equals("")) {
				for (int i = 0; i < mSourceSpinner.getCount(); i++) {
					if (lastScript.equals(mSourceSpinner.getItemAtPosition(i))) {
						mSourceSpinner.setSelection(i);
						break;
					}
				}

			}
		}

		mDiagnoseButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter == null) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							myMainActivity).create();
					alertDialog.setTitle("Device Problem");
					alertDialog
							.setMessage("This device does not support Bluetooth!");
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
							"Buy another",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					alertDialog.show();
					return;
				} else {
					if (!mBluetoothAdapter.isEnabled()) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								myMainActivity).create();
						alertDialog.setTitle("Config Problem");
						alertDialog
								.setMessage("Bluetooth is not switched on in settings!");
						alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
								"I'll do",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
						alertDialog.show();
						return;
					}
				}

				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
						.getBondedDevices();
				Log.v(this.getClass().getSimpleName(),
						"Anzahl paired devices: " + pairedDevices.size());

				// If there are paired devices
				if (pairedDevices.size() < 1) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							myMainActivity).create();
					alertDialog.setTitle("No Paired Devides");
					alertDialog.setMessage("No Paired Devices found!");
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
							"I'll change that",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					alertDialog.show();
					return;
				}
				BTDeviceName = preferences.getString("BTDEVICE", "");
				if (BTDeviceName.equalsIgnoreCase("")) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							myMainActivity).create();
					alertDialog.setTitle("Not configured yet");
					alertDialog.setMessage("BT Device not set in Settings");
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
							"I'll choose one",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					alertDialog.show();
					return;
				}
				createDisclaimerDialog();
			}
		});
		TextView versionView = (TextView) findViewById(R.id.versionView);
		versionView.setText("Build "
				+ getResources().getString(R.string.app_svnversion));
		final EditText input = new EditText(this);
		if (preferences.getBoolean("PGPENABLED", false)) {
			new AlertDialog.Builder(myMainActivity)
					.setTitle("PGP Pass Phrase")
					.setMessage(
							"If you want to use PGP scripts, please input your pass phrase here")
					.setView(input)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									try {
										OOBDApp.getInstance()
												.setUserPassPhrase(
														input.getText()
																.toString());
									} catch (Exception e) {
										// e.printStackTrace();
										OOBDApp.getInstance()
												.setUserPassPhrase("");
									}
								}
							})
					.setNegativeButton("Not today",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();
		}
		String pp = new String(OOBDApp.getInstance().getAppPassPhrase());
	}

	void createDisclaimerDialog() {
		InputStream resource = null;
		try {
			resource = OOBDApp.getInstance().generateResourceStream(
					OOBDConstants.FT_SCRIPT, OOBDConstants.DisclaimerFileName);
		} catch (MissingResourceException ex) {

		}
		String title = "";
		Boolean firstLine = true;
		if (resource != null) {
			try {
				StringBuilder sb = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(resource));
				String line = null;

				while ((line = reader.readLine()) != null) {
					if (firstLine) {
						title = line;
						firstLine = false;
					} else {
						sb.append(line);
					}
				}
				resource.close();

				DialogFragment newFragment = new ModalDialog(title,
						sb.toString());
				newFragment.show(getSupportFragmentManager(), "disclaimer");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			startScript();
		}
	}

	// this we need as a seperate function as it might be called by the
	// onclickhandler of the Disclaimer dialog, as Android does not allow modal
	// dialogs (only god knows why...)
	void startScript() {
		if (scriptName != null) {
			preferences.edit().putString("SCRIPT", scriptName).commit();
			// prepare the "load Script" message
			Diagnose.showDialog = true;
			// the following trick avoids a recreation of the Diagnose
			// TapActivity as long as the previous created one is still
			// in memory
			Intent i = new Intent();
			i.setClass(MainActivity.this, DiagnoseTab.class);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(i);

			// startActivity(new Intent(MainActivity.this,
			// Diagnose.class));
			try {
				AndroidGui.getInstance().startScriptEngine(
						new Onion("{" + "'scriptpath':'" + scriptName + "'"
								+ "}"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e("OOBD", "JSON creation error", e);
			}
		}

	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
			startActivity(new Intent(this, Settings.class));
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	
	/**
	 * Create Option menu with link to settings
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// please note: This "menu starter" is disabled through the onKeyDown - handler above..
  		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home_menu, menu);
		return true;
	}

	/**
	 * Gets called when a menu item is selected (in this case settings)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.settings) {
			startActivity(new Intent(this, Settings.class));
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}

	public static MainActivity getMyMainActivity() {
		return myMainActivity;
	}

	public static void setMyMainActivity(MainActivity myMainActivity) {
		MainActivity.myMainActivity = myMainActivity;
	}

	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

	}

	public void onStart() {
		super.onStart();
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
				Log.w(this.getClass().getSimpleName(),
						"Bluetooth not supported.");
			} else {
				if (!mBluetoothAdapter.isEnabled()) {
					Log.w(this.getClass().getSimpleName(),
							"Bluetooth not enabled.");
				}
			}
		}

	}

	public void onDialogPositiveClick(DialogFragment dialog) {
		startScript();

	}

	public void onDialogNegativeClick(DialogFragment dialog) {
		// TODO Auto-generated method stub

	}

}