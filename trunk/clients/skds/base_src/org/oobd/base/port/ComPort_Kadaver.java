/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.*;
import org.oobd.base.port.PortInfo;
import org.oobd.base.support.Onion;
//import gnu.io.*; // for rxtxSerial library
import java.io.*;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.util.*;
import java.util.prefs.Preferences;
import org.oobd.base.bus.OobdBus;

/**
 *
 * @author steffen
 */
public class ComPort_Kadaver extends WebSocketClient implements OOBDPort {

    InputStream inputStream = null;
    OutputStream outputStream = null;
    OobdBus msgReceiver;
    String channel;

    public ComPort_Kadaver(java.net.URI wsURL) {
        super(wsURL);
        String [] parts=wsURL.toString().split("@");
        parts=parts[0].split("://");
        channel=parts[1];
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean connect(Onion options, OobdBus receiveListener) {
        msgReceiver = receiveListener;
        attachShutDownHook();
        try {
            connectBlocking();
            return true;
        } catch (InterruptedException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }


    }

    public boolean available() {
        return isOpen();
    }

    public PortInfo[] getPorts() {

        PortInfo[] DeviceSet = new PortInfo[1];
        DeviceSet[0] = new PortInfo("", "No Comports found :-(");
        return DeviceSet;

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

    public void onMessage(String message) {
        try {
            Onion myOnion = new Onion(message);
            System.err.println("Answer from remote:"+myOnion.getOnionBase64String("reply"));
            msgReceiver.receiveString(myOnion.getOnionBase64String("reply"));
        } catch (JSONException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void write(String s) {
        try {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.INFO, "Serial output:{0}", s);
            send(new Onion("{'msg':'" + Base64Coder.encodeString(s) + "','channel': '"+channel+"'}").toString());

            // outStream.flush();
        } catch (JSONException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotYetConnectedException ex) {
            Logger.getLogger(ComPort_Win.class.getName()).log(Level.WARNING,
                    null, ex);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onError(Exception ex) {
        Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.WARNING,
                "Winsocket reports Error", ex);
    }

    @Override
    public void close() {
        super.close();
    }
}
