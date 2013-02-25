#!/bin/bash 
. settings.inc
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCRIPTNAME=$( basename ${BASH_SOURCE[0]})

# if no command line arg given
# set command to Unknown
if [ -z $1 ]
then
  command="no command"
elif [ -n $1 ]
then
# otherwise make first arg as a command
  command=$1
fi
 
# use case statement to make decision for command
case $command in
	"adduser")
		echo "adding " $2 " to keyring"
		$DIR/adduser2keyring.sh $2
	;;
	"deluser")
		echo "delete " $2 " from keyring"
		gpg --no-default-keyring --keyring ./userkey.pub --delete-key $2
	;;
	"update")
		echo "Updating user groupkey files"
		$DIR/updateUserGroupFiles.sh $AUTHORITYPUBLICKEY $OLDUSERACCESS $NEWUSERACCESS
	;;
	"grouplist")
		echo "generate grouplist file " $GROUPLIST "from directory " $LUAMAINDIR
		$DIR/creategrouplist.sh $LUAMAINDIR > $GROUPLIST
	;;
	"groupkeys")
		echo "generate groupkeys from " $GROUPLIST 
		$DIR/makegroups.sh $APPPASSPHRASEFILE  $GROUPLIST $GROUPPUBKEYS $GROUPSECKEYS
	;;
	"sendkeys")
		echo "send new generated keys.." 
		$DIR/sendgroupmails.vbs 
	;;
	"clean")
		echo "remove the temporary files"
		# rm -v $NEWUSERACCESS.sig_* $NEWUSERACCESS_* $TEMPUSERACCESS
	;;
	*)
		echo $SCRIPTNAME ": a part of the OOBD tool chain www.oobd.org"
		echo
		echo "Handles the OOBD access control"
		echo
		echo "Usage:"
		echo $SCRIPTNAME " adduser public_key_file"
		echo "add new user to actual and to archive key file"
		echo
		echo $SCRIPTNAME " deluser username"
		echo "delete user from actual key file, but keep him in archive key file"
		echo
		echo $SCRIPTNAME " update"
		echo "calculate updates out of old and new useraccess-file"
		echo
		echo $SCRIPTNAME " grouplist"
		echo "list the default directory into grouplist file"
		echo
		echo $SCRIPTNAME " groupkeys"
		echo "generate group keys out of the grouplist file"
		echo
		echo $SCRIPTNAME " sendkeys"
		echo "send actual generated group files to users. Caution: Works only in Windows"
		echo "in an Outlook/VBS environment"
		echo
		echo $SCRIPTNAME " clean"
		echo "remove the temporary files"
		echo
	;;
esac
