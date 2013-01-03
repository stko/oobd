gpg --yes --no-default-keyring --trust-model always  --keyring ./userkey.pub --recipient $1 --output $1_groups.pgp --encrypt oobd_groups.sec
 
