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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.util.*;
import java.util.prefs.Preferences;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.oobd.base.bus.OobdBus;

/**
 * 
 * @author steffen
 * 
 *         https://github.com/TooTallNate/Java-WebSocket
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

	public ComPort_Kadaver(java.net.URI wsURL, Proxy proxy, String proxyHost,
			int proxyPort) {
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
			String KEYSTORE = "/org/oobd/base/port/keystore.jks";
			// KEYSTORE = "/org/oobd/base/port/servercertCA.der";
			String STOREPASSWORD = "ausderferne";
			String KEYPASSWORD = "ausderferne";
			String KEYMANAGERTYPE = "SunX509";
			TrustManagerFactory tmf;
			try {
				if (System.getProperty("java.vm.name").equalsIgnoreCase(
						"Dalvik")) {
					STORETYPE = "BKS";
					KEYSTORE = "/org/oobd/base/port/servercertCA.der";
					KEYMANAGERTYPE = "X509";

					// Android developer version starts here
					// https://developer.android.com/training/articles/security-ssl.html

					// Load CAs from an InputStream
					// (could be from a resource or ByteArrayInputStream or ...)
					CertificateFactory cf = CertificateFactory
							.getInstance("X.509");

					InputStream caInput = new BufferedInputStream(this
							.getClass().getResourceAsStream(KEYSTORE));
					Certificate ca;
					try {
						ca = cf.generateCertificate(caInput);
						// System.out.println("ca=" + ((X509Certificate)
						// ca).getSubjectDN());
					} finally {
						caInput.close();
					}

					// Create a KeyStore containing our trusted CAs
					String keyStoreType = KeyStore.getDefaultType();
					KeyStore keyStore = KeyStore.getInstance(keyStoreType);
					keyStore.load(null, null);
					keyStore.setCertificateEntry("ca", ca);

					// Create a TrustManager that trusts the CAs in our KeyStore
					String tmfAlgorithm = TrustManagerFactory
							.getDefaultAlgorithm();
					tmf = TrustManagerFactory
							.getInstance(tmfAlgorithm);
					tmf.init(keyStore);
					// Android developer version ends here

				} else {
					// Java only vesion starts here

					KeyStore ks = KeyStore.getInstance(STORETYPE);
					ks.load(this.getClass().getResourceAsStream(KEYSTORE),
							STOREPASSWORD.toCharArray());
					KeyManagerFactory kmf = KeyManagerFactory
							.getInstance(KEYMANAGERTYPE);
					kmf.init(ks, KEYPASSWORD.toCharArray());
					tmf = TrustManagerFactory
							.getInstance(KEYMANAGERTYPE);

					tmf.init(ks);

					// java only version ends here

				}

				/*

   */

				SSLContext sslContext = null;
				sslContext = SSLContext.getInstance("TLS");
				// sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
				// null); // the non android version---
				sslContext.init(null, tmf.getTrustManagers(), null);
				// sslContext.init( null, null, null ); // will use java's
				// default key and trust store which is sufficient unless you
				// deal with self-signed certificates

				SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory)
																			// SSLSocketFactory.getDefault();
				Socket s = new Socket(proxy); // as the websocket does not
												// provide its socket, we have
												// to overwrite his socket
												// instead with our own one...
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
				// setSocket(s);
				// connectBlocking(); // the socket needs to be connected before
				// overlay it with SSL
				// setSocket(factory.createSocket(s, wsURI.getHost(),
				// wsURI.getPort(), true));
				setSocket(factory.createSocket(s, wsURI.getHost(), port, true));
				attachShutDownHook();
				connectBlocking();
				return true;
			} catch (IOException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;
			} catch (NoSuchAlgorithmException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;
			} catch (CertificateException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;
			} catch (KeyStoreException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;

			} catch (UnrecoverableKeyException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;
			} catch (KeyManagementException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;
			} catch (InterruptedException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
				return false;
			}

		} else {
			attachShutDownHook();
			try {
				connectBlocking();
				return true;
			} catch (InterruptedException ex) {
				Logger.getLogger(ComPort_Kadaver.class.getName()).log(
						Level.SEVERE, null, ex);
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
		} catch (JSONException ex) {
			Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public synchronized void write(String s) {
		try {
			Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.INFO,
					"Serial output:{0}", s);
			String onionMsg = new Onion("{'msg':'"
					+ Base64Coder.encodeString(s) + "','channel': '" + channel
					+ "'}").toString();
			send(onionMsg);

			// outStream.flush();
		} catch (JSONException ex) {
			Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (NotYetConnectedException ex) {
			Logger.getLogger(ComPort_Kadaver.class.getName()).log(
					Level.WARNING, null, ex);
		} catch (org.java_websocket.exceptions.WebsocketNotConnectedException ex) {
			Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE,
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

	public int adjustTimeOut(int originalTimeout) {
		// as the ws- based time could be much longer as a direct connection, we
		// multiply the normal time
		return originalTimeout * 1;
	}
}
