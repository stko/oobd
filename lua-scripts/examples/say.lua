

dofile("../../tools/lib_lua/lua_utils.lua")
-- dofile("../../tools/lib_lua/serial_dxm.lua")


function jsonStringService(oldvalue,id)
	-- contacting a jsonrpc server, suppling the parameters as json string
	ioInput("http://localhost:10023",' { "method": "say", "params": {"text":"'..oldvalue..'"}, "id": 1}',"json")
	-- reading the whole result as lua table..
	res = ioRead("*json")
	 -- here we certainly will need some convinience functions for the json-rpc format
	
	if res ~=nil then
		if res.result ~=nil then
			return oldvalue
		else
			return res.error
		end
	else
		return "no valid answer :-("
	end
end


---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- connect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	return oldvalue
end

function Main(oldvalue,id)
	openPage("OOBD Says Welcome")
        addElement("Text Input", "jsonStringService","Willkommen zu O O B D", 0x0, "", {  type="TextEdit" } )
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

