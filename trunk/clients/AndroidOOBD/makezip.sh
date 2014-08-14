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
cp ../../lua-scripts/obdII-standard/OOBD.lbc \
../../lua-scripts/obdII-standard/dtc.oodb \
../../lua-scripts/examples/openXCtest.lbc \
../../lua-scripts/examples/UICreation.lbc \
OOBD
zip  $ZIPFILE OOBD/*


