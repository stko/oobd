!include nsDialogs.nsh
!include LogicLib.nsh
!include "winMessages.nsh"



SetCompress force
SetCompressor /SOLID lzma
Name "OOBD-Swing"
OutFile "OOBD-Swing_Windows_Setup.exe"
XPStyle on
Var Dialog
Var Label
Var ComboBox
Var SelectionCB

InstallDir "$APPDATA\OOBD\OOBD-Swing"
InstallDirRegKey HKCU "SOFTWARE\OOBD\OOBD-Swing" "InstDir"
#!include "ZipDLL.nsh"
Page license
Page directory
Page custom nsDialogsPage 

Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

LicenseData gpl-2.0.txt
LicenseForceSelection checkbox

Function nsDialogsPage
nsDialogs::Create 1018
	Pop $Dialog

	${If} $Dialog == error
		Abort
	${EndIf}
	${NSD_CreateLabel} 0 0 100% 12u "Select your serial Comport (does not work yet, just ignore it..)"
	Pop $Label

	${NSD_CreateDropList} 100 40 20% 59u “”
	Pop $ComboBox

	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM1”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM2”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM3”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM4”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM5”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM6”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM7”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM8”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM9”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM10”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM11”
	SendMessage $ComboBox ${CB_ADDSTRING} 0 “STR:COM12”

	#SendMessage $ComboBox ${CB_SELECTSTRING} 0 “STR:COM2”

	GetFunctionAddress $0 CBOnChange
	nsDialogs::OnChange $ComboBox $0


	nsDialogs::Show

FunctionEnd

Function CBOnChange
	SendMessage $ComboBox ${CB_GETCURSEL} 0 0 $SelectionCB

	MessageBox MB_OK $SelectionCB
FunctionEnd

Section "OOBD-Swing"
SetOutPath $INSTDIR
File /r  "dist/lib"
#File /r  "build/classes/skdsswing/bus"
File /r  "../Base/build/classes/org/oobd/base/bus"
File /r  "../Base/build/classes/org/oobd/base/port"
File /r  "../Base/build/classes/org/oobd/base/scriptengine"
File /r  "../Base/build/classes/org/oobd/base/protocol"
File /oname=OOBD-Swing.jar  "dist/SKDS-Swing.jar"
File "buscom_dist.props"
File /oname=oobdcore.props  "oobdcore_dist.props"
File "enginelua_dist.props"
File "oobd.url"
File "jlogviewer.jar"
File "logging.props"
File /oname=OOBD.lbc "../../../OOBD-ME/res/OOBD.lbc"
File /oname=stdlib.lbc "../../../OOBD-ME/res/stdlib.lbc"
CreateDirectory "$INSTDIR\logs"

#FileOpen $0 $INSTDIR\file.dat w
#FileClose $0

WriteUninstaller "$INSTDIR\uninstaller.exe"
CreateDirectory "$SMPROGRAMS\OOBD\OOBD-Swing"
#CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk" "javaw" "-jar -Djava.library.path=bus/lib -Djava.util.logging.config.file=logging.props OOBD-Swing.jar"
#CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk" "java" "-jar -Djava.library.path=bus/lib -Djava.util.logging.config.file=logging.props OOBD-Swing.jar"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk" "java" "-jar -Djava.library.path=port -Djava.util.logging.config.file=logging.props OOBD-Swing.jar"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\View Log Files.lnk" "javaw" "-jar jlogviewer.jar logs\oobd0.xml"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing Homepage.lnk" "$INSTDIR\oobd.url"
CreateShortCut "$SMPROGRAMS\OOBD\OOBD-Swing\Uninstall OOBD-Swing.lnk" "$INSTDIR\uninstaller.exe"
WriteRegStr HKCU "SOFTWARE\OOBD\OOBD-Swing" "InstDir" $INSTDIR


SectionEnd

Section "un.Uninstall"
RMDir /r $INSTDIR\lib
RMDir /r $INSTDIR\logs
RMDir /r $INSTDIR\bus
RMDir /r $INSTDIR\scriptengine
RMDir /r $INSTDIR\protocol
RMDir /r $INSTDIR\SKDS-Swing
RMDir /r $INSTDIR\OOBD-Swing
Delete "$INSTDIR\jlogviewer.jar"
Delete "$INSTDIR\OOBD-Swing.jar"
Delete "$INSTDIR\oobd.url"
Delete "$INSTDIR\*.props"
Delete "$INSTDIR\OOBD.lbc"
Delete "$INSTDIR\stdlib.lbc"
Delete "$INSTDIR\uninstaller.exe"
RMDir $INSTDIR
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing.lnk"
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\OOBD-Swing Homepage.lnk"
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\View Log Files.lnk"
Delete "$SMPROGRAMS\OOBD\OOBD-Swing\Uninstall OOBD-Swing.lnk"
RMDir "$SMPROGRAMS\OOBD\OOBD-Swing"
RMDir "$SMPROGRAMS\OOBD"
DeleteRegKey HKCU "SOFTWARE\OOBD\OOBD-Swing" 
DeleteRegKey /ifempty HKCU "SOFTWARE\OOBD" 
SectionEnd

