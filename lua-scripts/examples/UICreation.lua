

dofile("../../tools/lib_lua/serial_dxm.lua")

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
	addElement("Simple Element", "createCMD01Menu",">>>",0x0, "")
        addElement("Full blown table", "createCMD02Menu",">>>",0x1, "",{  sev_r = "22", cmd = "id0xFEFD" , bus = "MS-CAN" , mid = "740" ,  title="DDM s/w WERS", call = "readAscPid", av = {
id0xFEFD00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192},
dummy=0}
})
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

