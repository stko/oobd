/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.core.connector;
import org.oobd.core.OobdPlugin;

/**
 * generic abstract for the implementation of connectors
 * @author steffen
 */
public abstract class OobdConnector extends OobdPlugin{

    public OobdConnector(String name){
        super(name);
    }

}
