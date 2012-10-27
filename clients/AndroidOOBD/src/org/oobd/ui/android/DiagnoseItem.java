package org.oobd.ui.android;

import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;

/**
 * @author Andreas Budde, Peter Mayer
 * Item that is returned by the OBDLua Engine and that is displayed as UI element.
 */
public class DiagnoseItem {
	
	
	

	
	private Visualizer value; 
	private Onion myOnion;
    boolean toBePlaced = true; //indicates, if the actual instance is already been placed on an canvas or not
    boolean awaitingUpdate = false;
    boolean removalState = false;

	
	public DiagnoseItem () {
		super();
	}
	
	public void initValue (Visualizer viz, Onion onion) {
		this.value = viz;
		this.myOnion = onion;
	}
	

	public String getFunctionValue() {
		return value.toString();
	}

	public String getFunctionName() {
		return value.getToolTip();
	}

	public String toString() {
		return getFunctionName();
	}


	public Visualizer getVisualizer() {
		return value;
	}

	public void setVisualizer(Visualizer myVisualizer) {
		this.value = myVisualizer;
	}

    public void setRemove(String pageID) {
        removalState = true;
        value.setRemove();
    }

 

	
}
