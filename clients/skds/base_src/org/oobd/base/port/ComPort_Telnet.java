/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.Socket;
import java.nio.channels.NotYetConnectedException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.support.Onion;

//import gnu.io.*; // for rxtxSerial library
/**
 *
 * @author steffen
 */
public class ComPort_Telnet implements OOBDPort, Runnable {

    OobdBus msgReceiver;
    Integer port;
    String Server;
    Socket sock;
    OutputStream out;
    InputStream in;
    static PortInfo[] udpAnouncements;

    public ComPort_Telnet(String thisURL) {
        String[] parts = thisURL.toString().split("://");
        parts = parts[1].split(":");
        Server = parts[0];
        port = Integer.decode(parts[1]);
        new Thread(this).start();
    }

    public boolean connect(Onion options, OobdBus receiveListener) {
        msgReceiver = receiveListener;
        attachShutDownHook();
        try {
            sock = new Socket(Server, port);
            out = sock.getOutputStream();
            in = sock.getInputStream();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ComPort_Telnet.class.getName()).log(Level.SEVERE,
                    null, ex);
            return false;
        }

    }

    public boolean available() {
        return sock != null;
    }

    public String connectInfo() {
        if (available()) {
            return "Remote Connect to " + Server + ":" + port;
        } else {
            return null;
        }
    }

    public static String getUrlFormat() {
        return "telnet://{device}";
    }

    public static PortInfo[] getPorts() {
        if (udpAnouncements == null) {
            PortInfo[] DeviceSet = new PortInfo[1];
            DeviceSet[0] = new PortInfo("", "No Devices visible");
            return DeviceSet;
        } else {
            return udpAnouncements;
        }

    }

    public static void setPorts(PortInfo[] newPorts) {
        udpAnouncements = newPorts;
    }

    public void run() {
        while (true) {

            int n;

            try {
                if (sock != null && sock.getInputStream() != null) {
                    n = sock.getInputStream().available();
                    if (n > 0) {
                        byte[] buffer = new byte[n];

                        n = sock.getInputStream().read(buffer, 0, n);

                        String recString = new String(buffer);
                        recString = recString.substring(0, n);
                        msgReceiver.receiveString(recString);

                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            // the VM doesn't want us to sleep anymore,
                            // so get back to work
                            Logger.getLogger(ComPort_Telnet.class.getName())
                                    .log(Level.WARNING, null, ex);

                        }
                    }
                } else {
                    // as this thread runs in an unstopped endless loop, as
                    // long
                    // there's no serial port open, we need to slow him down
                    // here...
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // the VM doesn't want us to sleep anymore,
                        // so get back to work
                    }
                }

            } catch (Exception ex) {
                Logger.getLogger(ComPort_Telnet.class.getName()).log(
                        Level.WARNING, "Unexpected error: Close down socket",
                        ex);
                close();
            }
        }
    }

    public void attachShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.err.println("Inside Add Shutdown Hook");
                close();
                System.err.println("Websocket closed");
            }
        });
        System.err.println("Shut Down Hook Attached.");

    }

    public void onMessage(String message) {
        msgReceiver.receiveString(message);
    }

    public synchronized void write(String s) {
        try {
            Logger.getLogger(ComPort_Telnet.class.getName()).log(Level.INFO,
                    "Telnet output:{0}", s);
            out.write(s.getBytes());

            // outStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(ComPort_Telnet.class.getName()).log(Level.SEVERE,
                    null, ex);
        } catch (NotYetConnectedException ex) {
            Logger.getLogger(ComPort_Telnet.class.getName()).log(Level.WARNING,
                    null, ex);
        }
    }

    public void close() {
        try {
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (sock != null) {
                sock.close();
                sock = null;
            }
        } catch (IOException ex) {

        }
    }

    public int adjustTimeOut(int originalTimeout) {
        // as the ws- based time could be much longer as a direct connection, we
        // multiply the normal time
        return originalTimeout * 1;
    }
}
