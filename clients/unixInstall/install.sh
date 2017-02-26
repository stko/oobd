
##### To install OOBD, get a virgin raspian lite image from raspberry.org
#  boot it, copy the oobdd.zip first as described below, start the install script with
#    bash <(curl -s http://mywebsite.com:8080/install.sh)
# and spent some hours with your friends or family. When you are back,
# the installation should be done

echo "The OOBD Installer starts"
cd
if [ ! -f oobdd.zip ]; then
	echo "oobdd.zip not found!"
	echo "As Google quite good avoids that files get automatically downloaded"
	echo "you need to download this file manually first"
	echo "and place it in your raspis home directory (/home/pi)"
	echo "https://drive.google.com/open?id=0B795A63vSunRa29qbGVxTllkRGM"
	exit 1 
fi
mkdir -p insttemp bin/oobd/oobdd bin/oobd/fw
cd insttemp
sudo apt-get update --assume-yes
sudo apt-get install --assume-yes \
build-essential \
libsocketcan2 \
libsocketcan-dev \
openjdk-8-jre-headless \
joe \
tofrodos \
indent \
bc



if [ ! -f development.zip ]; then
	wget  https://github.com/stko/oobd/archive/development.zip -O development.zip && unzip development.zip
fi

cd oobd-development/interface/Designs/POSIX/GCC/D3/app/ \
&& make \
&& cp OOBD_POSIX.bin ~/bin/oobd/fw \
&& cd ~/insttemp \
&& rm -r oobd-development


############### raspbian kernel sources #############
### this part is not working yet, so it's commented out
# sudo wget https://raw.githubusercontent.com/notro/rpi-source/master/rpi-source  -O /usr/bin/rpi-source && sudo chmod +x /usr/bin/rpi-source && /usr/bin/rpi-source -q --tag-update
# rpi-source --skip-gcc


############### gs_usb driver #############
# wget  https://github.com/HubertD/socketcan_gs_usb/archive/master.zip -O tmpfile \
# && unzip tmpfile \
# && cd socketcan_gs_usb-master\
# && make \
# && cp gs_usb.ko ~/bin/oobd/fw
#   modprobe can_dev can
#   insmod gs_usb.ko

cd ~/bin/oobd/oobdd && unzip ~/oobdd.zip
cat << 'SETTING' > localsettings.json
{
  "Bluetooth": {
    "ServerProxyPort": 0,
    "SerialPort": "/tmp/DXM",
    "ConnectServerURL": "",
    "ServerProxyHost": "",
    "SerialPort_lock": false
  },
  "UIHandler": "WsUIHandler",
  "UIHandler_lock": true,
  "Password_lock": true,
  "ConnectType": "Telnet",
  "LibraryDir": "lib_html",
  "LibraryDir_lock": true,
  "ScriptDir": "scripts",
  "Kadaver": {
    "ServerProxyPort": 0,
    "SerialPort": "",
    "ConnectServerURL": "wss://oobd.luxen.de/websockssl/",
    "ServerProxyPort_lock": false,
    "ServerProxyHost": "",
    "ServerProxyHost_lock": false,
    "SerialPort_lock": false,
    "ConnectServerURL_lock": false
  },
  "Telnet": {
    "SerialPort": "localhost:3001",
    "ConnectServerURL": "wss://oobd.luxen.de/websockssl/",
    "SerialPort_lock": false,
    "ConnectServerURL_lock": false
  },
  "PGPEnabled_lock": false,
  "ScriptDir_lock": false,
  "PGPEnabled": true,
  "ConnectType_lock": true,
  "Password": "bill"
}
SETTING
cat << 'SCRIPT' >oobdd.sh
sudo ../fw/OOBD_POSIX.bin -c can0 -p 3001 &
fwpid=$!
java -jar ./oobdd.jar --settings localsettings.json
sudo kill $fwpid
SCRIPT
chmod a+x oobdd.sh 

echo "Installation finished"
echo "to run oobd, goto $(pwd) and start oobdd.sh"


