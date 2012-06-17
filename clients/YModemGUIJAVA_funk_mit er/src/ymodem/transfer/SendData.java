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
			System.out.println("Serialport bereits geöffnet");
                        feedback ="Serialport bereits geöffnet";
			return false;
		}
		System.out.println("Öffne Serialport");
                feedback ="Öffne serialport";
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
			serialPort = (SerialPort) serialPortId.open("Öffnen und Senden", 500);
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
			System.out.println("TooManyListenersException für Serialport");
		}
		serialPort.notifyOnDataAvailable(true);

		serialPortGeoeffnet = true;
		return true;
	}

     public void schliesseSerialPort()
	{
		if ( serialPortGeoeffnet == true) {
			System.out.println("Schließe Serialport");
                         feedback ="Schließe Serialport";
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
    // prüfen on Acknolege zurückkommt
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
