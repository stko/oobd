SetCompress force
SetCompressor /SOLID lzma
Name "OOBD-Swing"
OutFile "OOBD-Swing_Windows_Setup.exe"
XPStyle on
InstallDir "$APPDATA\OOBD\OOBD-Swing"
InstallDirRegKey HKCU "SOFTWARE\OOBD\OOBD-Swing" "InstDir"
#!include "ZipDLL.nsh"
Page license
Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

LicenseData gpl-2.0.txt
LicenseForceSelection checkbox

Section "OOBD-Swing"
SetOutPath $INSTDIR
File /r  "dist/lib"
File /r  "build/classes/skdsswing/bus"
File /r  "../Base/build/classes/org/oobd/base/scriptengine"
File /r  "../Base/build/classes/org/oobd/base/protocol"
File /oname=OOBD-Swing.jar  "dist/SKDS-Swing.jar"
File /oname=buscom.props  "buscom_dist.props"
File /oname=oobdcore.props  "oobdcore_dist.props"
File "oobd.url"
File /oname=OOBD.lbc "../../../OOBD-ME/testscripts/HHOpen.lbc"

#FileOpen $0 $INSTDIR\file.dat w
#FileClose $0

WriteUninstaller "$INSTDIR\uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD\OOBD-Swing"
#CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk" "javaw" "-jar -Djava.library.path=bus/lib OOBD-Swing.jar"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk" "java" "-jar -Djava.library.path=bus/lib OOBD-Swing.jar"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing Homepage.lnk" "$INSTDIR\oobd.url"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\Uninstall OOBD-Swing.lnk" "$INSTDIR\uninstaller.exe"
WriteRegStr HKCU "SOFTWARE\OOBD\OOBD-Swing" "InstDir" $INSTDIR


SectionEnd

Section "un.Uninstall"
RMDir /r $INSTDIR\lib
RMDir /r $INSTDIR\bus
RMDir /r $INSTDIR\scriptengine
RMDir /r $INSTDIR\protocol
RMDir /r $INSTDIR\SKDS-Swing
RMDir /r $INSTDIR\OOBD-Swing
Delete "$INSTDIR\OOBD-Swing.jar"
Delete "$INSTDIR\oobd.url"
Delete "$INSTDIR\*.props"
Delete "$INSTDIR\OOBD.lbc"
Delete "$INSTDIR\uninstaller.exe"
RMDir $INSTDIR
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk"
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing Homepage.lnk"
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\Uninstall OOBD-Swing.lnk"
RMDir "$SMPROGRAMS\OOBD\OOBD-Swing"
RMDir "$SMPROGRAMS\OOBD"
DeleteRegKey HKCU "SOFTWARE\OOBD\OOBD-Swing" 
DeleteRegKey /ifempty HKCU "SOFTWARE\OOBD" 
SectionEnd

