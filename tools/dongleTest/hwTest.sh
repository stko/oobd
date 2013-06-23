#!/bin/sh
DIALOG=${DIALOG=dialog}
tempfile=`tempfile 2>/dev/null` || tempfile=/tmp/test$$
trap "rm -f $tempfile" 0 1 2 5 15
echo Scanning for Dongles around- please wait...
HANDYS=$(hcitool scan | awk 'BEGIN { FS = "\t" } ; /:/ {printf " \"%s\" \"%s\" ",$2,$3} ')
CMD=$DIALOG' --ok-label "Run the Test" --cancel-label "New Scan" --clear --title "OOBD Dongle Tester" --menu "Please select your Dongle" 20 51 4 "Device" "Name" '$HANDYS
sh -c "$CMD" 2> $tempfile
retval=$?

choice=`cat $tempfile`

case $retval in
	0)
		echo Try to connect ...
		echo $choice
		python hwTest.py $choice
		read  -p "Test done - press return to continue" inputline ;;
	1)
		echo ;;
	255)
		echo "ESC pressed.";;
esac

