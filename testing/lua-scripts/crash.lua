--[[crash.lua

this file is just made to make the lua engine crash, so don't expect much function :-)
--]]



function callCrash(oldvalue,id)
	callNotExistingFunction() -- this function does not exist
	return "Still Alive after callCrash"
end



---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	return oldvalue
end

function Main(oldvalue,id)
	startupCrash() -- this function does not exist
	openPage("Crash test")
	addElement("MenuCrash", "notExist","",0x0, "") -- also notExist does not exist, surprise!
	addElement("crash in Subfunction", "callCrash","",0x0, "")
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return