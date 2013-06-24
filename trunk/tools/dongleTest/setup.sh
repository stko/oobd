#!/bin/sh 

test -f oobd.conf && source oobd.conf
if test -n $HTTP_HOST -a -n $HTTP_PORT ; then
	export HTTP_PROXY=$HTTP_HOST:$HTTP_PORT

fi

echo "Acquire::http::Proxy \"http://"$HTTP_PROXY"\"/;" > /tmp/pp
sudo cp /tmp/pp /etc/apt/apt.conf.d/10-proxy

sudo apt-get update
sudo apt-get install subversion python-easygui python-bluetooth ussp-push blueman

# setting the subversion proxy

if test -n $HTTP_HOST -a -n $HTTP_PORT ; then
  test ! -d ~/.subversion && mkdir ~/.subversion
	echo "[global]" > ~/.subversion/servers
	echo "http-proxy-host = "$HTTP_HOST >> ~/.subversion/servers
	echo "http-proxy-port = "$HTTP_PORT >> ~/.subversion/servers

fi



mkdir -p ~/.config/autostart
mkdir ~/bin
rm -rf ~/bin/dongleTest
svn export http://oobd.googlecode.com/svn/trunk/tools/dongleTest ~/bin/dongleTest
chmod +x ~/bin/dongleTest/*.sh
rm -rf ~/bin/stm32flash
svn export http://oobd.googlecode.com/svn/trunk/tools/stm32flash ~/bin/stm32flash
chmod +x ~/bin/stm32flash/stm32flash


if test -d ~/Desktop
then
cat << EOF > ~/Desktop/OOBDongle.desktop
[Desktop Entry]
Version=1.0
Type=Application
Name=OOBD Dongle 
Comment=OOBD Dongle Maintanance Tools
Path=/home/knoppix/bin/dongleTest
Exec=lxterminal -e ./runDongle.sh
Icon=/home/knoppix/bin/dongleTest/runDonglelogo_48.xpm
EOF
fi
