/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.connector;

import org.oobd.base.Core;

/**
 *
 * @author steffen
 */
public class ConnectorLocal extends OobdConnector {

    public ConnectorLocal() {
        System.out.println("Ich bin der ConnectorLocal...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        System.out.println("Core registered...");
    }

    @Override
    public String getPublicName() {
        return "c:Local";
    }
}
