#!/bin/sh
blueman-applet >/dev/null &
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
		boot) cd ~/bin/dongleTest ; ./burnBoot.sh ;;
		flash) cd ~/bin/dongleTest ; ./downloadBins.sh ; if [ $? -eq 0 ]; then ./flashfw.sh ; fi  ;;
		hwTest) cd ~/bin/dongleTest ; ./hwTest.sh ;;
		Quit) rm $tempfile; exit;;
	esac
done
