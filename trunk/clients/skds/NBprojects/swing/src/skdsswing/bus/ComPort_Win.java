/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skdsswing.bus;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.support.Onion;
//import gnu.io.*; // for rxtxSerial library
import purejavacomm.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author steffen
 */
public class ComPort_Win implements OOBDPort {

    CommPortIdentifier portId;
    CommPortIdentifier saveportId;
    Enumeration portList;
    InputStream inputStream;
    OutputStream outputStream;
    SerialPort serialPort;

    public boolean available() {
        try {
            return inputStream != null && inputStream.available() > 0;
        } catch (IOException ex) {
            return false;
        }
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean connect(Onion options) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("buscom.props"));
        } catch (IOException ignored) {
        }
        String defaultPort = "";
        Boolean portFound = false;

        // determine the name of the serial port on several operating systems
        String osname = System.getProperty("os.name", "").toLowerCase();
        if (osname.startsWith("windows")) {
            // windows
            defaultPort = "COM1";
        } else if (osname.startsWith("linux")) {
            // linux
            defaultPort = "/dev/ttyS0";
        } else if (osname.startsWith("mac")) {
            // mac
            defaultPort = "????";
        } else {
            Logger.getLogger(ComPort_Win.class.getName()).log(Level.SEVERE, "OS os not supported");
            return false;
        }

        defaultPort = props.getProperty("SerialPort", defaultPort);
        boolean hwFlowControl = props.getProperty("HardwareFlowControl", "true").equalsIgnoreCase("true");

        // parse ports and if the default port is found, initialized the reader
        // first set a workaround to find special devices like ttyACM0 , accourding to https://bugs.launchpad.net/ubuntu/+source/rxtx/+bug/367833
        System.setProperty("gnu.io.rxtx.SerialPorts", defaultPort);
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(defaultPort)) {
                    Logger.getLogger(ComPort_Win.class.getName()).log(Level.CONFIG, "Found port: " + defaultPort);
                    portFound = true;

                    try {
                        serialPort = (SerialPort) portId.open("OOBD", 2000);

                        serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
//            if (hwFlowControl == true) {
//                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
//            } else {
//                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
//            }
                        serialPort.enableReceiveTimeout(5);

                        inputStream = serialPort.getInputStream();
                        // inStreamReader = new InputStreamReader(inStream);
                        outputStream = serialPort.getOutputStream();
                        //outStreamWriter = new OutputStreamWriter(outStream, "iso-8859-1");
                        serialPort.enableReceiveTimeout(5);
                        return true;
                    } catch (UnsupportedCommOperationException ex) {
                        Logger.getLogger(ComPort_Win.class.getName()).log(Level.SEVERE, "Unsupported serial port parameter", ex);
                        return false;

                    } catch (PortInUseException ex) {
                        Logger.getLogger(ComPort_Win.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return false;
                    }


                }
            }

        }
        if (!portFound) {
            Logger.getLogger(ComPort_Win.class.getName()).log(Level.WARNING, "serial port " + defaultPort + " not found.");
        }
        return portFound;
    }

    public boolean close() {
        if (serialPort != null) {
            try {
                inputStream.close();
                inputStream = null;
                outputStream.close();
                outputStream = null;
                serialPort.close();
                serialPort = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
