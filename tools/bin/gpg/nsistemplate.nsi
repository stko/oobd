!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"

SetCompress force
SetCompressor /SOLID lzma
Name "Scripts for $$user"
OutFile "$$installerpath.exe"
RequestExecutionLevel user
XPStyle on

function .onInit
	ClearErrors
	UserInfo::GetName
	IfErrors Continue
	Pop $R1
	UserInfo::GetAccountType
	Pop $R2
	 
	StrCmp $R2 "Admin" 0 Continue
	MessageBox MB_OK "Scripts can only be installed as normal user, not as Admin"
	Abort
	Goto Done
	 
	Continue:
	Done:
FunctionEnd


#!include "ZipDLL.nsh"
Page license
Page instfiles

LicenseData "$$licencefile"
LicenseForceSelection checkbox


Section "OOBDesk"
SetOutPath $PROFILE
File /oname=groupkey.sec "$$groupfile"


# create the script files
SetOutPath "$DOCUMENTS\OOBD-Scripts"
File /oname=groupkey.sec "$$groupfile"
$$files



WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Script/Dir" "$DOCUMENTS\OOBD-Scripts"

#uncomment, if your setup requires a proxy
#WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Kadaver/Server/Proxy/Host" "yourproxy.com"
#WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Server/Proxy/Host" "yourproxy.com"
#WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Kadaver/Server/Proxy/Port" "83"
#WriteRegStr HKCU "Software\JavaSoft\Prefs\com.oobd.preference.app.props" "/Server/Proxy/Port" "83"

SectionEnd
