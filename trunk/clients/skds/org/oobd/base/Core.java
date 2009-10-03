/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

import org.oobd.base.support.Onion;

/**
 * The interface for nearly all interaction between the generic oobd maschine and the different environments
 * @author steffen
 */
public class Core {

    public Core(IFui userInterface){
        //userInterface.sm("Moin");
        Onion testOnion=Onion.generate(null);
        testOnion.setValue("test", "moin");
        testOnion.setValue("test2", "moin2");
        testOnion.setValue("path/test3", "moin3");
        testOnion.setValue("path/test4", "moin4");
        testOnion.setValue("path/path2/test5", "moin5");
        Onion testOnion2=null;
        try{
            testOnion2=new Onion(testOnion.toString());
        }
        catch (org.json.JSONException e){

        }
        System.out.println(testOnion.toString());
        System.out.println(testOnion2.toString());


    }
}
