dofile("../../../tools/lib_lua/lua_utils.lua")
dofile("../../../tools/lib_lua/serial_dxm.lua")
dofile("../../../tools/lib_lua/lua_uds.lua")


function helloworld(oldvalue,id)
	return "Hello World"
end



---- testing the database 

function testdb(oldvalue,id)
	local newArray
	local index
	local textID
	local dbName
	local colum
	local nr
	if id=="1" then  -- all correct
		dbName = "testsuiteData"
		textID = "005"
		colum = "Value"
		nr = "1"
	end
	if id=="2" then -- database does not exist
		dbName = "foo"
		textID = "005"
		colum = "Value"
		nr = "1"
	end
	if id=="3" then -- textID does not exist
		dbName = "testsuiteData"
		textID = "foo"
		colum = "Value"
		nr = "1"
	end
	if id=="4" then -- read from second column
		dbName = "testsuiteData"
		textID = "005"
		colum = "Remark"
		nr = "1"
	end
	if id=="5" then -- read from existing column
		dbName = "testsuiteData"
		textID = "005"
		colum = "foo"
		nr = "1"
	end
	if id=="6" then  -- read second row
		dbName = "testsuiteData"
		textID = "005"
		colum = "Value"
		nr = "2"
	end
	if id=="7" then  -- read second row from second column
		dbName = "testsuiteData"
		textID = "005"
		colum = "Remark"
		nr = "2"
	end
	if id=="8" then  -- read not existing row
		dbName = "testsuiteData"
		textID = "005"
		colum = "Value"
		nr = "99"
	end
	if id=="9" then  -- read not existing row from second column
		dbName = "testsuiteData"
		textID = "005"
		colum = "Remark"
		nr = "99"
	end
	if id=="10" then  -- read not existing row from non existing column
		dbName = "testsuiteData"
		textID = "005"
		colum = "foo"
		nr = "99"
	end
	if dbLookup ~= nil then -- db functionality implemented?
		newArray= dbLookup(dbName..".oodb",textID) -- look up for the given category db and for the given LOCALE
		return getDBEntry( newArray, colum ,  nr , textID.." not found" )
	else -- no db functionality available
		return "noDBlookup"
	end
end

--------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	return oldvalue
end


function Main(oldvalue,id)
	openPage("OOBD-ME Main")
	addElement("Hello World", "helloworld","",0x0, "")
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return