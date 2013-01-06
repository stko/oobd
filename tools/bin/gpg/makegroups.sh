DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# as cygwin pipes dont work, we have to use temporary files instead
php $DIR/makegroups.php $1 $2 $3 $4 >keys.cfg
gpg --no-default-keyring --secret-keyring $4 --keyring $3 --batch --gen-key keys.cfg
rm keys.cfg
gpg --no-default-keyring --secret-keyring $4 --keyring $3 --list-secret-keys
 
