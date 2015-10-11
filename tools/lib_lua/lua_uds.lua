local actBusId
local actModulId
local stdTimeout = 1000
local stdBuffer = 500
local strID_NoSecureCode = "NoSecureCode"
local strID_DidNotDefined = "strID_DidNotDefined"
local strID_AnswerToShort = "strID_AnswerToShort"

--[[
Sources:
UDS Services: https://de.wikipedia.org/wiki/Unified_Diagnostic_Services
Response Error: https://www.emotive.de/doc/car-diagnostic-systems/protocols/dp
--]]

-- for better readibility, creating some GLOBAL constants
udsService_Read_Data_By_LocalIdentifier = "21"
udsService_Read_Data_By_Identifier = "22"
udsService_Read_DTC = "19"
udsService_Response = 0x40
udsService_Error = 0x7f
udsService_Input_Output_Control_By_Identifier = "2F"
udsService_Clear_Diagnostic_Information ="14"
udsService_Routine_Control = "31"


--[[
udsServiceRequest : helper function to cover the standard, always reoccuring part of a service request. 

params:
service: the service to be called as hex string (like "22")

did: the DiD service parameter as hex 

bufferTime: time in ms how long a previously received telegram with that DiD is still seen as valid and not requested again.
Senseful to avoid short term re-requests of the same DID, where the values have not changed in the meantime

handler((service, did, udsLen, udsBuffer)): Callback function, which is called when the Did got a positive response code


If any error occours, udsServiceRequest returns standard error texts and error codes for general response errors or missing answer from moduleName


return

udsServiceRequest() returns up to TWO (2!) values. The first one is a string, the second one an optional error code. Everything which is <0 is an error



--]]

function udsServiceRequest( service, did, bufferTime, handler)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - udsServiceRequest,%02d: %s", "00", "enter function udsServiceRequest")
	echoWrite(service..did.."\r") 
	udsLen=receive()
	if udsLen>0 then
		if udsBuffer[1]== tonumber(service,16)+udsService_Response then
			if handler ~= nil then
				return handler(service, did, udsLen, udsBuffer)
			else
				return "", 0
			end
		elseif udsBuffer[1]== udsService_Error then
			return string.format(getLocalePrintf("nrc",string.format("0x%x",udsBuffer[3]), "NRC: 0x%02X"),udsBuffer[3]), -1
		else
			return "Error" , -2
		end
	else
		return  "No Data received", -3
	end
end




--[[
function readBMPDiDByTable(value, id)
	return readBMPDiD( string.sub(value.cmd,7) , value.by, value.bi , value.lt , value.ht ) 
end
]]--

