Introduction
============
In it's latest version OOBD can run as a headless server application, with a web interface to the user on one side and a socketcan can connection on the other.

As this feature is brand new, please consider the software as being in beta state and all documentation as being "work in progress"

This (short) document descripes how to make OOBD run on hopefully any embedded linux system, here a raspi with raspian 

Note: Since Nov 16 ssh is [disabled by default in raspian](https://www.raspberrypi.org/documentation/remote-access/ssh/ ), so you can't do the initial setup straight over ssh, you need to boot at least once on a physical terminal to activate ssh with rasphi-config, or create an empty file called ssh in the root dir of the SD-Image before boot the raspi from it

    cd /boot
    touch ssh

Note 2: The install script mentions to edit /etc/fstab, but does not open the editor automatically. This needs to be done manually after the install script went through

- Take a virgin (raspian) image
- boot the raspi (or BeagleBone or ...) with it
- download the premilary oobdd zip file from the [Google Drive](https://drive.google.com/open?id=0B795A63vSunRa29qbGVxTllkRGM)
- place it in your raspi home directory (e.g. /home/pi)
- in the raspi terminal, run 


    cd  
    bash <(curl -s https://github.com/stko/oobd/raw/development/clients/unixInstall/install.sh)


This will install all necessary packages, download and compile the firmware etc.. The highlights are:

 - can-utils
 - python w. socketcan and isoTP support
 - bluetooth audio output
 - CANable device support
 - MCP Can support
 - all partitions read only for robustness at power loss
 - unisonFS overlay for /etc and /var for temporary configuration changes

After installation, do a final `sudo reboot`. 

After reboot, all partitions will be read only. Without any configuration file on external stick, the standard behaviour is:

 - OOBD will be started
 - The device will try to connect to a Wifi network with
    - SSID: OOBD
    - password oobdoobd


When having a usb Memory device attached, the startup checks if there's a executable named `/media/usb/oobd/autorun.sh`. This script is then called several times during the boot process, where the different events are given as argument to that script. The todays events are:

  - bluetooth: the bluetooth system is up (not implemented yet)
  - pulseaudio: the pulseaudio system is up (not implemented yet)
  - wifi: The right moment to configure wifi before wifi goes active
  - final: The system is ready for normal work


Here you can see a typical `autorun.sh`, (still in beta state)

````
pi@raspberrypi:~ $ more /media/usb/oobd/autorun.sh
#!/bin/bash
echo "call level $1" >> /oobd/log
case "$1" in
        bluetooth)
        mkdir -p /var/lib/bluetooth/B8:27:EB:1E:90:30/FC:58:FA:C1:A0:AE/
cat << EOF | sudo tee /var/lib/bluetooth/B8:27:EB:1E:90:30/FC:58:FA:C1:A0:AE/info__
[General]
Name=TCM BT Speaker
SupportedTechnologies=BR/EDR;
Trusted=false
Blocked=false
Services=00001101-0000-1000-8000-00805f9b34fb;00001108-0000-1000-8000-00805f9b34fb;0000110b-0000-1000-80
00-00805f9b34fb;0000110c-0000-1000-8000-00805f9b34fb;0000110e-0000-1000-8000-00805f9b34fb;0000111e-0000-
1000-8000-00805f9b34fb;

[LinkKey]
Key=539FDD52357D2344968BBF5EF367534F
Type=4
PINLength=0

EOF
        echo "Connect bluetooth"  >> /oobd/log
        echo -e "connect FC:58:FA:C1:A0:AE" | bluetoothctl >> /oobd/log  2>&1 
        sleep 2
        ;;
        wifi)

                wpa_passphrase "yourSSDID" "password" | sudo tee -a /etc/wpa_supplicant/wpa_supplicant.conf > /dev/null
                #wpa_supplicant -iwlan0 -c /etc/wpa_supplicant/wpa_supplicant.conf & dhcpcd wlan0 >> /oobd/log  2>&1
                sudo wpa_cli reconfigure
                echo "Connect wifi"  >> /oobd/log
        ;;
        pulseaudio)
                pactl set-default-sink 1 >> /oobd/log  2>&1 
                pactl set-sink-volume 1 60% >> /oobd/log  2>&1 
        ;;
        final)
                pico2wave --lang=de-DE  --wave=/tmp/test.wav "Moin"
                aplay /tmp/test.wav
                echo "init CAN interface"  >> /oobd/log
                cd /media/usb0/oobd
                /bin/echo usbautorun >> /tmp/mounttrigger
                /sbin/ip link set can0 type can bitrate 125000 triple-sampling on  >> /oobd/log
                /sbin/ifconfig can0 up >> /oobd/log
                echo "run CAN python demo"  >> /oobd/log

                /usr/bin/python example.py >> /tmp/mounttrigger 
                /usr/bin/python example.py >> /oobd/log
        ;;
