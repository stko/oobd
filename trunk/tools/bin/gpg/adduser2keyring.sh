#!/bin/sh 
export user=$(gpg --no-default-keyring --keyring ./userkey.pub --list-packets $1 | grep  -o "[A-Za-z0-0._]*@[A-Za-z0-9._]*")
echo $user
gpg --yes --batch --no-default-keyring --keyring ./userkey.pub --delete-key $user
gpg --no-default-keyring --keyring ./userkey.pub --import $1
gpg --no-default-keyring --keyring ./userkey_archive.pub --import $1
echo List of actual keys
gpg --no-default-keyring --keyring ./userkey.pub --list-keys
echo List of archive keys
gpg --no-default-keyring --keyring ./userkey_archive.pub --list-keys
