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
		python btTest.py $choice
		read  -p "Test done - press return to continue" inputline ;;
esac

