#!/usr/bin/php
<?php
	$jobDone=false;
	if ($argc==9){ // = 8 parameters, param 0 is the script name itself
		$useraccesslist=$argv[1];
		$username=strtolower($argv[2]);
		$groupfilename =$argv[3];
		$nsistemplate=$argv[4];
		$licencefile=$argv[5];
		$scriptinputdir=$argv[6];
		print("create Installer for $username\n");
		$outputdir=$argv[7]. DIRECTORY_SEPARATOR .$username;
		$getFullPathCmd=$argv[8];
		$allowedGroups=array();
		$userGroupListArray=file($useraccesslist);
		foreach ($userGroupListArray as $line){
			list($user, $group) = explode(":", $line,2);
			$user=strtolower($user);
			if ($user == $username){
				#print ($user. $group."\n");
				preg_match("/(.*)_(.*)/",$group,$matches);
				$carline=$matches[1];
				$accessLevel=$matches[2];
				#print("carline: ".$carline." accessLevel: ".$accessLevel."\n");
				#list($carline, $my , $accessLevel) = explode("_", $group,3);
				$accessLevel=trim($accessLevel);
				if (($level=strpos("rtsw",$accessLevel))!==FALSE){
					#$carline=$carline."_".$my;
					if (!isset($allowedGroups[$carline])){
						$allowedGroups[$carline]=array();
					}
					$allowedGroups[$carline][$accessLevel]=array_flip(getFileList($scriptinputdir. DIRECTORY_SEPARATOR .$carline."_".$accessLevel));
				}
			}
		}
		$ranking=["r","t","s","w"];
		foreach ($allowedGroups as $carline => $group){
			for ($higher=3; $higher>0; $higher--){
				for ($lower=$higher-1; $lower>=0; $lower--){
					if (isset($group[$ranking[$higher]]) && isset($group[$ranking[$lower]])){
						foreach($group[$ranking[$lower]] as $key => $value){
							#print ("Key: ".$key." Value ".$value."\n");
							if (isset($group[$ranking[$higher]][$key])){
								#print ("Remove ".$key.  " " . $ranking[$higher]. " and ". $ranking[$lower]."\n");
								$allowedGroups[$carline][$ranking[$lower]][$key]=-1;
							}
						}
					}
				}
			}
		}
		$installFiles="";
		foreach ($allowedGroups as $carline => $group){
			foreach ($group as $level => $dirlist){
				foreach ($dirlist as $fileName => $value){
					#print ("carline ".$carline.  " level " . $level. " file ". $fileName." value ".$value."\n");
					if ($value>-1){
						$installFiles.="File \"".createOSdependendPath($scriptinputdir . DIRECTORY_SEPARATOR.$carline."_". $level. "/". $fileName)."\"\n";
					}
				}
			}
		}
		#print_r($allowedGroups);
		#print ("files:\n ".$installFiles);
		$current = file_get_contents($nsistemplate);
		// change placeholders with real values
		$current = str_replace("\$\$user",$username,$current);
		$current = str_replace("\$\$groupfile",createOSdependendPath($groupfilename),$current);
		$current = str_replace("\$\$licencefile",createOSdependendPath($licencefile),$current);
		$current = str_replace("\$\$files",$installFiles,$current);
		$current = str_replace("\$\$installerpath",createOSdependendPath($outputdir),$current);
		// Write the contents back to the file
		file_put_contents($outputdir.".nsi", $current);
		$jobDone=true;
	}
	if (!$jobDone){
		usage();
	}

function getFileList($dir){
	$result = array(); 
	set_error_handler(function() { /* ignore errors */ });
	try {
		if(($cdir = scandir($dir))!==FALSE){ 
			foreach ($cdir as $key => $value) 
			{ 
				if (!in_array($value,array(".",".."))) 
				{ 
					if (!is_dir($dir . DIRECTORY_SEPARATOR . $value)) 
					{ 
						$result[] = $value; 
					} 
				}
			} 
		} 
	} catch (Exception $e) {
		echo 'Exception abgefangen: ',  $e->getMessage(), "\n";
	}
	restore_error_handler();

return $result; 
} 


function exitProg($message, $exitCode){
	file_put_contents('php://stderr', $message);
	exit ($exitCode);
}

function createOSdependendPath($path){
	global $getFullPathCmd;
	$newpath= exec($getFullPathCmd.' '.$path,$out, $err);
	if ($err!=0){
		return $path;
	}else{
		return $newpath;
	}
}


function usage(){
	global $argv;
	global $argc;
	exitProg( basename($argv[0])." - the OOBD Script Installer Generator tool
Part of the OOBD tool set (www.oobd.org)

wrong number of parameters: found $argc parameters

Usage:
	".basename($argv[0])." useraccesslist username groupfile nsistemplate licencefile scriptinputdir outputdir getpathcmd

useraccesslist: the file containing the users and their groups

username: the particular user for which the file shall be made for

groupkey: the groupkey file of that user

nsistemplate: the template for the nsisfile, where
	\$\$user represents the username
	\$\$files the script files
	\$\$groupfile the groupkey- file
	\$\$installerpath the location of the generated installer.exe
	\$\$licencefile the location of the licence text
	
licencefile: The file containing the licence text of the pack
	
scriptinputdir: directore, where the access group subdirs are in

outputdir: directory, where the finished installer should be placed

getpathcmd: the shell comand, which provides the full path of a file to be included in the installer
",0);
}
?>

