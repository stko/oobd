/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

/**
 * Base class for all oobd classes loadable during runtime
 * @author steffen
 */
public abstract class OobdPlugin {
    
    static Core core;

    public void registerCore(Core thisCore){
        core=thisCore;
    }
    abstract public String getPublicName();
}
