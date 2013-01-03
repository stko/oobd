gpg --yes --no-default-keyring --trust-model always --keyring ./oobd_groups.pub --recipient $1 --output $2.pgp --encrypt $2
 
