#!/bin/bash 
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
# as cygwin pipes dont work, we have to use temporary files instead
php $DIR/makegroups.php $1 $2 $3 $4 

#gpg --no-default-keyring --secret-keyring $4 --keyring $3 --batch --gen-key keys.cfg

for f in *.groupsec
do 
	GROUPNAME=$(basename $f .groupsec)
	GROUPSECFILE=$GROUPNAME.sec
	if [ ! -s $GROUPSECFILE ]  ; then 
		gpg --no-default-keyring   --keyring $3 --batch --yes --delete-key $GROUPNAME
		gpg --no-default-keyring  --secret-keyring ./$GROUPSECFILE --keyring $3 --batch --gen-key $f
		echo create $GROUPSECFILE
	else
		echo $GROUPSECFILE already exist
	fi
done
rm *.groupsec
gpg --no-default-keyring  --keyring $3 --list-keys
echo Add result to repository
svn add *.sec
