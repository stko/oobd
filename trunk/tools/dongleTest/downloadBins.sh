#!/bin/sh 

test -f ~/oobd.conf && source ~/oobd.conf
echo $HTTP_HOST $HTTP_PORT
if [ "$HTTP_HOST" -a "$HTTP_PORT" ]; then
	export HTTP_PROXY=$HTTP_HOST:$HTTP_PORT

fi
echo $HTTP_PROXY
mkdir flash
# for some reason we need to use http instead the original https URL
wget --no-cache --cache=off http://oobd.googlecode.com/files/Flashloader_Package.zip -O Flashloader_Package.zip	
unzip -d flash Flashloader_Package.zip
