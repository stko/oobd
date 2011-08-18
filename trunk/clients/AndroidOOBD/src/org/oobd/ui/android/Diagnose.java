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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.os.Message;
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
	private Handler myTimerHandler = new Handler();
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			System.out.println("Tick:"+Integer.toString((int) SystemClock.uptimeMillis()));
			if (myTimerButton.isChecked()) {
				refreshView(OOBDConstants.VE_TIMER);
				myTimerHandler.postAtTime(this,
						SystemClock.uptimeMillis() + OOBDConstants.LV_UPDATE);
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
					/* Refresh UI */
					((DiagnoseAdapter) mDiagnoseListView.getAdapter())
							.notifyDataSetChanged();
					break;
				case 2:
					/* Refresh UI */
					myDiagnoseInstance.setTitle(msg.obj.toString());
					break;
				}
			}
		};
		((ImageButton) findViewById(R.id.updateButton))
				.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						refreshView(OOBDConstants.VE_UPDATE);
					}
				});
		myTimerButton = (ToggleButton) findViewById(R.id.timerButton);
		myTimerButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (((ToggleButton) v).isChecked()) {
					myTimerHandler.removeCallbacks(mUpdateTimeTask);
					myTimerHandler.postDelayed(mUpdateTimeTask,  OOBDConstants.LV_UPDATE);
				}
			}
		});
		Drawable d = getResources().getDrawable(R.drawable.timer);
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		((ToggleButton) findViewById(R.id.timerButton)).setCompoundDrawables(
				null, d, null, null);
		registerForContextMenu(mDiagnoseListView);

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
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		int ListItemIndex = info.position;
		return true;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		DiagnoseItem selectedItem = mDiagnoseItems.get(position);

		Visualizer myVisualizer = selectedItem.getMyVisualizer();
		myVisualizer.updateRequest(OOBDConstants.UR_USER);
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

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("Diagnose.onPause(): unregister Broadcast Receiver");
		mDiagnoseAdapter.guiPaused();
	}

	@Override
	protected void onResume() {
		super.onPause();
		System.out.println("Diagnose.onResume(): register Broadcast Receiver");
		mDiagnoseAdapter.guiResumed();
	}

	protected void refreshView(int bitNr) {
		synchronized (mDiagnoseItems) {
			if (mDiagnoseItems != null) {

				for (int i = mDiagnoseListView.getFirstVisiblePosition(); i <= mDiagnoseListView
						.getLastVisiblePosition(); i++) {
					DiagnoseItem selectedItem = (DiagnoseItem) mDiagnoseListView
							.getItemAtPosition(i);
					Visualizer myVisualizer = selectedItem.getMyVisualizer();
					if (myVisualizer.getUpdateFlag(bitNr)) {
						myVisualizer.updateRequest(OOBDConstants.UR_UPDATE);
					}
				}
			}
		}
	}
}
