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
cp ../../lua-scripts/obd-standard/OOBD.lbc ../../lua-scripts/obd-standard/dtc.oodb OOBD
zip  $ZIPFILE OOBD/*


