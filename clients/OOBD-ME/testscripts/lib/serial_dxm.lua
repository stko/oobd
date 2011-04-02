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

function notyet(oldvalue,id)
	return "not implemented yet"
end

function nothing(oldvalue,id)
	return oldvalue
end

function echoWrite(text)
	serFlush()
	serWrite(text)
	serWait(text,2000)
end

-- the global receiving routine. Trys to read single- or multiframe answers from the dxm and stores it in udsbuffer, setting the received length in udslen

function receive_DXM()
	udsLen=0
	answ=""
	print("Receive via DXM...")
	answ=serReadLn(2000, true)
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
						serWait(">",2000)
					else
						answ=serReadLn(2000, true)
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
						answ=serReadLn(2000, true)

					end
				end
			else
				answ=serReadLn(2000, true)
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
      echoWrite("p 5 3\r\n")
    end
    if bus == "MS-CAN" then
      echoWrite("p 5 1\r\n")
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
		if firstChar == "." then -- end of data
		  print(" EOT detected")
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
    answ="not implemented yet"
  else
    echoWrite("at!10\r\n")
     answ=serReadLn(2000, true)
 end
  return answ
end

function interface_bus(oldvalue,id)
  local answ=""
  if hardwareID == "OOBD" then
    echoWrite("p 0 6\r\n")
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
	  hardwareID="OOBD"
	  -- to support older OOBD firmware, set the Module-ID to functional address
	  echoWrite("p 11 $7DF\r\n")

	else
	  receive = receive_DXM
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

