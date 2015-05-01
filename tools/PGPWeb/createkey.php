<?php
require_once 'config.php';
include("mail.php");


function createSecretKeyInstaller($keyFileName, $vbsHeaderFile , $installerFileName){
	$keyFileContent =file_get_contents($keyFileName);
	$vbsHeaderFileContent =file_get_contents($vbsHeaderFile);
	file_put_contents( $installerFileName ,$vbsHeaderFileContent."' ~START~\r\n' ".chunk_split(base64_encode($keyFileContent),76,"\r\n' ")."\r\n' ~END~\r\n");

}


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

			createSecretKeyInstaller($configfile.".sec","UserKey_install.vbs", $configfile.".vbs");
			// send the public key	
			$content = "Hello $fullname ($email),
 
this is your PUBLIC key file. This key file is NOT secret and is needed by everybody who wants to send you encrypted data.


Please forward this mail as it is to your OOBD key master. He'll need this file to generate your personal group file which will then allow you use encrypted OOBD data.

best regards

the OOBD Key Generator";

			sendMail(
			$config['senderemail'],		// sender email
			$config['sendername'],		// senders full name
			$email,				// Receivers email
			"Your Public PGP Key",		// subject
			$content,			// content
			[
			[
			"filename" => $configfile.".pub",		// file to send
			"mailname" => "userkey.pub",			// attachment file name
			]
			]
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


To install the secret key on Windows to use it with OOBDesk

- save the attachment userkey_installer.vbs.txt to your PC
- rename the file extension, so that 'userkey_installer.txt' becomes 'userkey_installer.vbs'
- Start it with a double click. This will save your public key at the right place



Finally you'll also need the group file from your key master which grants you access to the different OOBD data groups.

best regards

the OOBD Key Generator";

			sendMail(
			$config['senderemail'],		// sender email
			$config['sendername'],		// senders full name
			$email,				// Receivers email
			"Your SECRET! pgp key",		// subject
			$content,			// content
			[
			[
			"filename" => $configfile.".sec",		// file to send
			"mailname" => "userkey.sec",			// attachment file name
			] ,
			[
			"filename" => $configfile.".vbs",		// file to send
			"mailname" => "userkey_installer.txt",			// attachment file name
			]
			]
			);
			// inform the admin
			$content = "Just for info: A key has been generated for Name: $fullname Email: $email";

			sendMail(
			$config['senderemail'],		// sender email
			$config['sendername'],		// senders full name
			$config['adminemail'],		// Receivers email
			"Key Generation Report",	// subject
			$content			// content
			);

		}else{

			$content = "Error while generating key for Name: $fullname Email: $email 
retval: $retval
configfile name: $configfile
command:$command
command output:
$result";

			sendMail(
			$config['senderemail'],		// sender email
			$config['sendername'],	// senders full name
			$config['adminemail'],		// Receivers email
			"Key generation Error",		// subject
			$content,			// content
			[
			[
			"filename" => $configfile,		// file to send
			"mailname" => "config.txt",			// attachment file name
			]
			]
			);
			// inform the user
			$content = "An Error occured while generating key for Name: $fullname Email: $email.

The system administrator has been informed.";

			sendMail(
			$config['senderemail'],		// sender email
			$config['sendername'],	// senders full name
			$email,				// Receivers email
			"Key generation Error",		// subject
			$content			// content
			);
		}
		unlink($configfile);
		unlink($configfile.".pub");
		unlink($configfile.".sec");
		unlink($configfile.".vbs");
	}

?>
