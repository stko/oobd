'
' This is the Windows installer for the OOBD Secret Key 
'
'
'  How to use:
'  Make sure this file is saved or renamed with a .vbs file extension, like "SecretKey_Installer.vbs"
'
'
' Then, in the Windows Explorer, do a double click on this file
'
' This will start the installer and installs your file 
'
'
'
'
'




Function Base64Decode(ByVal strText) ' As ByteArray

    With CreateObject("MSXML2.DOMDocument.3.0").CreateElement("Base64")
        .DataType = "bin.base64"
        .Text = strText
        Base64Decode = .NodeTypedValue
    End With

End Function

Sub Base64ToBinaryFile(strBase64, strFileName)

    With CreateObject("ADODB.Stream")
        .Type = 1                       ' adTypeBinary
        .Open
        .Write Base64Decode(strBase64)  ' Write the byte array
        .SaveToFile strFileName, 2      ' Overwrite if file exists (adSaveCreateOverWrite)
    End With

End Sub


With CreateObject("Scripting.FileSystemObject").OpenTextFile(WScript.ScriptFullName)

    ' Look for the "start"...
    Do Until .AtEndOfStream

        strLine = .ReadLine()

        If strLine = "' ~END~" Then fRead = False
        If strLine <> "' " and fRead Then strBase64 = strBase64 & Mid(strLine, 3)
        If strLine = "' ~START~" Then fRead = True

    Loop

End With

Set WshShell = CreateObject("WScript.Shell")
Set WshSysEnv = WshShell.Environment("PROCESS")
userDir= WshSysEnv("USERPROFILE")
MsgBox "Done - Secret Key installed in the Users Home Dir"


' Write the key
Base64ToBinaryFile strBase64, userDir & "\userkey.sec"

