'On Error Resume Next
Dim fso, folder, files, NewsFile,args,i,msgSubject,msgAttachName
Const olByValue = 1
Const olMailItem = 0
    
Dim oOApp 
Dim oOMail
Dim  msgBody
Set oOApp = CreateObject("Outlook.Application")
Set fso = CreateObject("Scripting.FileSystemObject")

WScript.StdOut.WriteLine(" Checkpoint 1 ")

Const ForReading = 1
Set objDictionary = CreateObject("Scripting.Dictionary")

Set objFile = fso.OpenTextFile ("mailConfig.cfg", ForReading)
i = 0
msgSubject= objFile.Readline
msgAttachName= objFile.Readline
Do Until objFile.AtEndOfStream
	strNextLine = objFile.Readline
	If strNextLine <> "" Then
		msgBody= msgBody & strNextLine
		objDictionary.Add i, strNextLine
	End If
	i = i + 1
Loop
objFile.Close

'Then you can iterate it like this

'For Each strLine in objDictionary.Items
'WScript.Echo strLine
'Next



Set args=Wscript.Arguments
If args.count > 1  Then '  we need 0 or 1 argument
	WScript.StdErr.WriteLine "Usage: sendgroupmails.vbs [directory]"
	Wscript.Quit
End If
If args.count > 0  Then 
	Set folder = fso.GetFolder(Wscript.Arguments.Item(0))
Else
	Set folder = fso.GetFolder(".")
End If

Set files = folder.Files
Set objRegAusdr = New RegExp
objRegAusdr.IgnoreCase = True
strPattern = "([\w-\.]{1,}\@([\da-zA-Z-]{1,}\.){1,}[\da-zA-Z-]{2,4})(.groupkeys)$"
objRegAusdr.Pattern = strPattern
For each folderIdx In files
	Set Matches = objRegAusdr.Execute(folderIdx)	
	If Matches.Count>0 Then
		set match = Matches(0) 
		If match.SubMatches.Count > 0 Then
			WScript.StdOut.WriteLine(folderIdx.Name+ " for " & match.SubMatches(0) & " with " & msgAttachName)
			fso.CopyFile folderIdx.Name, msgAttachName
			set fileHandle =fso.GetFile(msgAttachName)
			Set oOMail = oOApp.CreateItem(olMailItem)

			With oOMail
			.To = match.SubMatches(0)
			.Subject = msgSubject
			.Body = msgBody
			.HTMLBody = msgBody
			.Attachments.Add fso.GetAbsolutePathName(msgAttachName), olByValue, 1
			.Send
			End With

		end if
	end if
Next





