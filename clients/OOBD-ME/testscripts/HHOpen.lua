
-- include the basic connectivity

-- require 'dxm'

initCellTable = initCellTableCall
addCell = addCellCall
showCellTable = showCellTableCall

--- use the following lines for debugging in lua editor
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
input[1]="Searching"
input[2]="014"
input[3]="1: 49 02 02 41 42 43"
input[4]="2: 44 45 46 47 48 49 4A"
input[5]="3: 4B 4C 4D 4E 4F 50 51"
input[6]=">"
input[7]=""

function serReadLn()
	readcount=readcount +1
	return input[readcount -1]
end

function serWait()
	return 0
end

function serWrite()
end

function serFlush()
end
--]]

-- define the receive buffer

udsBuffer = {}
udslen =0

-- the global receiving routine. Trys to read single- or multiframe answers from the dxm and stores it in udsbuffer, setting the reveived length in udslen

function send()
	udsLen=0
	answ=""
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
						doLoop = false
					else -- Multiframe
						-- die Längenangabe
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

---------------------- System Info Menu --------------------------------------

function SysInfo_Menu(oldvalue,id)
	initCellTable()
	addCell("DXM Serial", "dxmserial","-",true , false, "")
	addCell("DXM BIOS", "dxmbios","-",true, false, "")
	addCell("Power", "power","-",true , false, "")
	addCell("<<< Main", "Menu_Main","<<<",false , false, "")
	showCellTable("Sysinfo")
	return oldvalue
end


function dxmserial(oldvalue,id)
	echoWrite("at!00\r\n")
	local answ=""
	answ=serReadLn(2000, false)
	return answ
end


function dxmbios(oldvalue,id)
	echoWrite("at!01\r\n")
	local answ=""
	answ=serReadLn(2000, false)
	return answ
end


function power(oldvalue,id)
	echoWrite("at!10\r\n")
	local answ=""
	answ=serReadLn(2000, false)
	return answ
end


---------------------- Vehicle Info Menu --------------------------------------

function VIN_Menu(oldvalue,id)
	initCellTable()
	addCell("VIN", "vin","-",true , false, "")
	addCell("<<< Main", "Menu_Main","<<<",false , false, "")
	showCellTable("Vehicle Info")
	return oldvalue
end


function vin(oldvalue,id)
	echoWrite("0902\r\n")
	udsLen=send()
	if udsLen>0 then
		if udsBuffer[1]==73 then
			local pos=4
			local res=""
			while pos <= udsLen and pos < 36 do
				if udsBuffer[pos]>31 then
					res=res..string.char(udsBuffer[pos])
				end
				pos= pos +1
			end
			return res
		else
			return "Error"
		end
	else
		return "NO DATA"
	end
end


---------------------- Sensor Data Menu --------------------------------------

function Sensor_Menu(oldvalue,id)
	initCellTable()
	addCell("RPM", "sens_rpm","-",true , false, "")
	addCell("<<< Main", "Menu_Main","<<<",false , false, "")
	showCellTable("Vehicle Info")
	return oldvalue
end


function sens_rpm(oldvalue,id)
	echoWrite("010C\r\n")
	udsLen=send()
	if udsLen>0 then
		if udsBuffer[1]==65 then
			local value=(udsBuffer[3]*256 +udsBuffer[4] )/ 4
			local res=tonumber(value)
			res= res.." RPM"
			return res
		else
			return "Error"
		end
	else
		return "NO DATA"
	end
end


---------------------- Trouble Codes Menu --------------------------------------



function showdtcs(oldvalue,id)
	echoWrite("03\r\n")
	udsLen=send()
	if udsLen>0 then
		if udsBuffer[1]==67 then
			local nrOfDTC= udsBuffer[2]
			if nrOfDTC *2 +2 > udsLen then
				return "Format Error"
			else
				if nrOfDTC> 0 then
					local i = 0
					for i = 1 , nrOfDTC, 1 do
						serDisplayWrite(tostring(i)..": P"..tostring(udsBuffer[2*i + 1],16)..tostring(udsBuffer[2*i + 2],16))
					end
				end
				return tostring(nrOfDTC).." DTC(s)"
			end
		else
			return "Error"
		end
	else
		return "NO DATA"
	end
end




---------------------- Greetings --------------------------------------

function greet(oldvalue,id)
	serDisplayWrite("Many Thanks to:")
	serDisplayWrite("")
	serDisplayWrite("Joseph Urhahne")
	serDisplayWrite("Wolfgang Sommer")
	serDisplayWrite("Axel Bullwinkel")
	serDisplayWrite("Uli Schmoll")
	serDisplayWrite("")
	serDisplayWrite("and to all the others,")
	serDisplayWrite("who made this possible")
	return oldvalue
end
---------------------- Main Menu --------------------------------------

function Menu_Main(oldvalue,id)
	initCellTable()
	addCell("Sensor Data >>>", "Sensor_Menu",">>>",false , false, "")
	addCell("Trouble Codes", "showdtcs","-",false, false, "")
	addCell("Vehicle Info >>>", "VIN_Menu",">>>",false , false, "")
	addCell("Protocol Info >>>", "notyet",">>>",false , false, "")
	addCell("Change ECU >>>", "notyet",">>>",false , false, "")
	addCell("System Info >>>", "SysInfo_Menu",">>>",false , false, "")
	addCell("Greetings", "greet","",false , false, "")
	showCellTable("HHOpen Main")
	return oldvalue
end

function notyet(oldvalue,id)
	return "not implemented yet"
end

function echoWrite(text)
	serFlush()
	serWrite(text)
	serWait(text,2000)
end

----------------- Do the initial settings --------------
Menu_Main("")
return

