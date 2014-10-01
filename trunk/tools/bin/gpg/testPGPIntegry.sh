#!/usr/bin/bash
rm actgroupkeys.sec
echo "When entering pathes, you can use the bash autocomplete tab key function"
echo "use a leading ./ for key files in local directory, as gpg requires this"
read -e -p "Enter the full path to your userkey.pub file: " PUBFILE
PUBPATH=${PUBFILE%/*}
read -e -p "Enter the full path to your userkey.sec file: " -i $PUBPATH  SECFILE 
read -e -p "Enter the full path to your groupkey.sec file: " -i $PUBPATH  SECGROUPFILE 
read -e -p "Enter the full path to the public oobd_groups.pub file: "  PUBGROUPFILE 
PUBGROUPPATH=${PUBGROUPFILE%/*}
read -e -p "Enter the full path to the application key data file: " -i $PUBGROUPPATH  appPassPhraseFile
read -e -p "Enter the full path to a pgp Script file for testing: " -i $PUBPATH SCRIPTFILE
gpg --no-default-keyring --keyring "$PUBFILE" --secret-keyring "$SECFILE" --output actgroupkeys.sec --decrypt "$SECGROUPFILE"
if [ $? -eq 0 ] ; then
	echo "--------------------------------"
	echo "Step 1: the user keys allowed to decrypt the group keys"
	gpg  -o - --decrypt $appPassPhraseFile | gpg --passphrase-fd 0 --batch --yes --no-default-keyring --keyring "$PUBGROUPFILE" --secret-keyring ./actgroupkeys.sec -o - --decrypt "$SCRIPTFILE" | md5sum
	if [ $? -eq 0 ] ; then
		echo "--------------------------------"
		echo "Everything ok :-)"
	else
		echo "--------------------------------"
		echo "ERROR: encrypted script file  does not fit to group keys"
	fi

else
	echo "--------------------------------"
	echo "ERROR groupkey.sec can't be decrypted with users key files"

fi
rm actgroupkeys.sec
