--[[
OOBD.lua
--]]

local BusID = "HS-CAN";


dofile("lib/serial_dxm.lua")
--dofile("lib/json.lua")

---------------------- Vehicle Info Menu --------------------------------------


function vin(oldvalue,id)
	echoWrite("0902\r")
	udsLen=receive()
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
---------------------- Clear Trouble Codes --------------------------------------


function clearDTC(oldvalue,id)
	echoWrite("04\r")
	return "Codes Deleted"
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

-- parameter taken from http://en.wikipedia.org/wiki/OBD-II_PIDs


local PID01CMDs = {
	------------------------ these are the lines copied from the "CalcNumPid" section in the OBD2_PIDs - OpenOffice- File
-------------- 1 section -----------------------------------------------
id0x4 = { byte = 1 , size =  1 , mult = 0.392156862745 , offset = 0, unit = " %"} ,
id0x5 = { byte = 1 , size =  1 , mult = 1 , offset = -40, unit = "°C"} ,
id0x6 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x7 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x8 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0x9 = { byte = 1 , size =  1 , mult = 0.78125 , offset = -100, unit = " %"} ,
id0xA = { byte = 1 , size =  1 , mult = 3 , offset = 0, unit = "kPa (gauge)"} ,
id0xB = { byte = 1 , size =  1 , mult = 1 , offset = 0, unit = "kPa (absolute)"} ,
id0xC = { byte = 1 , size =  2 , mult = 0.25 , offset = 0, unit = "rpm"} ,
id0xD = { byte = 1 , size =  1 , mult = 1 , offset = 0, unit = "km/h"} ,
id0xE = { byte = 1 , size =  1 , mult = 0.5 , offset = -65, unit = "° relative to #1 cylinder"} ,
id0xF = { byte = 1 , size =  1 , mult = 1 , offset = -40, unit = "°C"} ,
id0x10 = { byte = 1 , size =  2 , mult = 0.01 , offset = 0, unit = "g/s"} ,
id0x11 = { byte = 1 , size =  1 , mult = 0.392156862745 , offset = 0, unit = " %"} ,


id0x14 = { byte = 1 , size =  1 , mult = 0.5 , offset = 0, unit = "Volts"} ,
id0x114 = { byte = 2 , size =  1 , mult = 0.78125 , offset = -100, unit = "%"} ,
id0x15 = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x115 = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x16 = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x116 = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x17 = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x117 = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x18 = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x118 = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x19 = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x119 = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x1A = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x11A = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x1B = { byte = 1 , size = 1 , mult = 0.005 , offset = 0, unit = "Volts%"} ,
id0x11B = { byte = 2 , size = 1 , mult = 0.78125 , offset = -100, unit = "Volts%"} ,
id0x1F = { byte = 1 , size = 2 , mult = 1 , offset = 0, unit = "seconds"} ,
-------------- 2 section -----------------------------------------------
id0x21 = { byte = 1 , size = 2 , mult = 1 , offset = 0, unit = "km"} ,
id0x22 = { byte = 1 , size = 2 , mult = 0.078125 , offset = 0, unit = "kPa"} ,
id0x23 = { byte = 1 , size = 2 , mult = 10 , offset = 0, unit = "kPa (gauge)"} ,
id0x24 = { byte = 1 , size = 2 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x124 = { byte = 2 , size = 2 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x25 = { byte = 1 , size = 2 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x125 = { byte = 2 , size = 2 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x26 = { byte = 1 , size = 2 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x126 = { byte = 2 , size = 2 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x27 = { byte = 1 , size = 2 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x127 = { byte = 2 , size = 2 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x28 = { byte = 1 , size = 2 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x128 = { byte = 2 , size = 2 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
-------------- 3 section -----------------------------------------------
id0x29 = { byte = 1 , size = 1 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x129 = { byte = 1 , size = 1 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x2A = { byte = 1 , size = 1 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x12A = { byte = 1 , size = 1 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x2B = { byte = 1 , size = 1 , mult = 0.000030517578 , offset = 0, unit = "N/AV"} ,
id0x12B = { byte = 1 , size = 1 , mult = 0.000122070313 , offset = 0, unit = "N/AV"} ,
id0x2C = { byte = 1 , size = 1 , mult = 0.3921568627 , offset = 0, unit = "%"} ,
id0x2D = { byte = 1 , size = 1 , mult = 0.78125 , offset = -100, unit = "%"} ,
id0x2E = { byte = 1 , size = 1 , mult = 0.3921568627 , offset = 0, unit = "%"} ,
id0x2F = { byte = 1 , size = 1 , mult = 0.3921568627 , offset = 0, unit = "%"} ,
id0x30 = { byte = 1 , size = 1 , mult = 1 , offset = 0, unit = "N/A"} ,
id0x31 = { byte = 1 , size = 1 , mult = 0 , offset = 0, unit = "km"} ,
id0x32 = { byte = 1 , size = 1 , mult = 0.25 , offset = -8.192, unit = "Pa"} ,
id0x33 = { byte = 1 , size = 1 , mult = 1 , offset = 0, unit = "kPa (Absolute)"} ,
id0x34 = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x134 = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x35 = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x135 = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x36 = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x136 = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x37 = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x137 = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x38 = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x138 = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x39 = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x139 = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x3A = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x13A = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x3B = { byte = 1 , size = 1 , mult = 0.000030517578, offset = 0, unit = "N/A mA"} ,
id0x13B = { byte = 1 , size = 1 , mult = 0.00390625, offset = -128, unit = "N/A mA"} ,
id0x3C = { byte = 1 , size = 1 , mult = 0.1 , offset = -40, unit = "°C"} ,
id0x3D = { byte = 1 , size = 1 , mult = 0.1 , offset = -40, unit = "°C"} ,
id0x3E = { byte = 1 , size = 1 , mult = 0.1 , offset = -40, unit = "°C"} ,
id0x3F = { byte = 1 , size = 1 , mult = 0.1 , offset = -40, unit = "°C"} ,
id0x42 = { byte = 1 , size = 1 , mult = 0.001 , offset = 0, unit = "V"} ,
id0x43 = { byte = 1 , size = 1 , mult = 0.392156862745 , offset = 0, unit = "%"} ,
id0x44 = { byte = 1 , size = 1 , mult = 0 , offset = 0, unit = "N/A"} ,
	id0xFF = "dummy"

  }



function getNumPIDs(oldvalue,id)
        print(" anfang getNumPIDs")
	id = string.sub(id,3)  -- remove the leading 0x
	numID=tonumber(id,16)
        ascID= string.format("%x",numID % 256)
        print("ascID=",ascID,id)
	if #ascID % 2 == 1 then -- adding leading 0, if necessary
		ascID = "0"..ascID
	end
	echoWrite("01"..ascID.."\r")
	udsLen=receive()
        print("Nach UDSsend")
	if udsLen>0 then
		if udsBuffer[1]==65 then
			res=""
			-- having the functions hashed by the id.
                        print("Id",id)
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
      smallId= id % 256
      if smallId > 31 then smallId = smallId % 31 end
      byteNr=(smallId - (smallId % 8))/8
      bitNr = 7- (smallId - byteNr *8)
      if hasBit(availPIDs[byteNr], bitNr) then
                idstring=string.format("%X",id)
                print("Id-String=",idstring,id)
		addElement(title, func,"-",0x6, "0x"..string.format("%X",id))

	end
end


function createCMD01Menu(oldvalue,id)
	echoWrite("0100\r")
      --echoWrite("0120\r")
      --echoWrite("0140\r")

	udsLen=receive()
	if udsLen>0 then
		if udsBuffer[1]==65 then
			availPIDs={}
			for i = 0 , 3, 1 do -- get the bit field, which PIDs are available
				availPIDs[i] = udsBuffer[3+i]
			end
			if availPIDs ~= 0 then
				openPage("Sensor Data")
				------------------------ these are the lines copied from the "createCall" section in the OBD2_PIDs - OpenOffice- File
				createCall(availPIDs, 0x4,"Calculated engine load value", "getNumPIDs")
				createCall(availPIDs, 0x5,"Engine coolant temperature", "getNumPIDs")
				createCall(availPIDs, 0x6,"Short term fuel % trim—Bank 1", "getNumPIDs")
				createCall(availPIDs, 0x7,"Long term fuel % trim—Bank 1", "getNumPIDs")
				createCall(availPIDs, 0x8,"Short term fuel % trim—Bank 2", "getNumPIDs")
				createCall(availPIDs, 0x9,"Long term fuel % trim—Bank 2", "getNumPIDs")
				createCall(availPIDs, 0xA,"Fuel pressure", "getNumPIDs")
				createCall(availPIDs, 0xB,"Intake manifold absolute pressure", "getNumPIDs")
				createCall(availPIDs, 0xC,"Engine RPM", "getNumPIDs")
				createCall(availPIDs, 0xD,"Vehicle speed", "getNumPIDs")
				createCall(availPIDs, 0xE,"Timing advance", "getNumPIDs")
				createCall(availPIDs, 0xF,"Intake air temperature", "getNumPIDs")
				createCall(availPIDs, 0x10,"MAF air flow rate", "getNumPIDs")
				createCall(availPIDs, 0x11,"Throttle position", "getNumPIDs")
				createCall(availPIDs, 0x14,"Bank 1, Sensor 1: Oxygen sensor voltage", "getNumPIDs")
				createCall(availPIDs, 0x114,"Bank 1, Sensor 1: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x15,"Bank 1, Sensor 2: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x115,"Bank 1, Sensor 2: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x16,"Bank 1, Sensor 3: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x116,"Bank 1, Sensor 3: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x17,"Bank 1, Sensor 4: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x117,"Bank 1, Sensor 4: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x18,"Bank 2, Sensor 1: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x118,"Bank 2, Sensor 1: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x19,"Bank 2, Sensor 2: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x119,"Bank 2, Sensor 2: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x1A,"Bank 2, Sensor 3: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x11A,"Bank 2, Sensor 3: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x1B,"Bank 2, Sensor 4: Oxygen sensor voltage", "getNumPIDs")
                                createCall(availPIDs, 0x11B,"Bank 2, Sensor 4: Short term fuel trim", "getNumPIDs")
                                createCall(availPIDs, 0x1F,"Run time since engine start", "getNumPIDs")

				-----------------------------------------
				addElement("<<< Main", "Start","<<<",0x10, "")
				pageDone()
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

function createCMD02Menu(oldvalue,id)
      echoWrite("0120\r")
      --echoWrite("0140\r")

	udsLen=receive()
	if udsLen>0 then
		if udsBuffer[1]==65 then
			availPIDs={}
			for i = 0 , 3, 1 do -- get the bit field, which PIDs are available
				availPIDs[i] = udsBuffer[3+i]
			end
			if availPIDs ~= 0 then
				openPage("Snapshots")
				------------------------ these are the lines copied from the "createCall" 2 section in the OBD2_PIDs - OpenOffice- File

                                createCall(availPIDs, 0x21,"Distance traveled with malfunction indicator lamp (MIL) on", "getNumPIDs")
                                createCall(availPIDs, 0x22,"Fuel Rail Pressure (relative to mainfold vacuum)", "getNumPIDs")
                                createCall(availPIDs, 0x23,"Fuel Rail Pressure (diesel)", "getNumPIDs")
                                createCall(availPIDs, 0x24,"O2S1_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x124,"O2S1_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x25,"O2S2_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x125,"O2S2_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x26,"O2S3_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x126,"O2S3_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x27,"O2S4_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x127,"O2S4_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x28,"O2S5_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x128,"O2S5_WR_lambda(1): Equivalence Ratio Voltage", "getNumPIDs")
				-----------------------------------------
				addElement("<<< Main", "Start","<<<",0x10, "")
				pageDone()
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

function createCMD03Menu(oldvalue,id)

     echoWrite("0140\r")

	udsLen=receive()
	if udsLen>0 then
		if udsBuffer[1]==65 then
			availPIDs={}
			for i = 0 , 3, 1 do -- get the bit field, which PIDs are available
				availPIDs[i] = udsBuffer[3+i]
			end
			if availPIDs ~= 0 then
				openPage("CMD 03 PIDs")
				------------------------ these are the lines copied from the "createCall" 3 section in the OBD2_PIDs - OpenOffice- File
                                createCall(availPIDs, 0x29,"O2S6_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x129,"Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x2A,"O2S7_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x12A,"Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x2B,"O2S8_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x12B,"Equivalence Ratio Voltage", "getNumPIDs")
                                createCall(availPIDs, 0x2C,"Commanded EGR", "getNumPIDs")
                                createCall(availPIDs, 0x2D,"EGR Error", "getNumPIDs")
                                createCall(availPIDs, 0x2E,"Commanded evaporative purge", "getNumPIDs")
                                createCall(availPIDs, 0x2F,"Fuel Level Input", "getNumPIDs")
                                createCall(availPIDs, 0x30,"# of warm-ups since codes cleared", "getNumPIDs")
                                createCall(availPIDs, 0x31,"Distance traveled since codes cleared", "getNumPIDs")
                                createCall(availPIDs, 0x32,"Evap. System Vapor Pressure", "getNumPIDs")
                                createCall(availPIDs, 0x33,"Barometic pressure", "getNumPIDs")
                                createCall(availPIDs, 0x34,"O2S1_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x134,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x35,"O2S2_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x135,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x36,"O2S3_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x136,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x37,"O2S4_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x137,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x38,"O2S5_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x138,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x39,"O2S6_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x139,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x3A,"O2S7_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x13A,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x3B,"O2S8_WR_lambda(1)", "getNumPIDs")
                                createCall(availPIDs, 0x13B,"Equivalence Ratio Current", "getNumPIDs")
                                createCall(availPIDs, 0x3C,"Catalyst Temperature Bank 1, Sensor 1", "getNumPIDs")
                                createCall(availPIDs, 0x3D,"Catalyst Temperature Bank 2, Sensor 1", "getNumPIDs")
                                createCall(availPIDs, 0x3E,"Catalyst Temperature Bank 1, Sensor 2", "getNumPIDs")
                                createCall(availPIDs, 0x3F,"Catalyst Temperature Bank 2, Sensor 2", "getNumPIDs")
                        	-----------------------------------------
				addElement("<<< Main", "Start","<<<",0x10, "")
				pageDone()
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


---------------------- Trouble Codes Menu --------------------------------------



function showdtcs(oldvalue,id)
	echoWrite("03\r")
	udsLen=receive()
	  print ( "udsLen: ", udsLen)
	if udsLen>0 then
		if udsBuffer[1]==67 then
			local nrOfDTC= udsBuffer[2]
			if nrOfDTC *2 +2 > udsLen then
				return "Format Error"
			else
				if nrOfDTC> 0 then
					local i = 0
					for i = 1 , nrOfDTC, 1 do
						DTCCode=translateDTC(udsBuffer[2*i + 1],udsBuffer[2*i + 2])
						if dbLookup ~= null then
							newArray= dbLookup("dtc.oodb",DTCCode)
							if newArray.len > 0 then
								index=tostring(newArray.header["Description"])
								DTCCode= "("..DTCCode..") " .. newArray.data["1"][index]
							else
							    print ("DB error :", newArray.len)
							end
						end
						serDisplayWrite(tostring(i)..": "..DTCCode)
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


---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	identifyOOBDInterface()
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	deactivateBus()
	return oldvalue
end

function Main(oldvalue,id)
	deactivateBus()
	setBus(BusID)
	setSendID("$7E8") -- set not UDS compatible sender (=answer) address for OOBD firmware
	setBus("HS-CAN")
	openPage("OOBD-ME Main")
	addElement("Sensor Data >", "createCMD01Menu",">>>",0x1, "")
        addElement("Snapshot Data >", "createCMD02Menu",">>>",0x1, "")
        addElement("Dynamic Menu3 >", "createCMD03Menu",">>>",0x1, "")
	addElement("Trouble Codes", "showdtcs","-",0x0, "")
	addElement("VIN Number", "vin","-",0x2, "")
	addElement("Clear Trouble Codes", "clearDTC","-",0x0, "")
	addElement("System Info >>>", "SysInfo_Menu",">>>",0x1, "")
	addElement("Greetings", "greet","",0x0, "")
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

