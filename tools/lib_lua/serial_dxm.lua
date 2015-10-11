dofile("luaSVNRevs.inc")
-- include the basic connectivity

--- use the following lines for debugging in lua editor
---[[
openPage = openPageCall
addElement = addElementCall
pageDone = pageDoneCall
openChannel = openChannelCall
dbLookup = dbLookupCall
--]]
---[[
serFlush = serFlushCall
serWrite = serWriteCall
serWait = serWaitCall
serReadLn = serReadLnCall
serDisplayWrite = serDisplayWriteCall
openXCVehicleData =openXCVehicleDataCall
ioInput = ioInputCall
ioRead = ioReadCall
ioWrite = ioWriteCall
--]]

--[[
readcount= 1
input = {}
input[1]="OOBD D2 212 Lux-Wolf Ostern"
input[2]="Searching"
input[3]="41 00 FF FF FF FF"
input[4]=">"
input[5]="41 14 FF FF FF FF"
input[6]=">"


function serReadLn()
	res= input[readcount]
	DEBUGPRINT("stko", 1, "serial_dxm.lua - serReadLn,%02d: %s %d, %d", "00", "read from input: ", res, readcount)
	readcount=readcount +1
	if readcount >#input then
		readcount = 1
	end
	return res
end

function serWait()
	return 0
end

function serWrite(data)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - serWrite,%02d: %s %d", "00", "Serwrite: ", data)
end

function serFlush()
end

--]]


--[[
function openPage(title)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - openPage,%02d: %s %s", "00", "Start Menu generation: ", title)
end

function addElement(title, func , intial,flags , id)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s", "00", "<---")
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s %s", "01", "title: ", title)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s %s", "02", "function: ", func)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s %s", "03", "id: ", id)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s", "00", "--->")
end

function pageDone()
	DEBUGPRINT("stko", 1, "serial_dxm.lua - pageDone,%02d: %s", "00", "Show Menu")
end



--]]

-- define the receive buffer

udsBuffer = {}
udslen =0
receive = nil
setTimeout = nil
setSendID = nil

-- actual hardware 
-- 0 ELM 
-- 1 DXM standard
-- 2 OOBD DXM 
-- 3 OOBD DXM w. CAN bus switcher (OOBD Cup v5)
-- 4 OOBD CAN Invader

hardwareID =0
firmware_revision=""
hardware_model=""
lastSwitchedBus=""



---------------------- Greetings --------------------------------------

function greet(oldvalue,id)
	serDisplayWrite("Thanks to")
	serDisplayWrite("")
	serDisplayWrite("Mike Luxen")
	serDisplayWrite("Joseph Urhahne")
	serDisplayWrite("Wolfgang Sauer")
	serDisplayWrite("Peter Mayer")
	serDisplayWrite("Axel Bullwinkel")
	serDisplayWrite("Uli Schmoll")
	serDisplayWrite("Wolfgang Sommer")
	serDisplayWrite("Günter Römer")
	serDisplayWrite("Ekkehard Pofahl")
	serDisplayWrite("Dennis Kurzweil")
	serDisplayWrite("Martin F.")
	serDisplayWrite("")
	serDisplayWrite("and to all the others,")
	serDisplayWrite("who made this possible")
	return oldvalue
end


function getStringPart(text, index)
	DEBUGPRINT("nexulm", 1, "serial_dxm.lua - getStringPart,%02d: %s", "00", "enter function getStringPart")
	local start, finish = string.find(text," ")
	local loop = 0
	local first = ""
	while loop < index and text ~="" do
		loop = loop + 1
		start, finish = string.find(text," ")
		if start ~=  nil then
			first=string.sub(text,1,finish-1)
			text=string.sub(text,finish+1)
			if first=="" then
				loop = loop -1 -- jump over additional blanks
			end
		else
			first = text
			text=""
		end
	end
	return first
end

function round(num, idp)
  return tonumber(string.format("%." .. (idp or 0) .. "f", num))
end

-- translate DTC bytes into their offical notation
-- as descripted on http://en.wikipedia.org/wiki/OBD-II_PIDs (30.4.11)

