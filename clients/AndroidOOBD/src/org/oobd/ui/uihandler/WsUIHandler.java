/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.ui.uihandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.Core;
import org.oobd.base.IFsystem;
import org.oobd.base.uihandler.WSOobdUIHandler;
import org.oobd.base.OOBDConstants;
import org.oobd.base.visualizer.Visualizer;

/**
 *
 * @author steffen
 */
public class WsUIHandler extends WSOobdUIHandler {

    private WsUIHandler myself;

    public WsUIHandler(String id, Core myCore, IFsystem mySystem) {
        super(id, myCore, mySystem, "Swing Desk UI id " + id);
        Logger.getLogger(WsUIHandler.class.getName()).log(Level.CONFIG,
                "Construct Swing Desk UI instance " + id);
        myself = this;

    }

    @Override
    public String getPluginName() {
        return "uh:ws-swing";
    }

    public static String publicName() {
        return UIHANDLER_WS_NAME;
    }

  

    @Override
    public void addVisualizer(String owner, Visualizer vis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
