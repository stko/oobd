/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;


import org.oobd.base.*;



/**
 * generic abstract for the implementation of protocols
 * @author steffen
 */
public class ScriptengineTerminal extends OobdScriptengine implements OOBDConstants {

    public ScriptengineTerminal(String ID, Core myCore) {
        super(ID, myCore);
        Debug.msg("scriptengineterminal",DEBUG_BORING,"Ich bin der ScriptengineTerminal...");

    }

    @Override
    public String getScriptEngineName() {
        return "se:Terminal";
    }

    public static String publicName() {
        return "Terminal";
    }

    public void start(){
        Debug.msg("scriptengineterminal",DEBUG_BORING,"positiver Actiontest...");
        Debug.msg("scriptengineterminal",DEBUG_BORING,"negativer Actiontest...");
        core.actionRequest("{\"type\":\"noaction\"}");
        core.actionRequest(""+
                "{'type':'"+CM_CANVAS+"'," +
                "'owner':'"+this.id+"'," +
                "'name':'Canvastest_1'}"
                );
        core.actionRequest(""+
                "{'type':'"+CM_CANVAS+"'," +
                "'owner':'"+this.id+"'," +
                "'name':'Canvastest_2'}"
                );
        core.actionRequest(""+
                "{'type':'"+CM_VISUALIZE+"'," +
                "'owner':'"+this.id+"'," +
                "'canvas':'Canvastest_1'," +
                "'name':'table_1'}"
                );
    }
}
