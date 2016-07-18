#!/bin/bash

#define your required files here
FIX="\
../../lua-scripts/*.epa \
../../tools/lib_html \
"

echo Fix $FIX

#define your optional files here
#OPT=../../lua-scripts/*.epa 


if [ ! -d "assets" ]; then
	# Control will enter here if $DIRECTORY doesn't exist.
	echo "No asset folder found!"
	exit 1 
fi


if [ -z "$FIX" -a -z "$OPT" ]; then
	echo "no source files given? -exit"
	exit 0
fi

rm -r assets/* 

git rev-list HEAD --count > assets/am.rev
if [ -n "$FIX" ]; then
	mkdir assets/fix
	if [ $? -ne 0 ] ; then
		echo "could not create asset fix folder!"
		exit 1
	fi
	cp -r $FIX assets/fix
	cd assets
	find fix -type f >> am.rev
	cd ..
fi


if [ -n "$OPT" ]; then
	mkdir assets/opt
	if [ $? -ne 0 ] ; then
		echo "could not create asset opt folder!"
		exit 1
	fi
	cp -r "$OPT" assets/opt
	cd assets
	find opt -type f >> am.rev
	cd ..
fi





