package org.oobd.mobile;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author axel
 */
public class ScriptCell extends CustomItem  {

    private String title;
    private String value;
    private String function;
    private String id;
    private String update="-";
    private String timer="-";
    private int  oobdElementFlags; //TODO Implement OOBD Element-Flags
    private int minHeigth=30;
    private int minWidth=150;
    private int prefHeight=30;
    private int prefWidth=250;

    public ScriptCell(String title, String function, String initalValue, int oobdElementFlags, String id) {
        super("");
        this.title = title;
        System.out.println("Title: "+title);
        this.function = function;
        this.value = initalValue;
        this.oobdElementFlags = oobdElementFlags;
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
        this.repaint();
    }

    public String getTitle() {
        return title;
    }

    public String getID() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getFunction() {
        return function;
    }

    public boolean getUpdate() {
        if (update.equals("-")){
            return false;
        }
        else
            return true;
    }

    public void toggleUpdate(){
        if (update.equals("-"))
            update="u";
        else
            update="-";

        this.repaint();
    }

    public boolean getTimer() {
        if (timer.equals("-"))
            return false;
        else
            return true;
    }

    public void toggleTimer(){
        if (timer.equals("-")){
            timer="t";

        }
        else
            timer="-";
        
        System.out.println("ToggleTimer called!");
        this.repaint();
    }

    public String toString() {
        return title + ":" + value;
    }

    public boolean execute(int updateType, Script scriptEngine) {
        value = scriptEngine.callFunction(function,new Object[]{value,id});
            return true;
    }

    protected int getMinContentWidth() {
        return minWidth;
    }

    protected int getMinContentHeight() {
        return minHeigth;
    }

    protected int getPrefContentWidth(int height) {
        return prefWidth;
    }

    protected int getPrefContentHeight(int width) {
        return prefHeight;
    }

    protected void paint(Graphics g, int w, int h) {

        g.drawString(value,0,0,g.LEFT|g.TOP);
        g.drawString(title,0,h,g.LEFT|g.BOTTOM);
        g.drawString(update,w,0,g.RIGHT|g.TOP);
        g.drawString(timer,w,h,g.RIGHT|g.BOTTOM);
    }
}
