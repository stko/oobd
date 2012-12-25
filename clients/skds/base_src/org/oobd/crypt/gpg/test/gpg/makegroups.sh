gpg --batch --gen-key groupkeys.cfg
gpg --no-default-keyring --secret-keyring ./oobd_groups.sec --keyring ./oobd_groups.pub --list-secret-keys
 
