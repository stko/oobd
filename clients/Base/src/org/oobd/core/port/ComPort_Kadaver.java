/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.port;

import org.oobd.core.Base64Coder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.core.port.OOBDPort;
import org.oobd.core.port.PortInfo;
import org.oobd.core.support.Onion;
//import gnu.io.*; // for rxtxSerial library
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.nio.channels.NotYetConnectedException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.util.*;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.oobd.core.bus.OobdBus;
import org.oobd.core.support.OnionNoEntryException;
import org.oobd.core.support.OnionWrongTypeException;

/**
 *
 * @author steffen
 *
 * https://github.com/TooTallNate/Java-WebSocket
 */
public class ComPort_Kadaver extends WebSocketClient implements OOBDPort {

    OobdBus msgReceiver;
    String channel;
    URI wsURI;
    String Server;
    String protocol;
    String proxyHost;
    int proxyPort;
    Proxy proxy;

    public ComPort_Kadaver(java.net.URI wsURL, Proxy proxy, String proxyHost, int proxyPort) {
        super(wsURL);
        if (proxy != Proxy.NO_PROXY) {
            System.out.println("use proxy..");
        }
        this.setProxy(proxy);
        this.proxy = proxy;
        this.wsURI = wsURL;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        String[] parts = wsURL.toString().split("@");
        Server = wsURL.toString();
        parts = parts[0].split("://");
        protocol = parts[0];
        channel = parts[1];
    }

    public boolean connect(Onion options, OobdBus receiveListener) {
        msgReceiver = receiveListener;

        WebSocketImpl.DEBUG = true;
        if ("wss".equalsIgnoreCase(protocol)) {
            // load up the key store
            String STORETYPE = "JKS";
            String KEYSTORE = "/org/oobd/base/port/servercert.jks";
            String KEYMANAGERTYPE = "SunX509";
            if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik")) {
                STORETYPE = "BKS";
                KEYSTORE = "/org/oobd/base/port/servercert.bks";
                KEYMANAGERTYPE = "X509";
            }
            try {
                KeyStore ks = KeyStore.getInstance(STORETYPE);
                //ks.load(this.getClass().getResourceAsStream(KEYSTORE), STOREPASSWORD.toCharArray());
                ks.load(this.getClass().getResourceAsStream(KEYSTORE), null);
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(KEYMANAGERTYPE);
                tmf.init(ks);

                SSLContext sslContext = null;
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);
                // sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates

                SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();
                Socket s = new Socket(proxy); // as the websocket does not provide its socket, we have to overwrite his socket instead with our own one...
                int port = uri.getPort();
                if (port == -1) {
                    String scheme = uri.getScheme();
                    if (scheme.equals("wss")) {
                        port = WebSocket.DEFAULT_WSS_PORT;
                    } else if (scheme.equals("ws")) {
                        port = WebSocket.DEFAULT_PORT;
                    } else {
                        throw new RuntimeException("unkonow scheme" + scheme);
                    }
                }
                s.connect(new InetSocketAddress(wsURI.getHost(), port), 10000);
                //setSocket(s);
                //connectBlocking(); // the socket needs to be connected before overlay it with SSL
                //setSocket(factory.createSocket(s, wsURI.getHost(), wsURI.getPort(), true));
                setSocket(factory.createSocket(s, wsURI.getHost(), port, true));
                attachShutDownHook();
                connectBlocking();
                return true;
            } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException | KeyManagementException | InterruptedException ex) {
                Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

        } else {
            attachShutDownHook();
            try {
                connectBlocking();
                return true;
            } catch (InterruptedException ex) {
                Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

    }

    public boolean available() {
        return isOpen();
    }

    public String connectInfo() {
        if (isOpen()) {
            return "Remote Connect to " + Server;
        } else {
            return null;
        }
    }

    public static String getUrlFormat() {
        return "{protocol}://{connectid}@{urlpath}";
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

    public void onMessage(String message) {
        try {
            Onion myOnion = new Onion(message);
            msgReceiver.receiveString(myOnion.getOnionBase64String("reply"));
        } catch (JSONException | OnionWrongTypeException | OnionNoEntryException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void write(String s) {

        Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.INFO, "Serial output:{0}", s);
        String onionMsg;
        try {
            onionMsg = new Onion("{'msg':'" + Base64Coder.encodeString(s) + "','channel': '" + channel + "'}").toString();
            System.err.println("Send to cadaver server:"+onionMsg);
            send(onionMsg);
        } catch (JSONException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Socket was closed!!");
        System.out.println("reason: " + reason);
        if (remote) {
            System.out.println("Remote");
        } else {
            System.out.println("local");
        }
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

    public int adjustTimeOut(int originalTimeout) {
        // as the ws- based time could be much longer as a direct connection, we multiply the normal time
        return originalTimeout * 1;
    }
}
