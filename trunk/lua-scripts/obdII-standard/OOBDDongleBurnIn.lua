local BusID = "HS-CAN";


dofile("../../tools/lib_lua/serial_dxm.lua")


function doBurnIn(oldvalue,id)
	local answ=""
	if hardwareID < 2 then
		return "No OOBD Dongle found"
	end
	echoWrite("p 1 2 0 1\r")
	answ=serReadLn(2000, true)
	if answ ~="." then
		return "Error while setting blue LED"
	end
	echoWrite("p 1 2 1 1\r")
	answ=serReadLn(2000, true)
	if answ ~="." then
		return "Error while setting green LED"
	end
	echoWrite("p 1 2 2 1\r")
	answ=serReadLn(2000, true)
	if answ ~="." then
		return "Error while setting red LED"
	end
	echoWrite("'p 8 4 1\r")
	answ=serReadLn(2000, true)
	if answ ~="." then
		return "Error while setting Relais"
	end
	echoWrite("'p 1 2 3 1000\r")
	answ=serReadLn(2000, true)
	if answ ~="." then
		return "Error while setting Buzzer"
	end
	return  'All on'
end

function doReset(oldvalue,id)
	local answ=""
	if hardwareID < 2 then
		return "No OOBD Dongle found"
	end
	echoWrite("p 0 99 0\r")
	return  'reset command sent'
end

---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	identifyOOBDInterface()
	
	-- do all the bus swttings in once
	deactivateBus()
	setBus(BusID)
	setModuleID("7DF") -- set legislated OBD/WWH-OBD functional request ID
	setCANFilter(1,"7E8","7F0") -- set CAN-Filter of ECU request/response CAN-ID range
	setSendID("7E8") 	-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - ECM, Engince Control Module)
	-- start the mail loop
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	deactivateBus()
	return oldvalue
end

function Main(oldvalue,id)
	openPage("OOBD Dongle Burn In")
	addElement("Activate all consumers", "doBurnIn","-",0x0, "")
	addElement("Reset Dongle", "doReset","-",0x0, "")
	addElement("System Info >>>", "SysInfo_Menu",">>>",0x1, "")
	addElement("Greetings", "greet","",0x0, "")
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

