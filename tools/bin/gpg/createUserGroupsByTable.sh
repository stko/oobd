#!/bin/bash 
DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$DIR" ]]; then DIR="$PWD"; fi
. "$DIR/settings.inc"
# delete old group rings first
rm *.groupring
rm *.groupring.tmp
declare -A USERS
while read line ; do
	user=$(echo $line | cut -d':' -f1)
	group=$(echo $line | cut -d':' -f2)
	USERS[$user]=$user
	echo "user: [$user]"
	echo "group:[$group]"
	gpg --yes --batch --options ./gpg.conf --no-default-keyring --secret-keyring ./$group.sec  --export-secret-key $group >> $user.groupring.tmp
	gpg --yes --batch --options ./gpg.conf --no-default-keyring --keyring ../oobd_groups.pub  --fingerprint $group >> $user.groupkeys.fingerprint
done < $1
for user in "${!USERS[@]}"
do
	echo "key :" $user
	echo "value:" ${USERS[$user]}
	echo "---- The fingerprints above are the groups for which you are actual authorized for.---------" >> $user.groupkeys.fingerprint
	echo "---- The fingerprint below is your public key which the groupkeys are encrypted with ----" >> $user.groupkeys.fingerprint
	gpg --yes --batch --no-default-keyring --secret-keyring ./$user.groupring.tmp  -o $user.groupring --export-secret-keys
	echo "adminreader :" $ADMINREADER

	gpg --yes --batch --options ./gpg.conf --no-default-keyring  --keyring ./userkeyring.pub --fingerprint $user >> $user.groupkeys.fingerprint
	gpg --yes --batch --options ./gpg.conf --no-default-keyring --trust-model always  --keyring ./userkeyring.pub --recipient $user $ADMINREADER --output $user.groupkeys --encrypt $user.groupring
#	gpg -v --yes --batch --options ./gpg.conf --no-default-keyring --trust-model always  --keyring ./userkeyring.pub --recipient $user --output $user.groupkeys --encrypt ./oobd_groups.sec
	grep -i "$user" useraccess.txt > $user.groupkeys.lst
	echo "Debug: Variables:" $NEWUSERACCESS $user $user.groupkeys $INSTALLERTEMPLATE $INSTALLERLICENCE $INSTALLERSOURCEDIR $INSTALLERTARGETDIR $GETFULLPATHCMD
	if [[ -d "${INSTALLERSOURCEDIR}" ]] ; then
		if [[ ! -d "${INSTALLERTARGETDIR}" ]] ; then
			mkdir -p "${INSTALLERTARGETDIR}"
		fi
		if [[ ! -d "${INSTALLERTARGETDIR}" ]] ; then
			echo "Error: can not create installer target" "${INSTALLERTARGETDIR}"
		else
			echo "make Installer!"
			echo  $NEWUSERACCESS $user $user.groupkeys $INSTALLERTEMPLATE $INSTALLERLICENCE $INSTALLERSOURCEDIR $INSTALLERTARGETDIR $GETFULLPATHCMD
			php $DIR/createNSISfiles.php  "$NEWUSERACCESS" "$user" "$user.groupkeys" "$INSTALLERTEMPLATE" "$INSTALLERLICENCE" "$INSTALLERSOURCEDIR" "$INSTALLERTARGETDIR" "$GETFULLPATHCMD"
			(cd "$INSTALLERTARGETDIR" && "$INSTALLEREXE" $user.nsi && zip "$user".zip "$user".exe)
		fi
		
	fi
done
 
rm *.groupring
rm *.groupring.tmp
