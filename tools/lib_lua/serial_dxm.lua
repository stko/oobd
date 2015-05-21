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
	print ("read from input: ",res, readcount)
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
	print ("Serwrite:" ,data)
end

function serFlush()
end

--]]


--[[
function openPage(title)
	print("Start Menu generation ", title);
end

function addElement(title, func , intial,flags , id)
	print("<---");
	print("title: ", title);
	print("function: ", func);
	print("id: ", id);
	print("--->");
end

function pageDone()
	print("Show Menu");
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
-- 3 OOBD DXM w. bus swichter

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
  start, finish = string.find(text," ")
  loop = 0
  first = ""
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
  hNibble= (highByte -(highByte % 16)) / 16 -- tricky to do an integer devide with luas float numbers..
  lNibble =highByte % 16
  start = "??"
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
  elseif hNibble == 15 then start = "U3" end
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
  if hardwareID==3 or hardwareID==4 then
    echoWrite("p 8 10 "..id.." $0"..addr.."\r") -- use CAN-Filter No <id>, CAN-ID <addr>
	echoWrite("p 8 11 "..id.." $0"..mask.."\r") -- use CAN-Filter No <id>, CAN-IDMask <mask>
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

function receive_DXM()
	udsLen=0
	answ=""
	print("Receive via DXM...")
	answ=serReadLn(1000, true)
	if answ == "" then
		return 0
	else
		doLoop = true
		byteCount= udsLen+1 -- auf Abbruchbedingung setzen
		while doLoop and  answ ~="" do
			nChar = string.byte(answ)
			if (nChar >=48 and nChar <=57) or (nChar >=65 and nChar <=70) or (nChar >=97 and nChar <=102) then  -- an 1. Stelle steht eine Zahl-> positive Antwort
				if string.sub(answ,2,2) == ":" then
					answ = string.sub(answ,4) -- wegschneiden des Zaehlers am Anfang
					while byteCount <=udsLen  and answ ~="" do
						byteStr= string.sub(answ,1,2)
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
						udsLen=tonumber(answ,16)
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
  if hardwareID == 2 then
    echoWrite("p 11 $"..id.."\r")
  elseif hardwareID==3 or hardwareID==4 then
    echoWrite("p 6 5 $"..id.."\r")
  else
    echoWrite("atci "..id.."\r")
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

function setBus(bus)
  if lastSwitchedBus ~= bus then
	-- first we need to translate the bus into speed and  port
	if bus == "IMS-CAN" or bus == "HS-CAN" or bus == "CAN" then
		speed = "500b11"
	else
		speed = "125b11"
	end
	if speed == "500b11" then
		port=1
	else
		port=2
	end
	-- now comes another tricky part: Do we have a bus topology lookup table available?
	if bustopology ~= nil and shortName ~= nil and bustopology[shortName] ~= nil then
		speed=bustopology[shortName].speed
		port=bustopology[shortName].port
	end
    if hardwareID == 2 then
      if speed == "500b11" then
		echoWrite("p 6 3\r")
      end
     if speed == "125b11" then
		echoWrite("p 6 1\r")
      end
    elseif hardwareID == 3 then
      if speed == "500b11" then
		echoWrite("p 8 3 3\r")
      elseif speed == "125b11" then
		echoWrite("p 8 3 1\r")
      end
		serWait(".|:",2000) -- wait 2 secs for an response
    elseif hardwareID == 4 then
      if speed == "500b11" then
		echoWrite("p 8 3 3\r")
     elseif speed == "125b11" then
		echoWrite("p 8 3 1\r")
     end
      if port == 1 then
		echoWrite("p 8 4 0\r")
     elseif port == 2 then
		echoWrite("p 8 4 1\r")
      end
		serWait(".|:",2000) -- wait 2 secs for an response
    end
    lastSwitchedBus = bus
  end
end

function receive_OOBD()
	udsLen=0
	answ=""
	answ=serReadLn(2000, true)
	if answ == "" then
	  return -1
	else
	  doLoop = true
	  while doLoop and  answ ~="" do
	    firstChar=string.sub(answ,1,1)
	    nChar = string.byte(answ)
	    --print ("firstchar ",firstChar," charcode ",nChar)
	    if (nChar >=48 and nChar <=57) or (nChar >=65 and nChar <=70) or (nChar >=97 and nChar <=102) then  -- an 1. Stelle steht eine Zahl-> positive Antwort
	      while  answ ~="" do
		      byteStr= string.sub(answ,1,2)
		      answ = string.sub(answ,3)
		      udsLen = udsLen + 1
		      udsBuffer[udsLen]=tonumber(byteStr,16)
	      end
	      answ=serReadLn(2000, true)
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
  if hardwareID == 2 then
     echoWrite("p 0 1\r")
     answ=serReadLn(2000, true)
    return answ
  elseif hardwareID == 3 or hardwareID == 4 then
    echoWrite("p 0 0 1\r") -- get BT-MAC address of OOBD-Cup v5 and OOBD CAN Invader
    err, answ = readAnswerArray()
    return answ[1]
  elseif hardwareID == 1 then -- DXM1
	echoWrite("at!00\r")
    answ=serReadLn(2000, true)
	return answ
  else -- ELM327 specific
	echoWrite("at @2\r")
    answ=serReadLn(2000, true)
    return answ
  end
end

function interface_voltage(oldvalue,id)
  local answ=""
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
  if hardwareID == 2 then
    echoWrite("p 0 6\r")
    answ=serReadLn(2000, true)
    return answ
  elseif hardwareID == 3 or hardwareID == 4 then
    echoWrite("p 9 0 0\r")
    err, answ = readAnswerArray()
    return answ[1]
  else 
    echoWrite("0100\r") -- first send something to let the DXM1 and ELM327 search for a available bus
    udsLen=receive()
    echoWrite("atdp\r") -- show current used protocol
    answ=serReadLn(2000, true)
    return answ
  end
end

function interface_deviceID(oldvalue,id)
  local answ=""
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
	local answ=""
	-- test for older software versions
	if openChannel ~= nil then
	  openChannel(connectURL)
	end 
	-- clean input queue
	echoWrite("\r\r\r")
	-- Abfrage auf OOBD -interface
	sameAnswerCounter=0
	repeatCounter=10 --maximal 10 trials in total
	oldAnsw="nonsens"
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
		print ("Busantwort:", answ)
	end
	-- Antwort auseinanderfiedeln und Prüfen
	idString= getStringPart(answ, 1)
	print ("teilstring:",idString)
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
	      --[[		    echoWrite("p 0 1 1\r") -- set protocol
			  err, res = readAnswerArray()
			  if err ~=0 then
			    print (" Set protocol error:", err, res[1])
			  end
			  echoWrite("p 0 1 1\r") -- set Bus
			  err, res = readAnswerArray()
			  if err ~=0 then
			    print (" Set protocol error:", err, res[1])
			  end
	      --]]	  
		elseif hardware_variant=="POSIX" or hardware_variant=="Lux-Wolf" then
			--[[ OOBD-Cup v5, new firmware paramater set ]]--
			hardwareID=4
--			echoWrite("p 1 1 1 0\r") -- activate Diagnostic protocol
			err, res = readAnswerArray()
			if err ~=0 then
				print (" Set protocol error:", err, res[1])
			end        
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
			echoWrite("at sp 0\r") -- ELMxxx device expected and set protocol automatically
			serWait(">",2000) -- wait 3 secs for an automatic protocol detection process
		else -- DXM1 support
			firmware_revision=getStringPart(answ, 3)
			hardware_model=getStringPart(answ, 1)
		end

		echoWrite("0100\r") -- DXM/ELMxxx detected and perform automatic diagnostic protocol detection
		serWait(">",3000) -- wait 3 secs for an automatic protocol detection process
	end
	print ("Hardware found: ", hardwareID, "Revision: ",firmware_revision,"Model",hardware_model, "Variant", hardware_variant)
end

---------------------- System Info Menu --------------------------------------

function SysInfo_Menu(oldvalue,id)
	openPage("Sysinfo")
	addElement("Lua Script Revision", "nothing"," "..SVNREVLUASCRIPT,0x0, "")
	addElement("Lua Library Revision", "nothing"," "..SVNREVLUALIB,0x0, "")
	addElement("Serial", "interface_serial","-",0x2, "")
	addElement("BIOS", "interface_version","-",0x2, "")
	addElement("Power", "interface_voltage","-",0x6, "")
	addElement("Which Bus?", "interface_bus","-",0x2, "")
	addElement("<<< Main", "Main","<<<",0x10, "")
	pageDone()
	return oldvalue
end

