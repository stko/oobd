#!/bin/sh 
sudo apt-get update
sudo apt-get install subversion
sudo apt-get install python-easygui
sudo apt-get install python-bluetooth
sudo apt-get install ussp-push
sudo apt-get install blueman
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
