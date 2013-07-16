#!/bin/sh
. flash/Flashloader_Package/filelist
echo Scanning for Dongles around- please wait...

./getBTMAC.sh
choice=`cat /tmp/BT`

case $choice in
	NONE)
		;;
	*)
		echo Try to connect ...
		echo $choice
		python flashfw.py $choice flash/Flashloader_Package/$FWBIN
 		read  -p "Flash finished - press return to continue" inputline ;;
esac

