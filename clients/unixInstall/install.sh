
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
sudo mkdir /oobd
cd insttemp
sudo apt-get update --assume-yes
sudo apt-get install --assume-yes \
build-essential \
libsocketcan2 \
libsocketcan-dev \
openjdk-8-jre-headless \
joe \
python-pip \
libttspico-utils \
can-utils \
tofrodos \
indent \
bc \
usbmount

#separate optional packages which might fail
sudo apt-get install --assume-yes \
aplay

# Read-Only Image instructions thankfully copied from https://kofler.info/raspbian-lite-fuer-den-read-only-betrieb/

# remove packs which do need writable partitions
sudo apt-get remove --purge --assume-yes cron logrotate triggerhappy dphys-swapfile fake-hwclock samba-common
sudo apt-get autoremove --purge --assume-yes
sudo pip install python-jsonrpc

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
  "LibraryDir": "/home/pi/bin/oobd/oobdd/lib_html",
  "LibraryDir_lock": true,
  "ScriptDir": "/home/pi/bin/oobd/oobdd/scripts",
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
sudo ../fw/OOBD_POSIX.bin -c can0 -t 3001 &
fwpid=$!
java -jar ./oobdd.jar --settings localsettings.json
sudo kill $fwpid
SCRIPT
chmod a+x oobdd.sh
# automatically connect to a OOBD hotspot, if around
sudo cat << 'WIFI' | sudo tee --append /etc/network/interfaces
auto wlan0
iface wlan0 inet dhcp
  wpa-ssid "OOBD"
  wpa-psk  "oobd"
WIFI


# start to make the system readonly
sudo rm -rf /var/lib/dhcp/ /var/spool /var/lock
sudo ln -s /tmp /var/lib/dhcp
sudo ln -s /tmp /var/spool
sudo ln -s /tmp /var/lock
if [ -f /etc/resolv.conf ]; then
	sudo mv /etc/resolv.conf /tmp/resolv.conf
fi
sudo ln -s /tmp/resolv.conf /etc/resolv.conf

# add the temporary directories to the mountlist
cat << 'MOUNT' | sudo tee --append /etc/fstab
tmpfs	/var/log	tmpfs	nodev,nosuid	0	0
tmpfs	/var/tmp	tmpfs	nodev,nosuid	0	0
tmpfs	/tmp	tmpfs	nodev,nosuid	0	0
tmpfs	/oobd	tmpfs	nodev,nosuid	0	0
MOUNT

#add boot options
echo -n " fastboot noswap" | sudo tee --append /boot/cmdline
cat << 'INFO'
The users task: 
Change the mountpoints for / and /boot to readonly by adding "ro" to
the mointpoint options like shown here:
proc            /proc           proc    defaults              0 0
/dev/mmcblk0p1  /boot           vfat    ro,defaults           0 2
/dev/mmcblk0p2  /               ext4    ro,defaults,noatime   0 1
tmpfs           /var/log        tmpfs   nodev,nosuid          0 0
tmpfs           /var/tmp        tmpfs   nodev,nosuid          0 0
tmpfs           /tmp            tmpfs   nodev,nosuid          0 0

A editor will open after pressing RETURN, save your changes with
CTRL+O and leave the editor with CTRL+X
INFO
read
sudo nano /etc/fstab


# setting up the systemd services
# very helpful source : http://patrakov.blogspot.de/2011/01/writing-systemd-service-files.html

# firmware as service
cat << 'FWSERVICE' | sudo tee --append /etc/systemd/system/oobdfw.service
[Unit]
Description=OOBD CanSocket Firmware
After=local-fs.target

[Service]
ExecStartPre=/usr/bin/sudo /bin/ln -sf /home/pi/oobd /oobd/oobd
ExecStart=/home/pi/bin/oobd/fw/OOBD_POSIX.bin -c can0 -t 3001
Restart=on-abort

[Install]
WantedBy=multi-user.target
FWSERVICE

# oobdd as service
cat << 'OOBDDSERVICE' | sudo tee --append /etc/systemd/system/oobdd.service
[Unit]
Description=OOBD Main Server
After=oobdfw.target local-fs.target
[Service]
ExecStart=/usr/bin/java -jar /home/pi/bin/oobd/oobdd/oobdd.jar --settings /oobd/oobd/oobdd/localsettings.json
Restart=on-abort

[Install]
WantedBy=multi-user.target
OOBDDSERVICE

# usb monitoring
cat << 'USBSERVICE' | sudo tee --append /etc/systemd/system/triggerusb0.service
[Unit]
Description=Starts on usb0 existance

[Service]
ExecStart=/home/pi/setoobdpath.sh usb
USBSERVICE

cat << 'USBPATH' | sudo tee --append /etc/systemd/system/triggerusb0.path
[Unit]
Description=Monitor existance of any data in usb0

[Path]
DirectoryNotEmpty=/media/usb0

[Install]
WantedBy=multi-user.target
USBPATH

# tmp monitoring
cat << 'TMPSERVICE' | sudo tee --append /etc/systemd/system/triggertmpmount.service
[Unit]
Description=Triggers oobdd library path relocation at boot
Requires=local-fs.target
After=local-fs.target

[Service]
ExecStart=/usr/bin/sudo /bin/ln -sf /media/usb0/oobd /oobd/oobd

[Install]
WantedBy=oobdd.service
TMPSERVICE






sudo systemctl enable oobdfw 
sudo systemctl enable oobdd

echo "Installation finished"
echo "to run oobd, goto $(pwd) and start oobdd.sh"


