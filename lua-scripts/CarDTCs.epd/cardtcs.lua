

dofile("../../tools/lib_lua/lua_utils.lua")
dofile("../../tools/lib_lua/serial_dxm.lua")
--dofile("../../../tools/lib_lua/lua_uds.lua")
dofile("../../tools/lib_lua/lua_uds.lua")


moduleBusTable={}


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
		bus=moduleBusTable[id]
		if bus ~= none then
			moduleName= id  -- that variable is needed by the dtc printout subroutine
			-- force re-set of the bus
			-- warum?? lastSwitchedBus=""
			setBus(bus)
			activateBus()
			setModuleID(id)				-- set ECU request CAN-ID
	--		echoWrite("p 6 1 100\r")
			setCANFilter(1,id,"700")	-- set CAN-Filter of ECU request/response CAN-ID range
			return getXmlDtc()
		end
	end
end

---------------------- dtcStatusXML --------------------------------------
-- returns an XML string with the error details

function DeleteModuleDTC(oldvalue,id,updType)
	if updType==1  or true then
		bus=moduleBusTable[id]
		if bus ~= none then
			moduleName= id  -- that variable is needed by the dtc printout subroutine
			-- force re-set of the bus
			-- warum?? lastSwitchedBus=""
			setBus(bus)
			activateBus()
			setModuleID(id)				-- set ECU request CAN-ID
	--		echoWrite("p 6 1 100\r")
			setCANFilter(1,id,"700")	-- set CAN-Filter of ECU request/response CAN-ID range
			return deleteDTC()
		end
	end
end

function pingSingleBus(bus, result)
	setBus(bus)
	activateBus()
	setModuleID("7DF")				-- set broadcast  ECU request CAN-ID
	setCANFilter(1,"700","700")	-- set CAN-Filter of ECU request/response CAN-ID range
	getCmdSuccess({6,1,10,0})	-- timeout 100ms
	if getCmdSuccess({6,11,1,0}) then
	--if true then
		echoWrite("190102\r")
		print ("pingbus", bus)
		--echoWrite("19018D\r")
		udsLen=receive()
		if udsLen>0 and udsLen % 12 == 0 then
			local i=0
			while i < udsLen do
				local i2
				local id =0
				for i2 = 0 , 3 do
					id=id * 256 + udsBuffer[i+i2+1] --remember: a lua array starts with 1..
				end
				local errCnt =0
				for i2 = 0 , 2 do
					errCnt=errCnt * 256 + udsBuffer[i+i2+9] --remember: a lua array starts with 1..
				end
				local modulID=string.format("%02X",id-8) -- -8 to get the original module address back
				result[modulID ] = errCnt
				moduleBusTable[modulID]=bus
				i=i+12
			end
			--if udsBuffer[1]==0x59 and udsLen> 5 then
			--end
			print ("pingbus received", udsLen)
		end	
	end
	return result
end


function Scanbus(oldvalue,id)
	params=Split(oldvalue,"_")
	local result= { }
	result=pingSingleBus("500b11p1",result)
	if params[2]=="bushm" then
		result=pingSingleBus("125b11p2",result)
	end
	if params[2]=="bushh" then
		result=pingSingleBus("500b11p2",result)
	end
	return "1", result
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


