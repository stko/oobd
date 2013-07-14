package org.oobd.base.db;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.support.Onion;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author steffen
 */
public class AVLLookup extends OobdDB implements OOBDConstants {

    static HashMap<String, byte[]> dbStore=null;

    public AVLLookup() {
        super("AVLLookup");
        dbStore = new HashMap<String, byte[]>();
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
        while (keepRunning == true) {
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            String command = on.getOnionString("command");
            if ("lookup".equalsIgnoreCase(command)) {
                String dbFilename = on.getOnionBase64String("dbfilename");
                String key = on.getOnionBase64String("key");
                Logger.getLogger(AVLLookup.class.getName()).log(Level.INFO,
                        "AVLLookup lookup in " + dbFilename + " for: >" + key + "<");

                try {
                	byte[] myDBBByteArray=dbStore.get(dbFilename);
                	ByteArrayInputStream myByteInputStream=null;
                	if (myDBBByteArray==null){
                		dbStore.put(dbFilename, org.apache.commons.io.IOUtils.toByteArray(Core.UISystem.generateResourceStream(FT_DATABASE, dbFilename)));
                	}
					myByteInputStream = new ByteArrayInputStream(
							dbStore.get(dbFilename));
                    lookupResult = OODBDictionary.doLookUp(myByteInputStream, key);
                } catch (Exception ex) {
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Onion result = new Onion();
                    if (lookupResult == null) {
                        result.setValue("len", -1);
                    } else if (lookupResult.size() < 2) {//just the header returned, but no data found
                        result.setValue("len", 0);
                    } else { //data found, lets start the translation
                        result.setValue("len", lookupResult.size() - 1);
                        String[] header = lookupResult.get(0).split("\t");
                       //writing the header
                        result.setValue("header/size", header.length - 1);
                        for (int i = 1; i < header.length; i++) {
                            result.setValue("header/" + header[i], i);
                        }
                        for (int lines = 1; lines < lookupResult.size(); lines++) {
                            String[] line = lookupResult.get(lines).split("\t");
                            for (int column = 0; column < line.length; column++) {
                                result.setValue("data/" + Integer.toString(lines) + "/" + Integer.toString(column + 1), line[column]);
                            }
                        }
                    }
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.INFO,
                            "AVLLookup lookupt: " + result);
                    Onion answer = new Onion("" + "{'type':'" + CM_RES_LOOKUP
                            + "'," + "'owner':" + "{'name':'" + getPluginName()
                            + "'}," + "'replyID':"
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

