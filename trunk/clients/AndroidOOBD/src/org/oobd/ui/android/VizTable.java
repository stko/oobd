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
	
	public static VizTable getInstance(String pageID, String vizName) {
		if (thisVizTable == null)
			thisVizTable = new VizTable();
		return thisVizTable;
	}
	
	public VizTable() {
		super();
	}
	
	public boolean isGroup() {
		// as the table contains several items, it's a group
		return true;
	}

	public void setVisualizer(Visualizer viz) {
		// TODO Auto-generated method stub
		System.out.println("Method Bin in VizTable.setVisualizer() not yet implemented");
		
	}

	public void initValue(Visualizer viz, Onion onion) {
		DiagnoseItem item = new DiagnoseItem (viz, onion);
		this.add(item);
	}

	public boolean update(int level) {
		switch (level) {
	        case 0: {
	            awaitingUpdate = true;
	        }
	        case 2: {
				Intent broadcast=new Intent(OOBDApp.VISUALIZER_UPDATE);
				broadcast.putExtra(OOBDApp.UPDATE_LEVEL, level);
				Diagnose.myDiagnoseInstance.getApplicationContext().sendBroadcast(broadcast);
				awaitingUpdate = false;
				return true;
			}
	    }
		return false;
	}

	public void setRemove(String pageID) {
		for(DiagnoseItem item: this){
			item.getMyVisualizer().setRemove();
		}
		
	}

}
