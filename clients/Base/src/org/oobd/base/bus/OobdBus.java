/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.bus;

import org.oobd.base.*;

/**
 * generic abstract for the implementation of busses
 * @author steffen
 */
public abstract class OobdBus extends OobdPlugin{

    public OobdBus(String name){
        super(name);
    }

    public abstract void receiveString (String msg);
    
}
