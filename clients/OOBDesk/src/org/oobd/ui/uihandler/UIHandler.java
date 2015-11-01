
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.ui.uihandler;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.Core;
import org.oobd.base.IFsystem;
import org.oobd.base.uihandler.LocalOobdUIHandler;
import org.oobd.base.OOBDConstants;

/**
 *
 * @author steffen
 */
public class UIHandler extends LocalOobdUIHandler {

    private UIHandler myself;

    public UIHandler(String id, Core myCore, IFsystem mySystem) {
        super(id, myCore, mySystem, "Swing Desk UI id " + id);
        Logger.getLogger(UIHandler.class.getName()).log(Level.CONFIG,
                "Construct Swing Desk UI instance " + id);
        myself = this;

    }

    @Override
    public String getPluginName() {
        return "uh:swingdesk";
    }

    public static String publicName() {
        return UIHANDLER_LOCAL_NAME;
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
