
package org.oobd.base.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.print.DocFlavor.STRING;


/**
 * This Class helo us to seach the parameter on database
 * @author M. M. F.
 * @version 1.0
 *
 */

public class OOBDictionary {

	private FileInputStream ins;
	private String searchIndex;
	private long upperIndex=0;
	private long lowerIndex=1;
	private String curr = "";
	private ArrayList<String> arrayList = new ArrayList<String>();
	//private ArrayList<Integer> al = new ArrayList<Integer>();
	private boolean notFound = true;
       // private boolean lowerandupper = false;
	private int ch;
        private  String header ="";
        private StringBuilder str = new StringBuilder();
	private byte[] sec = new byte[4];
        //private String[] strArray = new String[3];
        /*
        public String[] getTes(){
            strArray[1] = String.valueOf((char)0x09);
            return strArray;
        }
         *
         */
        /**
         * This method gives a string arraylist with parameter
         * @return  String array list
         * @throws IOException Filenot found
         */
	public ArrayList<String> getArrayList() throws IOException{
		writeHeader();
		getIndex();
		getLowerPosition();
		getUpperPosition();
		while(notFound==true) {
                   
                   	if((searchIndex.compareTo(curr)<0) && (lowerIndex>0)){

				ins.skip(lowerIndex-1);
				getIndex();
				getLowerPosition();
				getUpperPosition();
			}

			else if((searchIndex.compareTo(curr)>0)&& upperIndex>0)
			{
				ins.skip(upperIndex-1);
				getIndex();
				getLowerPosition();
				getUpperPosition();
			}

			else if(searchIndex.compareTo(curr)==0)
			{
				notFound = false;
				getValue();
                                arrayList.add(str.toString());
                                //strArray[2]= str.toString();

			}
                        else if(lowerIndex==0 || upperIndex==0){
                             notFound = false;
                            getValue();
                            arrayList.add(str.toString());
                            //strArray[1]= str.toString();
                        }
                        
			else{
				arrayList.clear();
				arrayList.add(null);
			}
		}
            
		return arrayList;

	}
        /**the constuctor
         * @param file The fileinputstream
         * @param index the searching index
         */
	public OOBDictionary(FileInputStream file, String index){
		this.ins= file;

		this.searchIndex = index;
	}

	/**
	 * Search for the current index in the database (binary file)
	 */
	private void getIndex(){
		curr = "";
		int m = 0;
		try{

			while((ch = ins.read())!=-1){
				if(ch != 0x0){

					curr = curr + String.valueOf((char)ch);


					}
				else{
					break;
				}
			}
		}catch(IOException ex){
			System.out.println(ex.toString());
		}
	}
	/**
	 *  write the header to the string array list
	 */
	private void writeHeader(){
           
		try{
			while(( ch = ins.read()) != -1){
				if(ch != 0x0){
                                    header = header + String.valueOf((char)ch);
					//al.add(ch);
					//arrayList.add(String.valueOf((char)ch));

				}
				else{
					break;
				}
			}

		}catch(IOException ex){
			System.out.println(ex.toString());
		}

	}
        /**
         * Search for the current upper index
         */
	private void getUpperPosition(){
		upperIndex = 0;
		try{
			ch = ins.read(sec);
			upperIndex= convertByteValueToLong(sec);


		}catch(IOException ex){
			System.out.println(ex.toString());
		}

	}
         /**
         * Search for the curent lower index
         */
	private void getLowerPosition(){
		lowerIndex = 0;
		try{

			ch = ins.read(sec);
			lowerIndex= convertByteValueToLong(sec);


		}catch(IOException ex){
			System.out.println(ex.toString());
		}

	}


	/**
	 * gives a long value from a byte array
	 * @param b A char byte array
	 * @return  the long value from a char byte array
	 */
	private long convertByteValueToLong(byte[] b){
		long m =0;
		for(int i = 0; i< b.length;i++){
        	m = (long) ((long) b[i])+m;
        }
		return m;
	}
        /**
         * Return the final string array list
         */

	private void getValue(){
		int counter = 0;
		//al.add(0);
                arrayList.add(header);
               // strArray[0]= header;
		try{
			while(( ch = ins.read()) != -1){


				if(ch != 0x0){
						//al.add(ch);
                                    str.append((char)ch);

						counter = 0;
						//arrayList.add(String.valueOf((char)ch));
					}
				else if (ch == 0x0 && counter <2){
						//al.add(ch);
                                        str.append((char)ch);
						counter = counter +1;
						//arrayList.add(String.valueOf((char)ch));
						if(counter==2){
							break;
						}
					}
                                


			}

		}catch(IOException ex){
			System.out.println(ex.toString());
		}
	}

}

