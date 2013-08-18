!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"



SetCompress force
SetCompressor /SOLID lzma
Name "OOBDesk"
OutFile "OOBDesk_Setup.exe"
RequestExecutionLevel user
XPStyle on


InstallDir "$APPDATA\OOBD\OOBDesk"
InstallDirRegKey HKCU "SOFTWARE\OOBD\OOBDesk" "InstDir"
#!include "ZipDLL.nsh"
Page license
Page directory


Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

LicenseData gpl-2.0.txt
LicenseForceSelection checkbox


Section "OOBDesk"
SetOutPath $INSTDIR
File /r  "dist/lib"
#File /r  "build/classes/skdsswing/bus"
File /r  "../skds/NBprojects/Base/build/classes/org/oobd/base/bus"
File /r  "../skds/NBprojects/Base/build/classes/org/oobd/base/port"
File /r  "../skds/NBprojects/Base/build/classes/org/oobd/base/db"
File /r  "../skds/NBprojects/Base/build/classes/org/oobd/base/scriptengine"
File /r  "../skds/NBprojects/Base/build/classes/org/oobd/base/protocol"
File "dist/OOBDesk.jar"
#File  "app_dist.props"
#File /oname=oobdcore.props  "oobdcore_dist.props"
#File  "enginelua_dist.props"
File "oobd.url"
File "jlogviewer.jar"
File "logging.props"
File /oname=OOBD.lbc "../../lua-scripts/obdII-standard/OOBD.lbc"
File "../../lua-scripts/obdII-standard/dtc.csv"
File "../../lua-scripts/obdII-standard/dtc.oodb"
File /oname=stdlib.lbc "../OOBD-ME/res/stdlib.lbc"
CreateDirectory "$INSTDIR\logs"

#FileOpen $0 $INSTDIR\file.dat w
#FileClose $0

WriteUninstaller "$INSTDIR\uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD\OOBDesk"
#CreateShortCut "$SMPROGRAMS\OOBD\OOBDesk\OOBDesk.lnk" "javaw" "-jar -Djava.library.path=bus/lib -Djava.util.logging.config.file=logging.props OOBDesk.jar"
#CreateShortCut "$SMPROGRAMS\OOBD\OOBDesk\OOBDesk.lnk" "java" "-jar -Djava.library.path=bus/lib -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\OOBDesk\OOBDesk.lnk" "java" "-jar -Djava.library.path=port -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\OOBDesk\View Log Files.lnk" "javaw" "-jar jlogviewer.jar logs\oobd0.xml"
CreateShortCut "$SMPROGRAMS\OOBD\OOBDesk\OOBDesk Homepage.lnk" "$INSTDIR\oobd.url"
CreateShortCut "$SMPROGRAMS\OOBD\OOBDesk\Uninstall OOBDesk.lnk" "$INSTDIR\uninstaller.exe"
WriteRegStr HKCU "SOFTWARE\OOBD\OOBDesk" "InstDir" $INSTDIR


SectionEnd

Section "un.Uninstall"
RMDir /r $INSTDIR\lib
RMDir /r $INSTDIR\logs
RMDir /r $INSTDIR\bus
RMDir /r $INSTDIR\port
RMDir /r $INSTDIR\db
RMDir /r $INSTDIR\scriptengine
RMDir /r $INSTDIR\protocol
RMDir /r $INSTDIR\SKDSesk
RMDir /r $INSTDIR\OOBDesk
Delete "$INSTDIR\jlogviewer.jar"
Delete "$INSTDIR\OOBDesk.jar"
Delete "$INSTDIR\oobd.url"
Delete "$INSTDIR\*.props"
Delete "$INSTDIR\OOBD.lbc"
Delete "$INSTDIR\dtc.*"
Delete "$INSTDIR\stdlib.lbc"
Delete "$INSTDIR\uninstaller.exe"
RMDir $INSTDIR
Delete "$SMPROGRAMS\OOBD\OOBDesk\OOBDesk.lnk"
Delete "$SMPROGRAMS\OOBD\OOBDesk\OOBDesk Homepage.lnk"
Delete "$SMPROGRAMS\OOBD\OOBDesk\View Log Files.lnk"
Delete "$SMPROGRAMS\OOBD\OOBDesk\Uninstall OOBDesk.lnk"
RMDir "$SMPROGRAMS\OOBD\OOBDesk"
RMDir "$SMPROGRAMS\OOBD"
DeleteRegKey HKCU "SOFTWARE\OOBD\OOBDesk" 
DeleteRegKey /ifempty HKCU "SOFTWARE\OOBD" 
SectionEnd

