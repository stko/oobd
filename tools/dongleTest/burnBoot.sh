#!/bin/sh

cd ~/bin/dongleTest
export USBMC=/dev/ttyUSB0

if [ ! -f flash/Flashloader_Package/filelist ]
then
	dialog --title 'Error' --msgbox 'No flash file config found\ Abort' 5 20
	exit 1
fi
. flash/Flashloader_Package/filelist

if [ ! -f flash/Flashloader_Package/$BLHEX ]
then
	dialog --title 'Error' --msgbox 'No Bootloader Hex file found\ Abort' 5 20
	exit 1
fi

if [ ! -f flash/Flashloader_Package/$FWHEX ]
then
	dialog --title 'Error' --msgbox 'No firmware Hex file found\ Abort' 5 20
	exit 1
fi

	dialog --title "Is your USB wire connected?"  --yesno "Continue?" 6 30
	if [ $? -ne 0 ] ; then
		exit 1
	fi

	dialog --title "Is Boot0 active?"  --yesno "Continue?" 6 25
	if [ $? -ne 0 ] ; then
		exit 1
	fi

	dialog --title "Did you pressed Reset?"  --yesno "Continue?" 6 25
	if [ $? -ne 0 ] ; then
		exit 1
	fi


while true
do
	tempfile=`tempfile 2>/dev/null` || tempfile=/tmp/test$$
	trap "rm -f $tempfile" 0 1 2 5 15

	dialog --backtitle "Dialog - OOBD Initial Flashing" --title " Flash Menu"\
		--cancel-label "Quit" \
		--menu "Move using [UP] [DOWN], [Enter] to select" 17 60 10\
		1 "Flash the Bootloader via USB"\
		2 "Flash the Firmware via USB"\
		3 "Flash both "\
		Quit "Exit the program" 2>$tempfile

	opt=${?}
	if [ $opt != 0 ]; then rm $tempfile; exit; fi
	menuitem=`cat $tempfile`
	echo "menu=$menuitem"
	case $menuitem in
		1)
			echo flash bootloader
			#../stm32flash/stm32flash -z $USBMC
			#../stm32flash/stm32flash -w flash/Flashloader_Package/$BLHEX -v -x $USBMC
			;;
		2)
			echo flash firmware
			#../stm32flash/stm32flash -z $USBMC
			#../stm32flash/stm32flash -w flash/Flashloader_Package/$FWHEX -v -o 0x23fC -g 0x0 $USBMC
			;;
		3)
			echo flash both
			#../stm32flash/stm32flash -z $USBMC
			#../stm32flash/stm32flash -w flash/Flashloader_Package/$BLHEX -v -x $USBMC
			#../stm32flash/stm32flash -w flash/Flashloader_Package/$FWHEX -v -o 0x23fC -g 0x0 $USBMC
			;;
		Quit) rm $tempfile; exit;;
	esac
read  -p "Process done - press return to continue" inputline 
done
