##########################################################################
#
#
# This is the settings file for the OOBD GPG key management
#
#
# copy this file as settings.inc into the directory with all your personal OOBD key files
#
# and then handle your OOBD access rights with the command line tool
#
#         accessmanager.sh command ....
#
##########################################################################


##########################################################################
#
# APPPASSPHRASEFILE : The name of the file containing the Application Pass Phase
#
# CAUTION: Depending of the file extension, the file is handled in two different ways:
#
# if the extension is ".gpg" than the accessmanager will try to gpg-encrypt the file to use the encrypted content as Application Pass Phase
#
# if the extension is not ".gpg" than the accessmanager will try to use the first text line of that file as Application Pass Phase
#
# IMPORTANT: Please note that gpg always wants to see a leading ./ for any key file in the local directory, otherways it would search in the
# default users gpg home directory
#
##########################################################################
export APPPASSPHRASEFILE=./appPassPhrase.txt.gpg


##########################################################################
#
# Files needed for update: 
#
# AUTHORITYPUBLICKEY: The keyfile which contains the public keys of the persons who are authorized to define the user access groups
#
# IMPORTANT: Please note that gpg always wants to see a leading ./ for any key file in the local directory, otherways it would search in the
# default users gpg home directory
#
# NEWUSERACCESS : The file which contains the actual user acess groups
#
# OLDUSERACCESS : The file which contains the old user acess groups
#
# TEMPUSERACCESS : The file which contains the temporary group information for the users which has been indentified as changed
#
# LOGUSERACCESS : The zip archive which stores all used accessgroup files for later reference
#
##########################################################################
export AUTHORITYPUBLICKEY=./authority.pub
export NEWUSERACCESS=./useraccess.txt
export OLDUSERACCESS=./olduseraccess.txt
export TEMPUSERACCESS=./tempuseraccess.txt
export LOGUSERACCESS=./useraccess_achive.zip


##########################################################################
#
# Files needed to store the access groups 
#
# LUAMAINDIR: The main directory, where all the dirs are in, which should be the access groups
#
# GROUPLIST: File to store the existing groups in
#
# GROUPSECKEYS: Groups secret keys
#
# GROUPPUBKEYS: Groups public keys
#
##########################################################################
export LUAMAINDIR=../lua
export GROUPLIST=./grouplist.txt
export GROUPSECKEYS=./oobd_groups.sec
export GROUPPUBKEYS=../oobd_groups.pub

##########################################################################
#
# Admins permitted to read all groupfiles 
#
# ADMINREADER: Admins, given by their emails, who are permitted to read each generated groupfile
#
# to use, uncommend the line below
#
##########################################################################
#export ADMINREADER="--recipient admin1 --recipient admin2.."

##########################################################################
#
# create NSIS Installer for each User identified by the "update" command 
#
# INSTALLERSOURCEDIR : path, where the directories are in which corosponts to the group names
#
# INSTALLERTARGETDIR : path, where created installers should be placed in
#
# INSTALLERTEMPLATE : NSIS installer script template file with some placeholders
#
# INSTALLERLICENCE : The licence text for the installer
#
# to use, uncommend the lines below
#
##########################################################################
#export INSTALLERSOURCEDIR=""
#export INSTALLERTARGETDIR=""
#export INSTALLERTEMPLATE=""
#export INSTALLERLICENCE=""


