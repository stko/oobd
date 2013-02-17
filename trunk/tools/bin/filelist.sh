if ls -U *.$1 >/dev/null 2>&1 ; then 
for i in *.$1
	do echo -n $i " "
done
fi