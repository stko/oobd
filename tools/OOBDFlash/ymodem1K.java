/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ymodem.transfer;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;
//import org.apache.log4j.Logger;
import purejavacomm.CommPortIdentifier;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.UnsupportedCommOperationException;
//import org.apache.log4j.BasicConfigurator;
import ymodemguijava.*;
/**
 *
 * @author maziar
 */
public class SendData {
    private Enumeration enumComm;
    private SerialPort serialPort;
    private CommPortIdentifier serialPortId;
    private OutputStream outputStream;
    private boolean serialPortGeoeffnet;
    private boolean isFirstSector=true;
    final int baudrate=115200;
    final int parity =serialPort.PARITY_NONE;
    final int stopBits=serialPort.STOPBITS_1;
    int flowcontrol = purejavacomm.SerialPort.FLOWCONTROL_NONE;
    final int dataBits=8;
  //  static Logger logger = Logger.getLogger(SendData.class);
    static boolean isEnd = false;
    static int counter =0;
    private InputStream inputStream;
    private File file;
    private FileInputStream ins;
    private String portName;
    private boolean isPortOpen = false;
    private static String feedback;
    private YModemGUIJAVAView  ym;
    private char in;
    private InitPacket init = new InitPacket();
    int errorcunter = 0;
    private boolean END=false;
    public static boolean  read= false;
    public void setPortname(String comport){
        this.portName= comport;
    }
    public void setFilePath(String path){
        this.file= new File(path);
    }
    public static String getFeedback(){
        return feedback;
    }


    public boolean getOpenPort(){
        return isPortOpen;
    }
    public SendData(String comPort, String path){
        this.portName = comPort;
        this.file = new File(path);

    }
    public SendData(String comPort){
        this.portName = comPort;
    }
    public SendData(){
        
    }

