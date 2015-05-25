#!/bin/bash
echo download cert from server
openssl s_client -showcerts -connect oobd.luxen.de:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >servercert.pem
echo check pem content
openssl x509 -in servercert.pem -text -noout
echo convert to p12
openssl pkcs12 -nokeys -export -out servercert.p12 -in servercert.pem -certfile servercert.pem
echo check p12 content
openssl pkcs12 -info -in servercert.p12
echo removing keys
openssl pkcs12 -in servercert.p12 -cacerts -nokeys -out servercertCA.pem
echo convert PEM to DER as Android certificate
openssl x509 -outform der -in servercertCA.pem -out servercertCA.der
echo Generated files:
ls -l servercert*
