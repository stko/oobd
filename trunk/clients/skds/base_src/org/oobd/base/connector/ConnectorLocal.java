/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.connector;

import org.oobd.base.*;

/**
 *
 * @author steffen
 */
public class ConnectorLocal extends OobdConnector implements OOBDConstants {

    public ConnectorLocal() {
        Debug.msg("connectorlocal",DEBUG_BORING,"Ich bin der ConnectorLocal...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("connectorlocal",DEBUG_BORING,"Core registered...");
    }

    @Override
    public String getPluginName() {
        return "c:Local";
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
