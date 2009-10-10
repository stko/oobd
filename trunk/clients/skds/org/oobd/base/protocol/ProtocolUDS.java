/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.protocol;

import org.oobd.base.Core;

/**
 *
 * @author steffen
 */
public class ProtocolUDS extends OobdProtocol{
    public ProtocolUDS(){
        System.out.println("Ich bin der ProtocolUDS...");

    }
    @Override
 public void registerCore(Core thisCore){
    super.registerCore(thisCore);
    System.out.println("Core registered...");
}
}
