#!/bin/sh
REV=$(svn info $1 | grep Rev: | awk -F: '{print $2}')

if [ $? -ne 0 ] || [ -z $REV ]; then
	REV=$(git rev-list HEAD --count)
	if [ $? -ne 0 ] || [ -z $REV ]; then
		REV=0;
	fi
fi
echo $2=\"$REV\"