/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.scriptengine;

import org.oobd.base.*;

/**
 * generic abstract for the implementation of scriptengines
 * @author steffen
 */
abstract public class OobdScriptengine extends OobdPlugin {

public static String publicName(){
    /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
    ** is the indicator for this abstract class
     */
    return "";
}

}
