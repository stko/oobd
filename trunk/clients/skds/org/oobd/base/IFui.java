/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

/**
 * Interface to allow the oobd core to talk to the graphical interface
 * @author steffen
 */
public interface IFui {

    /**
     * just a test
     * @todo remove test
     * @param msg
     */
    public void sm(String msg);

    /**
      * tells the UserInterface about the existence of a scriptengine, e.g. to add this to a selection menu
     * @param id the key of the scriptengines hash array where the loaded instances are been stored
     * @param visibleName
     */
    public void announceScriptengine(String id, String visibleName);

}
