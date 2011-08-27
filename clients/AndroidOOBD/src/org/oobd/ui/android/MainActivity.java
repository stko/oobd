package org.oobd.ui.android;

import java.io.File;

import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.support.Onion;
import org.oobd.ui.android.application.AndroidGui;
import org.oobd.ui.android.application.OOBDApp;
import org.oobd.ui.android.bus.BluetoothInitWorker;

import com.lamerman.FileDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

/**
 * 
 * @author Andreas Budde, Peter Mayer Activity that is launched when the app is
 *         launched.
 */
public class MainActivity extends Activity {

	private Button mDiagnoseButton;
	private Spinner mSourceSpinner;
	private String scriptName;

	public static MainActivity myMainActivity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

}