<?php
ini_set('include_path', '/usr/share/php' . PATH_SEPARATOR . ini_get('include_path'));

 require_once 'Mail.php';
 require_once 'Mail/mime.php';

	function sendMail($absender_email,$absender_name,$Empfaenger,$Betreff,$content,$dateiname,$dateiname_mail,$mimetype){



 //used for SMTP Authentification
 $host = "localhost";
 $username = "oobd@luxen.de";
 $password = "pgpStM32andrO";

 $crlf = "\n";

 $from = "$absender_name <$absender_email>";

 $headers = array ('From' => $from,
 'To' => $Empfaenger,
 'Subject' => $Betreff);

 $mime = new Mail_mime(array('eol' => $crlf));
 $mime->setTXTBody($content);

 if (isset($dateiname) && $dateiname!=""){ 
	$mime->addAttachment($dateiname,'application/octet-stream',$dateiname_mail,true,'base64');
 }
 $body = $mime->get(array('html_charset' => 'utf-8', 'text_charset' => 'utf-8', 'eol' => $crlf));
 $hdrs = $mime->headers($headers);

 $smtp = Mail::factory('smtp',
 array ('host' => $host,
 'auth' => true,
 'username' => $username,
 'password' => $password));

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
