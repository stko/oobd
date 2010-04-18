/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.visualizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.support.Onion;
import org.oobd.base.*;

/**
 *
 * @author steffen
 */
public class Visualizer {

    Onion ownerEngine;
    Onion value;
    String name;
    IFvisualizer myObject;
    boolean updateNeeded = false;

    public Visualizer(Onion onion) {
        ownerEngine = onion.getOnion(OOBDConstants.FN_OWNER);
        name = onion.getOnionString(OOBDConstants.FN_NAME);
//        this.myObject=myObject;
        Core.getSingleInstance().addVisualizer(ownerEngine.getOnionString(OOBDConstants.FN_NAME), this);
    }

    /** sets the corrosponding visual element to this visualizer
     * 
     * @param myObject
     */
    public void setOwner(IFvisualizer myObject) {
        this.myObject = myObject;
    }

    /** sets the Value for this visualizer
     * 
     * @param myObject
     */
    public void setValue(Onion value) {
        if (this.name.matches(value.getOnionString("to/name"))) {
            try {
                this.value = new Onion(value.toString());
                updateNeeded = true;
            } catch (JSONException ex) {
            }
        }
    }

    public String getValue(String name) {
        if (value!=null){
            return value.getOnionString(name);
        }else{
            return null;
        }
    }

    /** returns the name of the owning scriptengine
     *
     * @return
     */
    public String getOwnerEngine() {
        return ownerEngine.getOnionString(OOBDConstants.FN_NAME);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String val = getValue(OOBDConstants.FN_VALUESTRING);
        if (val == null) {
            return "#NA";
        } else {
            return val;
        }

    }

    /** do update
     *  0: start 1: update data 2: finish
     */
    public void doUpdate(int updateLevel) {
        if (myObject != null && updateNeeded) {
            myObject.update(updateLevel);
            updateNeeded =
                    false;
        }
    }
   /** Update request from Component
     *  0: start 1: update data 2: finish
     */
    public void updateRequest(int type) {
        System.out.println("Update request"+Integer.toString(type));
    }
}
