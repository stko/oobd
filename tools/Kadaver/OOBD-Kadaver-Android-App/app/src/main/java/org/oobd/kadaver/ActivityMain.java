package org.oobd.kadaver;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.channels.NotYetConnectedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



public class ActivityMain extends ActionBarActivity implements Constants{

	public static final String TAG = "KADAVER";
	public static ActivityMain mActivity;
	
	// EVENT FLOW CONTROL Events
	public static final int CHKBTAPAPTER = 1001;
	public static final int BTAVAILABLE = 1002;
	public static final int WSCONNECTED = 1003;
	public static final int DONGLEFOUND = 1004;
	public static final int BTCONNECTED = 1005;
	public static final int BTCONNECTING = 10051;
	public static final int BTDISCONNECTED = 10052;
	public static final int WSDISCONNECTED = 1006;
	public static final int WSRECEIVE = 1007;
	public static final int BTRECEIVE = 1008;
	public static final int JSONERRORRECV = 1009;
	public static final int WSSEND = 1010;
	public static final int BTSEND = 1011;
	public static final int ENDPROGRAM = 1012;
	
	public static final boolean ANIMATE_TO_RIGHT = true;
	public static final boolean ANIMATE_TO_LEFT = false;
	
	public static final int STATE_CONNECTED_GREEN = 3;
	public static final int STATE_CONNECTING_YELLOW = 2;
	public static final int STATE_NOT_CONNECTED_RED = 1;
	public boolean isWSConnected = false, isBTConnected = false;
	private MenuItem flash_menu_item, keep_screen_on_menu_item;


	
	// Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetootService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
//    public static final String wsURL = "wss://oobd.luxen.de/websockssl/";
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int LAYOUT_KADAVER = 1;
    private static final int LAYOUT_OOBDFLASH = 2;
	
    private int selectedLayout = 1;
    private int flash_selected_radio;
    
    private String versionCode="";
    private KadaverWebSocketClient mConnection;
////    WebSocket mConnection;
//    WebSocketConnection mConnction;
    

    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mBTService = null;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private TextView mBTStatus,mStatus1, tv_BuildVersion;
    
    private Button mStatus2;
    private ImageView pv_1,pv_2,pv_3,pv_4;
    private ImageView iv_car, iv_smartphone, iv_world, btn_start_flash;
    private LinearLayout ll_pv1, ll_pv2;
    private LayoutParams lp_car,lp_smartphone, lp_world, lp_pv1, lp_pv2;
    
    private View flash_shadow1, flash_shadow2;
    private RadioGroup flash_rg;
    private TextView flash_tv;
    private Spinner flash_spinner_files;
    private EditText flash_et_firmware_url;
    private RelativeLayout flash_rl, rl_mainscreen;
    private ArrayAdapter<String> mFilesArrayAdapter;
    private ArrayList<File> fileArrayList;
    private int file_ArrayPosition;
    private boolean isScreenOn, usePhoneNumber;
    
    
    private int intChannel;
    private String btDeviceAddress, websocketURL, defaultEmailReceiver,filepath, phoneNumber, stringChannel, previousCustomURL; 
    private SharedPreferences prefs;
    private Editor editor;
    
    private Dialog flashingDialog;
    private TextView tv_flashDialog;
    private Button btn_flashDialog;
    private ProgressBar pb_flashDialog;
    
    private boolean isFlashingDialogShowing = false;
    public boolean isFlashSuccessful = false;
    
    
    private WebSocketThread mWSThread;
    
    
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
               
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	eventFlowControl(BTCONNECTED, null);
                	break;
                case BluetoothService.STATE_CONNECTING:
                	eventFlowControl(BTCONNECTING, null);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                   eventFlowControl(BTDISCONNECTED, null);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                String writeMessage = new String(writeBuf);
                eventFlowControl(BTSEND, writeMessage);
                System.out.println("Kadaver: 1 Kadaver -> Dongle: " + writeMessage);

                break;
            case MESSAGE_READ:
                String readMessage = (String) msg.obj;
//                int index = readMessage.indexOf("\r");
//	             int index= readMessage.lastIndexOf("\r", readMessage.length()-6);
////	              System.out.println("Message from BT index: " + index);
//	              if(index != -1 && readMessage.length()>(index+1))
//	              readMessage = readMessage.substring(index+1);
	              System.out.println("Kadaver: 2 Dongle -> Kadaver: " + readMessage);
                eventFlowControl(BTRECEIVE, readMessage);
                