   public void findAllSerialport(){
       enumComm = CommPortIdentifier.getPortIdentifiers();
       while(enumComm.hasMoreElements()){
           serialPortId =(CommPortIdentifier)enumComm.nextElement();
           if(serialPortId.getPortType()==CommPortIdentifier.PORT_SERIAL){
               
           }
       }
   }
   public  boolean oeffneSerialPort()
	{
		Boolean foundPort = false;
		if (serialPortGeoeffnet != false) {
			System.out.println("Serialport bereits geÃ¶ffnet");
                        feedback ="Serialport bereits geÃ¶ffnet";
			return false;
		}
		System.out.println("Ã–ffne Serialport");
                feedback ="Ã–ffne serialport";
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (foundPort != true) {
			System.out.println("Serialport nicht gefunden: " + portName);
                        feedback ="Serialport nicht gefunden: " + portName;
			return false;
		}
		try {
			serialPort = (SerialPort) serialPortId.open("Ã–ffnen und Senden", 500);
		} catch (PortInUseException e) {
			System.out.println("Port belegt");
                        feedback ="Port belegt";
		}
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
                          feedback ="Keinen Zugriff auf OutputStream";
		}
                try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf InputStream");
		}

		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
                        serialPort.setFlowControlMode(flowcontrol);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
                        feedback ="Konnte Schnittstellen-Paramter nicht setzen";
		}
                try {
			serialPort.addEventListener(new PortEvetntListener());
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException fÃ¼r Serialport");
		}
		serialPort.notifyOnDataAvailable(true);

		serialPortGeoeffnet = true;
		return true;
	}

     public void schliesseSerialPort()
	{
		if ( serialPortGeoeffnet == true) {
			System.out.println("SchlieÃŸe Serialport");
                         feedback ="SchlieÃŸe Serialport";
			serialPort.close();
			serialPortGeoeffnet = false;
		} else {
			System.out.println("Serialport bereits geschlossen");
                        feedback ="Serialport bereits geschlossen";
		}
	}

     private  void serialPortDatenVerfuegbar() {
		try {
			byte[] data = new byte[50];
			int num;
			while(inputStream.available() > 0) {
				num = inputStream.read(data, 0, data.length);
                                if(read = true){
                                    YModemGUIJAVAView.jTextArea1.setText(new String(data));
                                    
                                }
                                else{
                                in =(char) data[0];
                                System.out.print("Daten vorhanden");
				System.out.println("Empfange: "+ new String(data, 0, num));
                                //YModemGUIJAVAView.jTextText = new String(data);
                               // YModemGUIJAVAView.jTextArea1.setText(new String(data));
                                YModemGUIJAVAView.jTextArea1.setText(Character.toString(in));
                            }
			}
                        
		} catch (IOException e) {
			System.out.println("Fehler beim Lesen empfangener Daten");
		}
	}
     private void sendDataToSerialPort(byte[] data){
         try{
             for(int i = 0; i< data.length;i++){
                 outputStream.write(data[i]);
             }
             //outputStream.write(data);
         }catch(IOException ex){}
     }

      // sende die Nachrichten um im reset modus zu gehen
    public void sendeStringSerialPort(String nachricht)
	{

		System.out.println("Sende: " + nachricht);
		if (serialPortGeoeffnet != true)
			return;
		try {
			outputStream.write(nachricht.getBytes());
		} catch (IOException e) {
			System.out.println("Fehler beim Senden");
		}
	}
    private void waitForResetDMX(){
        sendeStringSerialPort(TransferSpecification.DXM_Bluetooth_reset);
        for(int i = 0; i<5;i++){
            sendeStringSerialPort(TransferSpecification.BootloaderLetter);
        }
        sendeStringSerialPort(TransferSpecification.DMX_Bluetooth_init_Mode);
        while(!isC()){
            
            if(isC())
                break;
        }
    }
    private void restartDMX(){
         sendeStringSerialPort(TransferSpecification.DMX_RESTART);
    }
    public void updateFirmware(){
        //sendeStringSerialPort(TransferSpecification.DXM_Bluetooth_reset);
        //for(int i = 0; i<5;i++){
          //  sendeStringSerialPort(TransferSpecification.BootloaderLetter);
        //}
               
           // sendNotFirstSector();
        waitForResetDMX();
        if((isC()&& isFirstSector==true) && !END){
            SendFirstsector();
            sendNotFirstSector();
           updateFirmware();
        }
        
       // else if((isEnd&& isFirstSector==false)&&!END)
          //  sendLastSector();
       
            
        
        else if(errorcunter==TransferSpecification.MAXERRORS)
            YModemGUIJAVAView.jTextArea1.setText("Keine Acknolge");
        else
            YModemGUIJAVAView.jTextArea1.setText("Keine Acknolge");
    }
    // prÃ¼fen on Acknolege zurÃ¼ckkommt
    private boolean isAck(){
        if(in==TransferSpecification.ACK)
            return true;
        else
            return false;
    }
    private boolean isC(){
        if(in==TransferSpecification.C)
            return true;
        else
            return false;
    }
    private void sendNotFirstSector(){
        try{
            int b = 0;
            //int k = 1;
            ins = new FileInputStream(file);
            //int packet = ins.available()/128;
            //int positon = ins.available()%128;
            byte[] sector = new byte[TransferSpecification.sizeSectorNonZero];
            int blocknumber = 1;
            errorcunter = 0;
            init.setPacknumbet(blocknumber);
            
            
            
            
            while(!isC() && errorcunter <TransferSpecification.MAXERRORS){
                if(!isC() && errorcunter < TransferSpecification.MAXERRORS)
                    errorcunter++;
            }
            if(errorcunter == TransferSpecification.MAXERRORS){
                System.out.println("Maxx error");
            }
             
            int nBytes = ins.read(sector);
            errorcunter =0;
            byte[] k = new byte[1029];
            while(nBytes !=-1){
                
                if(nBytes < TransferSpecification.sizeSectorNonZero){
                    init.setPacknumbet(blocknumber);
                   // for(int k=0;k<11;k++){
                        sector[nBytes]= TransferSpecification.CPMEOF;
                        sendLastSector(init.initSector(sector));
                        ins.close();
                        isEnd=true;
                        break;
                    //}
                   // nBytes=-1;
                }
                
                //k = init.initSector(sector);
                
                
                sendDataToSerialPort(init.initSector(sector));
                
                while(!isAck() && errorcunter <TransferSpecification.MAXERRORS){
                    //sendDataToSerialPort(init.initSector(sector));
                    if(!isAck())
                        ++errorcunter;
                    else
                        break;
                }
                 
                for(int z= 0; z< sector.length ;z++){
                    sector[z]=0;
                }
                if(errorcunter == TransferSpecification.MAXERRORS){
                    System.out.println("Max error");
                    break;
                }
                init.setPacknumbet(++blocknumber);
                System.out.println(blocknumber);
                nBytes = ins.read(sector);
            }/*
            do {
                if(k==packet){
                    b = ins.read(c);
                    
                    sendDataToSerialPort(init.initLastSector(c, positon));
                    break;
                }
                else{
                b = ins.read(c);
                 if (b != -1) {
                     init.setPacknumbet(k);
                     k++;
                 sendDataToSerialPort(init.initSectorsmall(c));
                 }
                }
            } while (b != -1 && isAck());
              * 
              */
            
        }catch(IOException ex){}

    }
    private void sendLastSector(byte[] data){
        byte[] k = new byte[133];
        sendDataToSerialPort(data);
        sendDataToSerialPort(new byte[]{TransferSpecification.EOT});
        k= init.initLastSector();
        sendDataToSerialPort(init.initLastSector());
        END=true;
        restartDMX();
        //schliesseSerialPort();
    }
    private void SendFirstsector(){
       
        sendDataToSerialPort(init.initFirstSector(file));
         if(in!=TransferSpecification.ACK && counter <TransferSpecification.MAXERRORS){
             SendFirstsector();
             counter++;
        }
         else if(counter == TransferSpecification.MAXERRORS)
              YModemGUIJAVAView.jTextArea1.setText("Time out to send");
         else if(in!=TransferSpecification.ACK)
             YModemGUIJAVAView.jTextArea1.setText("Not ACK");
         else{
            counter = 0;
            isFirstSector=false;
         }
    }


     class PortEvetntListener implements purejavacomm.SerialPortEventListener{
          public void serialEvent(SerialPortEvent event) {
              System.out.println("serialPortEventlistener");
			switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				serialPortDatenVerfuegbar();
				break;
			case SerialPortEvent.BI:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.FE:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			case SerialPortEvent.PE:
			case SerialPortEvent.RI:
			default:
			}
          }
    }


}





