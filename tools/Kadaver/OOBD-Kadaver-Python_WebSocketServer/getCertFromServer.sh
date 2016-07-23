#!/bin/bash
if [ $# -eq 0 ]
	then
		echo "Usage: getCertFromServer <server:port>"
		echo "downloads the server certificate and stores it in servercert.jks,"
		echo "which can be used e.g. in OOBD to create a https- Websocket connection"
		echo "if asked for a password during the process, just use something (but the same)"
		exit 1
fi
echo "download cert from server"
openssl s_client -showcerts -connect oobd.luxen.de:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >servercert.pem
echo "check pem content"
openssl x509 -in servercert.pem -text -noout
echo "convert to p12"
openssl pkcs12 -nokeys -export -out servercert.p12 -in servercert.pem -certfile servercert.pem
echo "check p12 content"
openssl pkcs12 -info -in servercert.p12
echo "removing keys"
openssl pkcs12 -in servercert.p12 -cacerts -nokeys -out servercertCA.pem
echo "convert PEM to DER as Android certificate"
openssl x509 -outform der -in servercertCA.pem -out servercertCA.der
keytool -importcert -trustcacerts -keystore servercert.jks  -file servercertCA.pem
echo "clean up:"
rm  servercert.pem servercert.p12 servercertCA.pem servercertCA.der
echo "Generated files:"
ls -l servercert*
echo "For android, you've to convert the servercert.jks into a bouncycastle keystore (*.bks)"
echo "manually with portecle from http://portecle.sourceforge.net/"
