<?php
ini_set('include_path', '/usr/share/php' . PATH_SEPARATOR . ini_get('include_path'));

require_once 'Mail.php';
require_once 'Mail/mime.php';
require_once 'config.php';

function sendMail($absender_email,$absender_name,$Empfaenger,$Betreff,$content,$attachments ){

	global $config;

	$crlf = "\n";

	$from = "$absender_name <$absender_email>";

	$headers = array ('From' => $from,
	'To' => $Empfaenger,
	'Subject' => $Betreff);

	$mime = new Mail_mime(array('eol' => $crlf));
	$mime->setTXTBody($content);
	if (isset($attachments)){
		foreach ($attachments as $attachment){
			if (
				isset($attachment["filename"]) && $attachment["filename"]!="" &&
				isset($attachment["mailname"]) && $attachment["mailname"]!=""
			)
			{ 
				$mime->addAttachment($attachment["filename"],'application/octet-stream',$attachment["mailname"],true,'base64');
			}
		}
	}
	$body = $mime->get(array('html_charset' => 'utf-8', 'text_charset' => 'utf-8', 'eol' => $crlf));
	$hdrs = $mime->headers($headers);

	$smtp = Mail::factory('smtp',
	array ('host' => $config['smtphost'],
	'auth' => true,
	'username' => $config['smtpusername'],
	'password' => $config['smtppassword']));



	$mail = $smtp->send($Empfaenger, $hdrs, $body);
	/*
	if (PEAR::isError($mail)) {
	echo("<p>" . $mail->getMessage() . "</p>");
	} else {
	echo("<p>Message successfully sent!</p>");
	}
	*/
	//	mail($Empfaenger, $Betreff, $text, $Header) or die('Die Email
	//	konnte nicht versendet werden');

}
?>
