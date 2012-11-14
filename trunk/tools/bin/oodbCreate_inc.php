<?php
/**
oobdCreate_inc.php

part of the OOBD tool set (www.oobd.org)

this file contains the functionality to convert a presorted csv file into a binary format, which allows a key->value(s) list search in this file in one direction from the beginning to the end

The file format of the input file must be as follow:

HeaderLine \n
Line 1 \n
..
Line n \n

HeaderLine = (colum_name 0) \t (colum_name 1) \t (.. colum_name n)
Line = Key \t Values 
Values = (Value_of_Colum 0) \t (Value_of_Colum 1) \t (..Value_of_Colum n)


The input file must be sorted ascending by its keys

If there are more as one Value per key, the Values must sorted in the sequence as they should be used later


The generated output format will be as follows:

HeaderLine 0x0
Entry 1
..
Entry n

Entry = Key 0x0 (Offset, if key > Searchstring) (Offset, if key < Searchstring) Values 1  0x0 [..Values n  0x0] 0x0

Offset = binary unsigned 32-Bit Big Endian, calculated as skip() value from the fileposition after the second 4-Byte value up to the start of the next key string to be evaluated. To distingluish 
between an offset of 0 to the next key string and a 0 as the indicator for the end of the search tree, the skip() offset given in the file is always 1 higher as in really, so 1 needs to be
substracted to have the correct jump width (e.g. Offset in file: 9 means real jump width 9 -1 = 8   = skip(8)



How to read this file:

1 - Read Headerline (from the file beginning until the first 0x0). Store this data for later naming of the found columns.
2 - read key value (string until the next 0x0) and the both next 4 Byte long skip() offsets (= relativefile positions) for the greater and smaller key value. If they are 0 (zero), there's no more smaller or greater key available
3 - compare key with search string:
    - if equal, read attached values in an array. This array then contains the search result(s). Return this and the header line as positive search result.
    - if smaller:
	  if smaller file position is 0 (zero), then return from search with empty result array.
	  if smaller file position is not 0, jump per skip() to the file postion of the next index string and continue again with step 2
    - if bigger:
	  if bigger file position is 0 (zero), then return from search with empty result array.
	  if bigger file position is not 0, jump per skip() to the file postion of the next index string and continue again with step 2



*/
class dbEntry
{
  public $next;
  public $prev;
  public $lt;
  public $gt;
  public $index;
  public $start;
  public $contents;


// creates one index element with an initial first value and links this object to the previous one, if exist
  function __construct($prev,$index,$content){
    $this->prev=$prev;
    $this->index=$index;
    if ($prev!=null){
	$prev->next=$this;
    }
    $this->contents=array();
    $this->contents[] = $content;
   }

// adds another line of value to this index
  public function addContent($content){
    $this->contents[] = $content;   
  }
  
 //finds the first object in the linked object list
  public function findFirst(){
    $res=$this;
    while ($res->prev!=null){
      $res=$res->prev;
    }
    return $res;
  }

// finds the middle element in the list of linked objects
  public function findMiddle(){
    $res=$this;
    while ($res->next!=null){ // first go to end of list
      $res=$res->next;
    }
    $len=0;
    while ($res->prev!=null){ // run backwards to beginn of list
      $res=$res->prev;
      $len++;
    }
    $len= $len / 2; 
    while ($res->next!=null && $len > 0){ // go to the middle of the list
      $res=$res->next;
      $len--;
    }
    return $res;
  }

// takes a linear sorted list and split it in the middle, by making the left and right side to childs of the middle element.
//by doing this recursively, we'll finally have a balanced AVL-tree structure 
  public function split($level){
     if ($this->prev!=null){
      $p=$this->prev;
      $p->next=null;
      $p=$p->findMiddle();
      $p->split($level+1);
      $this->lt=$p;
    }
    if ($this->next!=null){
      $p=$this->next;
      $p->prev=null;
      $p=$p->findMiddle();
      $p->split($level+1);
      $this->gt=$p;
    }
  }

//to know the file positions of the related child elements during saving the result, this routine goes recursively through the tree and calculates the final
// filenposition of each element as a kind of file-saving-dryrun

