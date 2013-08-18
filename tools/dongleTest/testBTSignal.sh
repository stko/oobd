#!/bin/sh
for i in $(hcitool scan | awk 'BEGIN { FS = "\t" } ; /:/ {printf "%s ",$2} ') ; do 
	echo BT-Adress: $i
	hcitool cc $i && hcitool rssi $i && hcitool lq $i  && hcitool dc $i
done