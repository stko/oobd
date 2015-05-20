<?php
$config=array();

// salt the MD5 hash
$config['md5salt']="whateveryouwant";

// where is this side located?
$config['sideurl']="http://my.key.side";

//which email address should be the sender of the mails?
$config['sendername']="GPG Key Generator";
$config['senderemail']="keys@my.key.side";
$config['adminemail']="admin@my.key.side";

//used for SMTP Authentification
$config['smtphost']="your.smtp.host";
$config['smtpusername']="username@example.org";
$config['smtppassword']="yourpassword";

?>