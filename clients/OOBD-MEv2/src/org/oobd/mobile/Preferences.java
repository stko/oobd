package org.oobd.mobile;


import java.util.*;
import javax.microedition.rms.*;

public class Preferences {

    private final int btURLkey=1;
    private final int scriptpathKey=2;
    private final int adressbookKey=3;
    private final int loglevelKey=4;
    private final int maxRecordID=4;
    private String mRecordStoreName;
    RecordStore rs = null;
    private Hashtable mHashtable;
    MobileLogger log;
    OOBD_MEv2 mainMidlet;

    public Preferences(String recordStoreName, OOBD_MEv2 mainMidlet) throws RecordStoreException {
        this.mainMidlet = mainMidlet;
        log = mainMidlet.getLog();
        rs = RecordStore.openRecordStore(recordStoreName, true);
        
        checkRecords();
    }

    public String get(int key) {
        String value="null";
        try {
            value= new String(rs.getRecord(key));
            //        log.log("Try to get: "+key+"  Result: "+(String) mHashtable.get(key));
            
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
        log.log(0,"Fetching Record-# "+key+" with the value: "+value);
        return value;

    }

    public void put(int key, String value)  {
        if (value == null) {
            value = "";
        }
        try {
            log.log(1,"Trying to store Record-# "+key+" with the value: "+value);
            rs.setRecord(key, value.getBytes(), 0, value.getBytes().length);
            log.log(1,"Stored Record-# "+key+" with the value: "+value);
        } catch (RecordStoreNotOpenException ex) {
            log.log(3,"Error (RSNO) while storing record: "+ex.toString());
            ex.printStackTrace();
        } catch (InvalidRecordIDException ex) {
            log.log(3,"Error (IRID) while storing record: "+ex.toString());
            ex.printStackTrace();
        } catch (RecordStoreFullException ex) {
            log.log(3,"Error (RSF) while storing record: "+ex.toString());
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            log.log(3,"Error (RS) while storing record: "+ex.toString());
//            ex.printStackTrace();
        } 


    }
    
    private void checkRecords() {
        String dummy = "null";
        try {
            int nextRecord = rs.getNextRecordID();
            if (nextRecord< maxRecordID) {
                for (int i = nextRecord; i <= maxRecordID; i++) {
                    try {
//                        System.out.println("Next ID: " + rs.getNextRecordID());
                        log.log(0,"Checking record with the ID: " + i);
                        rs.getRecord(i);


                    } catch (RecordStoreException ex) {
//                        log.log(ex.getMessage());
//                        log.log(ex.toString());
//                        log.log(ex.getClass().getName());
//                        ex.printStackTrace();
                        log.log(0,"Dummy-Wert setzen für ID:"+i);
                        rs.addRecord(dummy.getBytes(), 0, dummy.getBytes().length);
                    }
                }
            }
        } catch (RecordStoreException ex) {
            log.log(3,"Error while checking Records!");
        }
        
    }

    public void closeStore(){
        try {
            rs.closeRecordStore();
        } catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }


}
