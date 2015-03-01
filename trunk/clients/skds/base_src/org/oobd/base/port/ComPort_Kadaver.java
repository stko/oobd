/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.*;
import org.oobd.base.support.Onion;
import java.net.Proxy;
import java.nio.channels.NotYetConnectedException;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.io.ByteBufferPool;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.oobd.base.bus.OobdBus;

/**
 *
 * @author steffen
 */
public class ComPort_Kadaver implements OOBDPort {

    OobdBus msgReceiver;
    String channel;
    String protocol;
    String Server;
    WebSocketClient client;
    OobdBusWebSocket socket;
    URI myUri;

    public ComPort_Kadaver(java.net.URI wsURL, Proxy proxy) {
        try {
            this.run(wsURL);
        } catch (IOException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
        }
        String[] parts = wsURL.toString().split("@");
        Server = wsURL.toString();
        parts = parts[0].split("://");
        channel = parts[1];

    }

    public void run(URI destinationUri) throws IOException {
        myUri = destinationUri;
        String[] parts = destinationUri.toString().split("@");
        Server = destinationUri.toString();
        parts = parts[0].split("://");
        protocol = parts[0];
        channel = parts[1];
        if ("wss".equalsIgnoreCase(protocol)) {
            SslContextFactory sslContextFactory = new SslContextFactory();
            Resource keyStoreResource = Resource.newResource(this.getClass().getResource("keystore.jks"));
            sslContextFactory.setKeyStoreResource(keyStoreResource);
            sslContextFactory.setKeyStorePassword("ausderferne");
            sslContextFactory.setKeyManagerPassword("ausderferne");
            client = new WebSocketClient(sslContextFactory);
        } else {

            client = new WebSocketClient();
        }
    }

    public boolean connect(Onion options, OobdBus receiveListener) {
        msgReceiver = receiveListener;
        socket = new OobdBusWebSocket(receiveListener);
        attachShutDownHook();
        try {
            client.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            System.out.println("Connecting to : " + myUri);
            client.connect(socket, myUri, request);
            //socket.awaitClose(5, TimeUnit.SECONDS);
            System.out.println("Connected to : " + myUri);
            return true;
        } catch (InterruptedException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } catch (IOException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        } catch (Exception ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();

            }
            return false;
        }
    }

    public boolean available() {
        return client.isRunning();
    }

    public String connectInfo() {
        if (client.isRunning()) {
            return "Remote Connect to " + Server;
        } else {
            return null;
        }
    }

    public static PortInfo[] getPorts() {

        PortInfo[] DeviceSet = new PortInfo[1];
        DeviceSet[0] = new PortInfo("", "No Devices for Websockets");
        return DeviceSet;

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

    public synchronized void write(String s) {
        try {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.INFO, "Serial output:{0}", s);
            System.out.println("Sending message: Hi server");
            socket.send(s, channel);
        } catch (NotYetConnectedException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.WARNING,
                    null, ex);
        }
    }

    @Override
    public void close() {
    }

    public int adjustTimeOut(int originalTimeout) {
        // as the ws- based time could be much longer as a direct connection, we multiply the normal time
        return originalTimeout * 1;
    }
}
