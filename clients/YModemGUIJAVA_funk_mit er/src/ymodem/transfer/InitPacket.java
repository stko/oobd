/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ymodem.transfer;

/**
 *
 * @author maziar
 */
import java.io.File;
import java.io.ByteArrayInputStream;
//import org.apache.log4j.Logger;

/**!
 * This Class creat the packet for transfer according the ymodem protocoll YMODEM 1K
 * in first step creat the telegramm  [SOH 00 FF "Filename" NULL[128 - Filename.length] CRC CRC]
 *XMODEM-1k blocks, CRC mode
*--------------------------
*   SENDER                                      RECEIVER
*                                          <-- C (command:rb)
*   SOH 00 FF foo.c NUL[123] CRC CRC        -->
*                                           <-- ACK
*                                           <-- C
*   STX 01 FE Data[1024] CRC CRC            -->
*                                           <-- ACK
*   STX 02 FD Data[1024] CRC CRC            -->
*                                           <-- ACK
*   STX 03 FC Data[1000] CPMEOF[24] CRC CRC -->
*                                           <-- ACK
*   EOT  ( 10 times)                       -->
*                                           <-- ACK
*
 * second step creat the telgramm [STX 0X !n DATA[1024] CRC CRC]
 * @author OOBD.org
 * @version 1.0
 */
public class InitPacket {
    private int packetnumber;
    private boolean isPacketInit;
    private boolean isEnd;
    private boolean isSectorZero = true;
    private byte[] packet;
    private byte[] data ;
    private byte[] innerdata;
    private byte[] header = new byte[3];
    private byte[] crcfooter = new byte[2];
    private int checksum = 0;
    private int intCrcLow;
    private int intCrcHigh;
    private File file;
    //static Logger log = Logger.getLogger(InitPacket.class);
    //private CRC16 crc = new CRC16(TransferSpecification.initValueZero);
   private CRC16 crc = new CRC16();
   /**!
    * Gives back the Low-Byte CRC
    * @return the Sector
    */
    private int getCRCLow(){
        return intCrcLow;
    }
    /**!
    * Gives back the High-Byte CRC
    * @return the Sector
    */
    private int getCRCHigh(){
        return intCrcHigh;
    }
    /**!
     * Gives information about the Sector Zero
     * @return is the sector Zero
     */
    public boolean getSectorZero(){
        return isSectorZero;
    }
    public void setPacknumbet(int packnum){
        this.packetnumber = packnum;
    }
  
    /**!
     * Build the Sector Zero
     * @param file to create the Filename and File Size and the CRC
     * @return the Sector Zero as Byte array with the length 128 Bytes
     * File file
     */
        public byte[] initFirstSector(File file){
            header[0] = TransferSpecification.SOH;
            header[1] = 0;
            header[2] = (byte)255;
            byte[] packet = new byte[133];
            crc.resetInitValue();
            innerdata = new byte[TransferSpecification.sizeSectorZero];  //128
             // copy the file name
            System.arraycopy(ConvertChartoByteArray(file.getName().toLowerCase().toCharArray()),0, innerdata, 0,file.getName().length());
            // copy the file size
            System.arraycopy(ConvertChartoByteArray(Long.toString(file.length()).toCharArray()),0, innerdata, file.getName().length()+1, Long.toString(file.length()).toCharArray().length);
            System.arraycopy(innerdata, 0, packet, 3, innerdata.length);
            System.arraycopy(header, 0, packet, 0, header.length);
            // Calculate the CRC
            crc.CRCCalculate(innerdata);
            //checksum = crc.getCRC();
            //intCrcHigh= (checksum>>8);
            //intCrcLow = (checksum&0x00ff);
            packet[131]= (byte)(crc.getCRCHigh());
            packet[132]= (byte)(crc.getCRCLow());
            //System.out.println("Module init first package!!");
            //log.debug("Module init first package");
            return packet;
            }
       /**
         * Create the Non zero sector
         * @param binaryfile
         * @return the Non Zero Sector
         */
        public byte[] initSector(byte[] binaryfile){
            byte[] packet = new byte[1029];
            header[0] = TransferSpecification.STX;
            header[1] = (byte)packetnumber;
            header[2] = (byte)(255-packetnumber);
            innerdata = new byte[TransferSpecification.sizeSectorNonZero];
            System.arraycopy(binaryfile, 0, innerdata, 0, binaryfile.length);
            crc.resetInitValue();
            crc.CRCCalculate(innerdata);
            checksum = crc.getCRC();
            //intCrcHigh= ((checksum>>8)& 0x00ff);
            //intCrcLow = (checksum&0xff); //0x00ff
            System.arraycopy(innerdata, 0, packet, 3, innerdata.length);
            System.arraycopy(header, 0, packet, 0, header.length);
            packet[1027] = (byte)(crc.getCRCHigh());
            packet[1028] = (byte)(crc.getCRCLow());
            //System.out.println("Module init package!!");
            //log.debug("Module init package:" + packetnumber);
            return packet;

        }
           public byte[] initSectorsmall(byte[] binaryfile){
            byte[] packet = new byte[133];
            header[0] = TransferSpecification.SOH;
            header[1] = (byte)packetnumber;
            header[2] = (byte)(255-packetnumber);
            innerdata = new byte[TransferSpecification.sizeSectorZero];
            System.arraycopy(binaryfile, 0, innerdata, 0, binaryfile.length);
            crc.resetInitValue();
            crc.CRCCalculate(innerdata);
            checksum = crc.getCRC();
            //intCrcHigh= ((checksum>>8)& 0x00ff);
            //intCrcLow = (checksum&0xff); //0x00ff
            System.arraycopy(innerdata, 0, packet, 3, innerdata.length);
            System.arraycopy(header, 0, packet, 0, header.length);
            packet[131] = (byte)(crc.getCRCHigh());
            packet[132] = (byte)(crc.getCRCLow());
           // System.out.println("Module init package!!");
            //log.debug("Module init package:" + packetnumber);
            return packet;

        }
        
