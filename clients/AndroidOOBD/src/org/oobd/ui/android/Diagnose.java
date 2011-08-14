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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.os.Message;
/**
 * @author Andreas Budde, Peter Mayer
 * Core of the app. Communicates with Lua OBD Engine and displays its results in a list view that contains {@link DiagnoseItem}
 */
public class Diagnose extends ListActivity  {
	
	
	
	private ListView mDiagnoseListView;
	public static VizTable mDiagnoseItems; // diese Liste updaten
	private static DiagnoseAdapter mDiagnoseAdapter;
	public static Diagnose myDiagnoseInstance = null;
	public static Handler myRefreshHandler;
	
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
		//mDiagnoseItems = OOBDApp.getInstance().getDiagnoseItems("INIT");
		
		mDiagnoseItems = (VizTable) VizTable.getInstance("", ""); 
		
		mDiagnoseAdapter = new DiagnoseAdapter(Diagnose.this, mDiagnoseItems);
		mDiagnoseListView.setAdapter(mDiagnoseAdapter);
		
		System.out.println("Anzahl Listenelemente: " + mDiagnoseListView.getCount());
		
		myRefreshHandler= new Handler(){
			@Override
			public void handleMessage(Message msg) {
			switch(msg.what){
		     case 1:
		            /*Refresh UI*/
		    	 ((DiagnoseAdapter) mDiagnoseListView.getAdapter()).notifyDataSetChanged();
		            break;
		     case 2:
		            /*Refresh UI*/
		    	 myDiagnoseInstance.setTitle(msg.obj.toString());
		    	 break;
			   }
			}
			};

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
		this.mDiagnoseAdapter = mDiagnoseAdatper;
	}
	
	public void setItems(VizTable data) {
		myRefreshHandler.sendEmptyMessage(1);
		}
	public void setMenuTitle(String title) {
		myRefreshHandler.sendMessage( Message.obtain(myRefreshHandler, 2, title));
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
	
	
}
