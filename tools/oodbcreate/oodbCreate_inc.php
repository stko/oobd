<?php

class dbEntry
{
  public $next;
  public $prev;
  public $index;
  public $contents=array();

  function __construct($prev,$index,$content){
    $this->prev=$prev;
    $this->index=$index;
    if ($prev!=null){
	$prev->next=$this;
    }
    $contents[] = $content;
   }


  public function addContent($content){
    $contents[] = $content;   
  }
  
  public function findFirst($list){
    $res=$list;
    while ($res->prev!=null){
      $res=$res->prev;
    }
    return $res;
  }

  public function findMiddle($list){
    $res=$list;
    while ($res->next!=null){ // first go to end of list
      $res=$res->next;
    }
    $len=0;
    while ($res->prev!=null){ // run backwards to beginn of list
      $res=$res->prev;
      $len++;
    }
    $len= $len / 2; 
    while ($res->next!=null && $len > 0){ // first go to end of list
      $res=$res->next;
      $len--;
    }
    return $res;
  }


}

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
      }else $eol=true;
    }
  }
  if($eof){
    return FALSE;
  }else{
    return $line;
  }
}


function createDB($inFp, $outFp){
  $headerLine=null;
  $firstEntry=null;
  while (false !== ($line = freadLine($inFp))) {
    if($headerLine==null){
      $headerLine=$line;
    }else{
      list($index,$content)=split("\t",$line,2);
      $actLine= new dbEntry($actLine,$index,$content);
      if ($firstEntry==null) $firstEntry=$actLine;
    }
    fwrite($outFp,$line);
    fwrite($outFp,"\n");
  }
  while ($firstEntry!=null){
    print $firstEntry->index;
    $firstEntry=$firstEntry->next;
  }
}
?>
