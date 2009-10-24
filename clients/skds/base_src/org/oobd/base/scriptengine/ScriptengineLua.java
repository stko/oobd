/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;

/**
 *
 * @author steffen
 */
public class ScriptengineLua extends OobdScriptengine {

    public ScriptengineLua() {
        System.out.println("Ich bin der ScriptengineLua...");

    }

    @Override
    public String getPublicName() {
        return "se:Lua";
    }

    public static String publicName() {
        return "se:Terminal";
    }
}
