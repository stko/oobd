<?php
include("mail.php");

	if ($argc==4){ // = 3 parameters, param 0 is the script name itself
		$fullname=	base64_decode ($argv[1]);
		$email=		base64_decode ($argv[2]);
		$pp=		base64_decode ($argv[3]);


		$configfile = tempnam("/tmp", "gpg");

		$handle = fopen($configfile, "w");
		fwrite($handle, "#%dry-run
%echo Generating key for $email
Key-Type: DSA
Key-Length: 2048
Subkey-Type: ELG-E
Subkey-Length: 2048
Name-Real: $fullname
Name-Comment: OOBD gpg user key  
Name-Email: $email
Expire-Date: 0
Passphrase: $pp
%pubring $configfile.pub
%secring $configfile.sec
%commit

");
		fclose($handle);
		$command="/usr/bin/gpg --no-default-keyring --keyring $configfile.pub --secret-keyring $configfile.sec --batch --gen-key $configfile 2> /tmp/gpg-errors.txt";
		//$command="/opt/bin/gpg2 --version";
		exec($command,$output,$retval);
		ob_start();
		var_dump($output);
		$result = ob_get_clean();
		if ($retval==0){
			// send the public key	
			$content = "Hello $fullname ($email),
 
this is your PUBLIC key file. This key file is NOT secret and is needed by everybody who wants to send you encrypted data.


Please forward this mail as it is to your OOBD key master. He'll need this file to generate your personal group file which will then allow you use encrypted OOBD data.

best regards

the OOBD Key Generator";

			sendMail(
			"keys@oobd.org",		// sender email
			"OOBD GPG Key Generator",	// senders full name
			$email,				// Receivers email
			"Your Public PGP Key",		// subject
			$content,			// content
			$configfile.".pub",		// file to send
//			$configfile,			// file to send
			"userkey.pub",			// attachment file name
			"application/octet-stream"	// mime type
			);
			// send the secret key	
			$content = "Hello $fullname ($email),
 
this is your SECRET key file and your SECRET passphrase. These data are SECRET!, SECRET!, SECRET! (Understood?)

passphrase: $pp


DO NOT GIVE THIS DATA TO ANYBODY ELSE!
STORE THIS DATA IN A SECURE PLACE!
WHEN GOES LOST, THIS DATA CAN NOT BE RECOVERED!

You'll need to transfer the attached file userkey.sec onto your android device (see documentation) to use encrypted OOBD files.

You'll also need your passphrase to activate this feature in OOBD


Finally you'll also need the group file from your key master which grants you access to the different OOBD data groups.

best regards

the OOBD Key Generator";

			sendMail(
			"keys@oobd.org",		// sender email
			"OOBD GPG Key Generator",	// senders full name
			$email,				// Receivers email
			"Your SECRET! pgp key",		// subject
			$content,			// content
			$configfile.".sec",		// file to send
//			$configfile,			// file to send
			"userkey.sec",			// attachment file name
			"application/octet-stream"	// mime type
			);
			// inform the admin
			$content = "Just for info: A key has been generated for Name: $fullname Email: $email";

			sendMail(
			"keys@oobd.org",		// sender email
			"OOBD GPG Key Generator",	// senders full name
			"steffen@koehlers.de",		// Receivers email
			"Key Generation Report",	// subject
			$content,			// content
			"",				// file to send
			"config.txt",			// attachment file name
			"text/plain"			// mime type
			);

		}else{

			$content = "Error while generating key for Name: $fullname Email: $email 
retval: $retval
configfile name: $configfile
command:$command
command output:
$result";

			sendMail(
			"keys@oobd.org",		// sender email
			"OOBD GPG Key Generator",	// senders full name
			"steffen@koehlers.de",		// Receivers email
			"Key generation Error",		// subject
			$content,			// content
			$configfile,			// file to send
			"config.txt",			// attachment file name
			"text/plain; charset=UTF-8"	// mime type
			);
			// inform the user
			$content = "An Error occured while generating key for Name: $fullname Email: $email.

The system administrator has been informed.";

			sendMail(
			"keys@oobd.org",		// sender email
			"OOBD GPG Key Generator",	// senders full name
			$email,				// Receivers email
			"Key generation Error",		// subject
			$content,			// content
			"",				// file to send
			"config.txt",			// attachment file name
			"text/plain"			// mime type
			);
		}
		unlink($configfile);
		unlink($configfile.".pub");
		unlink($configfile.".sec");
	}

?>
