/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author axel
 */
public class ReadTextData {
    
    
    String[] header;
    String[] content;
    HashMap <Integer, String []> data=new HashMap();
    boolean stringfound=false;
    int pointer;

    Vector rowNums;
    String searchkey;
     
    public ReadTextData(String filepath) {

        File dtcFile =new File(filepath);
        String line;
        String[] allParts;
        boolean firstRow=true;
        int rowcounter=0;
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
                    content=new String[allParts.length];
                    for (int i = 0; i < allParts.length; i++) {
                        content[i] = allParts[i];                        
                    }
                    data.put(rowcounter, content);
                    System.out.println(rowcounter+"Key: "+allParts[0]);
                }
                rowcounter++;
            }
        } catch (IOException ex) {
            Logger.getLogger(DTCReader.class.getName()).log(Level.SEVERE, null, ex);
        } 
              
    }
    
    public int find(String searchkey){
        this.searchkey=searchkey;
        pointer=0;        
        rowNums=new Vector (data.keySet());
        Collections.sort(rowNums);
        
        for (pointer = 1; pointer < rowNums.size(); pointer++) {
            for (String string : data.get(pointer)) {
                if (string.matches(".*"+searchkey+".*")){
                    stringfound=true;
                    return pointer;
                }                    
            }
        }
        return -1;
    }

    public int findNext(){
        pointer++;
        if (!stringfound)
            return -1;
        else{
//            SortedSet rowNums=(SortedSet)data.keySet();
            while (pointer<rowNums.size()){
                for (String string : data.get(pointer)) {
                    if (string.matches(".*"+searchkey+".*")){
                        stringfound=true;
                        return pointer;
                    }
                }
                pointer++;
            }
            return -2;   //EOF reached
        }
    }

    public String get(int row,String colName){
        int colNum=-1;
        for (int i = 0; i < header.length; i++) {
            if (header[i].matches(colName))
                    colNum=i;
        }
        if (colNum==-1)
            return "Invalid Collumn Name";
        else
            return data.get(row)[colNum];
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