                break;
            case MESSAGE_DEVICE_NAME:
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);

                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
    public void eventFlowControl(int event, final String data){
		switch(event){
		
		case WSCONNECTED:
			Log.d(TAG,"WSCONNECTED...");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(selectedLayout==LAYOUT_KADAVER){
						changeWorldImage(STATE_CONNECTED_GREEN);
						if(isBTConnected){
			            	changeSmartphoneImage(STATE_CONNECTED_GREEN);
			            	normalOperation();
			            } else {
			            	changeSmartphoneImage(STATE_CONNECTING_YELLOW);
			            }
					}
				}
			});
			sendToWS("OOBD rocks");
			break;
	
		case BTCONNECTED:
			if(selectedLayout==LAYOUT_KADAVER){
				showStatusTexts(true);
				startService();
				mBTStatus.setText(R.string.title_connected_to);
	            mBTStatus.append("\n"+mConnectedDeviceName+"\n"+versionCode);
	            changeCarImage(STATE_CONNECTED_GREEN);
	            if(isWSConnected || selectedLayout == LAYOUT_OOBDFLASH){
	            	changeSmartphoneImage(STATE_CONNECTED_GREEN);
	            } else {
	            	changeSmartphoneImage(STATE_CONNECTING_YELLOW);
	            	
	            	mWSThread = new WebSocketThread();
	            	mWSThread.start();
	            	
	            }
			} else {
				mBTStatus.setText(R.string.title_connected_to);
	            mBTStatus.append("\n"+mConnectedDeviceName+"\n"+versionCode);
				changeCarImage(STATE_CONNECTED_GREEN);
				changeSmartphoneImage(STATE_CONNECTED_GREEN);
			}
			
			break;
		case BTCONNECTING:
			mBTStatus.setText(R.string.title_connecting);
            showStatusTexts(false);
            changeCarImage(STATE_CONNECTING_YELLOW);
            break;
		case BTDISCONNECTED:
			if(selectedLayout==LAYOUT_KADAVER){
				mBTStatus.setText(R.string.title_not_connected);
	            showStatusTexts(false);
//	            changeCarImage(STATE_NOT_CONNECTED_RED);
	            if(isWSConnected){
	            	changeSmartphoneImage(STATE_CONNECTING_YELLOW);
	            } else {
	            	changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
	            }
	            connectToBluetoothDevice();
			} else {
				if(isFlashSuccessful){
				mBTStatus.setText(R.string.title_not_connected);
				changeCarImage(STATE_NOT_CONNECTED_RED);
				changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
				} else {
					connectToBluetoothDevice();
					changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
				}
			}
			break;
		case WSDISCONNECTED:
			Log.d(TAG,"WSDISCONNECTED...");
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(selectedLayout==LAYOUT_KADAVER){
						changeWorldImage(STATE_NOT_CONNECTED_RED);
							if(isBTConnected){
				            	changeSmartphoneImage(STATE_CONNECTING_YELLOW);
				            	mWSThread = new WebSocketThread();
				            	mWSThread.start();
				            } else {
				            	changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
				            }
						
						}
					}
				
			});
			break;
		
		case WSRECEIVE:
			Log.d(TAG,"WSRECEIVE..." + data);
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					triggerBarberPole(4);
					}
			});
			break;
		case BTRECEIVE:
			Log.d(TAG,"BTRECEIVE..." + data);
			if(data.contains("p 0 0 0")){
				if(data.indexOf("OOBD")!=-1){
					versionCode = data.substring(data.indexOf("OOBD"));
					if(versionCode.indexOf("+")!=-1){
						versionCode = versionCode.substring(0, versionCode.indexOf("+")+5);
					}
					mBTStatus.setText(R.string.title_connected_to);
		            mBTStatus.append("\n"+mConnectedDeviceName+"\n"+versionCode);
				}
			}
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					triggerBarberPole(1);
					}
			});
			
			if(isWSConnected){
				
            	sendToWS(data);
            }
			
			break;
		
		case WSSEND:
			Log.d(TAG,"WSSEND..." + data);
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					triggerBarberPole(3);
					}
			});
			break;
		case BTSEND:
			Log.d(TAG,"BTSEND..." + data);
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					triggerBarberPole(2);
					}
			});
			break;
			
		case JSONERRORRECV:
			Log.d(TAG,"JSONERRORRECV...");
			break;
		case ENDPROGRAM:
			
			break;
		}
	}
    
    Handler mFlashHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case HANDLER_FLASH_TOAST:
//				final String flash_info = (String) msg.obj;
//				cancelDialog = msg.arg1;
//				runOnUiThread(new Runnable() {
//					
//					@Override
//					public void run() {
////						if(tv_flashdialog!= null){
////							tv_flashdialog.append(flash_info+"\n");	
////						}
//					}
//				});
//				
//				break;

    		}
    	};
    };
    

	// --------------------------------- ON CREATE -----------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;
        createKadaverFolder();
        prefs = getSharedPreferences(PREF_KADAVER_PATH, MODE_PRIVATE);
        websocketURL = PREF_WEBSOCKET_URL_DEFAULT;
        defaultEmailReceiver = "";
        if(prefs != null){
        	editor = prefs.edit();
        	btDeviceAddress = prefs.getString(PREF_BLUETOOTH_DEVICE_MAC, null);
        	websocketURL = prefs.getString(PREF_WEBSOCKET_URL, PREF_WEBSOCKET_URL_DEFAULT);
        	defaultEmailReceiver = prefs.getString(PREF_EMAIL_RECEIVER, "");
        	isScreenOn = prefs.getBoolean(PREF_IS_SCREEN_ON, false);
        	usePhoneNumber = prefs.getBoolean(PREF_USE_PHONE_NUMBER, false);
        	phoneNumber = prefs.getString(PREF_PHONE_NUMBER, "");
        	previousCustomURL = prefs.getString(PREF_CUSTOM_URL, "");
        }
        if(btDeviceAddress == null){
        	Toast.makeText(this, "Select a Device first", Toast.LENGTH_LONG).show();
        }
        getSupportActionBar().setTitle("Kadaver");
        rl_mainscreen = (RelativeLayout) findViewById(R.id.rl_mainscreen);
        mBTStatus = (TextView) findViewById(R.id.TV_Connected);
        
        mStatus1 = (TextView) findViewById(R.id.TV_Status1);
        mStatus2 = (Button) findViewById(R.id.BTN_Status2);
        tv_BuildVersion = (TextView) findViewById(R.id.TV_app_version);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        String currentDateandTime = sdf.format(new Date());
