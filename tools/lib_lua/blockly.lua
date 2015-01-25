--[[

blockly.lua

library to wrap the blockly function calls to the existing lua libraries 
(C) 2015 the OOBD team www.oobd.org

--]]



--[[
setDongleMode(Busmode , channel , protocol)

sets the bus channel, speed and active mode
--]]

function setDongleMode(busMode , busID , protocol)
	setBus(busID)
	if hardwareID == 2 then
		echoWrite("p 5 "..busMode.."\r")
	elseif hardwareID==3 or hardwareID==4 then
		echoWrite("p 8 2 "..busMode.."\r")
	end
	echoWrite("p 1 "..protocol.."\r") -- activate Diagnostic protocol
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
evalResult(valType , valStartBit , valBitLen , valOffset , valMult , valUnit)

clcualtes the result
--]]

function setModuleParams(id , timeout)
also die erste Frage ist, wie man das machen soll, wenn jede heutige Routine den DID nochmal schickt...

und am besten sollten die Nachschlagetabellen hier on the fly erzeugt und dann einfach an die Auswerteroutine übergeben werden??

Menu2Data = {
Menu2Data_NOP_D111 = { t = "ECU Power Supply Voltage", sev_r = "22", ses_r ="01;03;", call = "readNumPid",  sd = {
sd_00 = {  bpos = 0 , blen = 1 , Bpos = 0 , Blen = 8 , mult = 0.1 , offset = 0 , unit = "V" , dtype = "UNSIGNED" , t = "ECU Power Supply Voltage"} ,
dummy=0}
},
Menu2Data_NOP_F113 = { sev_r = "22", ses_r ="01;03;", t="Delivery Assy", call = "readAscPid", sd = {
sd_00 = {bpos = 1 , blen =  1 , mult = 0.392156862745 , offset = 0, unit = " %"},
dummy=0}
},

end



--------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- connect, so all neccesary (re-)initalisation needs to be done here


function Start(oldvalue,id)
	identifyOOBDInterface()
	Main(oldvalue,id)
	return oldvalue
end

-------------------------------------------------------------------------------------------------------------------------------
openPage('')
  addElement("Description of this Value" , '', "-", 0x00, )
pageDone()

function CallName(oldvalue,id)
local result = oldvalue
  setDongleMode("off" , "HS-CAN", "1")
  setModuleParams("7E0" , 50)
  echoWrite(''..''.."\r")
  local udsLen=receive()
  if udsLen>0 then
  if udsBuffer[1]==tonumber('',16) + 0x40  then
    result = result=evalResult("ascii" , tonumber('') , tonumber('') , tonumber('') , tonumber('') , '')

  elseif udsBuffer[1]== 0x7F then
  return string.format(getLocalePrintf("nrc",string.format("0x%x",udsBuffer[3]), "NRC: 0x%02X"),udsBuffer[3])
  else
  return "Error"
  end
  else
  return "NO DATA"
  end
return result
end


