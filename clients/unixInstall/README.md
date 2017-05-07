Introduction
============
In it's latest version OOBD can run as a headless server application, with a web interface to the user on one side and a socketcan can connection on the other.

As this feature is brand new, please consider the software as being in beta state and all documentation as being "work in progress"

This (short) document descrip how to make OOBD run on hopefully any embedded linux system, here a raspi with raspian 

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


This will install all necessary packages, download and compile the firmware etc.

If everything goes well, you will find the script `oobdd.sh` in `~/home/bin/oobd/oobdd`. When executing this, the firmware will connect to the socketcan (if there is hopefully any..) and oobd will be reachable via its build in webserver. With `CTRL-C` the process can be stopped again.

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
