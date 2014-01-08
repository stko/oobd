Function XML_Read_Value_byTag(XMLFileName, XMLTag)
	Set oXMLFile = CreateObject("Msxml2.DOMDocument") 
	oXMLFile.Load(XMLFileName)
	Set oXMLFileVariale = oXMLFile.getElementsByTagName(XMLTag)
	XML_Read_Value_byTag = oXMLFileVariale.Item(0).Text
End Function


On Error Resume Next
Dim fso, folder, files, NewsFile,sFolder,sTag,sExtension, shortName

Set fso = CreateObject("Scripting.FileSystemObject")
sTag = Wscript.Arguments.Item(0)
sExtension = Wscript.Arguments.Item(1)
If sTag = "" or sExtension = ""  Then
	Wscript.Echo "Usage: renameMDX nodeID fileextension [newname_prefix]"
	Wscript.Quit
End If
sPrefix = Wscript.Arguments.Item(2)

Set folder = fso.GetFolder(".")
Set files = folder.Files

For each folderIdx In files
	if fso.GetExtensionName(folderIdx.Path) = sExtension then
		shortName=""
		shortName=XML_Read_Value_byTag(folderIdx.Name,sTag)
		if shortName <>"" then
			newName=sPrefix&shortName&"."&sExtension
			do while fso.FileExists(newName) and newName <>folderIdx.Name
				shortName=shortName&"_"
				newName=sPrefix&shortName&"."&sExtension
			Loop
			Wscript.Echo(folderIdx.Name & " -> " & newName)
			folderIdx.Name=newName
		Else
			Wscript.Echo("**** XML Error in "&folderIdx.Name)
		End If
	End If
Next
NewFile.Close
