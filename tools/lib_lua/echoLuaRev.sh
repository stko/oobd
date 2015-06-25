#!/bin/sh
REV=$(svn info | grep Revision  |  awk -F: '{print $2}')
if [ $? -ne 0 ] || [ -z $REV ]; then
	REV=$(git rev-list HEAD --count)
	if [ $? -ne 0 ] || [ -z $REV ]; then
		REV=0;
	fi
else
	REV=0;
fi
echo $1=\"$REV\"
