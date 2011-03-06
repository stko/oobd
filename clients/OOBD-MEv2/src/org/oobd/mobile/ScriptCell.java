package org.oobd.mobile;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author steffen
 */
public class ScriptCell {

    private String title;
    private String value;
    private String function;
    private String id;
    private int  oobdElementFlags;

    ScriptCell(String title, String function, String initalValue, int oobdElementFlags, String id) {
        this.title = title;
        this.value = initalValue;
        this.function = function;
        this.oobdElementFlags = oobdElementFlags;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getFunction() {
        return function;
    }

    public boolean getUpdate() {
        return (oobdElementFlags & 0x02)>0; //if OOBDELEMENTFLAG_UPDATE is set
    }

    public boolean getTimer() {
        return (oobdElementFlags & 0x04)>0; //if OOBDELEMENTFLAG_TIMER is set
    }

    public String toString() {
        return title + ":" + value;
    }

    public boolean execute(int updateType, Script scriptEngine) {
        value = scriptEngine.callFunction(function,new Object[]{value,id});
            return true;
    }
}
