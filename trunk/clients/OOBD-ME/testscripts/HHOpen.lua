--[[
HHOpen.lua
--]]



-- include the basic connectivity



--- use the following lines for debugging in lua editor
---[[
initCellTable = initCellTableCall
addCell = addCellCall
showCellTable = showCellTableCall
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
input[1]="Searching"
input[2]="41 00 E8 19 30 12"
input[3]=">"


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
function initCellTable()
	print("Start Menu generation");
end

function addCell(title, func , intial,update , timer , id)
	print("<---");
	print("title: ", title);
	print("function: ", func);
	print("id: ", id);
	print("--->");
end

function showCellTable(title)
	print("Show Menu", title);
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
						udsLen = udsLen - 1
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

function CalcNumPid( byteNr , nrOfBytes, multiplier, offset, unit)
	value=0
	for i = 0 , nrOfBytes -1, 1 do -- get raw value
		value = value * 256 + udsBuffer[ 2 + byteNr + i]
	end
	-- do the calculation
	value = value * multiplier + offset
	return value ..unit
end




-- here it becomes tricky: store the necessary function calls as a id- indexed hash table

local PID01CMDs = {
	------------------------ these are the lines copied from the "CalcNumPid" section in the OBD2_PIDs - OpenOffice- File
id0x4 = { byte = 1 , size =  1 , mult = 0.392156862745 , offset = 0, unit = " %"} ,
id0x5 = { byte = 1 , size =  1 , mult = 1 , offset = -40, unit = "°C"} ,
id0x6 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x7 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x8 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x9 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x0A = { byte = 1 , size =  1 , mult = 3 , offset = 0, unit = "kPa (gauge)"} ,
id0x0B = { byte = 1 , size =  1 , mult = 1 , offset = 0, unit = "kPa (absolute)"} ,
id0x0C = { byte = 1 , size =  2 , mult = 0.25 , offset = 0, unit = "rpm"} ,
id0x0D = { byte = 1 , size =  1 , mult = 1 , offset = 0, unit = "km/h"} ,
id0x0E = { byte = 1 , size =  1 , mult = 0.5 , offset = -65, unit = "° relative to #1 cylinder"} ,
id0x0F = { byte = 1 , size =  1 , mult = 1 , offset = -40, unit = "°C"} ,
id0x10 = { byte = 1 , size =  2 , mult = 0.01 , offset = 0, unit = "g/s"} ,
id0x11 = { byte = 1 , size =  1 , mult = 0.392156862745 , offset = 0, unit = " %"} ,


id0x14 = { byte = 1 , size =  1 , mult = 0.5 , offset = 0, unit = "Volts"} ,
id0x114 = { byte = 2 , size =  1 , mult = 0.78125 , offset = -100, unit = "%"} ,
    	-------------
	id0xFF = "dummy"

  }



function getNumPIDs(oldvalue,id)
	id = string.sub(id,3)  -- remove the leading 0x
	numID=tonumber(id)
	ascID=tostring(numID % 256 , 16 )
	if #ascID % 2 == 1 then -- adding leading 0, if necessary
		ascID = "0"..ascID
	end
	echoWrite("01"..ascID.."\r\n")
	udsLen=send()
	if udsLen>0 then
		if udsBuffer[1]==65 then
			res=""
			-- having the functions hashed by the id.		
			paramList=PID01CMDs["id0x"..id]
			res= paramList ~= null and CalcNumPid( paramList.byte , paramList.size , paramList.mult , paramList.offset, paramList.unit)  or "index error"
			return res
		else
			return "Error"
		end
	else
		return "NO DATA"
	end
end


function hasBit(value, bitNr) -- bitNr starts with 0
	bitValue=2 ^ bitNr
	return value % (bitValue + bitValue) >= bitValue -- found in the internet: nice trick to do bitoperations in Lua, which does not have such functions by default
end


function createCall(availPIDs, id, title, func)
	if hasBit(availPIDs, 20- (id % 256)) then
		addCell(title, func,"-",true , false, "0x"..tostring(id,16))
	end
end


function createCMD01Menu(oldvalue,id)
	echoWrite("0100\r\n")
	udsLen=send()
	if udsLen>0 then
		if udsBuffer[1]==65 then
			availPIDs=0
			for i = 0 , 3, 1 do -- get the bit field, which PIDs are available
				availPIDs=availPIDs*256 +udsBuffer[3+i]
			end
			if availPIDs ~= 0 then
				initCellTable()
				------------------------ these are the lines copied from the "createCall" section in the OBD2_PIDs - OpenOffice- File
				createCall(availPIDs, 0x4,"Calculated engine load value", "getNumPIDs")
				createCall(availPIDs, 0x5,"Engine coolant temperature", "getNumPIDs")
				createCall(availPIDs, 0x6,"Short term fuel % trim—Bank 1", "getNumPIDs")
				createCall(availPIDs, 0x7,"Long term fuel % trim—Bank 1", "getNumPIDs")
				createCall(availPIDs, 0x8,"Short term fuel % trim—Bank 2", "getNumPIDs")
				createCall(availPIDs, 0x9,"Long term fuel % trim—Bank 2", "getNumPIDs")
				createCall(availPIDs, 0x0A,"Fuel pressure", "getNumPIDs")
				createCall(availPIDs, 0x0B,"Intake manifold absolute pressure", "getNumPIDs")
				createCall(availPIDs, 0x0C,"Engine RPM", "getNumPIDs")
				createCall(availPIDs, 0x0D,"Vehicle speed", "getNumPIDs")
				createCall(availPIDs, 0x0E,"Timing advance", "getNumPIDs")
				createCall(availPIDs, 0x0F,"Intake air temperature", "getNumPIDs")
				createCall(availPIDs, 0x10,"MAF air flow rate", "getNumPIDs")
				createCall(availPIDs, 0x11,"Throttle position", "getNumPIDs")
				createCall(availPIDs, 0x14,"Bank 1, Sensor 1: Oxygen sensor voltage", "getNumPIDs")
				createCall(availPIDs, 0x114,"Bank 1, Sensor 1: Short term fuel trim", "getNumPIDs")
				-----------------------------------------
				showCellTable("CMD 01 PIDs")
				return oldvalue
			else
				return "No avail. PIDs found"
			end

		else
			return "Error"
		end
	else
		return "NO DATA"
	end
end


function Sensor_Menu(oldvalue,id)
	initCellTable()
	addCell("RPM", "sens_rpm","-",true , false, "")
	addCell("Dynamic Menu >>>", "createCMD01Menu",">>>",true , false, "")
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
--createCMD01Menu("",0)
--print (getNumPIDs("","0x5"))
return