esac

````

and here another one, which provide an Access Point. The "wifi" section empty, instead the hotspot is created with the `createAP.sh` call:

````
#!/bin/bash
echo "call level $1" >> /oobd/log
case "$1" in
        bluetooth)
        mkdir -p /var/lib/bluetooth/B8:27:EB:1E:90:30/FC:58:FA:C1:A0:AE/
cat << EOF | sudo tee /var/lib/bluetooth/B8:27:EB:1E:90:30/FC:58:FA:C1:A0:AE/info__
[General]
Name=TCM BT Speaker
SupportedTechnologies=BR/EDR;
Trusted=false
Blocked=false
Services=00001101-0000-1000-8000-00805f9b34fb;00001108-0000-1000-8000-00805f9b34fb;0000110b-0000-1000-80
00-00805f9b34fb;0000110c-0000-1000-8000-00805f9b34fb;0000110e-0000-1000-8000-00805f9b34fb;0000111e-0000-
1000-8000-00805f9b34fb;

[LinkKey]
Key=539FDD52357D2344968BBF5EF367534F
Type=4
PINLength=0

EOF
        echo "Connect bluetooth"  >> /oobd/log
        echo -e "connect FC:58:FA:C1:A0:AE" | bluetoothctl >> /oobd/log  2>&1 
        sleep 2
        ;;
        wifi)
        ;;
        pulseaudio)
                pactl set-default-sink 1 >> /oobd/log  2>&1 
                pactl set-sink-volume 1 60% >> /oobd/log  2>&1 
        ;;
        final)
               /home/pi/bin/createAP.sh &>> /oobd/log
                echo "setup Hotspot"  >> /oobd/log
                pico2wave --lang=de-DE  --wave=/tmp/test.wav "Moin"
                aplay /tmp/test.wav
                echo "init CAN interface"  >> /oobd/log
                cd /media/usb0/oobd
                /bin/echo usbautorun >> /tmp/mounttrigger
                /sbin/ip link set can0 type can bitrate 125000 triple-sampling on  >> /oobd/log
                /sbin/ifconfig can0 up >> /oobd/log
                echo "run CAN python demo"  >> /oobd/log

                /usr/bin/python example.py >> /tmp/mounttrigger 
                /usr/bin/python example.py >> /oobd/log
        ;;
esac

````

 

As the IP address of an embedded device is not easy to identify, OOBD broadcasts it's web presence via ZeroConfig (aka Bonjour) into the local subnet, which can be monitored and used by several programs to open the Browser.

On Android [Zentri](https://play.google.com/store/apps/details?id=discovery.ack.me.ackme_discovery) works quite well

Awaiting your comments on [Google Groups](https://groups.google.com/forum/#!forum/oobd-diagnostics) :-)


P.S.: For all Raspi-MCP CAN device users here are a sample add-on to `/boot/config.txt` 

    dtparam=spi=on
    
    # Additional overlays and parameters are documented /boot/overlays/README
    # its a 12Mhz Quarz, so it needs to be 12000000 instead of 16000000
    dtoverlay=mcp2515-can0,oscillator=12000000,interrupt=22
    dtoverlay=mcp2515-can1,oscillator=12000000,interrupt=25
    dtoverlay=spi-bcm2835-overlay
    dtoverlay=spi-dma-overlay
