/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.ui.uihandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.Core;
import org.oobd.base.IFsystem;
import org.oobd.base.uihandler.OobdUIHandler;
import org.oobd.base.OOBDConstants;

/**
 *
 * @author steffen
 */
public class UIHandler extends OobdUIHandler {

    private UIHandler myself;

    public UIHandler(String id, Core myCore, IFsystem mySystem) {
        super(id, myCore, mySystem, "Swing Desk UI id " + id);
        Logger.getLogger(UIHandler.class.getName()).log(Level.CONFIG,
                "Construct Swing Desk UI instance " + id);
        myself = this;

    }

    @Override
    public String getPluginName() {
        return "uh:android";
    }

    public static String publicName() {
        return OOBDConstants.UIHandlerMailboxName;
    }

    
	public void run() {
		// TODO Auto-generated method stub
		
	}

  
}
