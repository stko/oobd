package org.oobd.ui.android;

import java.util.ArrayList;
import android.os.Handler;

import org.oobd.base.Core;
import org.oobd.base.IFui;
import org.oobd.base.OOBDConstants;
import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.application.AndroidGui;
import org.oobd.ui.android.application.OOBDApp;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;

/**
 * @author Andreas Budde, Peter Mayer Core of the app. Communicates with Lua OBD
 *         Engine and displays its results in a list view that contains
 *         {@link DiagnoseItem}
 */
public class Diagnose extends ListActivity {

	private ListView mDiagnoseListView;
	public static VizTable mDiagnoseItems; // diese Liste updaten
	private static DiagnoseAdapter mDiagnoseAdapter;
	public static Diagnose myDiagnoseInstance = null;
	public static Handler myRefreshHandler;
	private ToggleButton myTimerButton;
	private ProgressDialog myProgressDialog;
	private Handler myTimerHandler = new Handler();
	private PowerManager.WakeLock wl;
	 
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			System.out.println("Tick:"+Integer.toString((int) SystemClock.uptimeMillis()));
			if (myTimerButton.isChecked()) {
				refreshView(OOBDConstants.VE_TIMER,OOBDConstants.VE_TIMER);
				myTimerHandler.postDelayed(this,
						OOBDConstants.LV_UPDATE);
			}else{
				myTimerHandler.removeCallbacks(mUpdateTimeTask);
			}
		}
	};

	public static Diagnose getInstance() {
		return myDiagnoseInstance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.diagnose);

		myDiagnoseInstance = this;

		mDiagnoseListView = getListView();

		// receive main menu from Lua Script Engine
		// mDiagnoseItems = OOBDApp.getInstance().getDiagnoseItems("INIT");

		mDiagnoseItems = (VizTable) VizTable.getInstance("", "");

		mDiagnoseAdapter = new DiagnoseAdapter(Diagnose.this, mDiagnoseItems);
		mDiagnoseListView.setAdapter(mDiagnoseAdapter);

		System.out.println("Anzahl Listenelemente: "
				+ mDiagnoseListView.getCount());

		myRefreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					/* Refresh view */
					((DiagnoseAdapter) mDiagnoseListView.getAdapter())
							.notifyDataSetChanged();
					break;
				case 2:
					/* Set Listview Title */
					myDiagnoseInstance.setTitle(msg.obj.toString());
					break;
				case 3:
					/* Set Listview Title */
					myProgressDialog=ProgressDialog.show(Diagnose.this, "",
							msg.obj.toString(), true);
					break;
				case 4:
					/* Stop Progress Dialog */
					if (myProgressDialog!=null){
						myProgressDialog.dismiss();
					}
					break;
				}
			}
		};
		((ImageButton) findViewById(R.id.updateButton))
				.setOnClickListener(new View.OnClickListener() {
					// mark all updateable list item as to be updated
					public void onClick(View v) {
						refreshView(OOBDConstants.VE_UPDATE,OOBDConstants.VE_UPDATE);
					}
				});
		myTimerButton = (ToggleButton) findViewById(R.id.timerButton);
		myTimerButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// start the regular timer tick to refresh items
				if (((ToggleButton) v).isChecked()) {
					myTimerHandler.removeCallbacks(mUpdateTimeTask);
					myTimerHandler.postDelayed(mUpdateTimeTask,  OOBDConstants.LV_UPDATE);
					  wl.acquire();
				}else{
					 wl.release();
				}
			}
		});
		// sweeze the icon into the timer- tooglebutton
		Drawable d = getResources().getDrawable(R.drawable.timer);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		((ToggleButton) findViewById(R.id.timerButton)).setCompoundDrawables(
				null, d, null, null);
		registerForContextMenu(mDiagnoseListView);
		startProgressDialog("Load Script");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		  wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("Toggle Flags");
			menu.add(Menu.NONE, 0, 0, "Update");
			menu.add(Menu.NONE, 1, 1, "Timer");
			menu.add(Menu.NONE, 2, 2, "Log");
			menu.add(Menu.NONE, 3, 3, "Back");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//super.onContextItemSelected(item);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int ListItemIndex = info.position;
		DiagnoseItem myItem=mDiagnoseItems.get(ListItemIndex);
		Visualizer myVisualizer=myItem.getMyVisualizer();
		switch (item.getItemId()){
		case 0:
			myVisualizer.setUpdateFlag(OOBDConstants.VE_UPDATE,!myVisualizer.getUpdateFlag(OOBDConstants.VE_UPDATE));
			break;
		case 1:
			myVisualizer.setUpdateFlag(OOBDConstants.VE_TIMER,!myVisualizer.getUpdateFlag(OOBDConstants.VE_TIMER));
			break;
		case 2:
			myVisualizer.setUpdateFlag(OOBDConstants.VE_LOG,!myVisualizer.getUpdateFlag(OOBDConstants.VE_LOG));
			break;
		case 3:
			myVisualizer.setUpdateFlag(OOBDConstants.VE_BACK,!myVisualizer.getUpdateFlag(OOBDConstants.VE_BACK));
			break;
			
		}
		System.out.println("Sende Broadcast Event...");
		Intent broadcast=new Intent(OOBDApp.VISUALIZER_UPDATE);
		broadcast.putExtra(OOBDApp.UPDATE_LEVEL, 1);
		Diagnose.myDiagnoseInstance.getApplicationContext().sendBroadcast(broadcast);
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		DiagnoseItem selectedItem = mDiagnoseItems.get(position);

		Visualizer myVisualizer = selectedItem.getMyVisualizer();
		myVisualizer.updateRequest(OOBDConstants.UR_USER);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if back- button is pressed
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			synchronized (mDiagnoseItems) {
				if (mDiagnoseItems != null) {
					// searching for list entry which contains the "back"- Flag
					int i;
					for (i = 0; i < mDiagnoseItems.size() && !mDiagnoseItems.get(i).getMyVisualizer().getUpdateFlag(OOBDConstants.VE_BACK); i++) {						
						}
					if (i< mDiagnoseItems.size()){ // a "back"-item found
						mDiagnoseItems.get(i).getMyVisualizer().updateRequest(OOBDConstants.UR_USER); // simulate a user selection of this list item
						return true; // stop further handling of the back-button
					}
				}
			}
	    }
	    return super.onKeyDown(keyCode, event);
	}

	
	public DiagnoseAdapter getmDiagnoseAdatper() {
		return mDiagnoseAdapter;
	}

	public void setmDiagnoseAdatper(DiagnoseAdapter mDiagnoseAdatper) {
		mDiagnoseAdapter = mDiagnoseAdatper;
	}

	public void setItems(VizTable data) {
		myRefreshHandler.sendEmptyMessage(1);
	}

	public void setMenuTitle(String title) {
		myRefreshHandler
				.sendMessage(Message.obtain(myRefreshHandler, 2, title));
	}
	
	public void startProgressDialog(String title) {
		myRefreshHandler
				.sendMessage(Message.obtain(myRefreshHandler, 3, title));
	}

	public void stopProgressDialog() {
		myRefreshHandler
				.sendMessage(Message.obtain(myRefreshHandler, 4, null));
	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("Diagnose.onPause(): unregister Broadcast Receiver");
		if (myTimerButton.isChecked()) {
			 wl.release();
		}

		mDiagnoseAdapter.guiPaused();
	}

	@Override
	protected void onResume() {
		super.onPause();
		System.out.println("Diagnose.onResume(): register Broadcast Receiver");
		if (myTimerButton.isChecked()) {
			  wl.acquire();
		}
		mDiagnoseAdapter.guiResumed();
	}

	protected void refreshView(int bitNr,int updateType) {
		synchronized (mDiagnoseItems) {
			if (mDiagnoseItems != null) {

				for (int i = mDiagnoseListView.getFirstVisiblePosition(); i <= mDiagnoseListView
						.getLastVisiblePosition(); i++) {
					DiagnoseItem selectedItem = (DiagnoseItem) mDiagnoseListView
							.getItemAtPosition(i);
					Visualizer myVisualizer = selectedItem.getMyVisualizer();
					if (myVisualizer.getUpdateFlag(bitNr)) {
						myVisualizer.updateRequest(updateType);
					}
				}
			}
		}
	}
}
