#!/bin/bash 
#. settings.inc
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCRIPTNAME=$( basename ${BASH_SOURCE[0]})

# if no command line arg given
# set command to Unknown
if [ -z $1 ] || [ -z $2 ]
then
  command="no command"
else
# otherwise make first arg as a treasebox
  treasurebox=$1
# otherwise make second arg as a command
  command=$2
fi
 
# use case statement to make decision for command
case $command in
    "create")
    	re='^[0-9]+$'
    		read -p "How many people shall share the key? :" people
		if ! [[ $people =~ $re ]] ; then
		   echo "error: Number of people ('$people') is not a numeric number" >&2; exit 1
		fi
    		read -p "How many people shall be needed to unlock the box? :" treshold
		if ! [[ $treshold =~ $re ]] ; then
		   echo "error: Number of people to unlock ('$treshold') is not a numeric number" >&2; exit 1
		fi
		if [ "$treshold" -gt "$people" ]; then
		    echo "error: Number of people to unlock ('$treshold') is bigger as people at all ('$people')" >&2; exit 1
		fi
		echo "create treasurebox " $treasurebox
		RANDPASSWORD=$(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c 32)
		echo "generated pass phrase:" $RANDPASSWORD
		echo "Generate PGP files $treasurebox.sec and $treasurebox.pub"
#cat <<EOF
gpg --no-default-keyring  --secret-keyring ./$treasurebox.sec --keyring ./$treasurebox.pub --batch --gen-key - <<EOF
Key-Type: DSA
#%dry-run
Key-Length: 2048
Subkey-Type: ELG-E
Subkey-Length: 2048
Expire-Date: 0
Passphrase: $RANDPASSWORD
%echo Generating  pgp key files  for $treasurebox
Name-Real: $treasurebox Treasurebox
Name-Comment: for details see http://www.oobd.org/doku.php?id=doc:tools_treasurebox
%commit
EOF
		echo $RANDPASSWORD > $treasurebox.passphrase
		echo 
		echo $RANDPASSWORD | ssss-split -q -Q -t $treshold -n $people -w "$treasurebox=$treshold=" > $treasurebox.list_of_keys
		rm $treasurebox.zip.treasurebox
		zip $treasurebox.zip.treasurebox $treasurebox.list_of_keys $treasurebox.sec $treasurebox.passphrase
		rm $treasurebox.treasurebox
		echo $RANDPASSWORD | gpg --batch --passphrase-fd 0 --symmetric -o $treasurebox.treasurebox $treasurebox.zip.treasurebox
		rm $treasurebox.zip.treasurebox
		echo "---------------------------------------------------------------"
		echo "Job done - the following files have been generated:"
		ls -l $treasurebox.*
		echo
		echo "$treasurebox.list_of_keys : List of shared keys. Take this keys"
		echo "line by line and distribute them separately to the people who"
		echo "should share the treasure box "
		echo 
		echo "$treasurebox.treasurebox : The treasurebox file containing all"
		echo "data to open the box. Place this file somewhere were all key holder"
		echo "can reach it easely to be able to open the box, if needed"
		echo
		echo "$treasurebox.pub : This is the public pgp key of the treasure box."
		echo "Publish it and/or share it with everybody who might want to put some"
		echo "into the box too"
		echo 
		echo "$treasurebox.sec : This is the secret pgp key of the treasure box."
		echo "This allows the originator of the treasure box to read (=decrypt) "
		echo "treasure box files"
		echo 
		echo "$treasurebox.passphrase : This is the secret pgp passphrase of the treasure box."
	;;
	"add")
		gpg --yes --batch --no-default-keyring --trust-model always  --keyring ./$treasurebox.pub --recipient $treasurebox --output $3.$treasurebox --encrypt $3
		if [ $? -ne 0 ] ; then
		   echo "error: something went wrong..." >&2; exit 1
		fi
		echo "put $3 in the $treasurebox treasurebox as file " $3.$treasurebox
		
	;;
	"openbox")
		re='^[0-9]+$'
		if ! [ -f $3  ] ; then
		   echo "error: No file given containing the keys to open the box" >&2; exit 1
		fi
		THRESHOLD=$(head -1 $3 | cut -d "=" -f 2)
		if ! [[ $THRESHOLD =~ $re ]] ; then
		   echo "error: key list file ('$3') does not contain valid data" >&2; exit 1
		fi
		RANDPASSWORD=$(ssss-combine -q -Q -t $THRESHOLD <$3 2>&1 | tail -1)
		if [ $? -ne 0 ] ; then
		   echo "error: key list file ('$3') does not contain valid data" >&2; exit 1
		fi
		echo $RANDPASSWORD
		echo $RANDPASSWORD | gpg --batch --passphrase-fd 0 --decrypt -o $treasurebox.treasurebox.zip $treasurebox.treasurebox 
		unzip $treasurebox.treasurebox.zip
	;;
	*)
		echo $SCRIPTNAME ": a part of the OOBD tool chain www.oobd.org"
		echo
		echo "Handles shared secrets to have a 'treasure Box' of shared, but security locked data inside a team"
		echo
		echo "Usage:"
		echo $SCRIPTNAME " treasureboxname create "
		echo "create all new files for a new treasure box named treasureboxname"
		echo "The treasureboxname is also used as prefix for several related files"
		echo
		echo $SCRIPTNAME " treasureboxname add file"
		echo "encrypts file with the treasureboxname public key to file.treasureboxname"
		echo
		echo $SCRIPTNAME " treasureboxname openbox treasure-keys-file"
		echo "opens a treasurebox, when enough keys have been collected in treasure-keys-file"
		echo
		
	;;
esac


