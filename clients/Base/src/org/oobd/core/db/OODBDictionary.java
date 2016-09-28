/**
 * \brief OODBDictionary
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
package org.oobd.core.db;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This Class helo us to seach the parameter on database
 * @author M. M. F.
 * @version 1.0
 *
 */
public class OODBDictionary {

    private InputStream ins;
    private String searchIndex;

    public static ArrayList<String> doLookUp(InputStream file, String index) throws IOException {
    	if (file==null || index==null || index.equals("")){
    		return null;
    	}
        OODBDictionary localDb = new OODBDictionary(file, index);
        ArrayList<String> res = localDb.getArrayList();
        file.close();
        return res;
    }

    /**
     * This method return a string arraylist for the index given i
     * @return  String array list
     * @throws IOException File not found
     */
    private ArrayList<String> getArrayList() throws IOException {
        long jumpTo = 0;
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(readStringUntil0x());
        boolean notFound = true;
        while (notFound == true) {
            ins.skip(jumpTo);
            String actIndex = readStringUntil0x();
            long lowerIndex = readJumpOffset();
            long upperIndex = readJumpOffset();
            if ((searchIndex.compareTo(actIndex) < 0) && (lowerIndex > 0)) {
                jumpTo = lowerIndex - 1;
            } else if ((searchIndex.compareTo(actIndex) > 0) && upperIndex > 0) {
                jumpTo = upperIndex - 1;
            } else if (searchIndex.compareTo(actIndex) == 0) {
                notFound = false;
                // reading all the found lines
                actIndex = readStringUntil0x();
                while (!actIndex.equalsIgnoreCase("")) {
                    arrayList.add(actIndex);
                    actIndex = readStringUntil0x();
                }
            } else {
                notFound = false;
                arrayList = null;
            }
        }
        return arrayList;
    }

    /**the constuctor
     * @param file The fileinputstream
     * @param index the searching index
     */
    public OODBDictionary(InputStream file, String index) {
        this.ins = file;
        this.searchIndex = index;
    }

    /**
     *  read the string until 0x char
     */
    private String readStringUntil0x() throws IOException {
        String thisLine = "";
        int ch;
        while ((ch = ins.read()) != -1) {
            if (ch != 0x0) {
                thisLine = thisLine + String.valueOf((char) ch);
            } else {
                break;
            }
        }
        return thisLine;
    }

    /**
     * Search for the current upper index
     */
    private long readJumpOffset() throws IOException {
        byte[] sec = new byte[4];
        ins.read(sec);
        return convertByteValueToLong(sec);
    }

    /**
     * gives a long value from a byte array
     * @param b A char byte array
     * @return  the long value from a char byte array
     */
    private long convertByteValueToLong(byte[] b) {
        long m = 0;
        for (int i = 0; i < b.length; i++) {
            m = m * 256 + (b[i] < 0 ? (256 + (long) b[i]) : b[i]);
        }
        return m;
    }
}

