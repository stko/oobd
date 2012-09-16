#!/bin/sh
if [ $# -ne 1 ]
then
  echo "Usage: `basename $0` Revison"
  exit 1
fi
APKFILE=OOBDAndroid_R$1.apk
ZIPFILE=OOBDAndroid_R$1.zip
rm $ZIPFILE
cp bin/OOBDAndroidSVN.apk OOBD/$APKFILE
cp ../OOBD-ME/testscripts/OOBD.lbc ../OOBD-ME/testscripts/dtc.oodb OOBD
zip  $ZIPFILE OOBD/*


