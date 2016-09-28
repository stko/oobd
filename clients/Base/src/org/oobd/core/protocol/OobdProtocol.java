/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.core.protocol;


import org.oobd.core.OobdPlugin;

/**
 * generic abstract for the implementation of protocols
 * @author steffen
 */

public abstract class OobdProtocol extends OobdPlugin{
   public  OobdProtocol(String name){
        super(name);
    }


}