  public function calculateSizes($start){
    $this->start=$start;
    $start+=(strlen($this->index)+1+4+4); // the index string length + 1 Zero (0) - Byte + 4 byte seek address for smaller leaf + 4  byte seek address for bigger leaf
    foreach ($this->contents as $content){
      $start+=strlen($content)+1; // len of each content entry + 1 Zero (0) - Byte
    }
    $start++; // and the final Zero (0) - Byte
    if ($this->gt!=null){
      $start=$this->gt->calculateSizes($start); // recursivelly calculate 
    }
    if ($this->lt!=null){
      $start=$this->lt->calculateSizes($start);
    }
    return $start;
  }

// just a help routine to save a number as 32-bit byte sequence 
  static function printBinaryValue($outFp,$value){
    fwrite($outFp,chr($value / (256 * 256 * 256)));
    $value %= (256 * 256 * 256);
    fwrite($outFp,chr($value / (256 * 256 )));
    $value %= (256 * 256 );
    fwrite($outFp,chr($value / (256 )));
    $value %= (256 );
    fwrite($outFp,chr($value ));
  }


//going through the tree recursively to save each element to the output file handle
  public function printElement($outFp){
    fwrite($outFp,$this->index);
    fwrite($outFp,chr(0));
    if ($this->lt!=null){
// this is the output with the absolute file position
//	self::printBinaryValue($outFp, $this->lt->start);
// this is the output with the relative skip() jump length, counted from behind the second (gt) 4 byte binary value of the actual element
	self::printBinaryValue($outFp, $this->lt->start-($this->start+strlen($this->index)+1+4+4-1));
    }else{
	self::printBinaryValue($outFp, 0);
    }
    if ($this->gt!=null){
// this is the output with the absolute file position
//	self::printBinaryValue($outFp, $this->gt->start);
// this is the output with the relative skip() jump length, counted from behind the second (gt) 4 byte binary value of the actual element
	self::printBinaryValue($outFp, $this->gt->start-($this->start+strlen($this->index)+1+4+4-1));
    }else{
	self::printBinaryValue($outFp, 0);
    }
    foreach ($this->contents as $content){
      fwrite($outFp,$content);
      fwrite($outFp,chr(0));
    }
    fwrite($outFp,chr(0));
    if ($this->gt!=null){
      $start=$this->gt->printElement($outFp); // recursive print 
    }
    if ($this->lt!=null){
      $start=$this->lt->printElement($outFp);// recursive print 
    }
  }

}


// help function to read a complete text line from the input file stream
// supresses comment line which starts  with an #
function freadLine($inFp){
  $line="";
  $eof=FALSE;
  $eol=FALSE;
  while (!$eof && !$eol) {
    $eof=false === ($char = fgetc($inFp));
    if (!$eof){
      if ($char!="\n"){
	if ($char!="\r"){
	  $line.=$char;
	}
      }else{
	if (preg_match ( "/^\s*#/", $line)){ // if this line starts with an #, then the line is surpressed
	  $line="";
	} else {
	  $eol=true;
	}
      }
    }
  }
  if($eof){
    return FALSE;
  }else{
    return $line;
  }

}

// main function: reading the input file from the input file stream and write the result to the output file stream
function createDB($inFp, $outFp){
  $headerLine=null;
  $firstEntry=null;
  $actLine=null;
  $lastIndex=null;
  while (false !== ($line = freadLine($inFp))) { // reading the input
    if($headerLine==null){
      $headerLine=$line; // store the headerLine which contains the colum names at the beginning
    }else{
      list($index,$content)=split("\t",$line,2); //split each line into key and value
       if ($lastIndex == $index){ // line belongs to previous key
	$actLine->addContent($content); //add content to previous key
      }else{
	$actLine= new dbEntry($actLine,$index,$content); //create a new key object
	$lastIndex=$index;
      }
    }
  }
//  $actLine=$actLine->findFirst();
  $actLine=$actLine->findMiddle();
  $actLine->split(0);
  fwrite($outFp,$headerLine); // first saving the headerline
  fwrite($outFp,chr(0));  //headerLine limiter
  $actLine->calculateSizes(strlen($headerLine)+1);//start after the headerline
  $actLine->printElement($outFp);
}
?>
