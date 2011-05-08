/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.bus;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oobd.base.*;
import org.oobd.base.support.Onion;

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
}
