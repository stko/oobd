/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package skdsswing.transfer;
import java.io.*;
import java.io.ByteArrayInputStream;
//import skdsswing.SDKSSwingFileExplorer;

/**
 * THis Class creat the packet for transfer
 * in first step creat the telegramm  [SOH 00 FF "Filename" NULL[128 - Filename.length] CRC CRC]
 * second step creat the telgramm [STX 0X !n DATA[1024] CRC CRC]
 * @author OOBD.org
 */
public class InitPacket {
   private short packetnumber;
    private boolean longPacket;
    private boolean isPacketInit;
    private boolean isEnd;
    private String fileName;
    private int fileLength;
    private byte[] packet;
    private byte[] data;
    private byte[] innerdata;
    private byte[] header;
    private byte[] crcfooter = new byte[2];
    private CRC16 crc = new CRC16();

   public void setFilelength(int size){
	   this.fileLength = size;
   }
   public void setFilename(String name){
	   this.fileName = name;
   }
    public short getPacketnumber() {
		return packetnumber;
	}
	public void setPacketnumber(short packetnumber) {
		this.packetnumber = packetnumber;
	}
	public boolean getIsLongPacket() {
		return longPacket;
	}
	public void setLongPacket(boolean longPacket) {
		this.longPacket = longPacket;
	}
	public boolean getIsPacketInit() {
		return isPacketInit;
	}
	public void setIsPacketInit(boolean isPacketInit) {
		this.isPacketInit = isPacketInit;
	}
	public boolean getIsEnd() {
		return isEnd;
	}
	public void setIsEnd(boolean isEnd) {
		this.isEnd = isEnd;
	}
	public byte[] getPacket() {
		return packet;
	}
	public void setPacket(byte[] packet) {
		this.packet = packet;
	}
	public byte[] getInnerdata() {
		return innerdata;
	}
	public void setInnerdata(byte[] innerdata) {
		this.innerdata = innerdata;
	}
	public byte[] getHeader() {
		return header;
	}
	public void setHeader(byte[] header) {
		this.header = header;
	}

	public InitPacket(){
		header = new byte[3];
		intForFirstUse(header);




	}
    public void createPacket() throws Exception{
    	if(longPacket==true){
    		header[0] = TransferSpecification.STX;
    		packet = new byte[1029];
    		innerdata = new byte[1024];
    	}
    	else{
    		header[0] = TransferSpecification.SOH;
    		packet = new byte[133];
    		innerdata = new byte[128];
    	}
    	if(isPacketInit==true){
    		if(packetnumber!=0){
    			throw new Exception("Packet number should beginn with 0");
    		}
    		else if(fileName.length() > 1023){
    			throw new Exception("Filename too large");
    		}
    		else{
    			header[1] =(byte)packetnumber;
    			header[2] = (byte)(0xff - packetnumber);
    			System.arraycopy(ConvertChartoByteArray(fileName.toCharArray()), 0, innerdata, 0, fileName.length());
    			System.arraycopy(ConvertChartoByteArray(Integer.toString(fileLength).toCharArray()), 0, innerdata, fileName.length()+1, Integer.toString(fileLength).toCharArray().length);
    			System.arraycopy(innerdata, 0, packet, 3, innerdata.length);
    			//crcfooter = crc.update(innerdata);
    			System.arraycopy(header, 0, packet, 0, 3);
    			System.arraycopy(crcfooter, 0, packet, packet.length-2, 2);

    		}
    	}
    	else if(isEnd){
    		if(packetnumber !=0){
    			throw new Exception("end can only have packetnum 0");
    		}
    		else{
    			System.arraycopy(data, 0, innerdata, 0, data.length);
    			header[1]=(byte)packetnumber;
    			header[2]=(byte)(0xff-packetnumber);
    			//crcfooter = crc.update(innerdata);
    			System.arraycopy(innerdata, 0, packet, 3, 128);
    			System.arraycopy(header, 0, packet, 0, 3);
    			System.arraycopy(crcfooter, 0, packet, packet.length-2, 2);
    		}
    	}
    	else if(packetnumber ==0){
    		throw new Exception("data can't be in packet one");
    	}
    	else{
    		System.arraycopy(data, 0, innerdata, 0, data.length);
    		header[1]=(byte)packetnumber;
			header[2]=(byte)(0xff-packetnumber);
			//crcfooter = crc.update(innerdata);
			System.arraycopy(innerdata, 0, packet, 3, 1024);
			System.arraycopy(header, 0, packet, 0, 3);
			System.arraycopy(crcfooter, 0, packet, packet.length-2, 2);
    	}
    }

    private byte[] ConvertChartoByteArray(char[] input)
    {
        byte[] output = new byte[input.length];
        for (int i = 0; i < output.length; i++)
        {
            output[i] = (byte)input[i];
        }
        return output;
    }
    private void intForFirstUse(byte[] bytes){
    	for(int i=0;i<bytes.length;i++){
    		bytes[i] =0x00;
    	}
    }

}
