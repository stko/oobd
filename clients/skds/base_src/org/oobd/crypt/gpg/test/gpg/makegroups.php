#!/usr/bin/php
<?php
	$jobDone=false;
	if ($argc==3){ // = 2 parameters, param 0 is the script name itself
		$appPassPhraseFile=$argv[1];
		$grouplist=$argv[2];
		$appPassPhrase=exec("gpg  -o - --decrypt " . $appPassPhraseFile, $output, $retval);
		if ($retval != 0 or $appPassPhrase == ""){
			exitProg("Error when decrypting application pass phrase!\n", 1);	
		}
		$groups=file($grouplist,FILE_IGNORE_NEW_LINES+FILE_SKIP_EMPTY_LINES);
		if (count($groups)==0){
			exitProg("Error while reading groups from " . $grouplist . "\n", 1);
		}
		foreach ($groups as $group){
			print "#%dry-run
%echo Generating  key for $group
Key-Type: DSA
Key-Length: 2048
Subkey-Type: ELG-E
Subkey-Length: 2048
Name-Real: $group
#Name-Comment: OOBD scipts for the car 
#Name-Email: joe@foo.bar
Expire-Date: 0
Passphrase: $appPassPhrase
%pubring oobd_groups.pub
%secring oobd_groups.sec
%commit

";

		}
		$jobDone=true;
	}
	if (!$jobDone){
		usage();
	}


function exitProg($message, $exitCode){
	file_put_contents('php://stderr', $message);
	exit ($exitCode);
}

function usage(){
	global $argv;
	exitProg( basename($argv[0])." - the OOBD GPG group key create tool
Part of the OOBD tool set (www.oobd.org)

Usage:
	".basename($argv[0])." appPassPhrase.gpg userlist

appkey.gpg: contains the application pass phrase in the last line of text, encrypted with the key masters gpg key

grouplist.txt : contains the name of the groups to be generated, one per row

".basename($argv[0])." writes the gpg config file to stdout, where gpg takes this as input with 'gpg --batch --gen-key -'


",0);
}
?>