function readBMPDiD(oldvalue, id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - readBMPDiD,%02d: %s", "00", "enter function readBMPDiD")
	local bytepos = 4;

	local did, data = translateTableID( id )
	local tabid = Split(id, "_") -- split id to it's pieces
	local subdata = data[tabid[4]][tabid[4].."_"..tabid[5]]

	if data.sev_r == nil then  -- check if data.sev_r is missing
		data.sev_r = udsService_Read_Data_By_Identifier;
	end
 
	return udsServiceRequest(data.sev_r,did ,0 , function ()
		if udsLen > subdata.by then
			if hasBit(udsBuffer[subdata.by+bytepos], subdata.bi) then
				return subdata.ht
			else
				return subdata.lt
			end
		else
			return "Wrong data format"
		end
	end )	
end


function writeBMPDiD(oldvalue, id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - writeBMPDiD,%02d: %s", "00", "enter function writeBMPDiD")
	local did, data = translateTableID( id )
	local tabid = Split(id, "_") -- split id to it's pieces
	local subdata = data[tabid[4]][tabid[4].."_"..tabid[5]]
	
	if data.sev_r == nil then  -- check if data.sev_r is missing
		data.sev_r = udsService_Read_Data_By_Identifier;
	end
	if data.sev_ioc == nil then  -- check if data.sev_ioc is missing
		data.sev_ioc = udsService_Input_Output_Control_By_Identifier;
	end
	
	local session = tonumber(string.sub(data.ses_ioc,1,2)) --take the first session, in case the sessions string contains more as one ("01;02;03;...")
	
	if secCodes[shortName]== nil or secCodes[shortName][session] == nil then
		return "No Sec Codes available"
	end
	local secCodeData=secCodes[shortName][session]
	
	return udsServiceRequest(data.sev_r,did ,0 , function ()
		if udsLen <= subdata.by then
			return string.format( "Read Error: %02X %02X",udsBuffer[1],udsBuffer[3])
		end
		-- start to toggle the requested bit
		local bitValue=2 ^ subdata.bi
		udsBuffer[subdata.by+4]=xor(udsBuffer[subdata.by+4],bitValue)
		local setCmd=did.."03" --currently hardcoded short term adjustment but should be depending on data.iocp content
		
		for i = 4, udsLen, 1 do
			setCmd=setCmd..string.format("%02X",udsBuffer[i])
		end
		for i = 1, subdata.by, 1 do
			setCmd=setCmd.."00"  
		end
		setCmd=setCmd..string.format("%02X",bitValue)
		for i = subdata.by+5, udsLen, 1 do
			setCmd=setCmd.."00"  
		end
		print ("Toogle cmd: ",setCmd)
		local securityLevel=secCodeData.level
		if secCodeData.code =="" then
			securityLevel=0 -- avoids security access challenge message in accessmode()
		end
		if accessMode == nil then
			return getLocalePrintf("lua_uds",strID_NoSecureCode, "secure access not implemented")
		end
		res= accessMode(session, securityLevel, secCodeData.code);
		if res < 0 then
			return string.format("Error Code %d",res)
		end
		print ("Access granted!! ")
		return udsServiceRequest(data.sev_ioc,setCmd ,0 , function ()
			return readBMPDiD(oldvalue, id)
		end )
	end )
end


function getIDValue(id) -- function delete prefix id0x of id
	return string.sub(id,5)
end


function readAscDiD(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - readAscDiD,%02d: %s", "00", "enter function readAscDiD")
	local did, data = translateTableID( id )
	if data.sev_r == nil then  -- check if data.sev_r is missing
		data.sev_r = udsService_Read_Data_By_Identifier;
	end
	return udsServiceRequest(data.sev_r,did ,0 , function ()
		local pos=4
		local res=""
		while pos <= udsLen and pos < 36 do
			if udsBuffer[pos]>31 and udsBuffer[pos]<128 then
				res=res..string.char(udsBuffer[pos])
			else
				res=res.."."
			end
			pos= pos +1
		end
		return res
	end )
end


function CalcNumDiD( byteNr , nrOfBytes, multiplier, offset, unit)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - CalcNumDiD,%02d: %s", "00", "enter function CalcNumDiD")
	local value=0
	local i
	if byteNr+nrOfBytes-1>#udsBuffer then
		return string.format(getLocalePrintf("lua_uds",strID_AnswerToShort, "Answer too short"),id)
	end
	for i = 0 , nrOfBytes -1, 1 do -- get raw value
		value = value * 256 + udsBuffer[ byteNr + i]
	end
	-- do the calculation
	value = value * multiplier + offset
	return value .." "..unit
end


function sCalcNumDiD( bitLen, bitPos, byteNr , nrOfBytes, multiplier, offset, unit, endianess)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - sCalcNumDiD,%02d: %s", "00", "enter function sCalcNumDiD")
	local value = 0
	value = CalcNumDiD_any( bitLen, bitPos, byteNr , multiplier, offset, endianess) -- calculate the "positive" value
	-- first see on RAW value if signed value is negative
	if value >= 128 and value <= 255 and nrOfBytes == 1 then
		value = value - 256
	elseif value >= 32768 and value <= 65535 and nrOfBytes == 2 then
		value = value - 65536
	elseif value >= 214748368 and value <= 4294967295 and nrOfBytes == 4 then
		value = value - 4294967296
	end
	-- calculation of RAW value 
	value = value * multiplier + offset
	return value .." "..unit
end

function CalcNumDiD_any( bitLen, bitPos, byteNr , multiplier, offset, endianess)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - CalcNumDiD_any,%02d: %s", "00", "enter function CalcNumDiD_any")
	local value=0
	if (bitLen<=(8-bitPos)) then -- check if we have less than a byte signal or just a byte signal
		value = bit.band (bit.brshift (udsBuffer[byteNr], bitPos), (2^bitLen)-1) -- move bit to bit position 0 and mask valid bits only

	else --the signal is on at least two bytes
		local value1=0
		local length_1=8-bitPos
		value=bit.band (bit.brshift (udsBuffer[byteNr], bitPos), (2^length_1)-1)--first byte (partial or not)

		local B_full=0
		B_full=math.floor((bitLen-length_1)/8)--number of full bytes (there can be none)
		for i = 0 , B_full-1, 1 do
			value1 = value1 * 256 + udsBuffer[ byteNr + (1 + i)*endianess]
		end
		value=value+value1*(2^length_1)

		local length_2=bitLen-(B_full*8)-length_1
		if (length_2~=0) then
			local value2=bit.band (bit.brshift (udsBuffer[byteNr+(B_full+1)*endianess], 0), (2^length_2)-1)--last byte (partial or not)
			value=value+value2*(2^(B_full*8+length_1))
		end
	end

	-- do the calculation
	value = value * multiplier + offset

	return value
end


-- convert 4byte/8byte little-endian string to IEEE754 single/double float
function str2float(x, datatype)
	local sign = 1
	local mantissa
	local exponent
	if datatype == "single" then
		mantissa = string.byte(x, 3) % 128
		for i = 2, 1, -1 do mantissa = mantissa * 256 + string.byte(x, i) end 
		if string.byte(x, 4) > 127 then sign = -1 end
		exponent = (string.byte(x, 4) % 128) * 2 +
		math.floor(string.byte(x, 3) / 128)
		if exponent == 0 then return 0 end
		mantissa = ((mantissa * (2 ^ -23) ) +1 ) * sign
		return mantissa * (2 ^ (exponent-127))
	elseif datatype == "double" then
		mantissa = string.byte(x, 7) % 16
		for i = 6, 1, -1 do mantissa = mantissa * 256 + string.byte(x, i) end
		if string.byte(x, 8) > 127 then sign = -1 end
		exponent = (string.byte(x, 8) % 128) * 16 +
		math.floor(string.byte(x, 7) / 16)
		if exponent == 0 then return 0 end
		mantissa = ((mantissa * (2 ^ -52) ) +1 ) * sign
		return mantissa * (2 ^ (exponent-1023))
	else
		return 0
	end
end

-- evaluates a DiD by its identifier, which is the reference to the content lookup table


function readNumDiD(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - readNumDiD,%02d: %s", "00", "enter function readNumDiD")
	local i
	local did, data = translateTableID( id )
	local content=getSubTable(id, 1,false)
	if content == nil then
		return string.format(getLocalePrintf("lua_uds",strID_DidNotDefined, "DID %s is not defined"),id)
	end
	if data.sev_r == nil then  -- check if data.sev_r is missing
		data.sev_r = udsService_Read_Data_By_Identifier;
	end
	return udsServiceRequest(data.sev_r,did ,0 , function ()
		local res=""
		local bytepos = math.floor(content.Bpos/8)+4
		if (content.dtype == "UNSIGNED") then
			res=  CalcNumDiD( bytepos, math.floor(content.Blen/8), content.mult, content.offset, content.unit) 
		elseif (content.dtype == "BYTE") then
			local value = ""
			if (math.floor(content.Blen/8) <= 8) then -- if byte length greater than 8 we'll interpret the datastream as raw values
				res= CalcNumDiD( bytepos, math.floor(content.Blen/8), content.mult, content.offset, content.unit)  
			else
				for i = 0, math.floor(content.Blen/8) -1, 1 do -- get raw value
					value = value..string.format("%02x", udsBuffer[bytepos + i])
				end
				-- due to possible long response the raw response content is on output window 
				serDisplayWrite("Response of Request $"..data.sev_r.." $"..did.."  - "..content.t..":")
				serDisplayWrite(""..value)
				-- normal response for application main request screen
				res= data ~= nil and "see output window" or "index error"
			end
			
		elseif (content.dtype == "SIGNED") then
			res=  sCalcNumDiD( bytepos , math.floor(content.Blen/8) , content.mult , content.offset, content.unit) 
			
		-- string.format for decimal transformation
		elseif (content.dtype == "BCD") then
			res=  CalcNumDiD( bytepos , math.floor(content.Blen/8) , content.mult , content.offset, content.unit) 
			
		-- string.format for HEX transformation
		elseif (content.dtype == "ENUM") then
			for key,contentEv in pairs(content.ev) do
				if key~="dummy" then
					if ( contentEv.bv == tonumber(udsBuffer[bytepos]) ) then
						res= data ~= nil and contentEv.t or "index error"
						return res
					else
						res = "NO DATA"
					end
				end
			end
		end
		return res
	end )
end


function calculatePacketedDiD(content, udsBuffer, bytepos)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - calculatePacketedDiD,%02d: %s", "00", "enter function calculatePacketedDiD")
	local res=""
	local endianess=1 --default => Intel format
	local bitPos=content.Bpos --init variable bitPos	
	--check if Motorola format => in case of Motorola format invert udsBuffer
	if (content.endianess=="Motorola") then
		endianess=-1 -- => Motorola format
		--where is the lsb? (bit position and byte position)
		if (content.Blen<=(content.Bpos%8+1)) then -- check if we have less than a byte signal or just a byte signal
			bitPos=bitPos-content.Blen+1 --lsb
			--no change on byte position of the lsb
		else --the signal is on at least two bytes
--			local nb_bits_first_byte = content.Bpos%8+1 --number of bits inside the first byte
--			local nb_bits_left = content.Blen - nb_bits_first_byte --number of bits left
--			bitPos=(math.floor(content.Bpos/8)+math.ceil(nb_bits_left/8))*8+((8-nb_bits_left%8)%8) -- lsb
			bitPos = content.Bpos
--			bytepos=bytepos+math.ceil(nb_bits_left/8) -- byte of the lsb
		end
	end
	--have a good bitPos
	bitPos=bitPos%8	
	if (content.dtype == "UNSIGNED") then
		res= data ~= nil and CalcNumDiD_any( content.Blen, bitPos, bytepos, content.mult, content.offset, endianess)  or "index error"
		res=string.format("%g ",res)..content.unit
	elseif (content.dtype == "BYTE") then
		res= data ~= nil and CalcNumDiD( bytepos, math.floor(content.Blen/8), content.mult, content.offset, "")  or "index error"
		res=string.format("%g ",res)..content.unit
	elseif (content.dtype == "SIGNED") then
		res= data ~= nil and sCalcNumDiD( content.Blen, bitPos, bytepos , math.floor(content.Blen/8) , content.mult , content.offset, "", endianess)  or "index error"
		res=string.format("%g ",res)..content.unit
	elseif (content.dtype == "BCD") then
		res= data ~= nil and CalcNumDiD( bytepos , math.floor(content.Blen/8) , content.mult , content.offset, "")  or "index error"
		-- string.format for HEX transformation
		res=string.format("%x ",res)..content.unit
	elseif (content.dtype == "ASCII") then
		local cnt = 0
		while cnt < math.floor(content.Blen/8) do
			if udsBuffer[bytepos]>20 then  -- verify valid ASCII character from Space (0x20 till 0x7F)
				res=res..string.char(udsBuffer[bytepos])
			end
			bytepos= bytepos +1
			cnt= cnt +1
		end
	elseif (content.dtype == "BITMAPPED") then
		if hasBit(udsBuffer[bytepos],content.bi) then
			res=content.ht
		else
			res=content.lt
		end					
	elseif (content.dtype == "FLOAT") then
		if math.floor(content.Blen/8) == 4 then
			for i = 3, 0, -1 do res = res..string.char(udsBuffer[bytepos+i]) end 
			res=string.format("%f",str2float(res, "single"))
		elseif math.floor(content.Blen/8) == 8 then
			for i = 7, 0, -1 do res = res..string.char(udsBuffer[bytepos+i]) end 
			res=string.format("%f",str2float(res, "double"))
		end
	elseif (content.dtype == "ENUM") then
		for keyEv,contentEv in pairs(content.ev) do
			if keyEv~="dummy" then
				res1 = CalcNumDiD_any( content.Blen, bitPos, bytepos, 1, 0, endianess)
				res2 = tonumber(res1)
				if ( contentEv.bv == res2)  then -- compare bitwise AND (bit.band) result (res) with current array key (.bv) to print out bit description
					res=contentEv.t
				end
			end
		end
	else 
		res="not implemented yet"
	end
	return res
end


function readPacketedDiD(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - readPacketedDiD,%02d: %s", "00", "enter function readPacketedDiD")
	local did, data = translateTableID( id )
	if (data.sev_r == "21") then	 -- if ReadDataByLocalID the leading byte "00" is cut off
		did = string.sub(did,3)
	end
	subTable=getSubTable(id, 1,true) 
	if subTable == nil then
		return string.format(getLocalePrintf("lua_uds",strID_DidNotDefined, "DID %s is not defined"),id)
	end
	if data.sev_r == nil then  -- check if data.sev_r is missing
		data.sev_r = udsService_Read_Data_By_Identifier;
	end
	return udsServiceRequest(data.sev_r, did , 0 , function ()
		openPage(data.t)   -- title/description of DID
		for key,content in pairs(data.sd) do
			if key~="dummy" then
				bytepos = math.floor(content.Bpos/8)+4
				res=calculatePacketedDiD(content, udsBuffer, bytepos)
				addElement(content.t, "nothing",res,0x00, "")
			end
		end
		addElement("<< Packeted Data","PacketedData_Menu","<",0x10, "")
		pageDone()
	end )
end

function readPacketedRTDDiD(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - readPacketedRTDDiD,%02d: %s", "00", "enter function readPacketedRTDDiD")
	local did, data = translateTableID( id )
	subTable=getSubTable(id, 1,false) 
	if subTable == nil then
		return string.format(getLocalePrintf("lua_uds",strID_DidNotDefined, "DID %s is not defined"),id)
	end
	local res, err=  udsServiceRequest(udsService_Read_Data_By_Identifier,did ,0 , function ()
		if tonumber(firmware_revision,10) == 794 then
			bytepos = math.floor(subTable.Bpos/8)+6
		else
			bytepos = math.floor(subTable.Bpos/8)+7   -- additional status byte added in OOBD Firmware for RTD protocol
		end
		res=calculatePacketedDiD(subTable, udsBuffer,bytepos)
		--- timestamp fehlt noch
		return res
	end)
	if err ==-1 then -- General Responce error
		if udsBuffer[4]== 1 then
				return "unknown CAN ID (not configured yet)"
		elseif udsBuffer[4]== 2 then
			return "no valid data received yet"
		else
			return string.format("unknown error 7F 0x%x",udsBuffer[5])
		end
	end
	return res
end


function doSelfTest(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - doSelfTest,%02d: %s", "00", "enter function doSelfTest")
	paramList=moduleSpecial[id]
	deleteDTC()
	serSleepCall(1000)
	return udsServiceRequest(udsService_Read_Data_By_Identifier,did ,0 , function ()
		serSleepCall(paramlist.timeout)
		showDTC()
		return "Test Done"
	end)
end


function evaluateDTCs(title)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - evaluateDTCs,%02d: %s", "00", "enter function evaluateDTCs")
	serDisplayWrite(title..":Start")
	local i = 4
	while i< ( udsLen -1 ) do
		local newArray
		local subCode= string.format(getLocalePrintf("subdtc",string.format("0x%01X",udsBuffer[i+2]), "unknown subCode 0x%01X"),udsBuffer[i+2])
		local transDTC = translateDTC(udsBuffer[i],udsBuffer[i+1])
		local DTCCode=string.format("%02X",udsBuffer[i])..string.format("%02X",udsBuffer[i+1])
		local dtcText= nil
		if DTCs ~= nil then
			dtcText=DTCs["DTCs_"..DTCCode]
		end
		if dtcText == nil then -- no dedicated DTC found?? then we'll try to use the overal DTC lookuptable
			if dbLookup ~= nil then
				newArray= dbLookup("dtc.oodb",DTCCode)
				if newArray.len > 0 then
					local index=tostring(newArray.header["Description"])
					dtcText = newArray.data["1"][index]
				end
			end
		end
		if dtcText ~= nil then
			DTCCode= transDTC.." (0x"..DTCCode..") " .. dtcText
		else
			DTCCode= transDTC.." (0x"..DTCCode..") "
		end
		serDisplayWrite(DTCCode.."-"..subCode)
		newArray= dbLookup("dtc_help.oodb",transDTC)
		if newArray.len > 0 then
			serDisplayWrite("Repair instructions:")
			DTCCode= ""
			local index=tostring(newArray.header["Description"])
			for helpindex = 1 , newArray.len , 1 do
				DTCCode= DTCCode.. newArray.data[tostring(helpindex)][index].."\n"
			end 
			serDisplayWrite(DTCCode)
		end
		i = i +4
	end
	serDisplayWrite(title..":End")
end


---------------------- Trouble Codes Menu --------------------------------------
function showDTC(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - showDTC,%02d: %s", "00", "enter function showDTC")
	udsServiceRequest(udsService_Read_DTC,"0202" ,0 , function ()
		evaluateDTCs("Actual DTCs of "..moduleName)
	end)
	udsServiceRequest(udsService_Read_DTC,"028D" ,0 , function ()
		evaluateDTCs("Old DTCs of "..moduleName)
	end)
	return "Done- See output for details"
end


function createXmlDTCs(type)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - createXmlDTCs,%02d: %s", "00", "enter function createXmlDTCs")
	local i = 4
	local res=""
	local newArray
	local index
	local helpindex
	while i+2< ( udsLen -1 ) do
		res=res.."<dtc>\n\t<type>"..type.."</type>\n"
		res=res.."\t<subcodetext>".. string.format(getLocalePrintf("subdtc",string.format("0x%02X",udsBuffer[i+2]), "unknown subCode 0x%02X"),udsBuffer[i+2]).."</subcodetext>\n"
		local transDTC = translateDTC(udsBuffer[i],udsBuffer[i+1])
		res=res.."\t<dtccode>"..transDTC.."</dtccode>\n"
		res=res..string.format("\t<subcodehex>%02X</subcodehex>\n" ,udsBuffer[i+2])
		res=res..string.format("\t<flags>%02X</flags>\n"           ,udsBuffer[i+3])
		
		local DTCCode=string.format("%02X%02X",udsBuffer[i],udsBuffer[i+1])
		res=res.."\t<dtchex>"..DTCCode.."</dtchex>\n"
		local dtcText= nil
		if DTCs ~= nil then
			dtcText=DTCs["DTCs_"..DTCCode]
		end
		if dtcText == nil then -- no dedicated DTC found?? then we'll try to use the overal DTC lookuptable
			if dbLookup ~= nil then
				newArray= dbLookup("dtc.oodb",DTCCode)
				if newArray.len > 0 then
					index=tostring(newArray.header["Description"])
					dtcText = newArray.data["1"][index]
				end
			end
		end
		if dtcText ~= nil then
			res=res.."\t<dtctext>"..dtcText.."</dtctext>\n"
		end
		newArray= dbLookup("dtc_help.oodb",transDTC)
		if newArray.len > 0 then
			DTCCode= ""
			index=tostring(newArray.header["Description"])
			for helpindex = 1 , newArray.len , 1 do
				DTCCode= DTCCode.. newArray.data[tostring(helpindex)][index].."\n"
			end 
			res=res.."\t<repair>"..DTCCode.."</repair>\n"
		end
		i = i +4
		res=res.."</dtc>\n"
	end
	return res
end

---------------------- create DTC list as XML string --------------------------------------

function getXmlDtc(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - getXmlDtc,%02d: %s", "00", "enter function getXmlDtc")
	local result="<dtcs>"
	local res, err = udsServiceRequest(udsService_Read_DTC,"0202" ,stdBuffer , nil)
	if err == 0 then
 		result=result..createXmlDTCs("actual")
	else
		result=result.."\n<error>\n<returncode>"..err.."</returncode>\n<desc>"..res.."</desc>\n<cmd>"..udsService_Read_DTC.."0202".."</cmd>\n</error>"
	end
	res, err = udsServiceRequest(udsService_Read_DTC,"028D" ,stdBuffer , nil)
	if err == 0 then
		result=result..createXmlDTCs("old")
	else
		result=result.."\n<error>\n<returncode>"..err.."</returncode>\n<desc>"..res.."</desc>\n<cmd>"..udsService_Read_DTC.."028D".."</cmd>\n</error>"
	end
	result=result.."</dtcs>"
	print( "result=" , result)
	return result
end

---------------------- getNrOfDTC--------------------------------------

function getNrOfDTC()
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - getNrOfDTC,%02d: %s", "00", "enter function getNrOfDTC")
-- if bit 1 (starting with 0) is set, then the active DTCs are requested. if bit 1 = 0 then the DTCs are the inactive ones
	local res1
	local res2
	local res, err = udsServiceRequest(udsService_Read_DTC,"0202" ,stdBuffer , nil)
	if err == 0 and udsLen> 5 then
			res1= udsBuffer[5] * 256 + udsBuffer[6]
	else
		res1= "Err."
	end
	local res, err = udsServiceRequest(udsService_Read_DTC,"018D" ,stdBuffer , nil)
	if err == 0 and udsLen> 5 then
			res2= udsBuffer[5] * 256 + udsBuffer[6]
	else
		res2 = "Err."
	end
	if type(res1) == "number" or type(res2) == "number" then
		return res1.." / "..res2
	elseif res1 == "Err." or res2 == "Err." then
		return "Err."
	else
		return "n/a"
	end
end

---------------------- delete DTC--------------------------------------

function deleteDTC()
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - deleteDTC,%02d: %s", "00", "enter function deleteDTC")
	return  udsServiceRequest(udsService_Clear_Diagnostic_Information,"FFFFFF" ,0 , function ()
		return "Done"
	end)
end