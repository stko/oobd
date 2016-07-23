!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"

SetCompress force
SetCompressor /SOLID lzma
Name "OOBDesk"
OutFile "OOBDesk_Setup.exe"
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
	StrCpy $INSTDIR  "$PROGRAMFILES\OOBD"
	StrCpy $menutype  "OOBDesk"
	SetShellVarContext all
	WriteRegStr HKLM "SOFTWARE\OOBD\OOBDesk" "InstDir" $INSTDIR\OOBDesk
	WriteRegStr HKLM "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Script/Dir" "$DOCUMENTS\OOBD-Scripts"
	WriteRegStr HKLM "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Library/Dir" "$DOCUMENTS\OOBD-Library"
	Goto Done
	 
	Continue:
	StrCpy $INSTDIR  "$APPDATA\OOBD"
	StrCpy $menutype  "OOBDesk(local)"
	WriteRegStr HKCU "SOFTWARE\OOBD\OOBDesk" "InstDir" $INSTDIR\OOBDesk
	WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Script/Dir" "$DOCUMENTS\OOBD-Scripts"
	WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Library/Dir" "$DOCUMENTS\OOBD-Library"
	 
	Done:
FunctionEnd

 ; GetParent
 ; input, top of stack  (e.g. C:\Program Files\Poop)
 ; output, top of stack (replaces, with e.g. C:\Program Files)
 ; modifies no other variables.
 ;
 ; Usage:
 ;   Push "C:\Program Files\Directory\Whatever"
 ;   Call GetParent
 ;   Pop $R0
 ;   ; at this point $R0 will equal "C:\Program Files\Directory"
 
Function un.GetParent
 
  Exch $R0
  Push $R1
  Push $R2
  Push $R3
 
  StrCpy $R1 0
  StrLen $R2 $R0
 
  loop:
    IntOp $R1 $R1 + 1
    IntCmp $R1 $R2 get 0 get
    StrCpy $R3 $R0 1 -$R1
    StrCmp $R3 "\" get
  Goto loop
 
  get:
    StrCpy $R0 $R0 -$R1
 
    Pop $R3
    Pop $R2
    Pop $R1
    Exch $R0
 
FunctionEnd

function un.onInit
	ClearErrors
	UserInfo::GetName
	IfErrors Continue
	Pop $R1
	UserInfo::GetAccountType
	Pop $R2
	 
	StrCmp $R2 "Admin" 0 Continue
	StrCpy $menutype  "OOBDesk"
	SetShellVarContext all

	DeleteRegKey HKLM "SOFTWARE\OOBD\OOBDesk" 
	DeleteRegKey /ifempty HKLM "SOFTWARE\OOBD"
	DeleteRegKey HKLM "Software\JavaSoft\Prefs\com.oobd.preference.app.props"
	Goto Done
	 
	Continue:
	StrCpy $menutype  "OOBDesk(local)"
	DeleteRegKey HKCU "SOFTWARE\OOBD\OOBDesk" 
	DeleteRegKey /ifempty HKCU "SOFTWARE\OOBD"
	DeleteRegKey HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props"

	Done:
FunctionEnd


#!include "ZipDLL.nsh"
Page license
#Page directory

Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

LicenseData gpl-2.0.txt
LicenseForceSelection checkbox

Section "OOBDesk"
SetOutPath $INSTDIR\OOBDesk

File /oname=OOBDesk.jar "store/OOBDesk_Rxxx_Branded.jar"
File "oobd.url"
File "jlogviewer.jar"
File "logging.props"


WriteUninstaller "$INSTDIR\OOBDesk\uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD\$menutype"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk.lnk" "cmd" "/C java -jar -Djava.util.logging.config.file=logging.props OOBDesk.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\View Log Files.lnk" "javaw" "-jar jlogviewer.jar"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\OOBDesk Homepage.lnk" "$INSTDIR\OOBDesk\oobd.url"
CreateShortCut "$SMPROGRAMS\OOBD\$menutype\Uninstall OOBDesk.lnk" "$INSTDIR\OOBDesk\uninstaller.exe"


# create the sample files
SetOutPath "$DOCUMENTS\OOBD-Scripts"
File "../../lua-scripts/*.epa"

# create the html library
SetOutPath "$DOCUMENTS\OOBD-Library"
File /r "../../tools/lib_html/libs"
File /r "../../tools/lib_html/theme"

SectionEnd

Section "un.Uninstall"

RMDir  /r $INSTDIR
Push $INSTDIR
Call un.GetParent
Pop $R0
RMDir $R0

RMDir /r "$SMPROGRAMS\OOBD\$menutype"
RMDir "$SMPROGRAMS\OOBD"

SectionEnd

