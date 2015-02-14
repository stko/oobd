--[[

blockly.lua

library to wrap the blockly function calls to the existing lua libraries 
(C) 2015 the OOBD team www.oobd.org

--]]


dofile("serial_dxm.lua")
dofile("lua_utils.lua")
dofile("lib_uds.lua")



--[[
setDongleMode(Busmode , channel , protocol)

sets the bus channel, speed and active mode
--]]

function setDongleMode(busMode , busID , protocol)
	echoWrite("p 1 1 "..protocol.."\r") -- activate Diagnostic protocol
	setBus(busID)
	if hardwareID == 2 then
		echoWrite("p 5 "..busMode.."\r")
	elseif hardwareID==3 or hardwareID==4 then
		echoWrite("p 8 2 "..busMode.." 0 \r")
	end
end


--[[
setModuleParams(id , timeout)

sets the module id & timeout
--]]

function setModuleParams(id , timeout)
	setModuleID(id)				-- set ECU request CAN-ID
	setCANFilter(1,id,"7F0")	-- set CAN-Filter of ECU request/response CAN-ID range
	echoWrite('p 6 1 '..timeout.."\r")
end


--[[
evalResult(valService, valParams, valType , valStartBit , valBitLen , valOffset , valMult , valUnit)

calcualtes the result
--]]

tempParamTable={}

function evalResult(valService, valParams, valType , valStartBit , valBitLen , valOffset , valMult , valUnit)
  
  result="unknown call.."
  if valType == "ascii" then
    tempParamTable['tempParamTable_NOP_'..valParams]={ sev_r = valService,  t="dummy title", call = "readAscPid", sd = {
    sd_00 = {Bpos = valStartBit , Blen =  valBitLen , mult = valMult , offset = valOffset, unit = valUnit,  dtype = "UNSIGNED", t="dummy subtitle"}}}
    result=readAscPid("",'tempParamTable_NOP_'..valParams)
  end
  if valType == "bit" then
    bitvalue=Split(valUnit, "|")
    tempParamTable['tempParamTable_NOP_'..valParams]={ sev_r = valService, cmd = "id0x" .. valParams , t="dummy title", call = "readBMPPid",  sd = {
sd_00 = { by = math.floor(valStartBit / 8) , bi = math.floor(valBitLen / 8), lt = bitvalue[1], ht = bitvalue[2]}}}
    result=readBMPPid("",'tempParamTable_NOP_'..valParams.."_sd_00")
  end
  if valType == "numeric" then
    tempParamTable['tempParamTable_NOP_'..valParams]={ sev_r = valService,  t="dummy title", call = "readNumPid", sd = {
    sd_00 = {Bpos = valStartBit , Blen =  valBitLen , bpos = math.floor(valStartBit / 8), blen =math.floor(valBitLen / 8),  mult = valMult , offset = valOffset, unit = valUnit,  dtype = "UNSIGNED", t="dummy subtitle"}}}
    result=readNumPid("",'tempParamTable_NOP_'..valParams.."_sd_00")
  end
  return result
end





--------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- connect, so all neccesary (re-)initalisation needs to be done here


function Start(oldvalue,id)
	identifyOOBDInterface()
	Main(oldvalue,id)
	return oldvalue
end

-------------------------------------------------------------------------------------------------------------------------------
