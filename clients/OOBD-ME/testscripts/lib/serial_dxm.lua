-- include the basic connectivity



--- use the following lines for debugging in lua editor
---[[
openPage = openPageCall
addElement = addElementCall
pageDone = pageDoneCall
--]]
---[[
serFlush = serFlushCall
serWrite = serWriteCall
serWait = serWaitCall
serReadLn = serReadLnCall
serDisplayWrite = serDisplayWriteCall
--]]

--[[
readcount= 1
input = {}
input[1]="OOBD D2 212"
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
receive = null
setTimeout = null
setSendID =null
hardwareID =""

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
  if hNibble ==  0 then start = "P0" end
  if hNibble ==  1 then start = "P1" end
  if hNibble ==  2 then start = "P2" end
  if hNibble ==  3 then start = "P3" end
  if hNibble ==  4 then start = "C0" end
  if hNibble ==  5 then start = "C1" end
  if hNibble ==  6 then start = "C2" end
  if hNibble ==  7 then start = "C3" end
  if hNibble ==  8 then start = "B0" end
  if hNibble ==  9 then start = "B1" end
  if hNibble == 10 then start = "B2" end
  if hNibble == 11 then start = "B3" end
  if hNibble == 12 then start = "U0" end
  if hNibble == 13 then start = "U1" end
  if hNibble == 14 then start = "U2" end
  if hNibble == 15 then start = "U3" end
  return start..string.format("%X",lNibble)..string.format("%02X",lowByte)
end


function notyet(oldvalue,id)
	return "not implemented yet"
end

function nothing(oldvalue,id)
	return oldvalue
end

-- set response timeout in 10ms units - only needed for OOBD 
function setTimeout_OOBD(timeout)
  echoWrite("p 7 "..timeout.."\r\n")
end


-- set sender address - only needed for OOBD 
function setSendID_OOBD(addr)
  echoWrite("p 16 "..addr.."\r\n")
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
	answ=serReadLn(500, true)
	if answ == "" then
		return 0
	else
		doLoop = true
		byteCount= udsLen+1 -- auf Abbruchbedingung setzen
		while doLoop and  answ ~="" do
			nChar = string.byte(answ)
			if nChar >=48 and nChar <=57 then  -- an 1. Stelle steht eine Zahl-> positive Antwort
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
						-- die LÃ€ngenangabe
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
  if hardwareID == "OOBD" then
    echoWrite("p 11 $"..id.."\r\n")
  else
    echoWrite("atci "..id.."\r\n")
  end
end


function setBus(bus)
  if hardwareID == "OOBD" then
    if bus == "HS-CAN" then
      echoWrite("p 6 3\r\n")
    end
    if bus == "MS-CAN" then
      echoWrite("p 6 1\r\n")
    end
  else
    
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
	    if nChar >=48 and nChar <=57 then  -- an 1. Stelle steht eine Zahl-> positive Antwort
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
		answ=getStringPart(answ,2)
		udsLen=tonumber(answ) * -1 -- return error code as negative value
	      else
		if firstChar == "." or firstChar == ">" then -- end of data or promt
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
  if hardwareID == "OOBD" then
    echoWrite("p 0 0\r\n")
  else
    echoWrite("at!01\r\n")
  end
    answ=serReadLn(2000, true)
  return answ
end

function interface_serial(oldvalue,id)
  local answ=""
  if hardwareID == "OOBD" then
    echoWrite("p 0 1\r\n")
  else
    echoWrite("at!00\r\n")
  end
    answ=serReadLn(2000, true)
  return answ
end

function interface_voltage(oldvalue,id)
  local answ=""
  if hardwareID == "OOBD" then
    echoWrite("p 0 6\r\n")
    answ=serReadLn(2000, true)
		answ=round(getStringPart(answ, 1)/1000, 2)
		answ=answ.." Volt"
  else
    echoWrite("at!10\r\n")
    answ=serReadLn(2000, true)
 end
  return answ
end

function interface_bus(oldvalue,id)
  local answ=""
  if hardwareID == "OOBD" then
    echoWrite("p 0 5\r\n")
  else
	  echoWrite("0100\r\n") -- first send something to let the DXM search for a available bus
	  udsLen=receive()
	  echoWrite("atdp\r\n")
  end
    answ=serReadLn(2000, true)
  return answ
end


function identifyOOBDInterface()
	local answ=""
	-- Abfrage auf OOBD -interfae
	echoWrite("p 0 0 \r\n")
	answ=serReadLn(2000, false)
	print ("Busantwort:", answ)
	-- Antwort auseinanderfiedeln und Prüfen
	answ= getStringPart(answ, 1)
	print ("teilstring:",answ)
	if answ=="OOBD" then
	  receive = receive_OOBD
	  setTimeout = setTimeout_OOBD
	  setSendID = setSendID_OOBD
	  hardwareID="OOBD"
	  -- to support older OOBD firmware, set the Module-ID to functional address
	  echoWrite("p 11 $7DF\r\n")

	else
	  receive = receive_DXM
	  setTimeout = doNothing
	  setSendID = doNothing
	  hardwareID="DXM"
	end
	print ("Hardware found: ", hardwareID)
end

---------------------- System Info Menu --------------------------------------

function SysInfo_Menu(oldvalue,id)
	openPage("Sysinfo")
	addElement("DXM Serial", "interface_serial","-",0x2, "")
	addElement("DXM BIOS", "interface_version","-",0x2, "")
	addElement("Power", "interface_voltage","-",0x2, "")
	addElement("Which Bus?", "interface_bus","-",0x2, "")
	addElement("<<< Main", "Start","<<<",0x1, "")
	pageDone()
	return oldvalue
end

