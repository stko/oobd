/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package skdsswing.transfer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import skdsswing.transfer.InitPacket;

public class FileSpecification {
	 private short packetnumber;
	    private boolean longPacket;
	    private boolean isPacketInit;
	    private boolean isEnd;
	    private byte[] packet;
	    private byte[] data;
	    private byte[] innerdata;
	    private byte[] header;
	    private byte[] crcfooter;
	    private File file;
	    private String filename;
	    private InitPacket initpack,sendpacket;
	    private FileInputStream in;
	    private boolean ack;

	    public byte[] getInnerData(){
	        return innerdata;
	    }
	    public void setInnerData(byte[] data){
	        this.innerdata = data;
	    }

	    public FileSpecification(String filename){
	    	this.filename = filename;
	    	try{
	    		initialBinaryFile(filename,in);
	    	}catch(IOException ex){

	    	}
	    }
	    public FileSpecification(String filename,FileInputStream input){
	    	this.filename = filename;
	    	this.in = input;

	    	try{
	    		 int k = input.available();
                        byte[] bytes = new byte[in.available()];
                        sendBinaryFile(bytes,filename,input);
	    	}catch(IOException ex){

	    	}
	    }
	    /**
	     * Create a Binary File
	     * @param filename
	     * @throws IOException
	     */
	    private  void initialBinaryFile(String filename, FileInputStream in)throws IOException{
	    	//FileInputStream inz = new FileInputStream(filename);
	    	//in = new FileInputStream(filename);
		byte[] bytes = new byte[in.available()];
			

			sendBinaryFile(bytes,filename,in);
		}
	    /**
	     * Send the binary file
	     * @param bytes
	     * @param filename
	     */
	    public void sendBinaryFile(byte[] bytes, String filename,FileInputStream inputstream){
	    	byte[] cp = new byte[2];



	    	// evtl. noch einen Befehl to Start the download

                 //  sp.Write("AT+pBINARYUPLOAD");
           // sendNewLine();
           // waitfor('C');
	    	 

            

	    	short packetnum = 0;
	    	initpack = new InitPacket();
	    	initpack.setIsPacketInit(true);
	    	initpack.setPacketnumber(packetnum);
	    	initpack.setFilelength(bytes.length);
	    	initpack.setFilename(filename);
	    	if(filename.length() > 125){
	    		initpack.setLongPacket(true);
	    	}
	    	else{
	    		initpack.setLongPacket(false);
	    	}

	    	try {
	    		 
                    initpack.createPacket();

                    //  sp.Write(initpacket.packet, 0, initpacket.packet.Length);

	            // waitforack();
	             //waitfor('C');

					byte[] temparray = new byte[1024];
                                        //int k = inputstream.available();
					long size = Math.abs((long)inputstream.available() /(long) 1024);
					
					 
					 for(int m = 0; m< size;m++){
						 sendpacket = new InitPacket();
						 inputstream.read(temparray,0,1024);

						 packetnum++;
						 sendpacket.setPacketnumber(packetnum);
						 sendpacket.setLongPacket(true);
						 sendpacket.setIsPacketInit(false);
						 sendpacket.setData(temparray);
						 sendpacket.createPacket();
                                                  //sp.Write(sendPacket.packet, 0, sendPacket.packet.Length);
                                                    //waitforack();
					 }
                                        // sendEndOftransmision();
                                        // waitforack();

                                        //  waitforline();


			} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}


	    }
		private void waitForAck(){
			while(ack!=true){

			}

		}
		private void sendOfTransmission(){
			InitPacket endPacket = new InitPacket();
			endPacket.setIsEnd(true);
			endPacket.setLongPacket(false);
			endPacket.setPacketnumber((short)0);
			endPacket.setData(new byte[128]);
			try{
				endPacket.createPacket();
			}catch(IOException ex){
				ex.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private void waitForAnswer(char p){

		}



}