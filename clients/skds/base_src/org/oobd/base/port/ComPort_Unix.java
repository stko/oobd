/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.port.PortInfo;
import org.oobd.base.support.Onion;
import gnu.io.*; // for rxtxSerial library
//import purejavacomm.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author steffen
 */
public class ComPort_Unix implements OOBDPort {

    CommPortIdentifier portId;
    CommPortIdentifier saveportId;
    Enumeration portList;
    InputStream inputStream;
    OutputStream outputStream;
    SerialPort serialPort;

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean connect(Onion options) {
        Properties props = Core.getSingleInstance().getSystemIF().loadProperty(OOBDConstants.FT_RAW, OOBDConstants.AppPrefsFileName);
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
            Logger.getLogger(ComPort_Unix.class.getName()).log(Level.SEVERE, "OS os not supported");
            return false;
        }

        defaultPort = props.getProperty(OOBDConstants.PropName_SerialPort, defaultPort);
        boolean hwFlowControl = props.getProperty("HardwareFlowControl", "true").equalsIgnoreCase("true");

        // parse ports and if the default port is found, initialized the reader
        // first set a workaround to find special devices like ttyACM0 , accourding to https://bugs.launchpad.net/ubuntu/+source/rxtx/+bug/367833
        System.setProperty("gnu.io.rxtx.SerialPorts", defaultPort);
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(defaultPort)) {
                    Logger.getLogger(ComPort_Unix.class.getName()).log(Level.CONFIG, "Found port: " + defaultPort);
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
                         attachShutDownHook();
                       return true;
                    } catch (UnsupportedCommOperationException ex) {
                        Logger.getLogger(ComPort_Unix.class.getName()).log(Level.SEVERE, "Unsupported serial port parameter", ex);
                        return false;

                    } catch (PortInUseException ex) {
                        Logger.getLogger(ComPort_Unix.class.getName()).log(Level.SEVERE, null, ex);
                        return false;
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return false;
                    }


                }
            }

        }
        if (!portFound) {
            Logger.getLogger(ComPort_Unix.class.getName()).log(Level.WARNING, "serial port " + defaultPort + " not found.");
        }
        return portFound;
    }

    public boolean available() {
        try {
            return inputStream != null && inputStream.available() > 0;
        } catch (IOException ex) {
            // broken socket: Close it..
            resetConnection();
            return false;
        }
    }

    public OOBDPort close() {
        if (serialPort != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
                outputStream = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                serialPort.close();
                serialPort = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public OOBDPort resetConnection() {
        return close();
    }

    public PortInfo[] getPorts() {
        Vector<PortInfo> portVector = new Vector();

                portList = CommPortIdentifier.getPortIdentifiers();
                if (portList == null || !portList.hasMoreElements()){
				PortInfo[] DeviceSet = new PortInfo[1];
				DeviceSet[0] = new PortInfo("", "No Comports found :-(");
				return DeviceSet;

                }
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                portVector.add(new PortInfo("",portId.getName()));
            }

        }
               ArrayList<PortInfo> myList= Collections.list(portList);
               return (PortInfo[])myList.toArray();

    }

    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.out.println("Inside Add Shutdown Hook");
                close();
                System.out.println("Serial line closed");
            }
        });
        System.out.println("Shut Down Hook Attached.");

    }
}
