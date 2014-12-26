package org.oobd.ui.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.Core;
import org.oobd.base.IFui;
import org.oobd.base.OOBDConstants;
import org.oobd.base.support.Onion;
import org.oobd.base.uihandler.OobdUIHandler;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.application.OOBDApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.VehicleManager.VehicleBinder;
import com.openxc.remote.VehicleService;

/**
 * 
 * @author Andreas Budde, Peter Mayer Activity that is launched when the app is
 *         launched.
 */
public class MainActivity extends FragmentActivity implements
		ModalDialog.NoticeDialogListener, IFui {

	public Core core;
	public Map<String, String> scriptEngineMap = new HashMap<String, String>();

	private String scriptEngineID;
	String connectURLDefault = "";

	Handler mHandler;
	String dialogResult;
	Onion dialogResultOnion;

	Button customCancelDialogButton;
	Button customOkDialogButton;
	TextView customTextOut;

	EditText customEditText;

	private Button mDiagnoseButton;
	private Spinner mSourceSpinner;
	private String scriptName;
	private String BTDeviceName = "";
	private String lastScript = "";
	private SharedPreferences preferences;
	private BluetoothAdapter mBluetoothAdapter;

	public static MainActivity myMainActivity;
	VehicleManager service = null;
	OOBDVehicleDataSource source;
	ServiceConnection mConnection;

	public void sm(String msg) {
		// TODO Auto-generated method stub
		OutputActivity.getInstance().addText(msg + "\n");
		// TODO outputtofront crashes
		// DiagnoseTab.getInstance().outputToFront();
	}

	void MessageBox(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		alertDialog.show();
	}

	public Onion requestParamInput_unused(Onion msg) {
		Onion answer = null;

		class OneShotTask implements Runnable {
			Onion thisOnion;

			OneShotTask(Onion o) {
				thisOnion = o;
			}

			public void run() {
				String message = "internal error: Invalid cmd parameters";
				ArrayList<Onion> params = thisOnion.getOnionArray("PARAM",
						"param");
				if (params != null && params.size() > 0) {
					Onion p0Onion = params.get(0);
					if (p0Onion != null) {
						message = Base64Coder.decodeString(p0Onion
								.getOnionString("tooltip"));
					}
				}
				// OutputActivity.getInstance().addText(message + "\n");
				MessageBox("OOBD Message", message);
				return;
			}
		}
		OneShotTask myRunnable = new OneShotTask(msg);
		synchronized (myRunnable) {

			Activity myDiag = this;

			// Diagnose.getInstance().runOnUiThread(myRunnable);
			myDiag.runOnUiThread(myRunnable);

		}
		return answer;
	}

	public void registerOobdCore(Core core) {
		this.core = core;
		Log.v(this.getClass().getSimpleName(), "Core registered in IFui");

	}

	public void announceScriptengine(String id, String visibleName) {
		Log.v(this.getClass().getSimpleName(),
				"Interface announcement: Scriptengine-ID: " + id
						+ " visibleName:" + visibleName);
		scriptEngineMap.put(id, visibleName);

		scriptEngineID = id;

	}

	public Class getVisualizerClass(Onion thisOnion) {
		return VizTable.class;
	}

	public void updateOobdUI() {

		runOnUiThread(new Runnable() {

			public void run() {
				OobdUIHandler uiHandler = OOBDApp.getInstance().getCore()
						.getUiHandler();
				if (uiHandler != null) {
					OOBDApp.getInstance().getCore().getUiHandler().handleMsg();
				}
			}
		});
	}

	public void visualize(final Onion myOnion) {

		IFvisualizer visualComponent;
		Visualizer newVisualizer = new Visualizer(myOnion);
		Class<IFvisualizer> visualizerClass = getVisualizerClass(myOnion);
		String debugText = myOnion.toString();
		Class[] argsClass = new Class[1]; // first we set up an pseudo -
											// args -
											// array for the
											// scriptengine-
											// constructor
		argsClass[0] = Onion.class; // and fill it with the info, that
									// the
									// argument for the constructor
									// will be
									// an Onion
		try {
			Method classMethod = visualizerClass.getMethod("getInstance",
					argsClass); // and let Java find the
								// correct constructor
								// with one Onoin as
								// parameter
			Object[] args = { myOnion }; // we will an args-array
											// with our
											// Onion parameter
			// now call getInstance() in class VizTable
			visualComponent = (IFvisualizer) classMethod.invoke(null, args); // and
																				// finally
																				// create
																				// the
																				// object
																				// from
																				// the
																				// scriptengine
																				// class
																				// with
																				// its
																				// unique
																				// id
																				// as
																				// parameter
			// add new Visualizer object to already existing VizTable
			// object
			newVisualizer.setOwner(visualComponent);

			// add to internal list
			(visualComponent).initValue(newVisualizer, myOnion);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void openPage(String seID, final String Name, int colcount,
			int rowcount) {
		runOnUiThread(new Runnable() {

			public void run() {
				Diagnose.getInstance().stopProgressDialog();
				DiagnoseTab.getInstance().setMenuTitle(Name);
				Diagnose.getInstance().startProgressDialog("Build Page...");
				Log.v(this.getClass().getSimpleName(), "open page ..");
				VizTable vizTable = VizTable.getInstance(null);
				if (vizTable != null && !vizTable.isEmpty()) {
					vizTable.setRemove("");
					vizTable.clear();
				}
				Diagnose.getInstance().stopVisibility();
				Diagnose.getInstance()
						.getListView()
						.setAdapter(
								new DiagnoseAdapter(Diagnose.getInstance(),
										vizTable));
				if (vizTable != null) {
					Intent broadcast = new Intent(OOBDApp.VISUALIZER_UPDATE);
					broadcast.putExtra(OOBDApp.UPDATE_LEVEL, 1);
					Diagnose.myDiagnoseInstance.getApplicationContext()
							.sendBroadcast(broadcast);
				}
			}
		});

	}

	public void openPageCompleted(String seID, String Name) {
		runOnUiThread(new Runnable() {

			public void run() {
				Diagnose.getInstance().setItems(VizTable.getInstance(null));
				Diagnose.getInstance().stopProgressDialog();

				Log.v(this.getClass().getSimpleName(), "...open page completed");
				Intent broadcast = new Intent(OOBDApp.VISUALIZER_UPDATE);
				broadcast.putExtra(OOBDApp.UPDATE_LEVEL, 1);
				Diagnose.myDiagnoseInstance.getApplicationContext()
						.sendBroadcast(broadcast);
			}
		});

	}

	public void startScriptEngine(Onion onion) {

		String seID = OOBDApp.getInstance().getCore()
				.createScriptEngine(scriptEngineID, onion);

		// JTabbedPane newjTabPane = new JTabbedPane(); //create a inner
		// JTabbedPane as container for the later coming scriptengine pages
		// newjTabPane.setName(seID); // set the name of that canvas that it can
		// be found again later
		// mainSeTabbedPane.addTab(seID, newjTabPane); // and put this canvas
		// inside the pane which belongs to that particular scriptengine
		// and now, after initialisation of the UI, let the games begin...
		OOBDApp.getInstance()
				.getCore()
				.setAssign(seID, org.oobd.base.OOBDConstants.CL_PANE,
						new Object()); // store the related drawing pane, the
										// TabPane for that scriptengine
		// stop the Progress Dialog BEFORE the script starts
		// Diagnose.getInstance().stopProgressDialog();
		OOBDApp.getInstance().getCore().startScriptEngine(seID, onion);

	}

	public void announceUIHandler(String id, String visibleName) {
		Onion onion = new Onion();
		String seID = OOBDApp.getInstance().getCore()
				.createUIHandler(id, onion);

		OOBDApp.getInstance().getCore().startUIHandler(seID, onion);

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		myMainActivity = this;
		Core thisCore = new Core(this, OOBDApp.getInstance(), "Core");
		Log.v(this.getClass().getSimpleName(), "Core creation finalized"
				+ thisCore.toString());

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
					// return;
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
						// return;
					}
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
							.getBondedDevices();
					Log.v(this.getClass().getSimpleName(),
							"Nr. of paired devices: " + pairedDevices.size());
					// If there are no paired devices
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
						// return;
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
						// return;
					}
				}
				createDisclaimerDialog();
			}
		});
		TextView versionView = (TextView) findViewById(R.id.versionView);
		versionView.setText("Build "
				+ getResources().getString(R.string.app_svnversion));
		final EditText input = new EditText(this);
		// if (preferences.getBoolean("PGPENABLED", false)) {
		if (true) {
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
		getBaseContext().stopService(
				new Intent(getBaseContext(), VehicleService.class));
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
		}
		Log.d("OpenXC binding", "Starting openXC intend");
		mConnection = new ServiceConnection() {
			// Called when the connection with the service is established
			public void onServiceConnected(ComponentName className,
					IBinder serviceConnect) {
				service = ((VehicleBinder) serviceConnect).getService();
				service.addSource(source);
				Log.d("OpenXC binding", "openXC service connected");
			}

			// Called when the connection with the service disconnects
			// unexpectedly
			public void onServiceDisconnected(ComponentName className) {
				service = null;
				Log.d("OpenXC binding", "openXC service disconnected");
			}

		};

		source = new OOBDVehicleDataSource(null, getBaseContext());
	}

	public void openXCVehicleData(Onion openXCJson) {
		if (source != null) {
			source.sendJSONString(openXCJson.toString());
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// When the activity starts up or returns from the background,
		// re-connect to the VehicleManager so we can receive updates.
		if (service == null) {

			Intent intent = new Intent(this, VehicleManager.class);
			getApplicationContext().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE);

		}
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
				newFragment.show(
						getSupportFragmentManager().beginTransaction(),
						"disclaimer");
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
		if (scriptName == null) {
			return;
		}
		preferences.edit().putString("SCRIPT", scriptName).commit();

		String connectURL = "serial"; // this looks obviously not like an URL
										// yet, but maybe in a later extension

		// ----------------------------------------------------------
		if (preferences.getBoolean("REMOTECONNECT", false)) {
			try {
				Onion answer = requestParamInput(new Onion(
						"{"
								+ "'PARAM' : [{ "
								+ "'type':'String',"
								+ "'title':'"
								+ Base64Coder
										.encodeString("Enter the Connect Number")
								+ "',"
								+ "'default':'"
								+ Base64Coder.encodeString(connectURLDefault)
								+ "',"
								+ "'tooltip':'"
								+ Base64Coder
										.encodeString("Please ask the person, who's connecting the dongle to the vehicle for the Connect Number displayed by his software")
								+ "'" + "}]}"));
				if (answer == null) {
					MessageBox("Missing Value",
							"For Remote Connect you need to enter the Connect Number");
					return;
				}
				connectURL = answer.getOnionBase64String("answer");
				if (connectURL == null || connectURL.equals("")) {
					MessageBox("Missing Value",
							"For Remote Connect you need to enter the Connect Number");
					return;
				}
				connectURLDefault = connectURL;

				String serverURL = preferences.getString(
						OOBDConstants.PropName_KadaverServer,
						OOBDConstants.PropName_KadaverServerDefault);
				String[] parts = serverURL.split("://");
				if (parts.length != 2) {
					MessageBox("Wrong Format",
							"The Remote Connect URL is not a valid URL");
					return;

				}
				connectURL = parts[0] + "://"
						+ Base64Coder.encodeString(connectURL) + "@" + parts[1];

			} catch (JSONException ex) {
				Log.e("OOBD", null, ex);
				return;
			}
		}

		// ----------------------------------------------------------
		// prepare the "load Script" message
		Diagnose.showDialog = true;
		// the following trick avoids a recreation of the Diagnose
		// TapActivity as long as the previous created one is still
		// in memory
		Intent i = new Intent();
		i.setClass(MainActivity.this, DiagnoseTab.class);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);

		try {
			Onion cmdOnion = new Onion("{" + "'scriptpath':'" + scriptName
					+ "'" + ",'connecturl':'"
					+ Base64Coder.encodeString(connectURL) + "'" + "}");
			startScriptEngine(cmdOnion);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("OOBD", "JSON creation error", e);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
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
		// please note: This "menu starter" is disabled through the onKeyDown -
		// handler above..
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

	public Onion requestParamInput(final Onion dialogOnion) {
		dialogResultOnion = null;

		final EditText input = new EditText(this);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message mesg) {
				// process incoming messages here
				// super.handleMessage(msg);
				throw new RuntimeException();
			}
		};
		String dlgTitle="Parameter error";
		String dlgMsg="Parameter error";
		String dlgDefault="Parameter error";
		ArrayList<Onion> params = dialogOnion.getOnionArray("PARAM",
				"param");
		if (params != null && params.size() > 0) {
			Onion p0Onion = params.get(0);
			if (p0Onion != null) {
				dlgTitle=p0Onion.getOnionBase64String("title");
				dlgMsg=p0Onion.getOnionBase64String("tooltip");
				dlgDefault=p0Onion.getOnionBase64String("default");
			}
		}
		input.setText(dlgDefault);
		AlertDialog customAlert = new AlertDialog.Builder(myMainActivity)
				.setTitle(dlgTitle)
				.setMessage(dlgMsg)
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						try {
							dialogResult = input.getText().toString();
							Message m = mHandler.obtainMessage();
							mHandler.sendMessage(m);
							dialogResultOnion=new Onion().setBase64Value("answer",dialogResult);
						} catch (Exception e) {
							// e.printStackTrace();
							dialogResult = "";
							Message m = mHandler.obtainMessage();
							mHandler.sendMessage(m);

						}
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								Message m = mHandler.obtainMessage();
								mHandler.sendMessage(m);

							}
						}).show();
		try {
			customAlert.show();
			// Looper.getMainLooper().loop();
			Looper.loop();
		} catch (RuntimeException e2) {
		}
		return dialogResultOnion;

	}

}
