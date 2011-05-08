/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.connector;
import org.oobd.base.*;

/**
 * generic abstract for the implementation of connectors
 * @author steffen
 */
public abstract class OobdConnector extends OobdPlugin{

    public OobdConnector(String name){
        super(name);
    }

}
