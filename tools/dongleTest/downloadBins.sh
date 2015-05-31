#!/bin/sh 

test -f ~/oobd.conf && source ~/oobd.conf
echo $HTTP_HOST $HTTP_PORT
if [ "$HTTP_HOST" -a "$HTTP_PORT" ]; then
	export HTTP_PROXY=$HTTP_HOST:$HTTP_PORT

fi
echo $HTTP_PROXY
mkdir flash
# for some reason we need to use http instead the original https URL
wget --no-cache --no-check-certificate --cache=off https://github.com/stko/oobd/blob/master/interface/Designs/CORTEX/STM32F103C8_Eclipse_GCC/D2/flashfiles/AllinOne/Flashloader_Package.zip?raw=true -O Flashloader_Package.zip.tmp	
if [ $? -ne 0 ]; then
	dialog --title "File update failed"  --yesno "Continue anyway?" 6 25
	if [ $? -ne 0 ] ; then
		exit 1
	fi
else
	cp Flashloader_Package.zip.tmp Flashloader_Package.zip
fi
if [ ! -f Flashloader_Package.zip ]; then
	dialog --title 'Error' --msgbox 'No flash files found\ Abort' 5 20
	exit 1
fi
unzip -o -d flash Flashloader_Package.zip
exit 0
