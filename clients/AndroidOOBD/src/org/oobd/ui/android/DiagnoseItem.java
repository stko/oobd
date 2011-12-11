package org.oobd.ui.android;

import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;

/**
 * @author Andreas Budde, Peter Mayer
 * Item that is returned by the OBDLua Engine and that is displayed as UI element.
 */
public class DiagnoseItem {
	
	
	

	
	private Visualizer myVisualizer; 
	private Onion myOnion;

	
	public DiagnoseItem (Visualizer viz, Onion onion) {
		this.myVisualizer = viz;
		this.myOnion = onion;
	}
	

	public String getFunctionValue() {
		return myVisualizer.toString();
	}

	public String getFunctionName() {
		return myVisualizer.getToolTip();
	}

	public String toString() {
		return getFunctionName();
	}


	public Visualizer getMyVisualizer() {
		return myVisualizer;
	}

	public void setMyVisualizer(Visualizer myVisualizer) {
		this.myVisualizer = myVisualizer;
	}

}
