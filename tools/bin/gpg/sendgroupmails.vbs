On Error Resume Next
Dim fso, folder, files, NewsFile,args,i
Const olByValue = 1
Const olMailItem = 0
    
Dim oOApp 
Dim oOMail

Set oOApp = CreateObject("Outlook.Application")
Set oOMail = oOApp.CreateItem(olMailItem)

Set fso = CreateObject("Scripting.FileSystemObject")
Set args=Wscript.Arguments
If args.count < 3 and args.count MOD 2 = 0  Then '  we need at least 3 arguments, and their number must be not equal
	WScript.StdErr.WriteLine "Usage: findlist dir prefix1 ext1 [prefix2 ext2..]"
	Wscript.Quit
End If
Set folder = fso.GetFolder(Wscript.Arguments.Item(0))
Set files = folder.Files
Set objRegAusdr = New RegExp
objRegAusdr.IgnoreCase = True
i=1
while i<args.count
	strPattern = "\."+args.item(i)+"$"
	objRegAusdr.Pattern = args.Item(i+1)+"$"
	WScript.StdOut.Write(args.Item(i)+ " ")
	For each folderIdx In files
		' ### Zeichenkette nach Pattern durchsuchen
		Set Matches = objRegAusdr.Execute(folderIdx)	
		If Matches.Count = 1 Then
			WScript.StdOut.Write(folderIdx.Name+ " ")
		end if
	Next
	Wscript.Echo
	i=i+2
wend




'With oOMail
'    .To = "mapitest@ihremaildomain.tld"
'    .Subject = "Dies ist der Betreff"
'    .Body = "Testnachricht "
'    .Attachments.Add "c:\boot.ini", olByValue, 1
'    .Send
'End With
