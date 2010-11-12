SetCompress force
SetCompressor /SOLID lzma
Name "OOBD-ME"
OutFile "OOBD-ME_Windows_Setup.exe"
XPStyle on
InstallDir "$APPDATA\OOBD-ME"
#!include "ZipDLL.nsh"
Page license
Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

LicenseData gpl-2.0.txt
LicenseForceSelection checkbox

Section "OOBD-ME"
SetOutPath $INSTDIR
File /r  "client/java/application/Kobs/dist/*.*"
File client/java/application/Kobs/kobs.lang
File "klobs.url"
WriteUninstaller "$INSTDIR\uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD-ME"
CreateShortCut "$SMPROGRAMS\OOBD-ME\OOBD-ME.lnk" "javaw" "-jar klobs.jar"
CreateShortCut "$SMPROGRAMS\OOBD-ME\OOBD-ME Homepage.lnk" "$INSTDIR\klobs.url"
CreateShortCut "$SMPROGRAMS\OOBD-ME\Uninstall OOBD-ME.lnk" "$INSTDIR\uninstaller.exe"
WriteRegStr HKCU "SOFTWARE\OOBD\OOBD-ME" "InstDir" $INSTDIR


SectionEnd

Section "un.Uninstall"
RMDir /r $INSTDIR\lib
Delete "$INSTDIR\Klobs.jar"
Delete "$INSTDIR\klobs.url"
Delete "$INSTDIR\README.TXT"
Delete "$INSTDIR\kobs.lang"
Delete "$INSTDIR\klobs.props"
Delete "$INSTDIR\kobs.props"
Delete "$INSTDIR\sessiondata.xml"
Delete "$INSTDIR\userdata.xml"
Delete "$INSTDIR\uninstaller.exe"
RMDir $INSTDIR
Delete "$SMPROGRAMS\OOBD-ME\OOBD-ME.lnk"
Delete "$SMPROGRAMS\OOBD-ME\OOBD-ME Homepage.lnk"
Delete "$SMPROGRAMS\OOBD-ME\Uninstall OOBD-ME.lnk"
RMDir "$SMPROGRAMS\OOBD-ME"
DeleteRegValue HKCU "SOFTWARE\OOBD\OOBD-ME" "InstDir"
SectionEnd

