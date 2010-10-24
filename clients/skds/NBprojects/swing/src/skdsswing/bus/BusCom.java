
package skdsswing.bus;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.bus.*;
import org.oobd.base.support.Onion;
import org.json.JSONException;

import java.io.*;
import java.io.IOException;
import java.util.*;
import javax.swing.Timer;
//import javax.comm.*;// for SUN's serial/parallel port libraries
import gnu.io.*; // for rxtxSerial library



public class BusCom extends OobdBus implements OOBDConstants{

  static CommPortIdentifier portId;
   static CommPortIdentifier saveportId;
   static Enumeration        portList;
   InputStream           inputStream;
   SerialPort           serialPort;
   Thread           readThread;

   static String        messageString = "Hello, world!";
   static OutputStream      outputStream;
   static boolean        outputBufferEmptyFlag = false;



    public BusCom() {
        Debug.msg("buscom",DEBUG_BORING,"Ich bin BusCom...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("busecho",DEBUG_BORING,"Core registered...");
    }

    @Override
    public String getPluginName() {
        return "b:Com";
    }

    public void run() {

             boolean           portFound = false;
      String           defaultPort;
      		Properties props = new Properties();
		try {
			props.load(new FileInputStream("klobsserial.props"));
		}
catch (IOException ignored) {}

      // determine the name of the serial port on several operating systems
      String osname = System.getProperty("os.name","").toLowerCase();
      if ( osname.startsWith("windows") ) {
         // windows
         defaultPort = "COM1";
      } else if (osname.startsWith("linux")) {
         // linux
        defaultPort = "/dev/ttyS0";
      } else if ( osname.startsWith("mac") ) {
         // mac
         defaultPort = "????";
      } else {
         System.out.println("Sorry, your operating system is not supported");
         return;
      }

                serPortName = props.getProperty("SerialPort",defaultPort);

      System.out.println("Set default port to "+defaultPort);

		// parse ports and if the default port is found, initialized the reader
      portList = CommPortIdentifier.getPortIdentifiers();
      while (portList.hasMoreElements()) {
         portId = (CommPortIdentifier) portList.nextElement();
         if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            if (portId.getName().equals(defaultPort)) {
               System.out.println("Found port: "+defaultPort);
               portFound = true;
               // init reader thread
               nulltest reader = new nulltest();
            }
         }

      }
      if (!portFound) {
         System.out.println("port " + defaultPort + " not found.");
      }

   }





        while (keepRunning == true) {
            Debug.msg("buscom", DEBUG_BORING, "sleeping...");
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            Debug.msg("buscom", DEBUG_BORING, "Msg received:" + msg.getContent().toString());
            try {

                replyMsg(msg,new Onion(""
                        + "{'type':'" + CM_RES_BUS + "',"
                        + "'owner':"
                        + "{'name':'" + getPluginName() + "'},"
                         + "'result':'" + "" + "'}"));
            } catch (JSONException ex) {
                Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
            }
            Debug.msg("buscom", DEBUG_BORING, "waked up after received msg...");

        }
    }
}
