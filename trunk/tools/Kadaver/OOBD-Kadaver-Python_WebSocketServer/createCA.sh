
#http://www.linux.org/threads/creating-a-self-signed-certificate-with-python.4591/
python createCA.py
cat oobd.luxen.de.* > luxen.cert
echo call the python server then with luxen.cert as keyfile
#https://blog.codecentric.de/en/2013/01/how-to-use-self-signed-pem-client-certificates-in-java/
openssl pkcs12 -export -out keystore.p12 -inkey luxen.cert -in luxen.cert
keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore keystore.p12

