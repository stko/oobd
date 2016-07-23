<?php

function startsWith($haystack, $needle)
{

	// search backwards starting from haystack length characters from the end

	return $needle === "" || strrpos($haystack, $needle, -strlen($haystack)) !== FALSE;
}

function endsWith($haystack, $needle)
{

	// search forward starting from end minus needle length characters

	return $needle === "" || (($temp = strlen($haystack) - strlen($needle)) >= 0 && strpos($haystack, $needle, $temp) !== FALSE);
}

function splitBrackets($input, $brLeft, $brRight)
{
	$res = array();
	$pos = 0;
	$bracketCount = 0;
	$posLeft = - 1;
	$posRight = - 1;
	$len = strlen($input);
	$cancel = false;
	while ($pos < $len and !$cancel) {
		$char = $input[$pos];
		if ($char == $brLeft) {
			if ($posLeft == - 1) { // opening bracket found
				$posLeft = $pos;
			}

			$bracketCount+= 1;
		}

		if ($char == $brRight) {
			if ($posLeft == - 1) { // no opening bracket found yet?
				$cancel = true; // error, stop it
			}
			else {
				$bracketCount-= 1;
				if ($bracketCount == 0) { // ok, closing bracket found
					$cancel = true; // we can stop
					$posRight = $pos;
				}
			}
		}

		$pos+= 1;
	}

	if ($posLeft > - 1 and $posRight > - 1) {
		$res[0] = substr($input, 0, $posLeft);
		$res[1] = substr($input, $posLeft + 1, $posRight - $posLeft - 2);
		$res[2] = substr($input, $posRight + 1);
	}
	else {
		$res[0] = $input;
	}

	return $res;
}

$line = "";
$bremse = false;

while ($f = fgets(STDIN)) {
	$f = trim($f);
	if ($f != "") {
		$line.= $f . "\n";
	}

	if (endsWith($f, ";")) { // end of command
		if (!startsWith($line, "geometry.attributes =")) {
			echo $line;
		}
		else {
			$res = splitBrackets($line, "{", "}");
			if (count($res) > 1) {
				$fields = preg_split("/\},/U", $res[1]);
				for ($i = 0; $i < count($fields); $i++) {
					$attrContent = explode(":", $fields[$i] . "}", 2); // adding the missing }, which goes lost somehow?!?
					$attrName = trim($attrContent[0]);
					$dummy = splitBrackets($attrContent[1], "{", "}"); // get the content inside the {}

					$dummy = explode(",", $dummy[1], 2); // split the content into itemsize and array key->value strings
					$itemSizePair = explode(":", $dummy[0], 2); //
					$arrayPair = explode(":", $dummy[1], 2); //
					$itemSizeName = trim($itemSizePair[0]);
					$itemSizeValue = trim($itemSizePair[1]);
					$arrayName = trim($arrayPair[0]);
					$arrayValue = trim($arrayPair[1]);
					echo "geometry.addAttribute( '$attrName', new THREE.BufferAttribute( \n$arrayValue), $itemSizeValue ) );\n";
				}
			}
		}

		$line = "";
	}
}

if ($line != "") { // print last line, in case not done already before
	echo $line;
}

?>