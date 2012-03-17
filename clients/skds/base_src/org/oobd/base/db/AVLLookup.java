/**
 * \brief AVLLookup
 * Read a binary format, which allows a key->value(s) list search in this file in one direction from the beginning to the end
 *
 *
 *
 * If there are more as one Value per key, the Values must sorted in the sequence as they should be used later
 *
 * Input file Format (to gererate such files try oodbcreate from the OOBD- Toolset)
 *
 *
 * HeaderLine 0x0
 *
 * Entry 1
 *
 * ..
 *
 * Entry n
 *
 * HeaderLine = (colum_name 0) \t (colum_name 1) \t (.. colum_name n)
 *
 * Entry = Key 0x0 (FilePosition of greater Key) (FilePosition of smaller Key) Values 1  0x0 [..Values n  0x0] 0x0
 *
 * Values = (Value_of_Colum 0) \t (Value_of_Colum 1) \t (..Value_of_Colum n)
 *
 * Fileposition = binary unsigned 32-Bit Big Endian
 *
 * 
 * How to read this file:
 *

1 - Read Headerline (from the file beginning until the first 0x0). Store this data for later naming of the found columns.
2 - read key value (string until the next 0x0) and the next 4 Byte long file positions for greater and smaller key values. If they are 0 (zero), there's no more smaller or greater key available
3 - compare key with search string:
    - if equal, read attached values in an array. This array then contains the search result(s). Return this and the header line as positive search result.
    - if smaller:
	  if smaller file position is 0 (zero), then return from search with empty result array.
	  if smaller file position is not 0, set file Seek pointer to file postion and continue again with step 2
    - if bigger:
	  if bigger file position is 0 (zero), then return from search with empty result array.
	  if bigger file position is not 0, set file Seek pointer to file postion and continue again with step 2



*/
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
        return "b:AVLLookup";
    }

    public void run() {

        while (keepRunning == true) {
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            System.out.println("AVLLookup Message abgeholt:" + on.toString());
            String command = on.getOnionString("command");
            if ("lookup".equalsIgnoreCase(command)) {
                String dbFilename = Base64Coder.decodeString(on.getOnionString("dbfilename"));
                String index = Base64Coder.decodeString(on.getOnionString("index"));
                Logger.getLogger(AVLLookup.class.getName()).log(Level.INFO,
                        "AVLLookup lookup in "+dbFilename+" for: >" + index + "<");
                try {
                    /// Datenbankabfrage...
                    String result = "";
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.INFO,
                            "AVLLookup lookupt: " + result);
                    replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_LOOKUP
                            + "'," + "'owner':" + "{'name':'" + getPluginName()
                            + "'}," + "'result':" + result + "," + "'replyID':"
                            + on.getInt("msgID") + "}"));
                } catch (JSONException ex) {
                    Logger.getLogger(AVLLookup.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
    }
}

