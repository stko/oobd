/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.core.db;


import org.oobd.core.OobdPlugin;

/**
 * generic abstract for the implementation of busses
 * @author steffen
 */
public abstract class OobdDB extends OobdPlugin{

    public OobdDB(String name){
        super(name);
    }
    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "";
    }

}
