Function XML_Read_Value_byTag(XMLFileName, XMLTag)
	Set oXMLFile = CreateObject("Msxml2.DOMDocument") 
	oXMLFile.Load(XMLFileName)
	Set oXMLFileVariale = oXMLFile.getElementsByTagName(XMLTag)
	XML_Read_Value_byTag = oXMLFileVariale.Item(0).Text
End Function


On Error Resume Next
Dim fso, folder, files, NewsFile,sFolder,sExtension, shortName

Set fso = CreateObject("Scripting.FileSystemObject")
sExtension = Wscript.Arguments.Item(0)
If sExtension = ""  Then
	Wscript.Echo "Usage: renameMDX fileextension [newname_prefix]"
	Wscript.Quit
End if
if  Wscript.Arguments.Length=2 then
	sPrefix = Wscript.Arguments.Item(1)&"_"
End If

Set folder = fso.GetFolder(".")
Set files = folder.Files

For each sf In files
	if fso.GetExtensionName(sf.Path) = sExtension then
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
		newName=sPrefix&shortName&"_"&specName&"."&sExtension
		'do while fso.FileExists(newName) and newName <>sf.Name
		'	specName=specName&"_"
		'	newName=shortName&"_"&specName&".mdx"
		'Loop
		Wscript.Echo sf.Name , " -> ",newName
		if newName <>sf.Name then
			if fso.FileExists(fso.GetParentFolderName(sf) &"\"& newName) then
				fso.DeleteFile (fso.GetParentFolderName(sf) &"\"& newName)
			end if
			sf.Name=newName
		end if
	End If
Next
NewFile.Close



Function RemoveDocTypeTag(XMLFileName)
	'Wscript.Echo "correct Doctype in ",  XMLFileName
	Set objFileToRead = CreateObject("Scripting.FileSystemObject").OpenTextFile(XMLFileName,1)
	strFileText = objFileToRead.ReadAll()
	objFileToRead.Close
	Set objFileToRead = Nothing
	Dim objRegExp 
	Set objRegExp = New Regexp
	objRegExp.IgnoreCase = True
	objRegExp.Global = True
	objRegExp.Pattern = "<!DOCTYPE(.|\n)+?>"
	'Replace all HTML tag matches with the empty string
	strFileText = objRegExp.Replace(strFileText, "")
	Set objFileToWrite = CreateObject("Scripting.FileSystemObject").OpenTextFile(XMLFileName,2,true)
	objFileToWrite.Write(strFileText)
	objFileToWrite.Close
	Set objFileToWrite = Nothing
End Function