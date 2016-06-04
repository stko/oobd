/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.oobd.kadaver;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class ActivitySettings extends Activity implements Constants {

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private SharedPreferences prefs;
    private Editor editor;
    // Member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    
    private String btDeviceAddress, websocketURL, defaultEmailReceiver, phoneNumber; 
    private int btDevice_ArrayPosition;
    
    private Spinner spinner_pairedBTDevices;
    private EditText et_WebsocketURL, et_email, et_phone;
    private RadioGroup rg_connection_number;
    private Boolean usePhoneNumber = false;
 
//    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.settings);
        prefs = getSharedPreferences(PREF_KADAVER_PATH, MODE_PRIVATE);
        websocketURL = PREF_WEBSOCKET_URL_DEFAULT;
        defaultEmailReceiver = "";
        if(prefs != null){
        	editor = prefs.edit();
        	btDeviceAddress = prefs.getString(PREF_BLUETOOTH_DEVICE_MAC, "");
        	defaultEmailReceiver  = prefs.getString(PREF_EMAIL_RECEIVER, "");
        	websocketURL = prefs.getString(PREF_WEBSOCKET_URL, PREF_WEBSOCKET_URL_DEFAULT);
        	phoneNumber = prefs.getString(PREF_PHONE_NUMBER, null);
        	usePhoneNumber = prefs.getBoolean(PREF_USE_PHONE_NUMBER, false);
        }
        spinner_pairedBTDevices = (Spinner) findViewById(R.id.spinner_bluetooth_devices);
        et_WebsocketURL = (EditText) findViewById(R.id.et_websocket_url);
        et_WebsocketURL.setText(websocketURL);
        et_WebsocketURL.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(editor != null){
					editor.putString(PREF_WEBSOCKET_URL, et_WebsocketURL.getText().toString());
					editor.commit();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
        
        et_email = (EditText) findViewById(R.id.et_email_receiver);
        et_email.setText(defaultEmailReceiver);
        et_email.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(editor != null){
					editor.putString(PREF_EMAIL_RECEIVER, et_email.getText().toString());
					editor.commit();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
        
        rg_connection_number = (RadioGroup) findViewById(R.id.rg_phoneNumber);
        rg_connection_number.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio0:
					et_phone.setVisibility(View.GONE);
					if(editor!=null){
						editor.putBoolean(PREF_USE_PHONE_NUMBER, false);
						editor.commit();
					}
					usePhoneNumber = false;
					break;
				case R.id.radio1:
					et_phone.setVisibility(View.VISIBLE);
					if(phoneNumber == null || phoneNumber.equalsIgnoreCase("")){
						try{
						TelephonyManager tMgr = (TelephonyManager)ActivitySettings.this.getSystemService(Context.TELEPHONY_SERVICE);
				        String mPhoneNumber = tMgr.getLine1Number();
				        if(mPhoneNumber.equalsIgnoreCase("")){
				        	phoneNumber = "";
				        } else {
				        	phoneNumber = mPhoneNumber;
				        }
						}catch(Exception e){
							phoneNumber = "";
						}
					} 
					usePhoneNumber = true;
					et_phone.setText(phoneNumber);
					if(editor!=null){
						editor.putBoolean(PREF_USE_PHONE_NUMBER, true);
						editor.commit();
					}
					break;
				}
				
			}
		});
        
        et_phone = (EditText) findViewById(R.id.et_phone_number);
        et_phone.setText(defaultEmailReceiver);
        et_phone.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if(editor != null){
					editor.putString(PREF_PHONE_NUMBER, et_phone.getText().toString());
					editor.commit();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
        if(usePhoneNumber){
        	rg_connection_number.check(R.id.radio1);
        }
        // Set result CANCELED incase the user backs out
//        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
//        Button scanButton = (Button) findViewById(R.id.button_scan);
//        scanButton.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                doDiscovery();
//                v.setVisibility(View.GONE);
//            }
//        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
//        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        
        spinner_pairedBTDevices.setAdapter(mPairedDevicesArrayAdapter);
        spinner_pairedBTDevices.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(editor!=null){
					if(mPairedDevicesArrayAdapter.getItem(position).length()>17){
						btDeviceAddress = mPairedDevicesArrayAdapter.getItem(position).substring(mPairedDevicesArrayAdapter.getItem(position).length() - 17);
			            editor.putString(PREF_BLUETOOTH_DEVICE_MAC, btDeviceAddress);
			            editor.commit();
			            System.out.println("KADAVER: btDeviceAddress " + btDeviceAddress);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
        	
        	
		});

//        // Find and set up the ListView for newly discovered devices
//        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
//        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
//        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        this.registerReceiver(mReceiver, filter);
//
//        // Register for broadcasts when discovery has finished
//        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        int i = 0;
        if (pairedDevices.size() > 0) {
//            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equalsIgnoreCase(btDeviceAddress)){
                	btDevice_ArrayPosition = i;
                }
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                i++;
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        
        spinner_pairedBTDevices.setSelection(btDevice_ArrayPosition);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
       if( keyCode == KeyEvent.KEYCODE_BACK && (usePhoneNumber && et_phone.getText().toString().replaceAll(" ", "").equals(""))){
    	   Toast.makeText(this, "Enter specific number (phone number)", Toast.LENGTH_SHORT).show();
		   return true;
       } else {
    	   return super.onKeyDown(keyCode, event);
       }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
//        if (mBtAdapter != null) {
//            mBtAdapter.cancelDiscovery();
//        }

        // Unregister broadcast listeners
//        this.unregisterReceiver(mReceiver);
    }

//    /**
//     * Start device discover with the BluetoothAdapter
//     */
//    private void doDiscovery() {
//        if (D) Log.d(TAG, "doDiscovery()");
//
//        // Indicate scanning in the title
//        setProgressBarIndeterminateVisibility(true);
//        setTitle(R.string.scanning);
//
//        // Turn on sub-title for new devices
////        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
//
//        // If we're already discovering, stop it
//        if (mBtAdapter.isDiscovering()) {
//            mBtAdapter.cancelDiscovery();
//        }
//
//        // Request discover from BluetoothAdapter
//        mBtAdapter.startDiscovery();
//    }

    // The on-click listener for all devices in the ListViews
//    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
//        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
//            // Cancel discovery because it's costly and we're about to connect
////            mBtAdapter.cancelDiscovery();
//
//            // Get the device MAC address, which is the last 17 chars in the View
//            String info = ((TextView) v).getText().toString();
//            String address = info.substring(info.length() - 17);
//            
//            
//            // Create the result Intent and include the MAC address
//            Intent intent = new Intent();
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
//
//            // Set result and finish this Activity
//            setResult(Activity.RESULT_OK, intent);
//            finish();
//        }
//    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            // When discovery finds a device
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Get the BluetoothDevice object from the Intent
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                // If it's already paired, skip it, because it's been listed already
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
////                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
//                }
//            }
//        }
//    }
//            // When discovery is finished, change the Activity title
////            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
////                setProgressBarIndeterminateVisibility(false);
////                setTitle(R.string.select_device);
////                if (mNewDevicesArrayAdapter.getCount() == 0) {
////                    String noDevices = getResources().getText(R.string.none_found).toString();
////                    mNewDevicesArrayAdapter.add(noDevices);
////                }
////            }
//        
//    };

}
