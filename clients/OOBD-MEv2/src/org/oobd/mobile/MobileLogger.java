/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.mobile;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 *
 * @author madley
 */


public class MobileLogger extends Form implements CommandListener{
    private RecordStore rs;
    private String recordStoreName="Logging";
    private int numOfRecords2keep=30;
    private int thisID=0;
    private RecordEnumeration re;
    private Command backCmd;
    private Command clearCmd;
    private Form parent;
    private OOBD_MEv2 mainMidlet;
    private String xtendedMessage;

    public MobileLogger(OOBD_MEv2 mainMidlet) {
        super("Logger");
        this.mainMidlet = mainMidlet;
        try {
            rs = RecordStore.openRecordStore(recordStoreName, true);
            cleanup();

        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        backCmd = new Command("Back", Command.BACK, 0);
        clearCmd = new Command("Clear Logs",Command.OK,0);

        this.addCommand(backCmd);
        this.addCommand(clearCmd);
        this.setCommandListener(this);

    }

    public void showlogs(OOBD_MEv2 mainMidlet){

        this.deleteAll();
        String[] logs=getLogs();
        int x = logs.length;
        System.out.println(x);
        for (int i = 0; i < x; i++) {
            System.out.println(i + " = "+logs[i]);
            this.append(logs[i]);
            
        }
        mainMidlet.getDisplay().setCurrent(this);
    }

    public void log(String message){
        
        try {
//            nextID=rs.getNextRecordID();
//            String newMessage=nextID+": "+message+"\n";
            thisID=rs.getNextRecordID()-1;
            xtendedMessage=thisID+": "+message;
            System.out.println("LOG: "+xtendedMessage);
            byte[] byteMessage = xtendedMessage.getBytes();
            rs.addRecord(byteMessage, 0, byteMessage.length);
//            System.out.println("Next LogID ist: "+nextID);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }

    public String[] getLogs(){
        try {
            int numOfRecords = rs.getNumRecords();
            String[] logs = new String[numOfRecords];
            int counter=0;
            re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()&counter<numOfRecords){
                logs[counter]=new String(re.nextRecord());
                counter++;
            }


            return logs;
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        return null;
    }



    //TODO Cleaning feature for the logger is missing
    public void cleanup(){

        

        try {
            
            if (rs.getNumRecords() > numOfRecords2keep) {
                
                int recordcount = rs.getNumRecords();
                int nextrecord = rs.getNextRecordID();

                for (int i = nextrecord-recordcount; i < nextrecord-numOfRecords2keep; i++) {
                    rs.deleteRecord(i);

                }

            }
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd){
            mainMidlet.showMain();
            
        } else if (c==clearCmd){
            try {
                rs.closeRecordStore();
                rs.deleteRecordStore(recordStoreName);
                rs= RecordStore.openRecordStore(recordStoreName, true);
                showlogs(mainMidlet);
            } catch (RecordStoreException ex) {
                this.log(ex.toString());
            }

        }
    }
    
    
    
    

}
