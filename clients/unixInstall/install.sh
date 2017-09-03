
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
sudo apt-get upgrade --assume-yes
sudo apt-get install --assume-yes \
build-essential \
clang \
libsocketcan2 \
libsocketcan-dev \
oracle-java8-jdk \
joe \
python-pip \
can-utils \
tofrodos \
indent \
bc \
usbmount \
hostapd \
isc-dhcp-server \
autoconf \
libconfig-dev

#not in raspian stretch anymore
# libttspico-utils \
# so we need to download and install it manually, until the package availabilty is fixed..
wget http://ftp.de.debian.org/debian/pool/non-free/s/svox/libttspico-utils_1.0+git20130326-5_armhf.deb
wget http://ftp.de.debian.org/debian/pool/non-free/s/svox/libttspico0_1.0+git20130326-5_armhf.deb
sudo apt-get install  --assume-yes libttspico-data
sudo apt install ./libttspico0_1.0+git20130326-5_armhf.deb   ./libttspico-utils_1.0+git20130326-5_armhf.deb


# Install stretch new alsa- Bluetooth bridge for BT  audio output to support the needed a2dp-sink profile
sudo apt-get  --assume-yes install bluealsa


## begin unisonfs overlay file system (http://blog.pi3g.com/2014/04/make-raspbian-system-read-only/)

### Do we need to disable swap?? actual not..

# dphys-swapfile swapoff
# dphys-swapfile uninstall
# update-rc.d dphys-swapfile disable


# Install MPlayer, along with some codecs, to later test audio output
sudo apt-get  --assume-yes install unionfs-fuse


# Create mount script

cat << 'EOF' | sudo tee /usr/local/bin/mount_unionfs
#!/bin/sh
DIR=$1
ROOT_MOUNT=$(awk '$2=="/" {print substr($4,1,2)}' < /etc/fstab)
if [ $ROOT_MOUNT = "rw" ]
then
	/bin/mount --bind ${DIR}_org ${DIR}
else
	/bin/mount -t tmpfs ramdisk ${DIR}_rw
	/usr/bin/unionfs-fuse -o cow,allow_other,suid,dev,nonempty ${DIR}_rw=RW:${DIR}_org=RO ${DIR}
fi
EOF

# make it executable:

sudo chmod +x /usr/local/bin/mount_unionfs
 ## see the directory renaming at the end of this installation script




## end unisonfs overlay file system




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

