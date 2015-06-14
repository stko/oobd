#!/usr/bin/php
<?php
	$jobDone=false;
	if ($argc==5){ // = 4 parameters, param 0 is the script name itself
		$appPassPhraseFile=$argv[1];
		$grouplist=$argv[2];
		$pubfile=$argv[3];
		$secfile=$argv[4];
		if (strtolower(substr($appPassPhraseFile,strripos($appPassPhraseFile,".")-strlen($appPassPhraseFile)+1)) == "gpg"){
			$appPassPhrase=exec("gpg  -o - --decrypt " . $appPassPhraseFile, $output, $retval);
			if ($retval != 0 or $appPassPhrase == ""){
				exitProg("Error when decrypting application pass phrase!\n", 1);	
			}
		}else{
			$content=file($appPassPhraseFile);
			$appPassPhrase=$content[0];
		}
		if (!isset($appPassPhrase) || strlen($appPassPhrase)==0){
			exitProg("Error while reading passphrase from " . $appPassPhraseFile . "\n", 1);
		}

		$groups=file($grouplist,FILE_IGNORE_NEW_LINES+FILE_SKIP_EMPTY_LINES);
		if (count($groups)==0){
			exitProg("Error while reading groups from " . $grouplist . "\n", 1);
		}
		foreach ($groups as $group){
			file_put_contents($group.".groupsec","
Key-Type: DSA
#%dry-run
Key-Length: 2048
Subkey-Type: ELG-E
Subkey-Length: 2048
Expire-Date: 0
Passphrase: $appPassPhrase
%echo Generating  key for $group
Name-Real: $group
#Name-Comment: OOBD scipts for the car 
#Name-Email: joe@foo.bar
#%pubring $pubfile
#%secring $secfile
#%secring ./$group.sec
%commit
");

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
	".basename($argv[0])." appPassPhrase.gpg userlist pubkeyfile seckeyfile

appkey.gpg: contains the application pass phrase in the last line of text, encrypted with the key masters gpg key

grouplist.txt : contains the name of the groups to be generated, one per row

pubkeyfile, seckeyfile: File to store the new keys in

".basename($argv[0])." writes the gpg config file to stdout, where gpg takes this as input with 'gpg --batch --gen-key -'


",0);
}
?>
