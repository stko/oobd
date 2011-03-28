package org.oobd.mobile;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.*;

/**
 * @author axel
 */
public class ScriptForm extends Form implements CommandListener, ItemCommandListener, Runnable {

    private Form parent; //Where this form was started from
    private Form mainMidlet; //Where the output routines are
    private Command backCommand = null;
    private Command detailCommand = null;
    private Display display;
    private List cellList = null;
    private Script myEngine = null;
    private Hashtable scriptTable;
    private Command selectCmd = new Command("Select", Command.ITEM, 0);
    private Command exitCmd = new Command("Exit", Command.EXIT,0);
    private ScriptCell tempCell;
    private String tempValue;

    public ScriptForm(Form mainMidlet, Script scriptEngine, Display display) {
        super("");
        this.myEngine = scriptEngine;
        this.display = display;
        this.mainMidlet = mainMidlet;
        new Thread(this).start();
        
    }

    public void showForm(String title,Hashtable scriptTable) {
        this.deleteAll();
        this.setTitle(title);

        Enumeration e = scriptTable.keys();
        System.out.println("Table-length: "+scriptTable.size());
        while (e.hasMoreElements()) {
            String id = (String) e.nextElement();
            System.out.println("Current-ID: "+id);
            tempCell = (ScriptCell)scriptTable.get(id);
            tempCell.addCommand(selectCmd);
            tempCell.setItemCommandListener(this);
            this.append(tempCell);
        }
        this.addCommand(exitCmd);
        this.setCommandListener(this);
        this.updateForm();
        
        display.setCurrent(this);
        this.updateForm();
//        showAlert("Showing page: "+title);
    }

    public void showAlert(String text){
        Alert check = new Alert("Debug Message",text,null,AlertType.WARNING);
        display.setCurrent(check);
    }

    public void updateForm(){

    }

    public void run() {
        try {
            while (true) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCmd){
            display.setCurrent(mainMidlet);
        }
    }

    public void commandAction(Command c, Item item) {
        if (c == selectCmd){
            tempCell = (ScriptCell) item;
            tempValue = myEngine.callFunction(tempCell.getFunction(),new Object[]{tempCell.getValue(),tempCell.getID()});
            System.out.println("Rückgabe von callFunction: " + tempValue);
            tempCell.setValue(tempValue);

        }
    }
}