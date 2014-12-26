package org.oobd.ui.android;

import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.res.Resources;

import org.oobd.base.OOBDConstants;
import org.oobd.ui.android.R;
import org.oobd.ui.android.application.OOBDApp;

public class DiagnoseTab extends TabActivity {
	private static DiagnoseTab myDiagnoseTabInstance = null;
	private TabHost myTabHost;
	public static Handler myRefreshHandler;
	private Handler connectInfoHandler = new Handler();
	private ConnectInfoTask connectInfoTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diagnosetab);
		myDiagnoseTabInstance = this;
		Resources res = getResources(); // Resource object to get Drawables
		myTabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, Diagnose.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = myTabHost.newTabSpec("Diagnose").setIndicator("Diagnose",
				getResources().getDrawable(R.drawable.oobd_32)).setContent(
				intent);
		myTabHost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, OutputActivity.class);
		spec = myTabHost.newTabSpec("Output").setIndicator("Output",
				getResources().getDrawable(R.drawable.text)).setContent(intent);
		myTabHost.addTab(spec);

		myRefreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 2:
					/* Refresh UI */
					myDiagnoseTabInstance.setTitle("OOBD - "
							+ msg.obj.toString());
					break;
				}
			}
		};

		// this fancy trick seems to be necessary to initialize the output tab
		// the object seems no be created until it's shown first time,
		// which causes a null error when trying to send some output
		myTabHost.setCurrentTab(1);
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// start the status update task
		connectInfoTask = new ConnectInfoTask(
				(TextView) findViewById(R.id.connectView), connectInfoHandler);
		connectInfoHandler
				.postDelayed(connectInfoTask, OOBDConstants.LV_STATUS);
		myTabHost.setCurrentTab(0);
	}

	public void outputToFront() {
		myTabHost.setCurrentTab(1);
	}

	public static DiagnoseTab getInstance() {
		return myDiagnoseTabInstance;
	}

	public void setMenuTitle(String title) {
		myRefreshHandler
				.sendMessage(Message.obtain(myRefreshHandler, 2, title));
	}

	protected void onStop() {
		super.onStop();
		OOBDApp.getInstance().closeHardwareHandle();
	}
}

class ConnectInfoTask implements Runnable {
	TextView label;
	Handler handler;
	String lastResult;

	ConnectInfoTask(TextView myLabel, Handler myHandler) {
		label = myLabel;
		handler = myHandler;
	}

	public void run() {
		handler.postDelayed(this, OOBDConstants.LV_STATUS);
		if (label != null) {
			String thisResult = OOBDApp.getInstance().connectInfo();
			if (thisResult != lastResult) {
				if (thisResult != null) {
					label.setText(thisResult);
				} else {
					label.setText("Not connected");
				}
			}
			lastResult = thisResult;
		}
	}

}
