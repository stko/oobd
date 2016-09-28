/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.connector;

import org.oobd.core.OOBDConstants;
import org.oobd.core.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author steffen
 */
public class ConnectorLocal extends OobdConnector implements OOBDConstants {

    public ConnectorLocal (String name) {
        super(name);
        Logger.getLogger(ConnectorLocal.class.getName()).log(Level.CONFIG,  "Construct ConnectorLocal instance "+id);

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
    }

    @Override
    public String getPluginName() {
        return "c:Local";
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