//        tv_BuildVersion.setText("Build: " + getResources().getString(R.string.app_version));
        tv_BuildVersion.setText("Build: " + currentDateandTime);
        ll_pv1 = (LinearLayout) findViewById(R.id.ll_pv1);
        ll_pv2 = (LinearLayout) findViewById(R.id.ll_pv2);
        selectedLayout = LAYOUT_KADAVER;
        pv_1 = (ImageView) findViewById(R.id.progress_view1);
//        pv_1.setBackgroundAsTile(R.drawable.tile,ANIMATE_TO_RIGHT);
//        pv_1.startAnimation();
        pv_1.setVisibility(View.INVISIBLE);

        pv_2 = (ImageView) findViewById(R.id.progress_view2);
//        pv_2.setBackgroundAsTile(R.drawable.tile, ANIMATE_TO_LEFT);
//        pv_2.startAnimation();
        pv_2.setVisibility(View.INVISIBLE);
        
        pv_3 = (ImageView) findViewById(R.id.progress_view3);
//        pv_3.setBackgroundAsTile(R.drawable.tile,ANIMATE_TO_RIGHT);
//        pv_3.startAnimation();
        pv_3.setVisibility(View.INVISIBLE);
        
        pv_4 = (ImageView) findViewById(R.id.progress_view4);
//        pv_4.setBackgroundAsTile(R.drawable.tile,ANIMATE_TO_LEFT);
//        pv_4.startAnimation();
        pv_4.setVisibility(View.INVISIBLE);
        
        iv_car = (ImageView) findViewById(R.id.iv_car);
        iv_car.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isBTConnected){
					connectToBluetoothDevice();
				} else {
//					startService();
				}
				
			}
		});
        iv_smartphone = (ImageView) findViewById(R.id.iv_smartphone);
        iv_world = (ImageView) findViewById(R.id.iv_world);
        iv_world.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isWSConnected){
//					if(mWSThread != null){
//						if(mWSThread.isAlive())
//						mWSThread.stop();;
//						mWSThread = null;
//					}
					mWSThread = new WebSocketThread();
					mWSThread.start();
				} else {
//					mConnection.write("TEST_MESSAGE");
				}
			}
		});
        
        changeCarImage(STATE_NOT_CONNECTED_RED);
        changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
        changeWorldImage(STATE_NOT_CONNECTED_RED);
        
        showStatusTexts(false);
        mStatus2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(prefs != null){
		        	defaultEmailReceiver = prefs.getString(PREF_EMAIL_RECEIVER, "");
		        	System.out.println("EMAIL RECEIVER: " + defaultEmailReceiver);
		        }
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, stringChannel);
				intent.putExtra(Intent.EXTRA_SUBJECT, "OOBD Kadaver Connection Number");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { defaultEmailReceiver });
				startActivity(Intent.createChooser(intent, "Send Connection Number"));
				
			}
		});
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lp_world = iv_world.getLayoutParams();
        lp_smartphone = iv_smartphone.getLayoutParams();
        lp_car = iv_car.getLayoutParams();
        lp_pv1 = ll_pv1.getLayoutParams();
        lp_pv2 = ll_pv2.getLayoutParams();
        
        btn_start_flash = (ImageView) findViewById(R.id.btn_start_flash);
        btn_start_flash.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				if(!isBTConnected){
//					Toast.makeText(ActivityMain.this, "Connect to Bluetooth device", Toast.LENGTH_SHORT).show();
//					return;
//				}
				System.out.println("FLASH ORDER 0: Selected Radio Button: " + flash_selected_radio);
				switch(flash_selected_radio){
				case 0:
					startFlash(null,null);
					break;
				case 1:
					if(flash_et_firmware_url.getText().toString().equalsIgnoreCase("")){
						
						Toast.makeText(ActivityMain.this, "Enter URL", Toast.LENGTH_SHORT).show();
						return;
					} else {
						if(editor!=null){
							previousCustomURL = flash_et_firmware_url.getText().toString();
							editor.putString(PREF_CUSTOM_URL, previousCustomURL).commit();
						}
						startFlash(previousCustomURL, null);
					}
					break;
				case 2:
					if(flash_spinner_files.getSelectedItem().toString().equalsIgnoreCase("No Files in Directory")){
						Toast.makeText(ActivityMain.this, "Select a valid File", Toast.LENGTH_SHORT).show();
						return;
					} else {
						int position = flash_spinner_files.getSelectedItemPosition();
						
						startFlash(null, fileArrayList.get(position).getAbsolutePath());
					}
					break;
				}
			}
		});
        
        int min = 10000000;
    	int max = 99999999;
    	intChannel = (int) Math.floor(Math.random() * (max - min + 1) + min);
    	stringChannel = String.valueOf(intChannel);
//        mStatus2.setText(String.valueOf(thisChannel));
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if(usePhoneNumber){
        	if(phoneNumber.equals("")){
        		Toast.makeText(this, "Enter your Phone Number in Settings", Toast.LENGTH_SHORT).show();
        	} else {
        		stringChannel = phoneNumber;
        	}
        }
