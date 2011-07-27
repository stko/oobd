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
//import javax.swing.Timer;
//import javax.comm.*;// for SUN's serial/parallel port libraries
//import gnu.io.*; // for rxtxSerial library
import purejavacomm.*;

public class BusCom extends OobdBus implements OOBDConstants {

    static CommPortIdentifier portId;
    static CommPortIdentifier saveportId;
    static Enumeration portList;
    InputStream inputStream;
    SerialPort serialPort;
    Thread readThread;
    static String messageString = "Hello, world!";
    static OutputStream outputStream;
    static boolean outputBufferEmptyFlag = false;

    public BusCom() {
        super("Buscom");
    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
    }

    @Override
    public String getPluginName() {
        return "b:Com";
    }

    public void run() {

        boolean portFound = false;
        String defaultPort;
        ComReader reader = new ComReader();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("buscom.props"));
        } catch (IOException ignored) {
        }

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
             Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,"OS os not supported");
            return;
        }

        defaultPort = props.getProperty("SerialPort", defaultPort);
        boolean hwFlowControl=props.getProperty("HardwareFlowControl", "true").equalsIgnoreCase("true");

        // parse ports and if the default port is found, initialized the reader
        // first set a workaround to find special devices like ttyACM0 , accourding to https://bugs.launchpad.net/ubuntu/+source/rxtx/+bug/367833
        System.setProperty("gnu.io.rxtx.SerialPorts", defaultPort);
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(defaultPort)) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.CONFIG,"Found port: " + defaultPort);
                    portFound = true;
                    try {
                        reader.connect(portId,hwFlowControl);
                    } catch (IOException ex) {
                        Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
        if (!portFound) {
            Logger.getLogger(BusCom.class.getName()).log(Level.WARNING,"serial port " + defaultPort + " not found.");
        }
        while (keepRunning == true) {
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            String command = on.getOnionString("command");
            if ("serWrite".equalsIgnoreCase(command)) {
                String data =Base64Coder.decodeString(on.getOnionString("data"));
                Logger.getLogger(BusCom.class.getName()).log(Level.INFO,"busCom serWrite: >" + data+"<");
                reader.write(data);
            } else if ("serFlush".equalsIgnoreCase(command)) {
               Logger.getLogger(BusCom.class.getName()).log(Level.INFO,"busCom serFlush ");
                 reader.flush();

            } else if ("serWait".equalsIgnoreCase(command)) {
                try {
                    Integer result = reader.wait(Base64Coder.decodeString(on.getOnionString("data")), on.getInt("timeout"));
                    Logger.getLogger(BusCom.class.getName()).log(Level.INFO,"busCom serWait: " + result);
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':" + result + ","
                            + "'replyID':" + on.getInt("msgID")
                            + "}"));
                } catch (JSONException ex) {
                   Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }


            } else if ("serReadLn".equalsIgnoreCase(command)) {
                try {
                    String result = reader.readln(on.getInt("timeout"), on.optBoolean("ignore"));
                    Logger.getLogger(BusCom.class.getName()).log(Level.INFO,"busCom readline: " + result);
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'replyID':" + on.getInt("msgID") + ","
                             + "'result':'" + Base64Coder.encodeString(result) + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