function translateDTC(highByte, lowByte)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - translateDTC,%02d: %s", "00", "enter function translateDTC")
	local hNibble= (highByte -(highByte % 16)) / 16 -- tricky to do an integer devide with luas float numbers..
	local lNibble =highByte % 16
	local start = "??"
	if hNibble ==  0 then start = "P0" 
	elseif hNibble ==  1 then start = "P1" 
	elseif hNibble ==  2 then start = "P2" 
	elseif hNibble ==  3 then start = "P3" 
	elseif hNibble ==  4 then start = "C0" 
	elseif hNibble ==  5 then start = "C1" 
	elseif hNibble ==  6 then start = "C2" 
	elseif hNibble ==  7 then start = "C3" 
	elseif hNibble ==  8 then start = "B0" 
	elseif hNibble ==  9 then start = "B1" 
	elseif hNibble == 10 then start = "B2" 
	elseif hNibble == 11 then start = "B3" 
	elseif hNibble == 12 then start = "U0" 
	elseif hNibble == 13 then start = "U1" 
	elseif hNibble == 14 then start = "U2" 
	elseif hNibble == 15 then start = "U3"
	end
	return start..string.format("%X",lNibble)..string.format("%02X",lowByte)
end

function notyet(oldvalue,id)
	return "not implemented yet"
end

function nothing(oldvalue,id)
	return oldvalue
end

function automaticBusSwitch()
	return hardwareID==4
end

-- set response timeout in 10ms units - only needed for OOBD 
function setTimeout_OOBD(timeout)
  if hardwareID==2 then
    echoWrite("p 7 "..timeout.."\r")
  elseif hardwareID==3 or hardwareID==4 then
    echoWrite("p 6 1 "..timeout.."\r")
  end
end

-- set sender address - only needed for OOBD 
function setSendID_OOBD(addr)
  if hardwareID==2 then
    echoWrite("p 16 $"..addr.."\r")
  elseif hardwareID==3 or hardwareID==4 then
    echoWrite("p 6 9 $"..addr.."\r")
  end
end

-- set CAN-ID filter - only needed for OOBD 
function setCANFilter_OOBD(id, addr, mask)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - setCANFilter_OOBD,%02d: %s", "00", "enter function setCANFilter_OOBD")
	if hardwareID==3 or hardwareID==4 then
		echoWrite ("p 8 14\r") -- reset CAN filter
		if tonumber(addr,16) <= 0x7FF  then
			echoWrite("p 8 10 "..id.." $0"..addr.."\r") -- use CAN-Filter No <id>, 11bit-CAN-ID <addr>
			echoWrite("p 8 11 "..id.." $0"..mask.."\r") -- use CAN-Filter No <id>, 11bit-CAN-IDMask <mask>
		else
			echoWrite("p 8 12 "..id.." $0"..addr.."\r") -- use CAN-Filter No <id>, 29bit-CAN-ID <addr>	
			echoWrite("p 8 13 "..id.." $0"..mask.."\r") -- use CAN-Filter No <id>, 29bit-CAN-IDMask <mask>
		end
	end
end

function doNothing(value)
-- do nothing
end

function echoWrite(text)
	serFlush()
	serWrite(text)
	serWait(text,500)
end

-- the global receiving routine. Trys to read single- or multiframe answers from the dxm and stores it in udsbuffer, setting the received length in udslen

function receive_DXM(timeOut)
	udsBuffer = {}
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - receive_DXM,%02d: %s", "00", "enter function receive_DXM")
	udsLen=0
	local timeOut =getPrefs("timeOut" , 1000)
	
	local answ=""
	DEBUGPRINT("stko", 1, "lua_utils.lua - receive_DXM,%02d: %s", "01", "Receive via DXM...")
	answ=serReadLn(timeOut, true)

	if answ == "" then
		return 0
	else
		local doLoop = true
		local byteCount= udsLen+1 -- auf Abbruchbedingung setzen
		while doLoop and  answ ~="" do
			local nChar = string.byte(answ)
			if (nChar >=48 and nChar <=57) or (nChar >=65 and nChar <=70) or (nChar >=97 and nChar <=102) then  -- an 1. Stelle steht eine Zahl-> positive Antwort
				if string.sub(answ,2,2) == ":" then
					answ = string.sub(answ,4) -- wegschneiden des Zaehlers am Anfang
					while byteCount <=udsLen  and answ ~="" do
						local byteStr= string.sub(answ,1,2)
						answ = string.sub(answ,4)
						udsBuffer[byteCount]=tonumber(byteStr,16)
						byteCount = byteCount + 1
					end
					if byteCount >=udsLen then
						doLoop=false
						serWait(">",500)
					else
						answ=serReadLn(500, true)
					end
				else
					if string.sub(answ,3,3) == " " then -- singleframe
						udsLen=1
						while answ ~="" do
							byteStr= string.sub(answ,1,2)
							answ = string.sub(answ,4)
							udsBuffer[udsLen]=tonumber(byteStr,16)
							udsLen = udsLen +1
						end
						udsLen = udsLen - 1
						doLoop = false
					else -- Multiframe
						-- die Längenangabe
						udsLen=tonumber(answ,16) or 0
						byteCount=1
						answ=serReadLn(500, true)
					end
				end
			else
				answ=serReadLn(500, true)
			end
		end
	end
	return  udsLen
