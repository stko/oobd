dofile("../../tools/lib_lua/lua_utils.lua")

myedit=""

function showlastValue(oldvalue,id)
	return myedit
end

function bool(oldvalue,id)
	myedit =oldvalue
	return myedit
end

function edittext(oldvalue,id)
	myedit =oldvalue
	return myedit
end

function donothing(oldvalue,id)
	myedit =oldvalue
	return myedit
end

function donothing2(oldvalue,id)
	myedit =oldvalue
	return myedit
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
	openPage("OOBD UI Test")
        addElement("Text Input", "edittext","Input some Text here", 0x0, "", {  type="TextEdit" } )
        addElement("Text Input with Regex (\\d\\d)", "edittext","Input some Text here", 0x0, "", {  type="TextEdit" , regex ="\\d\\d" } )
	addElement("Click here to see last input value", "showlastValue", myedit, 0x0, "")
        addElement("Checkbox with longer Text...", "bool","True",0x0, "",{ type="CheckBox" } )
        addElement("Value Slider", "donothing","40",0x0, "",{  type="Slider", min=10, max=60})
        addElement("Gauge with last Slider Value", "donothing","40",0x0, "",{  type="Gauge", min=10, low=20 , optimum= 30, high=50 , max=60, unit=" km/h"} )
        addElement("Combobox", "donothing2","2",0x0, "",{  type="Combo", content={"one","two","three","four","five","six"}} )
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

