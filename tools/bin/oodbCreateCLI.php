<?php
require("oodbCreate_inc.php");
$inFp = fopen( $argv[1], 'r');
if (!$inFp) {
    echo 'Konnte Datei  nicht öffnen';
}else createDB($inFp, STDOUT);
?>
