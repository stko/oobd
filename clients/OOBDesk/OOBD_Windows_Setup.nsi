!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"



SetCompress force
SetCompressor /SOLID lzma
Name "OOBDesk"
OutFile "OOBDesk_Setup.exe"
RequestExecutionLevel highest
XPStyle on






var fileToMake
var menutype
Function createDummy 
	ClearErrors
	FileOpen $0 $fileToMake w
	IfErrors noFile
	FileWrite $0 "just a dummy file to satisfy the class loader"
	FileClose $0
noFile:
FunctionEnd

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

#CreateDirectory $INSTDIR\lib
#File /oname=$INSTDIR\lib\appframework-1.0.3.jar dist/lib/appframework-1.0.3.jar
#File /oname=$INSTDIR\lib\jna.jar dist/lib/jna.jar
#File /oname=$INSTDIR\lib\kahlua.jar dist/lib/kahlua.jar
#File /oname=$INSTDIR\lib\purejavacomm.jar dist/lib/purejavacomm.jar
#File /oname=$INSTDIR\lib\RXTXcomm.jar dist/lib/RXTXcomm.jar
#File /oname=$INSTDIR\lib\swing-worker-1.1.jar dist/lib/swing-worker-1.1.jar

#File /oname=$INSTDIR\lib\java_websocket.jar dist/lib/java_websocket.jar
File /r dist/lib






CreateDirectory $INSTDIR\bus
StrCpy $fileToMake "$INSTDIR\bus\BusCom.class"
Call createDummy 
CreateDirectory $INSTDIR\scriptengine
StrCpy $fileToMake  "$INSTDIR\scriptengine\ScriptengineLua.class"
Call createDummy
CreateDirectory $INSTDIR\uihandler
StrCpy $fileToMake  "$INSTDIR\uihandler\UIHandler.class"
Call createDummy
StrCpy $fileToMake  "$INSTDIR\uihandler\WsUIHandler.class"
Call createDummy
CreateDirectory $INSTDIR\db
StrCpy $fileToMake  "$INSTDIR\db\AVLLookup.class"
Call createDummy

File "dist/OOBDesk.jar"
File /oname=oobdcore.props  "oobdcore_dist.props"
#File  "enginelua_dist.props"
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


SectionEnd

Section "un.Uninstall"
SetOutPath "$INSTDIR"
RMDir /r $INSTDIR\lib
RMDir /r $INSTDIR\logs
RMDir /r $INSTDIR\bus
RMDir /r $INSTDIR\port
RMDir /r $INSTDIR\db
RMDir /r $INSTDIR\scriptengine
RMDir /r $INSTDIR\protocol
RMDir /r $INSTDIR\uihandler
RMDir /r $INSTDIR\OOBDesk
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

