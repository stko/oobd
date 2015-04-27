<?php
function createSecretKeyInstaller($keyFileName, $vbsHeaderFile , $installerFileName){
	$keyFileContent =file_get_contents($keyFileName);
	$vbsHeaderFileContent =file_get_contents($vbsHeaderFile);
		
		file_put_contents( $installerFileName ,$vbsHeaderFileContent."' ~START~\r\n' ".chunk_split(base64_encode($keyFileContent),76,"\r\n' ")."\r\n' ~END~\r\n");

}

createSecretKeyInstaller("mail.php","UserKey_install.vbs", "Installer.vbs");


?>