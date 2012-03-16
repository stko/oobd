<?php
$fp = fopen('somefile.txt', 'r');
if (!$fp) {
    echo 'Konnte Datei somefile.txt nicht öffnen';
}
while (false !== ($char = fgetc($fp))) {
    echo "$char\n";
}
?>
