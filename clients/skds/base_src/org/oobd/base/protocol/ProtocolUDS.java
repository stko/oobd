/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.protocol;

import org.oobd.base.*;

/**
 *
 * @author steffen
 */
public class ProtocolUDS extends OobdProtocol implements OOBDConstants{

    public ProtocolUDS() {
        Debug.msg("protocoluds",DEBUG_BORING,"Ich bin der ProtocolUDS...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("protocoluds",DEBUG_BORING,"Core registered...");
    }

    @Override public String getPluginName(){
        return "p:UDS";
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
