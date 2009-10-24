/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.bus;

import org.oobd.base.Core;

/**
 *
 * @author steffen
 */
public class BusEcho extends OobdBus {

    public BusEcho() {
        System.out.println("Ich bin BusEcho...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        System.out.println("Core registered...");
    }

    @Override
    public String getPublicName() {
        return "b:Echo";
    }
}
