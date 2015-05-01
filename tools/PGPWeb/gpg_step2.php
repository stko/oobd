<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head><title>OOBD GPG Online Key Generator - Define your Phassphrase</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script language="JavaScript" type="text/javascript">

	//function that performs all functions, defined in the onsubmit event handler

	function check(form){
		 if (form.pp1.value == "" || form.pp1.value == null ||  form.pp1.value.charAt(0) == ' ' ||  form.pp1.value.length<8 || form.pp.value!=form.pp1.value)
		{
			alert('The both pass phrases are not equal or too short!\nPlease amend and retry');
			return false;
		} 

		return true;
	}

</script>
</head>
<body>
<h1>OOBD GPG Online Key Generator - Define your Phassphrase</h1>
<?php
	require_once 'config.php';

	$email=$_REQUEST['e'];
	$fullname=$_REQUEST['n'];
	$sid=$_REQUEST['sid'];

	print "Your Name: $fullname<br>\n";
	print "Your email address: $email<br>\n";
	$id=md5($config['md5salt'].$email.$fullname);
	print "<hr>Session- ID: $sid<br>\n";
	print "Check-ID:    $id<br><hr>\n";

	if ($sid == $id){
?>

Finally you have to give a pass phrase (At least 8 characters). This pass phrase protects your data from been stolen, so make it as complicated as possible.<p>
<form name="ppform" method="post" action="gpg_step3.php" onSubmit="return check(this);">
  Your Passphrase (required!)<br />
<input name="pp1" type="text" />
  <br>
  re-type your Passphrase (required!)<br>
  <input name="pp" type="text" id="pp" />
  <br />
<input name="submit" type="submit" />
<input name="e" type="hidden" value="<?php echo $email;?>"/>
<input name="n" type="hidden" value="<?php echo $fullname;?>"/>
<input name="sid" type="hidden" value="<?php echo $sid;?>"/>
</form>



<?php
	}else{

		
	 	print "invalid data.. - something went wrong, please try again\n";
	}


?>
