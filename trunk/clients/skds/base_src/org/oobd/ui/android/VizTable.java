package org.oobd.ui.android;

import java.util.ArrayList;

import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.application.OOBDApp;

import android.content.Intent;

public class VizTable extends ArrayList<DiagnoseItem> implements IFvisualizer {

	public static VizTable thisVizTable = null;;
	boolean awaitingUpdate = false;
	
	public static IFvisualizer getInstance(String pageID, String vizName) {
		if (thisVizTable == null)
			thisVizTable = new VizTable();
		return thisVizTable;
	}
	
	public VizTable() {
		super();
	}
	
	@Override
	public boolean isGroup() {
		// TODO Auto-generated method stub
		System.out.println("Method VizTable.isGroup() not implemented.");
		return false;
	}

	@Override
	public void setVisualizer(Visualizer viz) {
		// TODO Auto-generated method stub
		System.out.println("Method Bin in VizTable.setVisualizer() not yet implemented");
		
	}

	@Override
	public void initValue(Visualizer viz, Onion onion) {
		DiagnoseItem item = new DiagnoseItem (viz, onion);
		this.add(item);
		System.out.println("ListArray Elemente: " + this.size());
	}

	@Override
	public boolean update(int level) {
		System.out.println("VizTable: received update level: " + level);
		switch (level) {
	        case 0: {
	            awaitingUpdate = true;
	        }
	        case 2: {
				System.out.println("Sende Broadcast Event...");
				Intent broadcast=new Intent(OOBDApp.VISUALIZER_UPDATE);
				broadcast.putExtra(OOBDApp.UPDATE_LEVEL, Integer.toString(level));
				Diagnose.myDiagnoseInstance.getApplicationContext().sendBroadcast(broadcast);
				awaitingUpdate = false;
				return true;
			}
	    }
		return false;
	}

	@Override
	public void remove(String pageID) {
		// TODO Auto-generated method stub
		System.out.println("Method VizTable.remove() not implemented.");
		
	}

}
