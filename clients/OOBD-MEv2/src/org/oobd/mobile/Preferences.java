package org.oobd.mobile;


import java.util.*;
import javax.microedition.rms.*;

public class Preferences {

    private final int btURL=1;
    private final int scriptpath=2;
    private final int adressbook=3;
    private final int maxRecordID=3;
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
        log.log("Fetching Record-# "+key+" with the value: "+value);
        return value;

    }

    public void put(int key, String value)  {
        if (value == null) {
            value = "";
        }
        try {
            log.log("Trying to store Record-# "+key+" with the value: "+value);
            rs.setRecord(key, value.getBytes(), 0, value.getBytes().length);
            log.log("Stored Record-# "+key+" with the value: "+value);
        } catch (RecordStoreNotOpenException ex) {
            log.log("Error (RSNO) while storing record: "+ex.toString());
            ex.printStackTrace();
        } catch (InvalidRecordIDException ex) {
            log.log("Error (IRID) while storing record: "+ex.toString());
            ex.printStackTrace();
        } catch (RecordStoreFullException ex) {
            log.log("Error (RSF) while storing record: "+ex.toString());
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            log.log("Error (RS) while storing record: "+ex.toString());
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
                        log.log("Checking record with the ID: " + i);
                        rs.getRecord(i);


                    } catch (RecordStoreException ex) {
//                        log.log(ex.getMessage());
//                        log.log(ex.toString());
//                        log.log(ex.getClass().getName());
//                        ex.printStackTrace();
                        log.log("Dummy-Wert setzen für ID:"+i);
                        rs.addRecord(dummy.getBytes(), 0, dummy.getBytes().length);
                    }
                }
            }
        } catch (RecordStoreException ex) {
            log.log("Error while checking Records!");
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

//    private void load() throws RecordStoreException {
//        RecordStore rs = null;
//        RecordEnumeration re = null;
//
//        try {
//            rs = RecordStore.openRecordStore(mRecordStoreName, true);
//            re = rs.enumerateRecords(null, null, false);
//            while (re.hasNextElement()) {
//                byte[] raw = re.nextRecord();
//                String pref = new String(raw);
//                // Parse out the name.
//                int index = pref.indexOf('|');
//                String name = pref.substring(0, index);
//                String value = pref.substring(index + 1);
//                put(name, value);
////                log.log("Loaded: "+name+" -> "+value);
//            }
//        } finally {
//            if (re != null) {
//                re.destroy();
//            }
//            if (rs != null) {
//                rs.closeRecordStore();
//            }
//        }
//    }
//
//    public void save() throws RecordStoreException {
//        RecordStore rs = null;
//        RecordEnumeration re = null;
//        try {
//            rs = RecordStore.openRecordStore(mRecordStoreName, true);
//            re = rs.enumerateRecords(null, null, false);
//
//            // First remove all records, a little clumsy.
//            while (re.hasNextElement()) {
//                int id = re.nextRecordId();
//                rs.deleteRecord(id);
//            }
//
//            // Now save the preferences records.
//            Enumeration keys = mHashtable.keys();
//            while (keys.hasMoreElements()) {
//                String key = (String) keys.nextElement();
//                String value = get(key);
//                String pref = key + "|" + value;
//                byte[] raw = pref.getBytes();
//                rs.addRecord(raw, 0, raw.length);
////                log.log("Stored: "+key+" -> "+value);
//            }
//        } finally {
//            if (re != null) {
//                re.destroy();
//            }
//            if (rs != null) {
//                rs.closeRecordStore();
////                log.log("RecordStore closed");
//            }
//        }
//    }
//    
}
