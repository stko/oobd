#!/bin/bash 
# $1 grouplist- keyfile , $2 old groupfile , $3 new groupfile 
# first check, if the group file signature is valid
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo keyring $1
echo signature $3
gpg --status-fd 1 --no-default-keyring --keyring $1 --verify $3.sig | grep "GOODSIG"
if [ $? -eq 0 ] ; then	
	echo Signed
	php $DIR/diffUserGroups.php $2 $3 > $TEMPUSERACCESS
	# if update file is not empty
	if [ -s $TEMPUSERACCESS ] ; then
		$DIR/createUserGroupsByTable.sh $TEMPUSERACCESS 
		TIMESTAMP=$(date  +%F_%T)
		cp $3.sig $3.sig_$TIMESTAMP
		cp $3 $3_$TIMESTAMP 
		zip $LOGUSERACCESS $3.sig_$TIMESTAMP $3_$TIMESTAMP 
		cp $NEWUSERACCESS $OLDUSERACCESS 
		rm $3.sig_* $3_*
	else
		echo No updates found
	fi
else
	echo "Group access config file  $3 has no valid signature"
fi
