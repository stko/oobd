#!/bin/sh
REV=$(git describe --dirty --always)
if [ -z $REV ]; then
REV=norev;
fi
echo \'\(const char \*\)\(\"$REV\"\)\'
