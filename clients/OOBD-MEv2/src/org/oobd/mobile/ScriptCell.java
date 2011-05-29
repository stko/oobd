package org.oobd.mobile;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Font;
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
    private OOBD_MEv2 mainMidlet;
    private MobileLogger log;
    private int  oobdElementFlags; //TODO Implement OOBD Element-Flags
    private static int minHeigth=50;
    private static int minWidth=150;
    private static int prefHeight=50;
    private static int prefWidth=250;

    public ScriptCell(String title, String function, String initalValue, int oobdElementFlags, String id) {
        super("");

        this.title = title;
        System.out.println("Title: "+title);
        this.function = function;
        this.value = initalValue;
        System.out.println("Value: "+value);
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

    public void setLog(MobileLogger log) {
        this.log = log;
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

    public static void setHeight(int height) {
        ScriptCell.prefHeight = height;
        ScriptCell.minHeigth = height;
        
    }
    
    

    protected void paint(Graphics g, int w, int h) {

//        log.log(1,title+" "+value+": Width: "+ w + "  Height: "+h);
        Font f=g.getFont();
//        log.log(1,"Schrifthöhe: "+f.getHeight());
        
//        g.setFont(Font.getFont(Font.SIZE_SMALL));
//        if((f.getHeight()*2)<h){
//            log.log(1,"Höhenkorrektur erforderlich!");
//            minHeigth=f.getHeight()*2;
//        }

        g.setColor(255, 0, 0);
        g.drawString(value,0,0,g.LEFT|g.TOP);
        g.drawString(title,0,h,g.LEFT|g.BOTTOM);
        g.drawString(update,w,0,g.RIGHT|g.TOP);
        g.drawString(timer,w,h,g.RIGHT|g.BOTTOM);
    }
}
