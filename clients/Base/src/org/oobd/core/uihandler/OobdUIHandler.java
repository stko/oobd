/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.uihandler;

import org.oobd.core.support.Onion;
import org.oobd.core.OOBDConstants;
import org.oobd.core.OobdPlugin;
import org.oobd.core.IFsystem;
import org.oobd.core.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.core.visualizer.Visualizer;

/**
 * generic abstract for the implementation of scriptengines
 * @author steffen
 */
abstract public class OobdUIHandler extends OobdPlugin implements OOBDConstants {

    protected Onion myStartupParam;

    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "";
    }

    public OobdUIHandler(String myID, Core myCore, IFsystem mySystem, String name) {
        super(name);
        id = myID;
        core = myCore;
        UISystem = mySystem;
        Logger.getLogger(OobdUIHandler.class.getName()).log(Level.CONFIG, "UIHandler  object created: " + id);

    }

    public void setStartupParameter(Onion onion) {
        myStartupParam = onion;
    }

    public void start() {
        
        // set userInterface here (somehow..)
    }

  

 abstract   public void handleMsg() ;

    /**
     * \brief add generated visualizers to global list
     * 
     * several owners (=scriptengines) do have their own visualizers. This is
     * stored in the visualizers hash
     * 
     * @param owner
     *            who owns the visualizer
     * @param vis
     *            the visualizer
     */
  abstract  public void addVisualizer(String owner, Visualizer vis) ;

    /**
     * \brief Tells Value to all visualizers of a scriptengine
     * 
     * @param value
     *            Onion containing value and scriptengine
     * 
     */
 
}
