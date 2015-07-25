package org.oobd.kadaver;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;

public class Utils {
	
	public static SSLContext getSSLContextForSubDomain(Context context){

		String STORETYPE = "BKS";
        String KEYSTORE = "/org/oobd/kadaver/keystore.bks";
        String STOREPASSWORD = "ausderferne";
        String KEYPASSWORD = "ausderferne";
        String KEYMANAGERTYPE = "X509";
        
        try {
            KeyStore ks = KeyStore.getInstance(STORETYPE);
            
            ks.load(context.getClass().getResourceAsStream(KEYSTORE), STOREPASSWORD.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEYMANAGERTYPE);
            kmf.init(ks, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KEYMANAGERTYPE);
            tmf.init(ks);
			
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
			
			sslContext.init(null, tmf.getTrustManagers(), null);
		return sslContext;
		} catch (Exception e){

		}
	return null;
	}
	
	public static SSLContext getSSLContextForWebsocketWSS(Context context){

		TrustManagerFactory tmf;
		try{

			String STOREPASSWORD = "ausderferne";
			String KEYPASSWORD = "ausderferne";
			String STORETYPE = "BKS";
			String KEYSTORE = "/org/oobd/kadaver/servercertCA.der";
			String KEYMANAGERTYPE = "X509";
			
			CertificateFactory cf = CertificateFactory
					.getInstance("X.509");

			InputStream caInput = new BufferedInputStream(context
					.getClass().getResourceAsStream(KEYSTORE));
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);

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

		
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance("TLS");
				// sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
				// null); // the non android version---
			sslContext.init(null, tmf.getTrustManagers(), null);
			return sslContext;
		} catch (Exception e){

		}
		return null;
	}
	
}
