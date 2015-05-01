!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"



SetCompress force
SetCompressor /SOLID lzma
Name "Java 1.8 portable w. security Patch"
OutFile "Java18_portable_unlimited_policy.exe"
RequestExecutionLevel highest
XPStyle on


var menutype
var oobddir

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
	ReadRegStr $oobddir HKLM "SOFTWARE\OOBD\OOBDesk" "InstDir"
	Goto Done
	 
	Continue:
	StrCpy $INSTDIR  "$APPDATA\OOBD\OOBDesk"
	StrCpy $menutype  "OOBDesk(local)"
	WriteRegStr HKCU "SOFTWARE\OOBD\OOBDesk" "InstDir" $INSTDIR
	ReadRegStr $oobddir HKCU "SOFTWARE\OOBD\OOBDesk" "InstDir"

	 
	Done:
	${If} $oobddir == ""
		MessageBox MB_OK "No OOBD Installation found - Terminating"
		Abort
	${EndIf}
FunctionEnd


#!include "ZipDLL.nsh"
Page license
#Page directory


Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

LicenseData java_licence.txt
LicenseForceSelection checkbox


Section "OOBDesk"
SetOutPath $INSTDIR

File /a /r java18

WriteUninstaller "$INSTDIR\Java18_uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD\$menutype"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk (portable Java).lnk" "$INSTDIR\java18\bin\java.exe" " -jar -Djava.library.path=port -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\Uninstall Java Portable.lnk" "$INSTDIR\Java18_uninstaller.exe"




SectionEnd

Section "un.Uninstall"
SetOutPath "$INSTDIR"
RMDir /r $INSTDIR\java18
Delete "$INSTDIR\Java18_uninstaller.exe"
Delete "$SMPROGRAMS\OOBD\$menutype\OOBDesk (JRE actual).lnk"
Delete "$SMPROGRAMS\OOBD\$menutype\Uninstall Java Portable.lnk"

SectionEnd

