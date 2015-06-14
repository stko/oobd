#!/bin/sh

echo Scanning for Dongles around- please wait...

./getBTMAC.sh
choice=`cat /tmp/BT`

case $choice in
	NONE)
		;;
	*)
		echo Try to connect ...
		echo $choice
		bluez-simple-agent hci0 $choice
		python3 hwTest33.py $choice
		bluez-simple-agent hci0 $choice remove
		read  -p "Test done - press return to continue" inputline ;;
esac

