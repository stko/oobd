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
	    		initialBinaryFile(filename);
	    	}catch(IOException ex){
	    		
	    	}
	    }
	    public FileSpecification(String filename,FileInputStream input){
	    	this.filename = filename;
	    	this.in = input;
	    	try{
	    		initialBinaryFile(filename);
	    	}catch(IOException ex){
	    		
	    	}
	    }
	    /**
	     * Create a Binary File
	     * @param filename
	     * @throws IOException
	     */
	    private  void initialBinaryFile(String filename)throws IOException{
	    	FileInputStream inz = new FileInputStream(filename);
	    	//this.in = new FileInputStream(filename);
			byte[] bytes = new byte[in.available()];
			byte[] bit2= new byte[inz.available()];
			
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
	    	 //  sp.Write(initpacket.packet, 0, initpacket.packet.Length);

            // waitforack();
             //waitfor('C');
	    	
	    	 
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
	    		 //  sp.Write(initpacket.packet, 0, initpacket.packet.Length);

	            // waitforack();
	             //waitfor('C');
	    		
	    		
					initpack.createPacket();
					
					byte[] temparray = new byte[1024];
					long size = inputstream.available() / 1024;
					int rest = inputstream.available() % 1024;
					 int pos = 1;
					 for(int m = 0; m< size;m++){
						 sendpacket = new InitPacket();
						 inputstream.read(temparray,0,1024);
						 
						 packetnum++;
						 sendpacket.setPacketnumber(packetnum);
						 sendpacket.setLongPacket(true);
						 sendpacket.setIsPacketInit(false);
						 sendpacket.setData(temparray);
						 sendpacket.createPacket();
						 
					 }
					
					
					
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