php makegroups.php $1 $2 | gpg --batch --gen-key -
gpg --no-default-keyring --secret-keyring ./oobd_groups.sec --keyring ./oobd_groups.pub --list-secret-keys
 
