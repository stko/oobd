<?php
require("oodbCreate_inc.php");
$inFp = fopen('CCC.csv', 'r');
if (!$inFp) {
    echo 'Konnte Datei  nicht öffnen';
}
createDB($inFp, STDOUT);
?>
