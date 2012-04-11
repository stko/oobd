#!/bin/sh
REV=$(svn info | grep Revision  |  awk -F: '{print $2}')
if [ -z $REV ]; then
REV=norev;
fi
echo \'\(const char \*\)\(\"$REV\"\)\'
