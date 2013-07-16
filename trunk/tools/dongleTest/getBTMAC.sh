#!/bin/sh
DIALOG=${DIALOG=dialog}
tempfile=`tempfile 2>/dev/null` || tempfile=/tmp/test$$
trap "rm -f $tempfile" 0 1 2 5 15
HANDYS=$(hcitool scan | awk 'BEGIN { FS = "\t" } ; /:/ {printf " \"%s\" \"%s\" ",$2,$3} ')
CMD=$DIALOG' --ok-label "Select" --cancel-label "Cancel" --clear --title "OOBD Dongle Selector" --menu "Please select your Dongle" 20 51 4 "Device" "Name" '$HANDYS
sh -c "$CMD" 2> $tempfile
retval=$?

choice=`cat $tempfile`

case $retval in
	0)
		echo -n $choice > /tmp/BT ;;
	1)
		echo n "NONE" > /tmp/BT ;;
	255)
		echo -n "NONE" > /tmp/BT ;;
esac

