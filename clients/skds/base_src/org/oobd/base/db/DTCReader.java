/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Axel
 */
public class DTCReader {
    File dtcFile;
    String[] header;
    String[] content;
    HashMap <String,String[]> dtcs = new HashMap();



    public void  readNativeDTCs(String filepath) {
        File dtcFile =new File(filepath);
        String line;
        String[] allParts;
        boolean firstRow=true;
        try {
            BufferedReader br = new BufferedReader(new FileReader(dtcFile));
            while((line=br.readLine())!=null){
                allParts=line.split("\t");
                if (firstRow){
                    header=allParts;
                    firstRow=false;
                    System.out.println("Header: "+header);
                }
                else{
                    content=new String[allParts.length-1];
                    for (int i = 1; i < allParts.length; i++) {
                        content[i-1] = allParts[i];
                        
                    }
                    dtcs.put(allParts[0], content);
                    System.out.println("Key: "+allParts[0]);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(DTCReader.class.getName()).log(Level.SEVERE, null, ex);
        } 
              
    }

    public String[] getDTC(String key){
        if (!dtcs.containsKey(key)){
            String[] error={"No such DTC found!"};
            return error;
        }
        return dtcs.get(key);
    }

    public String[] getHeader(){
        return header;
    }

    void storeCompressedDTCs(String filepath){
//        not yet implemented

    }

    void readCompressedDTCs(String filepath){
//        not yet implemented
    }

}

