#!/bin/bash
echo collect all necessary key files first in one directory and start this script from there, just with the script to evaluate as single parameter
gpg --no-default-keyring --secret-keyring ./userkey.sec --keyring ./userkey.pub -o secret_groupring --decrypt ./groupkey.sec 
gpg --no-default-keyring --secret-keyring ./secret_groupring --keyring ./oobd_groups.pub  $1
