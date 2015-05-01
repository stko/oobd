<?php
$config=array();

// salt the MD5 hash
$config['md5salt']="saltANDpepper";

// where is this side located?
$config['sideurl']="http://keys.oobd.org";

//which email address should be the sender of the mails?
$config['sendername']="OOBD GPG Key Generator";
$config['senderemail']="keys@oobd.org";
$config['adminemail']="steffen@koehlers.de";

//used for SMTP Authentification
$config['smtphost']="localhost";
$config['smtpusername']="oobd@luxen.de";
$config['smtppassword']="pgpStM32andrO";

?>