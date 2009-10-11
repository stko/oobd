/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.scriptengine;

/**
 * generic abstract for the implementation of protocols
 * @author steffen
 */
public class ScriptengineTerminal extends OobdScriptengine {
        public ScriptengineTerminal() {
        System.out.println("Ich bin der ScriptengineTerminal...");

    }

        @Override public String getPublicName(){
        return "se:Terminal";
    }

        public static String publicName(){
    return "se:Terminal";
}

}
