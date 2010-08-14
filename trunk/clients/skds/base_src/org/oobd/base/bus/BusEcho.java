/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.bus;

import org.oobd.base.*;

/**
 *
 * @author steffen
 */
public class BusEcho extends OobdBus implements OOBDConstants{

    public BusEcho() {
        Debug.msg("busecho",DEBUG_BORING,"Ich bin BusEcho...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("busecho",DEBUG_BORING,"Core registered...");
    }

    @Override
    public String getPluginName() {
        return "b:Echo";
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