        public byte[] initLastSector(){
            header[0] = TransferSpecification.SOH;
            header[1] = 0;
            header[2] = (byte)255;
            byte[] packet = new byte[133];
            for(int i = 0;i< packet.length;i++){
                packet[i]=0;
            }
            crc.resetInitValue();
            innerdata = new byte[TransferSpecification.sizeSectorZero];  //128
             // copy the file name
            //System.arraycopy(ConvertChartoByteArray(file.getName().toLowerCase().toCharArray()),0, innerdata, 0,file.getName().length());
            // copy the file size
            //System.arraycopy(ConvertChartoByteArray(Long.toString(file.length()).toCharArray()),0, innerdata, file.getName().length()+1, Long.toString(file.length()).toCharArray().length);
            System.arraycopy(innerdata, 0, packet, 3, innerdata.length);
            System.arraycopy(header, 0, packet, 0, header.length);
            // Calculate the CRC
            crc.CRCCalculate(innerdata);
            //checksum = crc.getCRC();
            //intCrcHigh= (checksum>>8);
            //intCrcLow = (checksum&0x00ff);
            packet[131]= (byte)(crc.getCRCHigh());
            packet[132]= (byte)(crc.getCRCLow());
            //System.out.println("Module init first package!!");
            //log.debug("Module init first package");
            return packet;
        }
         /*
        public byte[] initLastSector(byte[] binaryfile, int position){
            byte[] packet = new byte[1024];
            header[0] = TransferSpecification.STX;
            header[1] = (byte)packetnumber;
            header[2] = (byte)(packetnumber-255);
            innerdata = new byte[TransferSpecification.sizeSectorNonZero];
            crc.resetInitValue();
            System.arraycopy(binaryfile, 0, innerdata, 0, binaryfile.length);
            for(int i = position; i< 11; i++){
            	innerdata[i] = TransferSpecification.CPMEOF;
            }
            crc.CRCCalculate(innerdata);
            checksum = crc.getCRC();
            intCrcHigh= (checksum>>8);
            intCrcLow = (checksum&0x00ff);
            System.arraycopy(innerdata, 0, packet, 3, innerdata.length);
            System.arraycopy(header, 0, packet, 0, header.length);
            packet[131] = (byte)intCrcHigh;
            packet[132] = (byte)intCrcLow;
            //return innerdata;
            System.out.println("Module init last package!!");
            //log.debug("Module init last package");
            return packet;

        }
         * 
         */

         /**!
         * Convert A chararray to a byte array
         * @param input Char array
         * @return  Byte Array
         */
    private byte[] ConvertChartoByteArray(char[] input)
    {
        byte[] output = new byte[input.length];
        for (int i = 0; i < output.length; i++)
        {
            output[i] = (byte)input[i];
        }
        return output;
    }


}
