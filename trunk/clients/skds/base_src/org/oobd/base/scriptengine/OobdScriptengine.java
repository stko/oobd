/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;

import org.oobd.base.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * generic abstract for the implementation of scriptengines
 * @author steffen
 */
abstract public class OobdScriptengine extends OobdPlugin implements OOBDConstants {


  
    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "";
    }

    public OobdScriptengine(String myID, Core myCore, IFsystem mySystem, String name) {
        super(name);
        id = myID;
         core = myCore;
         UISystem=mySystem;
        Logger.getLogger(OobdScriptengine.class.getName()).log(Level.CONFIG,"Scriptengine  object created: " + id);

    }

    
}