//        thisChannel = 12345678;
        initOOBDFlashViews();
        
		 
        IntentFilter f1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter f2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, f1);
        this.registerReceiver(mReceiver, f2);
        
    }
    
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        

		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())){
				if(selectedLayout == LAYOUT_KADAVER || !isFlashSuccessful){
					eventFlowControl(BTDISCONNECTED, null);
					
				}
			}
		}
	 };
    
    private void startFlash(String downloadURL, String flashFilename){
    	setFlashStatusText("Initialize Flashing Sequence...", false);
    	System.out.println("FLASH ORDER: 1");
    	mBTService.startFlash(downloadURL, flashFilename, mFlashHandler);    	
    }
    
    private int cancelDialog = 0;
    
    
    private void initOOBDFlashViews(){
    	flash_tv = (TextView) findViewById(R.id.tv_flash);
    	flash_et_firmware_url = (EditText) findViewById(R.id.et_firmware_url);
    	flash_et_firmware_url.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(editor!=null){
					previousCustomURL = s.toString();
					editor.putString(PREF_CUSTOM_URL, previousCustomURL).commit();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
    	flash_shadow1 = (View) findViewById(R.id.shadow_flash1);
    	flash_shadow2 = (View) findViewById(R.id.shadow_flash2);
    	flash_rg = (RadioGroup) findViewById(R.id.rg_flash_buttons);
    	flash_rl = (RelativeLayout) findViewById(R.id.rl_flash);
    	flash_spinner_files = (Spinner) findViewById(R.id.spinner_files);
    	flash_rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				System.out.println("ONCHECKEDCHANGED: " + checkedId);
				switch (checkedId) {
				
				case R.id.radio0:
					flash_tv.setVisibility(View.GONE);
					flash_spinner_files.setVisibility(View.GONE);
					flash_et_firmware_url.setVisibility(View.GONE);
					flash_selected_radio = 0;
					break;
				case R.id.radio1:
					flash_tv.setVisibility(View.VISIBLE);
					flash_spinner_files.setVisibility(View.GONE);
					flash_et_firmware_url.setVisibility(View.VISIBLE);
					flash_tv.setText("Firmware Download URL");
					flash_selected_radio = 1;
					break;
				case R.id.radio2:
					flash_tv.setVisibility(View.VISIBLE);
					flash_spinner_files.setVisibility(View.VISIBLE);
					flash_et_firmware_url.setVisibility(View.GONE);
					flash_tv.setText("Files in ..OOBD/Kadaver");
					flash_selected_radio = 2;
					mFilesArrayAdapter = new ArrayAdapter<String>(ActivityMain.this, R.layout.device_name);
					flash_spinner_files.setAdapter(mFilesArrayAdapter);
			        flash_spinner_files.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent, View view,
								int position, long id) {
							if(editor!=null){
								
									file_ArrayPosition = position;
									if(fileArrayList != null && fileArrayList.size()>position){
										filepath = fileArrayList.get(position).getAbsolutePath();
										if(!filepath.equalsIgnoreCase("No Files in Directory")){
								            editor.putString(PREF_FIRMWARE_FILEPATH, filepath);
								            editor.commit();
										}
									}
								
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {
							
						}
			        	
			        	
					});

			        fileArrayList = getFilesFromStorage();
			        for(int i = 0; i < fileArrayList.size(); i ++){
			        	mFilesArrayAdapter.add(fileArrayList.get(i).getName());
			        }
			        if(mFilesArrayAdapter.getCount()==0){
			        	mFilesArrayAdapter.add("No Files in Directory");
			        }
			        flash_spinner_files.setSelection(file_ArrayPosition);
					break;
				
				}
			}
		});
    }
    
    public static void setFlashStatusText(final String text, final boolean isLastMessage){
    	mActivity.runOnUiThread(new Runnable() {
			public void run() {
				if(!mActivity.isFlashingDialogShowing){
					mActivity.isFlashSuccessful = false;
					mActivity.showDialogFlashing();
				}
				mActivity.tv_flashDialog.setText(text);
				System.out.println("isLastMessage " + isLastMessage);
				if(isLastMessage){
		        	mActivity.btn_flashDialog.setVisibility(View.VISIBLE);
					mActivity.pb_flashDialog.setVisibility(View.GONE);
				}
			}
		});
    }
    
    private ArrayList<File> getFilesFromStorage() {
    	ArrayList<File> fileArrayList = new ArrayList<File>();
       File parentDir = new File(Environment.getExternalStorageDirectory() + KADAVER_FOLDER);
       if(!parentDir.exists()){
    	   parentDir.mkdirs();
       }
       ArrayList<File> inFiles = new ArrayList<File>();
       File[] files = parentDir.listFiles();
       if(files != null){
	       Arrays.sort(files, new Comparator<File>(){
	           public int compare(File f1, File f2)
	           {
	               return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
	           } 
	       });
       
	       for (File file : files) {
	           
	//      if(file.getName().endsWith(".csv")){
	          fileArrayList.add(file);
	//      }
	           
	       }
	      
       }
       return fileArrayList;
   }
    private void createKadaverFolder(){
	    File parentDir = new File(Environment.getExternalStorageDirectory() + KADAVER_FOLDER);
	    if(!parentDir.exists()){
	 	   parentDir.mkdirs();
	    }
    }
    
    private void showStatusTexts(boolean show){
    	if(show){
    		mStatus1.setVisibility(View.VISIBLE);
            mStatus2.setVisibility(View.VISIBLE);
            mStatus2.setText(stringChannel);
    	} else {
    		mStatus1.setVisibility(View.INVISIBLE);
            mStatus2.setVisibility(View.INVISIBLE);
    	}
    }

    private void connectToBluetoothDevice(){
    	if(prefs != null){
			btDeviceAddress = prefs.getString(PREF_BLUETOOTH_DEVICE_MAC, null);
		}
		if(btDeviceAddress == null){
        	Toast.makeText(ActivityMain.this, "Select a Device first", Toast.LENGTH_LONG).show();
        } else {
        	if(mBTService != null){
			    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(btDeviceAddress);
			    // Attempt to connect to the device
			    mBTService.connect(device, false);
        	}
        }
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	System.out.println("On START:" );
    	if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mBTService == null){
            	 // Initialize the BluetoothService to perform bluetooth connections
            	
                mBTService = new BluetoothService(this, mHandler);

                // Initialize the buffer for outgoing messages
                mOutStringBuffer = new StringBuffer("");
                connectToBluetoothDevice();
            }
        }
    	
        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (mBTService != null) {
            if (mBTService.getState() == BluetoothService.STATE_NONE) {
              mBTService.start();
            }
        }
    	if(prefs != null){
        	btDeviceAddress = prefs.getString(PREF_BLUETOOTH_DEVICE_MAC, null);
        	websocketURL = prefs.getString(PREF_WEBSOCKET_URL, PREF_WEBSOCKET_URL_DEFAULT);
        	defaultEmailReceiver = prefs.getString(PREF_EMAIL_RECEIVER, "");
        	usePhoneNumber = prefs.getBoolean(PREF_USE_PHONE_NUMBER, false);
        	phoneNumber = prefs.getString(PREF_PHONE_NUMBER, "");
        }
    	if(usePhoneNumber){
        	if(phoneNumber.equals("")){
        		Toast.makeText(this, "Enter specific number (phone number)", Toast.LENGTH_SHORT).show();
        	} else {
        		stringChannel = phoneNumber;
        	}
        }
    }
    
    @Override
    protected void onDestroy() {
    	this.unregisterReceiver(mReceiver);
    	if (mBTService != null) mBTService.stop();
    	if(mConnection != null && mConnection.isConnecting()) mConnection.close();
    	if(mConnection != null && mConnection.isOpen()) mConnection.close();
    	System.exit(0);
    	super.onDestroy();
    	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        if(isScreenOn){
        	menu.getItem(0).setIcon(R.drawable.light1);
        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
        	menu.getItem(0).setIcon(R.drawable.light0);
        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        int id = item.getItemId();
        switch(id){
            
    	case R.id.settings_menu:
	        // Launch the DeviceListActivity to see devices and do scan
	        Intent serverIntent = new Intent(this, ActivitySettings.class);
	        startActivity(serverIntent);
	        return true;
        
        
	    case R.id.flash_menu:
	    	flash_menu_item = item;
	        if(selectedLayout == LAYOUT_KADAVER){
	        	showDialogFlashDongle();
	        } else {
	        	setUpKadaverLayout();
	        }
	        
	        return true;
	        
	    case R.id.keep_screen_on_menu:
//	    	keep_screen_on_menu_item = item;
	        if(isScreenOn == true){
	        	isScreenOn = false;
	        	item.setIcon(R.drawable.light0);
	        	editor.putBoolean(PREF_IS_SCREEN_ON, isScreenOn);
	        	editor.commit();
	        	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        } else {
	        	item.setIcon(R.drawable.light1);
	        	isScreenOn = true;
	        	editor.putBoolean(PREF_IS_SCREEN_ON, isScreenOn);
	        	editor.commit();
	        	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        }
	        
	        return true;
	    }
        return false;
    }
    
    
   
    
        
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(ActivitySettings.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mBTService.connect(device, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
            	connectToBluetoothDevice();
            } else {
                // User did not enable Bluetooth or an error occured
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void showDialogFlashDongle(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
	    builder.setTitle("Do you want to select the flash menu?");
	    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setUpOOBDFlashLayout();
			}
		});
	    builder.setNegativeButton("No", null);
		
	    AlertDialog alert = builder.create();
	    alert.show();
    
    }
    
    private void showDialogFlashing(){
        	System.out.println("SHOW FLASH DIALOG");
    		flashingDialog = new Dialog(this);
    		
    		flashingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		flashingDialog.setContentView(R.layout.flash);
//    		Drawable background = getResources().getDrawable(R.drawable.dialog);
//    		flashingDialog.getWindow().setBackgroundDrawable(background);
    		flashingDialog.setCanceledOnTouchOutside(false);
    		flashingDialog.setCancelable(false);
    		 WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    		    lp.copyFrom(flashingDialog.getWindow().getAttributes());
    		    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
    		    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
    		   
    		
    		
    		btn_flashDialog = (Button) flashingDialog.findViewById(R.id.btn_flashdialog);
    		pb_flashDialog = (ProgressBar) flashingDialog.findViewById(R.id.pb_flashdialog);
    		
    		// if button is clicked, close the custom dialog
    		btn_flashDialog.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				flashingDialog.dismiss();
    				isFlashingDialogShowing = false;
    				setUpKadaverLayout();
    			} 
    			
    		});
    		
    		tv_flashDialog = (TextView) flashingDialog.findViewById(R.id.tv_flashdialog);

    		
    	
    		flashingDialog.show();
    		flashingDialog.getWindow().setAttributes(lp);
    		isFlashingDialogShowing = true;
    }
    
    
    private void setUpOOBDFlashLayout(){
    	flash_menu_item.setIcon(R.drawable.kadaver_actionbar);
    	isFlashSuccessful = false;
    	selectedLayout = LAYOUT_OOBDFLASH;
    	getSupportActionBar().setTitle("Firmware Flash");
    	getSupportActionBar().setIcon(R.drawable.icon_oobdflash);
    	rl_mainscreen.setBackgroundResource(R.drawable.background_oobdflash);
    	if(mConnection != null){
    		mConnection.close();
    	}
    	flash_rl.setVisibility(View.VISIBLE);
    	flash_shadow1.setVisibility(View.VISIBLE);
    	flash_shadow2.setVisibility(View.VISIBLE);
    	mStatus1.setVisibility(View.GONE);
    	mStatus2.setVisibility(View.GONE);
//    	mBTStatus.setVisibility(View.GONE);
    	LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
    	p.weight = 0;
    	
    	iv_world.setVisibility(View.GONE);
    	
    	ll_pv2.setVisibility(View.GONE);
    	iv_world.setLayoutParams(p);
    	ll_pv2.setLayoutParams(p);
    	p.weight=4;
    	iv_car.setLayoutParams(p);
    	iv_smartphone.setLayoutParams(p);
//    	p.weight = 4;
    	ll_pv1.setLayoutParams(p);
    	if(isBTConnected){
    		changeSmartphoneImage(STATE_CONNECTED_GREEN);
    	} else {
    		changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
    	}
    }
    
    private void setUpKadaverLayout(){
    	if(!isBTConnected){
    		connectToBluetoothDevice();
    	}
    	flash_menu_item.setIcon(R.drawable.oobdflash);
    	selectedLayout = LAYOUT_KADAVER;
    	getSupportActionBar().setTitle("Kadaver");
    	getSupportActionBar().setIcon(R.drawable.icon);
    	rl_mainscreen.setBackgroundResource(R.drawable.background_kadaver);
    	flash_rl.setVisibility(View.GONE);
    	flash_shadow1.setVisibility(View.GONE);
    	flash_shadow2.setVisibility(View.GONE);
    	if(isBTConnected){
	    	mStatus1.setVisibility(View.VISIBLE);
	    	mStatus2.setVisibility(View.VISIBLE);
    	} 
    	iv_world.setVisibility(View.VISIBLE);
    	ll_pv2.setVisibility(View.VISIBLE);
    	pv_3.setVisibility(View.INVISIBLE);
    	pv_4.setVisibility(View.INVISIBLE);

    	iv_world.setLayoutParams(lp_world);
    	iv_car.setLayoutParams(lp_car);

    	iv_smartphone.setLayoutParams(lp_smartphone);
    	ll_pv2.setLayoutParams(lp_pv1);
    	ll_pv1.setLayoutParams(lp_pv2);
    	if(isBTConnected){
    		changeSmartphoneImage(STATE_CONNECTED_GREEN);
    	} else {
    		changeSmartphoneImage(STATE_NOT_CONNECTED_RED);
    	}
    	
    }
    
    private void startWebsocketConnection() {
    	mConnection = new KadaverWebSocketClient(URI.create(websocketURL));
    }

	

	
	private void receivedFromWS(String evt) {
		Log.d(TAG, "wsOnMessage " + evt);
		eventFlowControl(WSRECEIVE,evt);
		try {
			JSONObject obj = new JSONObject(evt);
			if (obj.has("msg") && !decode(obj.getString("msg")).equalsIgnoreCase("")) {
				Log.d(TAG, "wsOnMessage " + evt);
				sendToBT(decode(obj.getString("msg")));
				eventFlowControl(BTSEND, decode(obj.getString("msg"))); 
			}
		} catch (Exception e) {
			eventFlowControl(JSONERRORRECV,null);
			e.printStackTrace();
		}
	}

	private void sendToWS(String message) {
		JSONObject msg = new JSONObject();
		try {
			msg.put("reply", encode(message));
			msg.put("channel",encode(stringChannel));
			
		} catch (JSONException e) {
			eventFlowControl(JSONERRORRECV,null);
			e.printStackTrace();
		}
		
		
		eventFlowControl(WSSEND,msg.toString());
		System.out.println("Kadaver: 3 Kadaver -> WS: " + msg.toString());
		mConnection.write(msg.toString());
	}
	
	
	public String decode(String s) {
	    try {
			return new String(Base64.decode(s.getBytes(),Base64.NO_WRAP),"UTF-8") ;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	public String encode(String s) {
	    return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP);
	}

	 private void sendToBT(String message) {
	        if (mBTService.getState() != BluetoothService.STATE_CONNECTED) {
	            return;
	        }
	        
	        if (message != null && message.length() > 0) {
	            byte[] send = message.getBytes();
	            mBTService.write(send);
	            eventFlowControl(BTSEND, message);

	            mOutStringBuffer.setLength(0);
	        }
	    }
	
    
	
	Handler handler = new Handler();
    
	
    class HideBarberPole extends Thread{
    	int pole;
    	public void setPole(int pole){
        	this.pole = pole;
        }
    	
    	@Override
    	public void run() {
    		super.run();
    		switch (pole) {
    		case 1:
    			pv_1.setVisibility(View.INVISIBLE);
    			break;
    		case 2:
    			pv_2.setVisibility(View.INVISIBLE);
    			break;
    		case 3:
    			pv_3.setVisibility(View.INVISIBLE);
    			break;
    		case 4:
    			pv_4.setVisibility(View.INVISIBLE);
    			break;
    	    }
    	}
    }
       
    
    
	private void triggerBarberPole(int pole){
		
		HideBarberPole hideBarberPole = new HideBarberPole();
	    hideBarberPole.setPole(pole);
	    switch (pole) {
		case 1:
			pv_1.setVisibility(View.VISIBLE);
			break;
		case 2:
			pv_2.setVisibility(View.VISIBLE);
			break;
		case 3:
			pv_3.setVisibility(View.VISIBLE);
			break;
		case 4:
			pv_4.setVisibility(View.VISIBLE);
			break;
	    }
		 handler.postDelayed(hideBarberPole, 500);
	}
	
	public void showStatusLine(String line){
		mStatus1.setText(line);
	}
	
	public void normalOperation() {
		showStatusLine("Connection Number");
		mStatus2.setText(stringChannel);
		startService();
	}
	
	public void startService() {
		Log.d(TAG,"Starting...");
//		sendMessage(Base64.encode("\r".getBytes(), Base64.DEFAULT));
		sendToBT("\r");
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				sendToBT("p 0 0 0 \r");
//				sendMessage(Base64.encode("p 0 0 0 \r".getBytes(), Base64.DEFAULT));
			}
		};
		
		handler.postDelayed(r, 200);
