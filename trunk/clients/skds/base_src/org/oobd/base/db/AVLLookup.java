package org.oobd.base.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.support.Onion;
import org.json.JSONException;
import java.io.IOException;
import java.util.*;


/**
 *
 * @author steffen
 */
public class AVLLookup extends OobdDB implements OOBDConstants {

    static Enumeration portList;
    static boolean outputBufferEmptyFlag = false;

    public AVLLookup() {
        super("AVLLookup");
        Logger.getLogger(AVLLookup.class.getName()).log(Level.CONFIG, "AVLLookup  object created: " + id);
    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
    }

    @Override
    public String getPluginName() {
        return "d:AVLLookup";
    }

    public void run() {
        ArrayList<String> lookupResult = null;
        System.out.println("AVLLookup started");
        while (keepRunning == true) {
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            System.out.println("AVLLookup Message abgeholt:" + on.toString());
            String command = on.getOnionString("command");
            if ("lookup".equalsIgnoreCase(command)) {
                String dbFilename = on.getOnionString("dbfilename");
                String key = on.getOnionString("key");
                Logger.getLogger(AVLLookup.class.getName()).log(Level.INFO,
                        "AVLLookup lookup in " + dbFilename + " for: >" + key + "<");
                OODBDictionary myDic = new OODBDictionary(core.UISystem.generateResourceStream(FT_DATABASE, dbFilename), key);
                try {
                    lookupResult = myDic.getArrayList();
                } catch (IOException ex) {
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                   Onion result=new Onion();
                   if (lookupResult==null){
                       result.setValue("len", -1);
                   }else if(lookupResult.size()<2){//just the header returned, but no data found
                       result.setValue("len", 0);
                   }else{ //data found, lets start the translation



                       //result.setValue("len", lookupResult.size()-1);
                       result.setValue("len", 1);


                       
                       String[] header =new String[]{"wert","com","data"};
                      String[] line =new String[]{"001","blabup","toll"};
                      int NrOfLines=1;
                      //writing the header
                          result.setValue("header/size",header.length);
                      for (int i=0;i<header.length;i++){
                          result.setValue("header/"+header[i],i+1);
                      }
                      //for (int lines=1;lines<lookupResult.size;lines++){
                       for (int i=0;i<line.length;i++){
                          result.setValue("data/"+Integer.toString(1)+"/"+Integer.toString(i+1),line[i]);
                      }

                      //}
                   }
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.INFO,
                            "AVLLookup lookupt: " + result);
                    Onion answer = new Onion("" + "{'type':'" + CM_RES_LOOKUP
                            + "'," + "'owner':" + "{'name':'" + getPluginName()
                            + "'},"  + "'replyID':"
                            + on.getInt("msgID") + "}");
                    answer.put("result", result);
                    replyMsg(msg, answer);
                } catch (JSONException ex) {
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
    }
}

