package org.oobd.ui.android;

import java.io.File;
import java.util.Set;

import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.support.Onion;
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
import android.util.Log;
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


/**
 * 
 * @author Andreas Budde, Peter Mayer Activity that is launched when the app is
 *         launched.
 */
public class MainActivity extends Activity {

	private Button mDiagnoseButton;
	private Spinner mSourceSpinner;
	private String scriptName;
	private BluetoothAdapter mBluetoothAdapter;

	public static MainActivity myMainActivity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Remove title bar
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

		mDiagnoseButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter == null) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							myMainActivity).create();
					alertDialog.setTitle("Device Problem");
					alertDialog.setMessage("This device does not support Bluetooth!");
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"Buy another",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					alertDialog.show();
				    return;
				}
				else {
					if (!mBluetoothAdapter.isEnabled()) {
						AlertDialog alertDialog = new AlertDialog.Builder(
								myMainActivity).create();
						alertDialog.setTitle("Config Problem");
						alertDialog.setMessage("Bluetooth is not switched on in settings!");
						alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"I'll do",
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


				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				Log.v(this.getClass().getSimpleName(), "Anzahl paired devices: " + pairedDevices.size());
				
				// If there are paired devices
				if (pairedDevices.size() <1) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							myMainActivity).create();
					alertDialog.setTitle("No Paired Devides");
					alertDialog.setMessage("No Paired Devices found!");
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"I'll change that",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					alertDialog.show();
				    return;
				}
				String BTDeviceName="";
				SharedPreferences preferences = getSharedPreferences("OOBD_SETTINGS", MODE_PRIVATE);
				if (preferences != null) {
					BTDeviceName = preferences.getString("BTDEVICE",
							"");
				}
				if (BTDeviceName.equalsIgnoreCase("")) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							myMainActivity).create();
					alertDialog.setTitle("Not configured yet");
					alertDialog.setMessage("BT Device not set in Settings");
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"I'll choose one",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
					alertDialog.show();
				    return;
				}
				if (scriptName != null) {
					//prepare the "load Script" message
					Diagnose.showDialog=true;
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
								new Onion("{" + "'scriptpath':'"
										+ scriptName + "'" + "}"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Log.e("OOBD", "JSON creation error", e);
					}
				}
			}
		});
		TextView versionView=(TextView) findViewById(R.id.versionView);
		versionView.setText("Build " +getResources().getString(R.string.app_svnversion));
		
	}

	/**
	 * Create Option menu with link to settings
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
	
	public void onStart(){
		super.onStart();
	   	if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			if (mBluetoothAdapter == null) {
			    Log.w(this.getClass().getSimpleName(), "Bluetooth not supported.");
			}
			else {
				if (!mBluetoothAdapter.isEnabled()) {
					Log.w(this.getClass().getSimpleName(), "Bluetooth not enabled.");
				}
			}
		}

	}

}