//		setTimeout(function() {
//
//			sendMessage(str2ab("p 0 0 0\r"));
//		}, 200);
	}
	
//	public int[] str2ab(String str) {
//		int[] buf = new int[str.length()];
////		int[] bufView = new Uint8Array(buf);
//		for (int i = 0, strLen = str.length(); i < strLen; i++) {
//			buf[i] = str.charAt(i);
//		}
//		return buf;
//	}
	
	private void changeCarImage(int state){
		switch(state){
		case STATE_CONNECTED_GREEN:
			iv_car.setImageDrawable(getResources().getDrawable(R.drawable.car_green));
			isBTConnected = true;
			break;
		case STATE_CONNECTING_YELLOW:
			iv_car.setImageDrawable(getResources().getDrawable(R.drawable.car_yellow));
			break;
		case STATE_NOT_CONNECTED_RED:
			iv_car.setImageDrawable(getResources().getDrawable(R.drawable.car_red));
			isBTConnected = false;
			break;
		}
	}
	
	private void changeSmartphoneImage(int state){
		if(selectedLayout== LAYOUT_KADAVER){
		switch(state){
		case STATE_CONNECTED_GREEN:
			iv_smartphone.setImageDrawable(getResources().getDrawable(R.drawable.smartphone_green));
			break;
		case STATE_CONNECTING_YELLOW:
			iv_smartphone.setImageDrawable(getResources().getDrawable(R.drawable.smartphone_yellow));
			break;
		case STATE_NOT_CONNECTED_RED:
			iv_smartphone.setImageDrawable(getResources().getDrawable(R.drawable.smartphone_red));
			break;
		}
		} else {
		switch(state){
		case STATE_CONNECTED_GREEN:
			iv_smartphone.setImageDrawable(getResources().getDrawable(R.drawable.smartphone_flash_green));
			break;
		case STATE_CONNECTING_YELLOW:
			iv_smartphone.setImageDrawable(getResources().getDrawable(R.drawable.smartphone_flash_yellow));
			break;
		case STATE_NOT_CONNECTED_RED:
			iv_smartphone.setImageDrawable(getResources().getDrawable(R.drawable.smartphone_flash_red));
			break;
		}
		}
	}
	
	private void changeWorldImage(int state){
		switch(state){
		case STATE_CONNECTED_GREEN:
			iv_world.setImageDrawable(getResources().getDrawable(R.drawable.world_green));
			isWSConnected = true;
			break;
		case STATE_CONNECTING_YELLOW:
			iv_world.setImageDrawable(getResources().getDrawable(R.drawable.world_yellow));
			break;
		case STATE_NOT_CONNECTED_RED:
			iv_world.setImageDrawable(getResources().getDrawable(R.drawable.world_red));
			isWSConnected = false;

			break;
		}
	}
	
	class WebSocketThread extends Thread {
		@Override
		public void run() {
			startWebsocketConnection();
			super.run();
		}
	}
	
	public class KadaverWebSocketClient extends WebSocketClient {

		boolean isOpen= false;
		String channel;
	    URI wsURI;
	    String Server;
	    String protocol;
	    String proxyHost;
	    int proxyPort;
	    Proxy proxy;

	    public KadaverWebSocketClient(java.net.URI wsURL, Proxy proxy, String proxyHost, int proxyPort) {
	    	
	        super(wsURL);
	        if (proxy != Proxy.NO_PROXY) {
	            System.out.println("use proxy..");
	        }
	        if(proxy!=null){
	        this.setProxy(proxy);
	        this.proxy = proxy;
	        }
	        this.wsURI = wsURL;
	        this.proxyHost = proxyHost;
	        this.proxyPort = proxyPort;
	        String[] parts = wsURL.toString().split("@");
	        Server = wsURL.toString();
	        parts = parts[0].split("://");
	        protocol = parts[0];
	        channel = parts[1];
	        System.out.println("PROXY 1: " + proxy + "  " + wsURL + "   " + proxyHost + "   " + proxyPort);
	        System.out.println("PROXY 2: " + protocol + "  " + channel );
	        
	        
	    }
	    
	    public KadaverWebSocketClient(java.net.URI wsURL) {
	    	
	        super(wsURL);
	        
	        this.proxy = null;
	        
	        this.wsURI = wsURL;
	        this.proxyHost = null;
	        this.proxyPort = 0;
	        channel = stringChannel;
	        protocol = websocketURL.split("://")[0];
	        
	        connect(protocol);
	    }
	    


	    public void connect(String connect) {
	       
	        WebSocketImpl.DEBUG = true;
	        
	        runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					changeWorldImage(STATE_CONNECTING_YELLOW);
				}
			});
        	System.out.println("Response Code URL: " + websocketURL + " protocol: " + protocol + "  "  + wsURI.getHost());

	        if ("wss".equalsIgnoreCase(protocol)) {
				
				
				try {
					
//					SSLContext sslContext = Utils.getSSLContextForSubDomain(ActivityMain.this);
					SSLContext sslContext = Utils.getSSLContextForWebsocketWSS(ActivityMain.this);
					SSLSocketFactory factory = sslContext.getSocketFactory();
					Socket s = null;
	                if(proxy!=null){
	                	s = new Socket(proxy); 
	                }
	                else {
	                	s = new Socket(); 
	                }
	                int port = uri.getPort();
	                if (port == -1) {
	                    String scheme = uri.getScheme();
	                    if (scheme.equals("wss")) {
	                        port = WebSocket.DEFAULT_WSS_PORT;
	                    } else if (scheme.equals("ws")) {
	                        port = WebSocket.DEFAULT_PORT;
	                    } else {
	                        throw new RuntimeException("unkonow scheme" + scheme);
	                    }
	                }
//	                String host = "oobd.luxen.de";
//	                port = 9001;
//	                s.connect(new InetSocketAddress(host, port), 10000);
//	                
//	                setSocket(factory.createSocket(s, host, port, true));
	                s.connect(new InetSocketAddress(wsURI.getHost(), port), 10000);
	                
	                setSocket(factory.createSocket(s, wsURI.getHost(), port, true));

	                
	                attachShutDownHook();
	                connectBlocking();
	                
	                

	            } catch (final IOException ex) {
	            	eventFlowControl(WSDISCONNECTED,ex.getLocalizedMessage());
	                Logger.getLogger(KadaverWebSocketClient.class.getName()).log(Level.SEVERE, null, ex);
	               
	            } 
				  catch (final InterruptedException ex) {
					  eventFlowControl(WSDISCONNECTED,ex.getLocalizedMessage());
	                Logger.getLogger(KadaverWebSocketClient.class.getName()).log(Level.SEVERE, null, ex);
	                
	            } 

	        } else {
	        	try {
		        	System.out.println("Response Code: " + 1);

	        	SSLContext context = Utils.getSSLContextForSubDomain(ActivityMain.this);
	        	
	        	HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
	        	System.out.println("Response Code: " + 11);

	        	// connect to url
	        	URL u= wsURI.toURL();
	        	System.out.println("Response Code: " + 21);

	        	HttpsURLConnection urlConnection = (HttpsURLConnection) u.openConnection();
	        	System.out.println("Response Code: " + 22);
	        	urlConnection.setSSLSocketFactory(context.getSocketFactory());
	        	System.out.println("Response Code: " + 23);
//	        	urlConnection.setConnectTimeout(timeoutMillis);
	        	urlConnection.setHostnameVerifier(hostnameVerifier);
	        	System.out.println("Response Code: " + 24);
	        	urlConnection.connect();
	        	System.out.println("Response Code: 3 " + urlConnection.getResponseCode());
	        	} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }

	    }

	    public boolean available() {
	        return isOpen();
	    }

	    public String connectInfo() {
	        if (isOpen()) {
	            return "Remote Connect to " + Server;
	        } else {
	            return null;
	        }
	    }

	   
	    public void attachShutDownHook() {
	        Runtime.getRuntime().addShutdownHook(new Thread() {

	            @Override
	            public void run() {
	                System.err.println("Inside Add Shutdown Hook");
	                close();
	                System.err.println("Websocket closed");
	            }
	        });
	        System.err.println("Shut Down Hook Attached.");

	    }

	    public void onMessage(final String message) {
	    	receivedFromWS(message);
	    	eventFlowControl(WSRECEIVE, message);
//	    	runOnUiThread(new Runnable() {
//				
//				@Override
//				public void run() {
//					Toast.makeText(ActivityMain.this, "RECEIVED: " + message, Toast.LENGTH_SHORT).show();
//				}
//			});

	    }

	    public synchronized void write(final String s) {
	    	
	        try {
	            
	            if(isOpen()){
	            	eventFlowControl(WSSEND, s);
	            	this.send(s);
	            }

	        } 

	        catch (NotYetConnectedException ex) {
	            Logger.getLogger(KadaverWebSocketClient.class.getName()).log(Level.WARNING,
	                    null, ex);
	        }
	    }

	    @Override
	    public void onOpen(ServerHandshake handshakedata) {
	    	Log.d(TAG, "wsOnOpen ");
	        isWSConnected = true;
	        eventFlowControl(WSCONNECTED,null);

	    }

	    @Override
	    public void onClose(int code, String reason, boolean remote) {
	    	Log.d(TAG, "wsOnClose " + reason);
//	    	super.close();
	    	eventFlowControl(WSDISCONNECTED,null);
	    }

//	    @Override
//	    public void onError(final Exception ex) {
//	    	super
//	    	Log.d(TAG, "wsOnError:  "  + ex.getLocalizedMessage());
//	        
//	    }

	    
	    public int adjustTimeOut(int originalTimeout) {
	        // as the ws- based time could be much longer as a direct connection, we multiply the normal time
	        return originalTimeout * 1;
	    }

		@Override
		public void onError(Exception ex) {
			
			// TODO Auto-generated method stub
			
		}
	}
	
}
