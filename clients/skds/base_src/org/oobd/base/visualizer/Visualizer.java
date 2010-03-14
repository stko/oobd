/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.visualizer;
import org.oobd.base.support.Onion;
import org.oobd.base.Core;
/**
 *
 * @author steffen
 */
public class Visualizer {

    String owner;
    String name;
    IFvisualizer myObject;

    public Visualizer(Onion onion){
        owner=onion.getOnionString("owner");
        name=onion.getOnionString("name");
        this.myObject=myObject;
        Core.getSingleInstance().addVisualizer(owner, this);
    }

    public void setOwner(IFvisualizer myObject){
        this.myObject=myObject;
    }

    public String toString(){
        return "moin";
    }
}
