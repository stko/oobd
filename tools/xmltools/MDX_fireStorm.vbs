' MDX_fireStorm.vbs
' 
Wscript.Echo "CAUTION: HANDLE WITH EXTREME CARE !! - IT CAN WIPE OUT YOUR WHOLE HARDDISK WITH A FINGER SNAP !!!!!"


' MDX_fireStorm crawls though the whole directory given as command line 
' unpacks all zip files it founds,
' delete all files, which don't have a mdx file extension
' also delete all empty dirs
' so leaves only the folder skeleton with its remaining mdx files inside


Set fso = CreateObject("Scripting.FileSystemObject")
set objShell = CreateObject("Shell.Application")


'kept in here for reference only
' Set objFile = fso.GetFile("C:\Scripts\Test.txt")
' Wscript.Echo "Absolute path: " & fso.GetAbsolutePathName(objFile)
' Wscript.Echo "Parent folder: " & fso.GetParentFolderName(objFile) 
' Wscript.Echo "File name: " & fso.GetFileName(objFile)
' Wscript.Echo "Base name: " & fso.GetBaseName(objFile)
'Wscript.Echo "Extension name: " & fso.GetExtensionName(objFile)


'--------------------  subfolders ------------------------
Dim initialDir
on error resume next
If Not fso.FolderExists(WScript.Arguments.Item(0)) Then
	Wscript.Echo "Usage: cscript MDX_fireStorm.vbs <your_cleanup_directory>"
	WScript.Quit 
end if
initialDir = fso.GetFolder(WScript.Arguments.Item(0))
doAnotherLoop=True
LoopCount=1
while (doAnotherLoop) 
	doAnotherLoop=False
	Wscript.Echo "Do Directory Loop Nr. ", LoopCount
	LoopCount=LoopCount + 1
	TraverseFolders fso.GetFolder(initialDir)
Wend


Function TraverseFolders(fldr)
	dirname=fso.GetAbsolutePathName(fldr)
	Wscript.Echo("Enter dir " & dirname)
	For Each sf In fldr.Files
		ext=UCase(fso.GetExtensionName(sf))
		if ext="ZIP" then
			Wscript.Echo "Zip found, unpack & delete it",  fso.GetAbsolutePathName(sf)
			Unzip2Dir(sf) 'unpack it
			If sf.attributes and 1 Then
				sf.attributes = sf.attributes - 1 ' remove the read only flag...
			end if
			sf.Delete 'and delete it..
			doAnotherLoop=True 'and mark that the structure has changed
		elseif ext="MDX" then
			If sf.attributes and 1 Then
				sf.attributes = sf.attributes - 1 ' remove the read only flag...
			end if
			RemoveDocTypeTag(fso.GetAbsolutePathName(sf)) ' removes the ugly !Doctype Tag
			Set oXMLFile = CreateObject("Msxml2.DOMDocument") 
			oXMLFile.Load(fso.GetAbsolutePathName(sf))
			Set oXMLFileVariale = oXMLFile.getElementsByTagName("ADMINISTRATION/SHORTNAME")
			shortName = oXMLFileVariale.Item(0).Text
			Set oXMLFileVariale = oXMLFile.getElementsByTagName("SSDS_INFORMATION/SSDS_PART_NUMBER")
			specName = oXMLFileVariale.Item(0).Text
			newName=shortName&"_"&specName&".mdx"
			'do while fso.FileExists(newName) and newName <>sf.Name
			'	specName=specName&"_"
			'	newName=shortName&"_"&specName&".mdx"
			'Loop
			if newName <>sf.Name then
				if fso.FileExists(fso.GetParentFolderName(sf) &"\"& newName) then
					Wscript.Echo( "Delete: " & sf.Name)
					fso.DeleteFile (fso.GetParentFolderName(sf) &"\"& newName)
				end if
					Wscript.Echo( "newname: " & fso.GetParentFolderName(sf) &"\"& newName)
				Wscript.Echo(sf.Name & " -> " & newName)
				sf.Name=newName
			else 
				Wscript.Echo("MDX: " & fso.GetAbsolutePathName(sf))
			end if
			Wscript.Echo("MDX continue")
		else
			Wscript.Echo "Irrelevant file, delete..",  fso.GetAbsolutePathName(sf)
			If sf.attributes and 1 Then
				sf.attributes = sf.attributes - 1 ' remove the read only flag...
			end if
			sf.Delete
			doAnotherLoop=True 'and mark that the structure has changed
		end if
	Next

	If fldr.Files.Count = 0 And fldr.SubFolders.Count = 0 Then
		Wscript.Echo "Dir empty, delete..",  fso.GetAbsolutePathName(fldr)
		If fldr.attributes and 1 Then
			fldr.attributes = fldr.attributes - 1 ' remove the read only flag...
		end if
		fldr.Delete
		doAnotherLoop=True 'and mark that the structure has changed
	else
		For Each sd In fldr.SubFolders
			TraverseFolders sd  '<- recurse here
		Next
	end if
		Wscript.Echo("leaving dir " & dirname)

End Function



Public Sub Unzip2Dir(sf)

	'The location of the zip file.
	ZipFile=fso.GetAbsolutePathName(sf)
	'The folder the contents should be extracted to.
	ExtractTo=fso.GetParentFolderName(sf)+"\"+fso.GetBaseName(sf)

	'If the extraction location does not exist create it.
	If NOT fso.FolderExists(ExtractTo) Then
		Wscript.Echo "Create folder",  ExtractTo
		fso.CreateFolder(ExtractTo)
	End If

	'Extract the contants of the zip file.
	set FilesInZip=objShell.NameSpace(ZipFile).items
	objShell.NameSpace(ExtractTo).CopyHere(FilesInZip)
end sub


Function RemoveDocTypeTag(XMLFileName)
Wscript.Echo "Read MDX file",  XMLFileName
Set objFileToRead = CreateObject("Scripting.FileSystemObject").OpenTextFile(XMLFileName,1)
strFileText = objFileToRead.ReadAll()
objFileToRead.Close
Set objFileToRead = Nothing
Wscript.Echo "Read MDX file done input length:", Len(strFileText)

Dim objRegExp 
  Set objRegExp = New Regexp

  objRegExp.IgnoreCase = True
  objRegExp.Global = True
  objRegExp.Pattern = "<!DOCTYPE(.|\n)+?>"

  'Replace all HTML tag matches with the empty string
  strFileText = objRegExp.Replace(strFileText, "")
Wscript.Echo " MDX regex done input length:", Len(strFileText)




Set objFileToWrite = CreateObject("Scripting.FileSystemObject").OpenTextFile(XMLFileName,2,true)
objFileToWrite.Write(strFileText)
objFileToWrite.Close
Set objFileToWrite = Nothing
Wscript.Echo " MDX file save done input length:", Len(strFileText)
End Function