end

function setModuleID(id)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - setModuleID,%02d: %s", "00", "enter function setModuleID")
	if hardwareID == 2 then
		echoWrite("p 11 $"..id.."\r")
	elseif hardwareID==3 or hardwareID==4 then
		echoWrite("p 6 5 $"..id.."\r")
	elseif hardwareID==1 then -- DXM 1
		echoWrite("atci "..id.."\r")
	else -- ELM327
		echoWrite("atsh "..id.."\r")
	end
end

function activateBus()
	if hardwareID == 2 then
		echoWrite("p 5 3\r")
	elseif hardwareID==3 or hardwareID==4 then
		echoWrite("p 8 2 3\r")
  end
end

function deactivateBus()
	if hardwareID == 2 then
		echoWrite("p 5 0\r")
	elseif hardwareID==3 or hardwareID==4 then
		echoWrite("p 8 2 0\r")
  end
end


--[[ setbus (bus)
set the CAN bus speed, frame size and port by a string
the format is as : (IMS-CAN | HS-CAN | CAN |MS-CAN | (speed  size)) [px]

where speed can be 500 , 250 or 125
size can be b11 or b29

px can be p1 or p2

per default only 125 is port 2, all other presets are port 1


if needed by the application, the settings calculated out of the strings can be
"manually" overwritten by the content of a global bustopology table


--]]

function setBus(bus)
	DEBUGPRINT("nexulm", 1, "serial_dxm.lua - setBus,%02d: %s", "00", "enter function setBus")
	local port =1 
	-- at first we separate the trailing port number, if given 
	local params=Split(bus,"p")
	bus=params[1]
	-- then we translate the legacy names bus into speed and  port
	if bus == "IMS-CAN" or bus == "HS-CAN" or bus == "CAN" then
		bus = "500b11"
	elseif bus == "MS-CAN" then
		bus = "125b11"
	end
	if bus=="125b11" then
		port=2
	end
	-- another port given instead of the default setting? Then overwrite it
	if params[2]~=nil then
		port=tonumber(params[2])
	end
	-- now comes another tricky part: Do we have a bus topology lookup table available?
	if bustopology ~= nil and shortName ~= nil and bustopology[shortName] ~= nil then
		bus=bustopology[shortName].speed
		port=bustopology[shortName].port
	end
	if lastSwitchedBus ~= bus..port then
		DEBUGPRINT("nexulm", 1, "serial_dxm.lua - setBus bus=>%s<, port=>%d<, hardwareID=%d", bus, port, hardwareID)
		
		if hardwareID == 0 then -- ELM327 
			if bus == "500b11" then
				echoWrite("at sp 6\r")
			elseif bus == "500b29" then
				echoWrite("at sp 7\r")
			elseif bus == "250b11" then
				echoWrite("at sp 8\r")
			elseif bus == "250b29" then
				echoWrite("at sp 9\r")
			elseif bus == "autoprobing" then
				echoWrite("0100\r") 	-- first send something to let the DXM1 and ELM327 search for an available bus
				serWait(">",3000) 		-- wait 3 secs for an automatic protocol detection process
			end
		elseif hardwareID == 1 then -- DXM1
			if bus == "500b11" then
				echoWrite("atp 6\r")
			elseif bus == "500b29" then
				echoWrite("atp 7\r")
			elseif bus == "250b11" then
				echoWrite("atp 8\r")
			elseif bus == "250b29" then
				echoWrite("atp 9\r")
			elseif bus == "autoprobing" then
				echoWrite("0100\r") 	-- first send something to let the DXM1 and ELM327 search for an available bus
				serWait(">",3000) 		-- wait 3 secs for an automatic protocol detection process
			end
		elseif hardwareID == 2 then  	-- Original DXM without relay
			if bus == "500b11" then
				echoWrite("p 6 3\r")
			end
			if bus == "125b11" then
				echoWrite("p 6 1\r")
			end
		elseif hardwareID == 3 or hardwareID == 4 then 
			if bus == "500b11" then
				port = 1
				echoWrite("p 8 3 3\r")
			elseif bus == "250b11" then
				echoWrite("p 8 3 2\r")
			elseif bus == "125b11" then
				echoWrite("p 8 3 1\r")
			elseif bus == "500b29" then
				echoWrite("p 8 3 7\r")
			elseif bus == "250b29" then
				echoWrite("p 8 3 6\r")
			end
		end

		if port == 1 then
			echoWrite("p 8 4 0\r")
		elseif port == 2 then
			echoWrite("p 8 4 1\r")
		end
		serWait(".|:",2000) -- wait 2 secs for an response	
		if port == nil then
			DEBUGPRINT("nexulm", 1, "serial_dxm.lua - setBus,%02d: %s=%d, port=%s, bus=%s", "02", "hardwareID ", hardwareID, "unavailable", bus)
		else
			DEBUGPRINT("nexulm", 1, "serial_dxm.lua - setBus,%02d: %s=%d, port=%d, bus=%s", "02", "hardwareID ", hardwareID, port, bus)
		end
		lastSwitchedBus = bus..port
	end
