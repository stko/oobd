/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.protocol;

import org.oobd.core.OOBDConstants;
import org.oobd.core.Core;

/**
 *
 * @author steffen
 */
public class ProtocolUDS extends OobdProtocol implements OOBDConstants{

    public ProtocolUDS() {
        super("ProtocolUDS");
    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
    }

    @Override public String getPluginName(){
        return "p:UDS";
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
