#!/bin/sh
JSPATH=$(dirname $1)
JSNAME=$(basename $1 .wrap.js)
if [ "$#" -ne 2 ] || ! [ -f "$JSPATH/$JSNAME.wrap.js" ] || ! [ -f "$2" ]; then
  echo "Usage: $0 <initsequence>.js <luac-executable.js>" >&2
  echo "merges <initsequence>.wrap.js and <luac-executable.js> into <initsequence>.js" >&2
  exit 1
fi

echo path $JSPATH
echo base $JSNAME

cp $JSPATH/$JSNAME.wrap.js $JSPATH/$JSNAME.js 
cat $2 >> $JSPATH/$JSNAME.js 
echo  >> $JSPATH/$JSNAME.js 
echo "}" >> $JSPATH/$JSNAME.js 
