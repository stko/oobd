/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

import org.oobd.base.Core;

/**
 * This interface defines the interface between the core and the enviroment for IOs and other system related stuff
 * @author steffen
 */
public interface IFsystem {

    public void register(Core core);
    public void loadConnectors();
}
