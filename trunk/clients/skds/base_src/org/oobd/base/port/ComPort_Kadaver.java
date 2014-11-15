/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.util.logging.Level;
import java.util.logging.Logger;
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

    InputStream inputStream=null;
    OutputStream outputStream=null;
    OobdBus msgReceiver;
    
    public ComPort_Kadaver(java.net.URI wsURL){
        super(wsURL);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean connect(Onion options, OobdBus receiveListener) {
        Preferences props = Core.getSingleInstance().getSystemIF().loadPreferences(OOBDConstants.FT_RAW, OOBDConstants.AppPrefsFileName);
        msgReceiver = receiveListener;
        attachShutDownHook();
        return true;


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

   public  void onMessage( String message ){
                    msgReceiver.receiveString(message);
                }
            
    
        public synchronized void write(String s) {
            try {
                Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.INFO,
                        "Serial output:" + s);
                send(s);
                // outStream.flush();
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
