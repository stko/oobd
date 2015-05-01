<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head><title>OOBD GPG Online Key Generator - Email Verification</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
</head>
<body>
<h1>OOBD GPG Online Key Generator - Email Verification</h1>
<?php
	require_once 'config.php';
	include("mail.php");
	// Name des Absenders
	$absender_name = $config['sendername'];
	// E-Mail Adresse vom Absender
	$absender_email = $config['senderemail'];
	$Betreff = "OOBD GPG Online Key Generator - Email Verification";
	$email=$_REQUEST['email'];
	$fullname=$_REQUEST['fullname'];

	print "Hello $fullname, ";
	//print "Your email address: $email<br>\n";

	if (isValidEmail($email) && isValidName($fullname)){
		$id=md5($config['md5salt'].$email.$fullname);
		$link=$config['sideurl']."/gpg_step2.php?n=".urlencode($fullname)."&e=".urlencode($email)."&sid=$id";
		$content="OOBD GPG Online Key Generator - Email Verification

If you don't requested a online key generation, that please delete this mail, no further action is needed.


In case you just in the process to generate your gpg key, than please use the link below to verify your email address:

$link

";

		sendMail($absender_email,$absender_name,$email,$Betreff,$content);

		
		print "the email to verify your email address is sent to $email. Please use the link inside that mail to proceed\n";
	}else{

		if (!isValidEmail($email)){
			print "Your email address is not a valid email address!<p>\n";
		}
		if (!isValidName($fullname) ){
			print "Your name is not a valid name with first and last name!<p>\n";
		}
		print "Go back to the previous page and try again..\n";
	}

	function isValidEmail($email){
	return  preg_match("/^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,3})$/i", $email)==1;
	}

	function isValidName($name){
	return  preg_match("/\w+\s+\w+/", $name)==1;
	}
?>
