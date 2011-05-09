package org.oobd.mobile;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.*;

/**
 * @author axel
 */
public class ScriptForm extends Form implements CommandListener, ItemCommandListener, Runnable {

    private Form parent; //Where this form was started from
    private OOBD_MEv2 mainMidlet; //Where the output routines are
    private Form messageForm;
    private Command backCommand = null;
    private Command detailCommand = null;
    private Command sendTextCmd;
    private Command clearCmd;
    private Display display;
    private List cellList = null;
    private Script myEngine = null;
    private Hashtable scriptTable;
    private Command selectCmd = new Command("Select", Command.ITEM, 0);
    private Command exitCmd = new Command("Exit", Command.EXIT,0);
    private ScriptCell tempCell;
    private String tempValue="";
    private String message;
    private boolean resetMessage=true;


    public ScriptForm(OOBD_MEv2 mainMidlet, Script scriptEngine, Display display) {
        super("");
        this.myEngine = scriptEngine;
        this.display = display;
        this.mainMidlet = mainMidlet;
        new Thread(this).start();
        
    }

    public void showForm(String title,Hashtable scriptTable) {
        resetMessage=true;
        this.deleteAll();
        this.setTitle(title);
        
        System.out.println("Table-length: "+scriptTable.size());
        
        Enumeration e = scriptTable.keys();
        for (int i = 1; i < scriptTable.size()+1; i++) {
//            System.out.println("Current-ID: "+i);
            tempCell = (ScriptCell)scriptTable.get(Integer.toString(i));
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

    public void showMessage(String text){
        if (resetMessage){
            messageForm=new Form("Message");
            messageForm.setCommandListener(this);
            backCommand = new Command("Back", Command.BACK, 0);
            messageForm.addCommand(backCommand);
            sendTextCmd = new Command("Send Text",Command.OK,0);
            messageForm.addCommand(sendTextCmd);
            clearCmd = new Command("Clear",Command.OK,0);
            messageForm.addCommand(clearCmd);
            resetMessage=false;
            tempValue = text;
            message = text;
        } else {
        tempValue = text + "\n";
        message = message + " \n" + text;
        }
        messageForm.append(tempValue);        
        display.setCurrent(messageForm);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == exitCmd){
            display.setCurrent(mainMidlet.mainwindow);
        } else if (c == backCommand){
            display.setCurrent(this);
        } else if (c == sendTextCmd){
            SendMMS mms = new SendMMS(message, this, mainMidlet);
//            display.setCurrent(mms);
        } else if (c == clearCmd){
            resetMessage = true;
            showMessage("");
        }
    }

    public void commandAction(Command c, Item item) {
        if (c == selectCmd){
            tempCell = (ScriptCell) item;
            tempValue = myEngine.callFunction(tempCell.getFunction(),new Object[]{tempCell.getValue(),tempCell.getID()});
//            System.out.println("Rückgabe von callFunction: " + tempValue);
            tempCell.setValue(tempValue);
            this.updateForm();
            tempValue="";

        }
    }
}