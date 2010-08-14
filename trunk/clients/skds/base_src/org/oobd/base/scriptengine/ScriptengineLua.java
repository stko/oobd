/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;

import org.oobd.base.*;


/**
 *
 * @author steffen
 */
public class ScriptengineLua extends OobdScriptengine {

    public ScriptengineLua(String ID, Core myCore) {
        super(ID, myCore);
        Debug.msg("scriptenginelua",DEBUG_BORING,"Ich bin der ScriptengineLua...");

    }

    @Override
    public String getPluginName() {
        return "se:Lua";
    }

    public static String publicName() {
        return "Script";
    }

    public void run(){
        
    }
}
