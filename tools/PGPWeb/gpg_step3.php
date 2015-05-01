<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head><title>OOBD GPG Online Key Generator - Key Generation in Progress...</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
<h1>OOBD GPG Online Key Generator - Key Generation in Progress...</h1>
<?php
	require_once 'config.php';
	
	$email=$_REQUEST['e'];
	$fullname=$_REQUEST['n'];
	$sid=$_REQUEST['sid'];
	$pp=$_REQUEST['pp'];

	print "Your Name: $fullname<br>\n";
	print "Your email address: $email<br>\n";
	//print "Your pass phase: $pp<br>\n";
	$id=md5($config['md5salt'].$email.$fullname);
	//print "<hr>Session- ID: $sid<br>\n";
	//print "Check-ID:    $id<br><hr>\n";

	if ($sid == $id){
		print "All went well... The key is now been generated and will be sent to you.<p>This can take easily 15 minutes, so please be patient..<p>Please read and follow the instructions in the mails carefully<p>";
		$command= "php createkey.php ".base64_encode ($fullname)." ".base64_encode ($email)." ".base64_encode ($pp)." > /dev/null &";
		exec($command,$output,$retval);
		//print "Retval: $retval\n";
		//var_dump($output);
	}else{

		
	 	print "invalid data.. - something went wrong, please try again\n";
	}


?>
