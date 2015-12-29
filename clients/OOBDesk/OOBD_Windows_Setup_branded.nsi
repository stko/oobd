!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"



SetCompress force
SetCompressor /SOLID lzma
Name "OOBDesk"
OutFile "OOBDesk_Setup_Branded.exe"
RequestExecutionLevel highest
XPStyle on






var menutype

function .onInit
	ClearErrors
	UserInfo::GetName
	IfErrors Continue
	Pop $R1
	UserInfo::GetAccountType
	Pop $R2
	 
	StrCmp $R2 "Admin" 0 Continue
	StrCpy $INSTDIR  "$PROGRAMFILES\OOBD\OOBDesk"
	StrCpy $menutype  "OOBDesk"
	SetShellVarContext all
	WriteRegStr HKLM "SOFTWARE\OOBD\OOBDesk" "InstDir" $INSTDIR
	Goto Done
	 
	Continue:
	StrCpy $INSTDIR  "$APPDATA\OOBD\OOBDesk"
	StrCpy $menutype  "OOBDesk(local)"
	WriteRegStr HKCU "SOFTWARE\OOBD\OOBDesk" "InstDir" $INSTDIR
	WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Script/Dir" "$DOCUMENTS\OOBD-Scripts"
	WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Library/Dir" "$DOCUMENTS\OOBD-Library"

	 
	Done:
FunctionEnd


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




File /oname=OOBDesk.jar "store/OOBDesk_Rxxx_Branded.jar"
File "oobd.url"
File "jlogviewer.jar"
File "logging.props"
File "../../tools/bin/OOBDCopyShop.hta"

#CreateDirectory "$DOCUMENTS\OOBD-logs"

#FileOpen $0 $INSTDIR\file.dat w
#FileClose $0

WriteUninstaller "$INSTDIR\uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD\$menutype"
#CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk.lnk" "javaw" "-version:1.7 -jar -Djava.library.path=bus/lib -Djava.util.logging.config.file=logging.props OOBDesk.jar"
#CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk.lnk" "java" "-version:1.7 -jar -Djava.library.path=bus/lib -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk (JRE 1.6).lnk" "java" "-version:1.6 -jar -Djava.library.path=port -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk (JRE 1.7).lnk" "java" "-version:1.7 -jar -Djava.library.path=port -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk (JRE actual).lnk" "java" " -jar -Djava.library.path=port -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDCopyShop.lnk" "$INSTDIR\OOBDCopyShop.hta"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\View Log Files.lnk" "javaw" "-jar jlogviewer.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk Homepage.lnk" "$INSTDIR\oobd.url"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\Uninstall OOBDesk.lnk" "$INSTDIR\uninstaller.exe"


# create the sample files
SetOutPath "$DOCUMENTS\OOBD-Scripts"
File "../../lua-scripts/obdII-standard/OOBD.lbc"
File "../../lua-scripts/obdII-standard/OOBD.lua"
File "../../lua-scripts/obdII-standard/dtc.csv"
File "../../lua-scripts/obdII-standard/dtc.oodb"
File "../../lua-scripts/examples/UICreation.lua"
File "../../lua-scripts/examples/UICreation.lbc"
File /oname=stdlib.lbc "../OOBD-ME/res/stdlib.lbc"

# create the html library
SetOutPath "$DOCUMENTS\OOBD-Library"
File /r "../../tools/lib_html/libs"
File /r "../../tools/lib_html/theme"


SectionEnd

Section "un.Uninstall"
SetOutPath "$INSTDIR"
RMDir /r $INSTDIR\lib
RMDir /r $INSTDIR\logs
Delete "$INSTDIR\jlogviewer.jar"
Delete "$INSTDIR\OOBDesk.jar"
Delete "$INSTDIR\oobd.url"
Delete "$INSTDIR\OOBDCopyShop.hta"
Delete "$INSTDIR\*.props"
Delete "$INSTDIR\*.log"
Delete "$INSTDIR\*.session"
Delete "$INSTDIR\OOBD.lbc"
Delete "$INSTDIR\dtc.*"
Delete "$INSTDIR\stdlib.lbc"
Delete "$INSTDIR\uninstaller.exe"
RMDir $INSTDIR
Delete "$SMPROGRAMS\OOBD\$menutype\OOBDesk.lnk"
Delete "$SMPROGRAMS\OOBD\$menutype\OOBDesk Homepage.lnk"
Delete "$SMPROGRAMS\OOBD\$menutype\OOBDCopyShop.lnk"
Delete "$SMPROGRAMS\OOBD\$menutype\View Log Files.lnk"
Delete "$SMPROGRAMS\OOBD\$menutype\Uninstall OOBDesk.lnk"
RMDir "$SMPROGRAMS\OOBD\$menutype"

DeleteRegKey HKCU "SOFTWARE\OOBD\OOBDesk" 
DeleteRegKey /ifempty HKCU "SOFTWARE\OOBD" 
SectionEnd

