Introduction
============
In it's latest version OOBD can run as a headless server application, with a web interface to the user on one side and a socketcan can connection on the other.

As this feature is brand new, please consider the software as being in beta state and all documentation as being "work in progress"

This (short) document descrip how to make OOBD run on hopefully any embedded linux system, here a raspi with raspian 

- Take a virgin (raspian) image
- boot the raspi (or BeagleBone or ...) with it
- download the premilary oobdd zip file from the [Google Drive](https://drive.google.com/open?id=0B795A63vSunRa29qbGVxTllkRGM])
- place it in your raspi home directory (e.g. /home/pi)
- in the raspi terminal, run 


    cd
    bash <(curl -s https://github.com/stko/oobd/raw/development/clients/unixInstall/install.sh)


This will install all necessary packages, download and compile the firmware etc.

If everything goes well, you will find the script `oobdd.sh` in `~/home/bin/oobd/oobdd`. When executing this, the firmware will connect to the socketcan (if there is hopefully any..) and oobd will be reachable via its build in webserver. With `CTRL-C` the process can be stopped again.

As the IP address of an embedded device is not easy to identify, OOBD broadcasts it's web presence via ZeroConfig (aka Bonjour) into the local subnet, which can be monitored and used by several programs to open the Browser.

On Android [Zentri](https://play.google.com/store/apps/details?id=discovery.ack.me.ackme_discovery) works quite well

Awaiting your comments on [Google Groups](https://groups.google.com/forum/#!forum/oobd-diagnostics) :-)