end

function receive_OOBD(timeOut)
	udsBuffer = {}
	DEBUGPRINT("nexulm", 1, "serial_dxm.lua - receive_OOBD,%02d: %s", "00", "enter function receive_OOBD")
		udsLen=0
		local timeOut =getPrefs("timeOut" , 2000)
		local answ=""
		answ=serReadLn(timeOut, true)
		if answ == "" then
			return -1
		else
			local doLoop = true
			while doLoop and  answ ~="" do
				local firstChar=string.sub(answ,1,1)
				local nChar = string.byte(answ)
				DEBUGPRINT("stko", 1, "serial_dxm.lua - receive_OOBD,%02d: %s %d %s %d", "01", "firstchar ", firstChar, " charcode ", nChar)
				if (nChar >=48 and nChar <=57) or (nChar >=65 and nChar <=70) or (nChar >=97 and nChar <=102) then  -- an 1. Stelle steht eine Zahl-> positive Antwort
				while  answ ~="" do
					local byteStr= string.sub(answ,1,2)
					answ = string.sub(answ,3)
					udsLen = udsLen + 1
					udsBuffer[udsLen]=tonumber(byteStr,16)
				end
				answ=serReadLn(timeOut, true)
			else
				if firstChar == ":" then -- error message
				doLoop= false
				answ=getStringPart(answ,3)
				udsLen=tonumber(answ) * -1 -- return error code as negative value
			else
				if firstChar == "." or firstChar == ">" then -- end of data or prompt
				doLoop = false
			else -- unknown data
			udsLen=-2
			doLoop = false
		end
	end
end
	  end
	end
	return  udsLen
end

function interface_version(oldvalue,id)
	local answ=""
	local err
	if hardwareID == 2 then
		echoWrite("p 0 0\r")
		answ=serReadLn(2000, true)
		return answ
	elseif hardwareID == 3 or hardwareID == 4 then
		echoWrite("p 0 0 0\r")
		err, answ = readAnswerArray()
		return answ[1]
	elseif hardwareID == 1 then -- DXM1 support
		echoWrite("at!01\r")
		answ=serReadLn(2000, true)
		return answ
	else -- ELM327 specific
		echoWrite("AT I\r")
		answ=serReadLn(2000, true)
		return answ
	end
end

function interface_serial(oldvalue,id)
  local answ=""
  local err
  if hardwareID == 2 then
	echoWrite("p 0 1\r")
	answ=serReadLn(2000, true)
	if answ ~= nil then
		return answ
	else
		return "not available"
	end
  elseif hardwareID == 3 or hardwareID == 4 then
	echoWrite("p 0 0 1\r") -- get BT-MAC address of OOBD-Cup v5 and OOBD CAN Invader
    err, answ = readAnswerArray()
	if answ[1] ~= nil then
		return answ[1]
	else
		return "not available"
	end
  elseif hardwareID == 1 then -- DXM1
	echoWrite("at!00\r")
    answ=serReadLn(2000, true)
	if answ ~= nil then
		return answ
	else
		return "not available"
	end
  else -- ELM327 specific
	echoWrite("at @2\r")
    answ=serReadLn(2000, true)
	if answ ~= nil then
		return answ
	else
		return "not available"
	end
  end
