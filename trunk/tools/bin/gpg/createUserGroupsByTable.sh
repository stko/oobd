#!/bin/bash
# delete old group rings first
rm *.groupring
rm *.groupring.tmp
declare -A USERS
while read line ; do
	user=$(echo $line | cut -d':' -f1)
	group=$(echo $line | cut -d':' -f2)
	emailId=$(echo $user | md5sum)
	USERS[$user]=$user
	echo "user: [$user]"
	echo "group:[$group]"
	gpg --yes --batch --no-default-keyring --secret-keyring ./oobd_groups.sec  --export-secret-key $group >> $user.groupring.tmp
done < $1
for user in "${!USERS[@]}"
do
	echo "key :" $user
	echo "value:" ${USERS[$user]}
	gpg --yes --batch --no-default-keyring --secret-keyring ./$user.groupring.tmp  -o $user.groupring --export-secret-keys

	gpg -v --yes --batch --no-default-keyring --trust-model always  --keyring ./userkey.pub --recipient $user --output $user.groupkeys --encrypt $user.groupring
#	gpg -v --yes --batch --no-default-keyring --trust-model always  --keyring ./userkey.pub --recipient $user --output $user.groupkeys --encrypt ./oobd_groups.sec
done
 
