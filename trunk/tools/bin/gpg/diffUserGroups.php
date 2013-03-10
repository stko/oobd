#!/usr/bin/php
<?php
	$jobDone=false;
	if ($argc==3){ // = 2 parameters, param 0 is the script name itself
		$users=array();
		$oldlist=loadUserFile($argv[1]);
		$newlist=loadUserFile($argv[2]);
		foreach ($newlist as $entry => $dummy){
			if (!isset($oldlist[$entry])){ // a new entry found in newlist
				list($user, $group) = explode(":", $entry,2);
				$users[$user]=1;
			}
		}
		foreach ($oldlist as $entry => $dummy){
			if (!isset($newlist[$entry])){ // a new entry found in newlist
				list($user, $group) = explode(":", $entry,2);
				$users[$user]=1;
			}
		}
		foreach ($users as $updateUser => $dummy){
			foreach ($newlist as $entry => $dummy){
				list($user, $group) = explode(":", $entry,2);
				if ($updateUser==$user){
					echo "$entry";
				}
			}
		}

		$jobDone=true;
	}
	if (!$jobDone){
		usage();
	}


function loadUserFile($filename){
	$res=array();
	$handle = @fopen($filename, "r");
	if ($handle) {
		while (($buffer = fgets($handle, 4096)) !== false) {
			list($user, $group) = explode(":", $buffer,2);
			$res[strtolower($buffer)]=1;
		}
		if (!feof($handle)) {
		echo "Error: unexpected fgets() fail\n";
		}
		fclose($handle);
	}
	return $res;
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
