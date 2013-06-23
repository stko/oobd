#!/bin/sh 
sudo apt-get update
sudo apt-get install subversion
sudo apt-get install python-easygui
sudo apt-get install ussp-push
mkdir -p ~/.config/autostart
mkdir ~/bin
svn export http://oobd.googlecode.com/svn/trunk/tools/dongleTest ~/bin/dongleTest
chmod +x ~/bin/dongleTest/*.sh
svn export http://oobd.googlecode.com/svn/trunk/tools/stm32flash ~/bin/stm32flash
chmod +x ~/bin/stm32flash/stm32flash


