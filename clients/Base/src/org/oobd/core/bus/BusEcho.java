/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.bus;

import org.oobd.core.OOBDConstants;
import org.oobd.core.Core;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oobd.core.support.Onion;

/**
 *
 * @author steffen
 */
public class BusEcho extends OobdBus implements OOBDConstants {

    protected boolean keepRunning = true;

    public BusEcho(String name) {
        super(name);
        Logger.getLogger(BusEcho.class.getName()).log(Level.CONFIG,  "Construct BusEchoAndroid instance "+id);

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
    }

    @Override
    public String getPluginName() {
        return "b:Echo";
    }

    public void run() {

        throw new UnsupportedOperationException("Not supported yet.");




    }

    @Override
    public void receiveString(String msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