final class TransferSpecification {
    public static final int poly = 0x1021;
    public static final int initValueCRCModem = 0x0;
    public static final byte SOH=0x01;
    public static final byte STX=0x02;
    public static final byte EOT=0x04;
    public static final byte ACK=0x06;
    public static final byte NAK=0x15;
    public static final byte CAN=0x18;
    public static final byte G=0x47;
    public static final byte C=0x43;
    public static final byte CPMEOF = 26; //0x1a
    public static final int waitForBootloaderTimeout = 500;
    public static final String DMX_Bluetooth_init_Mode = "1";
    public static final String DMX_RESTART="3";
    public static final String FirmwareLevel ="p 0 0";
    public static final String BootloaderLetter = "f";
    public static final String DXM_Bluetooth_reset="p 99 2\r\n";
    public static final String DMX_Bluetooth_readFirmware ="p 0 0\r\n";
    public static final String BootloaderLetter_help="h";
    public static final int sizeSectorZero = 128;  //<soh><00><ff><128 data><crchigh><crclow>
    public static final int sizeSectorNonZero = 1024;
    public static final int MAXERRORS = 90;
    public static final long MAXSENDTIMEOUT=300;
    public static final byte[] END = new byte[]{0x04,0x04,0x04,0x04,0x04,0x04};

}




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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ymodem.transfer;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * RReads in a sequence of bytes and prints out its 16 bit
 *  Cylcic Redundancy Check (CRC-CCIIT 0xFFFF).
 *
 *  1 + x + x^5 + x^12 + x^16 is irreducible polynomial. 0x1021
 */
/**
 *
 * @author maziar
 */
/**
 * RReads in a sequence of bytes and prints out its 16 bit
 * Cylcic Redundancy Check
 * 1 + x + x^5 + x^12 + x^16 is irreducible polynomial.
 * For CRC-CCITT  polynomial = 0x1021, Initial Value = 0xffff;
 * For CRC-16  polynomial = 0xa001, Initial Value = 0xffff;
 * For CRC-XModem  polynomial = 0x1021, Initial Value = 0x0; is used here
 * @author OOBD.org
 * @version 1.0
 */
public class CRC16 {

     private int intCrcLow;
    private int intCrcHigh; // get the
    private int initValue=TransferSpecification.initValueCRCModem;  //0x0
   /**!
     * Get the low CRC-Bit
     * @return low CRC-Bit
     */
    public int getCRCLow(){
        return intCrcLow;
    }
    /**! get the high crc bit
     *
     * @return HIgh CRC-Bit
     */
    public int getCRCHigh(){
        return intCrcHigh;
    }
    /**! give the calculate crc
     * Gives the Calculate the CRC
     * @return  CRC as integer
     */
    public int getCRC(){
        return initValue;
    }
    /**!
     * Reset the initValue before claculate the crc
     */
    public void resetInitValue(){
        initValue = TransferSpecification.initValueCRCModem;  //0x0
    }
    /**!
     * Calculate the CRC from a Byte array
     * @param value The byte value which has to ben calculate
     */
     public void CRCCalculate(byte[] value){
        for (byte b : value) {
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b   >> (7-i) & 1) == 1);
				boolean c15 = ((initValue >> 15    & 1) == 1);
				initValue <<= 1;
				if (c15 ^ bit)
                        initValue ^= TransferSpecification.poly;
               }
			}
        initValue &= 0xffff;
        intCrcHigh= (initValue>>8)& 0x00ff;
        //intCrcHigh= (initValue>>8);
        intCrcLow = initValue&0xff;

        /*
        System.out.println("CRC16 = " +Integer.toHexString(initValue));
        System.out.println("Highbit   " + intCrcHigh);
        System.out.println("Lowbit   " + intCrcLow);
		*/

     }

}
