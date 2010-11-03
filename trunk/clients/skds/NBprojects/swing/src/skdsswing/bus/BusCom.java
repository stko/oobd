package skdsswing.bus;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.bus.*;
import org.oobd.base.support.Onion;
import org.json.JSONException;
import sun.misc.*;


import java.io.*;
import java.io.IOException;
import java.util.*;
import javax.swing.Timer;
//import javax.comm.*;// for SUN's serial/parallel port libraries
import gnu.io.*; // for rxtxSerial library

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
        Debug.msg("buscom", DEBUG_BORING, "Ich bin BusCom...");

    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("busecho", DEBUG_BORING, "Core registered...");
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
            System.out.println("Sorry, your operating system is not supported");
            return;
        }

        defaultPort = props.getProperty("SerialPort", defaultPort);

        System.out.println("Set default port to " + defaultPort);

        // parse ports and if the default port is found, initialized the reader
        // first set a workaround to find device ttyACM0 , accourding to https://bugs.launchpad.net/ubuntu/+source/rxtx/+bug/367833
        System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0");
        System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM1");
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            System.out.println("Scan port: " + portId.getName() + "ID:"+portId.getPortType());
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (portId.getName().equals(defaultPort)) {
                    System.out.println("Found port: " + defaultPort);
                    portFound = true;
                    // init reader thread

                    reader.connect(portId);
                }
            }

        }
        if (!portFound) {
            System.out.println("port " + defaultPort + " not found.");
        }
        while (keepRunning == true) {
            Debug.msg("buscom", DEBUG_BORING, "sleeping...");
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            Debug.msg("buscom", DEBUG_BORING, "Msg received:" + msg.getContent().toString());
            String command = on.getOnionString("command");
            if ("serWrite".equalsIgnoreCase(command)) {
                try {
                    reader.write(new String(new BASE64Decoder().decodeBuffer(on.getOnionString("data"))));
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if ("serFlush".equalsIgnoreCase(command)) {
                try {
                    reader.flush();
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if ("serWait".equalsIgnoreCase(command)) {
                try {
                    Integer result = reader.wait(new String(new BASE64Decoder().decodeBuffer(on.getOnionString("data"))), on.getInt("timeout"));
                    System.out.println("busCom serWait: " + result);
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':" + result +","
                            + "'replyID':"+on.getInt("replyID")
                            + "}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }


            } else if ("serReadLn".equalsIgnoreCase(command)) {
                try {
                    String result = reader.readln(on.getInt("timeout"), on.optBoolean("ignore"));
                    System.out.println("busCom readline: " + result);
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'replyID':"+on.getInt("replyID")+ ","
                            + "'result':'" + new BASE64Encoder().encode(result.getBytes()) + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                try {

                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Debug.msg("buscom", DEBUG_BORING, "waked up after received msg...");

        }
    }
}