cp -r oobd-development/clients/unixInstall/tools/* ~/bin


cd oobd-development/interface/Designs/POSIX/GCC/D3/app/ \
&& make \
&& cp OOBD_POSIX.bin ~/bin/oobd/fw

cd ~/insttemp \
&& rm -r oobd-development


############### raspbian kernel sources #############
sudo wget https://raw.githubusercontent.com/notro/rpi-source/master/rpi-source  -O /usr/bin/rpi-source && sudo chmod +x /usr/bin/rpi-source && /usr/bin/rpi-source -q --tag-update
sudo rpi-source --skip-gcc



############### gs_usb driver #############
wget  https://github.com/HubertD/socketcan_gs_usb/archive/master.zip -O tmpfile \
&& unzip tmpfile \
&& cd socketcan_gs_usb-master
# we overwrite the crappy makefile with a corrected one
cat << 'MAKEFILE' > Makefile
obj-m += gs_usb.o
gs_usb-m := can_change_mtu.o
ccflags-m := -include /home/pi/insttemp/socketcan_gs_usb-master/can_change_mtu.h

all:
	make -C /lib/modules/$(shell uname -r)/build M=/home/pi/insttemp/socketcan_gs_usb-master modules

clean:
	make -C /lib/modules/$(shell uname -r)/build M=/home/pi/insttemp/socketcan_gs_usb-master clean

modules_install:
	make -C /lib/modules/$(shell uname -r)/build M=/home/pi/insttemp/socketcan_gs_usb-master modules_install
MAKEFILE


sudo make \
&& sudo make modules_install

cd ~/insttemp 
############### can-isotp #############
wget  https://github.com/hartkopp/can-isotp/archive/master.zip -O tmpfile \
&& unzip tmpfile \
&& cd can-isotp-master \
&& sudo make \
&& sudo make modules_install

cd ~/insttemp 
############### socketcand #############
wget  https://github.com/dschanoeh/socketcand/archive/master.zip -O tmpfile \
&& unzip tmpfile \
&& cd socketcand-master \
&& autoconf \
&& ./configure --disable-init-script \
&& make \
&& sudo make install


# don't foget to fix the socketcand parameters like here: https://superuser.com/a/728962



cat << 'EOF' | sudo tee --append /etc/systemd/system/socketcand.service
[Unit]
Description=SocketCan Daemon 
Wants=network.target

[Service]
ExecStart=/usr/local/bin/socketcand --interfaces can0 -d
Restart=on-abort

EOF


# update module dependencies
sudo depmod -a


################# pysotp python isotp bindings ###################

cd ~/insttemp 
############### pysotp #############
wget  https://github.com/stko/pysotp/archive/master.zip -O tmpfile \
&& unzip tmpfile \
&& cd pysotp* \
&& sudo python setup.py install

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
wpa_passphrase "OOBD" "oobdoobd" | sudo tee -a /etc/wpa_supplicant/wpa_supplicant.conf > /dev/null


# start to make the system readonly
#sudo rm -rf /var/lib/dhcp/ /var/spool /var/lock
#sudo ln -s /tmp /var/lib/dhcp
#sudo ln -s /tmp /var/spool
#sudo ln -s /tmp /var/lock
#if [ -f /etc/resolv.conf ]; then
#	sudo mv /etc/resolv.conf /tmp/resolv.conf
#fi
#sudo ln -s /tmp/resolv.conf /etc/resolv.conf

# add the temporary directories to the mountlist
cat << 'MOUNT' | sudo tee /etc/fstab
proc            /proc           proc    defaults          0       0
/dev/mmcblk0p1  /boot           vfat    ro,defaults          0       2
/dev/mmcblk0p2  /               ext4    ro,defaults,noatime  0       1
# a swapfile is not a swap partition, no line here
#   use  dphys-swapfile swap[on|off]  for that
##tmpfs	/var/log	tmpfs	nodev,nosuid	0	0
##tmpfs	/var/tmp	tmpfs	nodev,nosuid	0	0
tmpfs	/tmp	tmpfs	nodev,nosuid	0	0
tmpfs	/oobd	tmpfs	nodev,nosuid	0	0
/dev/sda1       /media/usb0     vfat    ro,defaults,nofail,x-systemd.device-timeout=1   0       0


mount_unionfs   /etc            fuse    defaults          0       0
mount_unionfs   /var            fuse    defaults          0       0


MOUNT

#add boot options
echo -n " fastboot noswap" | sudo tee --append /boot/cmdline



# setting up the systemd services
# very helpful source : http://patrakov.blogspot.de/2011/01/writing-systemd-service-files.html

cat << 'EOF' | sudo tee --append /etc/systemd/system/triggeroobd.service
[Unit]
Description=Triggers oobd startup when having basic system set up
Wants=basic.target

[Service]
ExecStart=/home/pi/initoobd.sh basic

[Install]
WantedBy=default.target
EOF

# cat << 'EOF' | sudo tee --append /etc/systemd/system/triggerusb0.path
# [Unit]
# Description=Monitor existance of any data in usb0
# 
# [Path]
# DirectoryNotEmpty=/media/usb0
# 
# EOF
# 
# cat << 'EOF' | sudo tee --append /etc/systemd/system/triggerusb0.service
# [Unit]
# Description=Starts on usb0 existance
# 
# [Service]
# ExecStart=/home/pi/initoobd.sh usbdata
# 
# EOF
# 
# cat << 'EOF' | sudo tee --append /etc/systemd/system/triggerusbmount.service
# [Unit]
# Description=Informs oobd about mounted usb memory
# Wants=triggeroobd.service media-usb0.mount
# 
# [Service]
# ExecStart=/home/pi/initoobd.sh usbmount
# EOF

cat << 'EOF' | sudo tee --append /etc/systemd/system/oobdd.service
[Unit]
Description=OOBD Main Server
Wants=oobdfw.service

[Service]
ExecStart=/usr/bin/java -jar /home/pi/bin/oobd/oobdd/oobdd.jar --settings /oobd/localsettings.json
Restart=on-abort

EOF

cat << 'EOF' | sudo tee --append /etc/systemd/system/oobdfw.service
[Unit]
Description=OOBD CanSocket Firmware
Wants=network.target
Before=oobdd.service

[Service]
ExecStart=/home/pi/bin/oobd/fw/OOBD_POSIX.bin -c can0 -t 3001
Restart=on-abort

EOF


cat << 'EOF' | sudo tee --append /etc/systemd/system/triggerwifi.service
[Unit]
Description=Triggers oobd startup before wifi gets started
Wants=network.target 
Before= network.target 

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/home/pi/initoobd.sh wifi

[Install]
WantedBy=default.target


EOF




cat << 'EOF' | tee --append /home/pi/initoobd.sh
#!/bin/bash
AUTORUN=/media/usb/oobd/autorun.sh
AUTOPYTHON=/media/usb/oobd/autorun.py
LOG=/oobd/log
/bin/echo "initscript $1" >> $LOG
if [[ -x "$AUTORUN" ]]
then
        $AUTORUN "$1"
fi
if [ "$1" == "basic" ]; then
	if [[ -x "$AUTORUN" ]]
	then
		$AUTORUN final
	elif [[ -x "$AUTOPYTHON" ]]
	then
		cd /media/usb/oobd/
		python "$AUTOPYTHON"
	else
    		/usr/bin/sudo /bin/ln -sf /home/pi/bin/oobd/oobdd/localsettings.json /oobd/localsettings.json 
		service  oobdd start
	fi
fi

EOF
chmod a+x /home/pi/initoobd.sh


cat << 'EOF' | tee --append /home/pi/startcan.sh
#!/bin/bash
# nicely descripted on https://developer.ridgerun.com/wiki/index.php/How_to_configure_and_use_CAN_bus

if [ -z "$1" ]
then
	BUS=can0
else
	BUS="$1"
fi
if [ -z "$2" ]
then
	RATE=500000
else
	RATE="$2"
fi

sudo ifconfig $BUS down
sudo ip link set $BUS type can bitrate $RATE triple-sampling on
sudo ifconfig $BUS up
EOF
chmod a+x /home/pi/startcan.sh



sudo systemctl enable triggeroobd 
#sudo systemctl enable triggerusb0
#sudo systemctl enable triggerusbmount
sudo systemctl enable triggerwifi


#Prepare unisonfs  directories
sudo cp -al /etc /etc_org
sudo mv /var /var_org
sudo mkdir /etc_rw
sudo mkdir /var /var_rw

cat << 'EOF'
Installation finished

SSH is enabled and the default password for the 'pi' user has not been changed.
This is a security risk - please login as the 'pi' user and type 'passwd' to set a new password."

Also this is the best chance now if you want to do some own modifications,
as with the next reboot the image will be write protected

if done, end this session with
 
     sudo halt

and your OOBD all-in-one is ready to use

have fun :-)

the OOBD team
EOF


