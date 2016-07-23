

dofile("../../tools/lib_lua/lua_utils.lua")
dofile("../../tools/lib_lua/serial_dxm.lua")
--dofile("../../../tools/lib_lua/lua_uds.lua")
dofile("../../tools/lib_lua/lua_uds.lua")





---------------------- dtcStatus --------------------------------------
-- returns "i" (inactive) for no connection, "n" (no) for no errors, "y" for some errors  "e" for a communication error 

function dtcStatus(oldvalue,id,updType)
	params=Split(id,"_")
--[[
	print( "dtcstatus id=" , id)
	print("updtype=" , updType )
	print("bus" , params[1])
	print("Module=" , params[2])
--]]
	moduleName= params[2]  -- that variable is needed by the dtc printout subroutine
	-- force re-set of the bus
	lastSwitchedBus=""
	setBus(params[1])
	activateBus()
	setModuleID(params[2])				-- set ECU request CAN-ID
	setCANFilter(1,params[2],"7F0")	-- set CAN-Filter of ECU request/response CAN-ID range
	if updType==1 then
		showDTC()
	end
	echoWrite("190102\r")
	--echoWrite("19018D\r")
	udsLen=receive()
	if udsLen>0 then
		if udsBuffer[1]==0x59 and udsLen> 5 then
			if udsBuffer[5] * 256 + udsBuffer[6] == 0 then
				return "n"
			else
				return "y"
			end
		else
			return "e"
		end
	else
		return "i"
	end	
end

---------------------- dtcStatusXML --------------------------------------
-- returns an XML string with the error details

function dtcStatusXML(oldvalue,id,updType)
	if updType==1  or true then
		params=Split(id,"_")
		moduleName= params[2]  -- that variable is needed by the dtc printout subroutine
		-- force re-set of the bus
		lastSwitchedBus=""
		setBus(params[1])
		activateBus()
		setModuleID(params[2])				-- set ECU request CAN-ID
--		echoWrite("p 6 1 100\r")
		setCANFilter(1,params[2],"7F0")	-- set CAN-Filter of ECU request/response CAN-ID range
		return getXmlDtc()
	end
end

---------------------- dtcStatusXML --------------------------------------
-- returns an XML string with the error details

function DeleteModuleDTC(oldvalue,id,updType)
	if updType==0  then
		params=Split(id,"_")
		moduleName= params[2]  -- that variable is needed by the dtc printout subroutine
		setBus(params[1])
		activateBus()
		setModuleID(params[2])				-- set ECU request CAN-ID
		setCANFilter(1,params[2],"7F0")	-- set CAN-Filter of ECU request/response CAN-ID range
		return deleteDTC()
	end
end

function Start(oldvalue,id)
	identifyOOBDInterface()
	-- the openPage call does nothing obvious, but it's needed to initialize some WSUIHandler data
	openPage("OOBD testing Demo Script")
	addElement("ECU DTC XML", "dtcStatusXML","-",0x0, "500b11_7E0")
	addElement("Cluster DTC Status", "dtcStatus","-",0x0, "500b11_7E0")
	pageDone()
	return oldvalue
end

function CloseScript(oldvalue,id)
	deactivateBus()
	return oldvalue
end



----------------- Do the initial settings --------------
Start("","")
return


