#!/bin/sh
blueman-applet &
while true
do
	tempfile=`tempfile 2>/dev/null` || tempfile=/tmp/test$$
	trap "rm -f $tempfile" 0 1 2 5 15

	dialog --backtitle "Dialog - OOBD Dongle Maintenance" --title " Main Menu"\
		--cancel-label "Quit" \
		--menu "Move using [UP] [DOWN], [Enter] to select" 17 60 10\
		boot "Flash the bootloader"\
		flash "Flash the Firmware via BT"\
		hwTest "Test the Hardware via BT"\
		Quit "Exit the program" 2>$tempfile

	opt=${?}
	if [ $opt != 0 ]; then rm $tempfile; exit; fi
	menuitem=`cat $tempfile`
	echo "menu=$menuitem"
	case $menuitem in
		boot) dialog --msgbox "Not implemented yet" 10 50 ;;
		flash) cd ~/bin/dongleTest ; ./downloadBins.sh ; ./flashfw.sh ;;
		hwTest) cd ~/bin/dongleTest ; ./hwTest.sh ;;
		Quit) rm $tempfile; exit;;
	esac
done
