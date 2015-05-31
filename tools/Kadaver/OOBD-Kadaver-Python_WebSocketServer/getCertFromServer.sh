#!/bin/bash
echo download cert from server
openssl s_client -showcerts -connect oobd.luxen.de:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >servercert.pem
echo check pem content
openssl x509 -in servercert.pem -text -noout
echo convert PEM to DER as Android certificate
openssl x509 -outform der -in servercert.pem -out servercert.der
echo Generated files:
ls -l servercert*