end

function interface_voltage(oldvalue,id)
  local answ=""
  local err
  if hardwareID == 2 then
    echoWrite("p 0 6\r")
    answ=serReadLn(2000, true)
    answ=round(getStringPart(answ, 1)/1000, 2)
    answ=answ.." Volt"
    return answ
  elseif hardwareID == 3 or hardwareID == 4 then
    echoWrite("p 0 0 2\r")
    err, answ = readAnswerArray()
    if err <0 then
      return answ[1]
    else
      answ=round(getStringPart(answ[1], 1)/1000, 2)
      answ=answ.." Volt"
      return answ
    end
  elseif hardwareID == 1 then   -- DXM1
    echoWrite("at!10\r")
    answ=serReadLn(2000, true)
	return answ
  else
	echoWrite("AT RV\r") -- ELM327 specific
    answ=serReadLn(2000, true)
	return answ
  end
end

function interface_bus(oldvalue,id)
  local answ=""
  local err
  if hardwareID == 2 then
    echoWrite("p 0 6\r")
    answ=serReadLn(2000, true)
    return answ
  elseif hardwareID == 3 or hardwareID == 4 then
    echoWrite("p 9 0 0\r")
    err, answ = readAnswerArray()
    return answ[1]
  else 
    echoWrite("atdp\r") -- show current used protocol
    answ=serReadLn(2000, true)
    return answ
  end
end

function interface_deviceID(oldvalue,id)
  local answ=""
  local err
  if hardwareID == 2 then  -- in case of using Original DXM1 Hardware with firmwar <= SVN 346
     echoWrite("p 0 8\r")
     answ=serReadLn(2000, true)
    return answ
  elseif hardwareID == 3 or hardwareID == 4 then -- in case of using OOBD Cup v5 and OOBD CAN Invader
    echoWrite("p 0 0 8\r") -- get device String i.e. OOBD-CIV xxxxxx of OOBDCup v5 and OOBD CAN Invader
    err, answ = readAnswerArray()
    return answ[1]
  elseif hardwareID == 1 then
    echoWrite("AT!00\r")  -- in case of original DXM1 Hard-/Software use serialnumber
    answ=serReadLn(2000, true)
    return answ
  else -- ELM327
    echoWrite("AT @2\r")  -- Read out ELM327 Device ID
    answ=serReadLn(2000, true)
	return answ
  end
end

function readAnswerArray()
  local res={}
  local answ=""
  answ=serReadLn(2000, true)
  while  answ ~="" and answ ~="." do
    firstChar=string.sub(answ,1,1)
    table.insert(res,answ)
    if firstChar == ":" then -- error message
      res={}
      table.insert(res,answ)
      answ=getStringPart(answ,3)
      return tonumber(answ) * -1 , res -- return error code as negative value
    end
    answ=serReadLn(2000, true)
  end
  return #res , res -- return nr of lines and answer array
end

