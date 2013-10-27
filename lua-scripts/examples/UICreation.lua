

dofile("../../tools/lib_lua/serial_dxm.lua")

myedit=""

function donothing(oldvalue,id)
	return myedit
end

function edittext(oldvalue,id)
	myedit =oldvalue.."+"
	return myedit
end



---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	identifyOOBDInterface()
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	return oldvalue
end

function Main(oldvalue,id)
	openPage("OOBD UI Test")
	addElement("Simple Element", "donothing",myedit,0x0, "")
        addElement("Text Input", "edittext",">>>",0x1, "",{  type="TextEdit"})
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

