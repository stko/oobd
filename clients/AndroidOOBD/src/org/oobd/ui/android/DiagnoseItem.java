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
		System.out.println("Creatin new Diagnose Item...");
		this.myVisualizer = viz;
		this.myOnion = onion;
	}
	

	public String getFunctionValue() {
		System.out.println("New Function Value: " + myVisualizer.toString());
		return myVisualizer.toString();
	}

	public String getFunctionName() {
		System.out.println("Function Name: " + myVisualizer.getToolTip());
		return myVisualizer.getToolTip();
	}

	public String getFunctionFlag1() {
		return "A";
	}

	public String getFunctionFlag2() {
		return "B";
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