function identifyOOBDInterface(connectURL)
	DEBUGPRINT("nexulm", 1, "serial_dxm.lua - identifyOOBDInterface,%02d: %s", "00", "enter function identifyOOBDInterface")
	local answ=""
	-- test for older software versions
	if openChannel ~= nil then
	  openChannel(connectURL)
	end 
	-- clean input queue
	echoWrite("\r\r\r")
	-- Abfrage auf OOBD -interface
	local sameAnswerCounter=0
	local repeatCounter=10 --maximal 10 trials in total
	local oldAnsw="nonsens"
	while  sameAnswerCounter<2 and repeatCounter>0 do -- 3 same ansers are needed 
		repeatCounter=repeatCounter-1
		echoWrite("p 0 0 \r")
		answ=serReadLn(2000, false)
		if answ==oldAnsw then
			sameAnswerCounter=sameAnswerCounter+1
		else
			sameAnswerCounter=0 -- no match? reset success counter
		end
		oldAnsw=answ
		DEBUGPRINT("stko", 1, "serial_dxm.lua - identifyOOBDInterface,%02d: %s %s", "01", "Busantwort: ", answ)
	end
	-- Antwort auseinanderfiedeln und Prüfen
	local idString= getStringPart(answ, 1)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - identifyOOBDInterface,%02d: %s %s", "01", "teilstring: ", idString)
	if idString=="OOBD" then
	  firmware_revision=getStringPart(answ, 3)
	  hardware_model=getStringPart(answ, 2)
	  hardware_variant=getStringPart(answ, 4)
	  receive = receive_OOBD
	  setTimeout = setTimeout_OOBD
	  setSendID = setSendID_OOBD
	  setCANFilter = setCANFilter_OOBD

	  --[[ Original DXM1, with old OOBD firmware <= Revision 346 ]]--
	  hardwareID=2
	  if hardware_model=="POSIX" or hardware_model=="D2" or hardware_model=="D2a" then
	    if hardware_variant=="POSIX" or hardware_variant=="dxm" then
		  --[[ Original DXM1, with new firmware paramater set > Revision 346 ]]--
		  hardwareID=3
		  echoWrite("p 1 1 1 0\r") -- activate Diagnostic protocol and initialize OOBD-Dongle
		elseif hardware_variant=="POSIX" or hardware_variant=="Lux-Wolf" then
			--[[ OOBD-Cup v5, new firmware paramater set ]]--
			hardwareID=4
			local err, res = readAnswerArray()
			if err ~=0 then
				DEBUGPRINT("stko", 4, "serial_dxm.lua - identifyOOBDInterface,%02d: %s %s %x", "02", "Set protocol error: ", err, res[1])
			end
			echoWrite("p 1 1 1 0\r") -- activate Diagnostic protocol and initialize OOBD-Dongle			
		else
			-- to support older OOBD firmware, set the Module-ID to functional address
			echoWrite("p 11 $7DF\r")
	    end
	  end
	else
		receive = receive_DXM
		setTimeout = doNothing
		setSendID = doNothing
		setCANFilter = doNothing
		hardwareID=1
		echoWrite("at i\r") -- request version
		answ=serReadLn(2000, false)
		hardware_variant=getStringPart(answ, 2)
	    if hardware_variant ~= "DXM1" then -- ELM327 specific
			hardwareID=0
			hardware_variant=getStringPart(answ, 1)
			firmware_revision=getStringPart(answ, 2)
		else -- DXM1 support
			firmware_revision=getStringPart(answ, 3)
			hardware_model=getStringPart(answ, 1)
		end
		echoWrite("atz\r")
--		echoWrite("0100\r") -- DXM/ELMxxx detected and perform automatic diagnostic protocol detection
--		serWait(">",3000) -- wait 3 secs for an automatic protocol detection process
	end
	DEBUGPRINT("stko", 1, "serial_dxm.lua - identifyOOBDInterface,%02d: %s %s %s %s %s %s %s %s", "02", "Hardware found: ", hardwareID, ", Revision: ", firmware_revision, ", Model: ", hardware_model, ", Variant: ", hardware_variant)
end

function getSVNLuaScript(oldvalue,id)
    return ""..SVNREVLUASCRIPT
end

function getSVNLuaLib(oldvalue,id)
    return ""..SVNREVLUALIB
end

function setResponsePendingTimeOut(timeOut)
	DEBUGPRINT("nexulm", 1, "serial_dxm.lua - setResponsePendingTimeOut,%02d: %s", "00", "enter function setResponsePendingTimeOut")
	
	if hardwareID == 3 or hardwareID == 4 then	-- set ResponsePendingTimeOut for OOBD Cup v5 or OOBD CAN Invader
		echoWrite("p 6 2 "..timeOut.."\r")
	end
end

---------------------- System Info Menu --------------------------------------

function SysInfo_Menu(oldvalue,id)
	openPage("Sysinfo")
	addElement("Lua Script Revision", "getSVNLuaScript", ""..SVNREVLUASCRIPT, 0x0, "")
	addElement("Lua Library Revision", "getSVNLuaLib", ""..SVNREVLUALIB, 0x0, "")
	addElement("Serial", "interface_serial", "-", 0x2, "")
	addElement("BIOS", "interface_version", "-", 0x2, "")
	addElement("Power", "interface_voltage", "-", 0x6, "")
	addElement("Which Bus?", "interface_bus", "-", 0x2, "")
	addElement("<<< Main", "Main", "<<<", 0x10, "")
	pageDone()
	return oldvalue
end

