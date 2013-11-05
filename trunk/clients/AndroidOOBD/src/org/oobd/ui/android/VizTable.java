package org.oobd.ui.android;

import java.util.ArrayList;

import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.application.OOBDApp;

import android.content.Intent;

//public class VizTable extends ArrayList<DiagnoseItem> implements IFvisualizer {
	public class VizTable extends ArrayList<Visualizer> implements IFvisualizer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static VizTable thisVizTable = null;;
	boolean awaitingUpdate = false;
     boolean removalState = false;
	
	public static VizTable getInstance(Onion myOnion) {
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
		//DiagnoseItem item = new DiagnoseItem ();
		//item.initValue(viz, onion);
		//this.add(item);
		this.add(viz);
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
        removalState = true;
//		for(DiagnoseItem item: this){
//			item.getVisualizer().setRemove();
			for(Visualizer item: this){
				item.setRemove();
		}
		
	}

	public Visualizer getVisualizer() {
		// TODO Auto-generated method stub
		return null;
	}

}
