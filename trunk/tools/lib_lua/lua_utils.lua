
-- taken from  http://stackoverflow.com/questions/1426954/split-string-in-lua
function Split(pString, pPattern)
   local Table = {}  -- NOTE: use {n = 0} in Lua-5.0
   local fpat = "(.-)" .. pPattern
   local last_end = 1
   local s, e, cap = pString:find(fpat, 1)
   while s do
      if s ~= 1 or cap ~= "" then
     table.insert(Table,cap)
      end
      last_end = e+1
      s, e, cap = pString:find(fpat, last_end)
   end
   if last_end <= #pString then
      cap = pString:sub(last_end)
      table.insert(Table, cap)
   end
   return Table
end


-- helper function to translate a table identifier into its table data and the related ID,
-- returns ID,  data
function translateTableID(id)
	local info = Split(id, "_")
	return info[3] , _G[info[1]][info[1].."_"..info[2].."_"..info[3]] --nice trick to get a variable just by name :-)
end


-- helper function to get a subtable just out of the ID. If wholeTable is true, then the function returns the whole subtable, if wholeTable is false, it only return that particual element out of the subtable which is indexed by the string
-- returns ID,  data
	function getSubTable(id, level,wholeTable)
	local info = Split(id, "_")
	local data =_G[info[1]]
	if data ~=nil and #info>2 then
		data= data[info[1].."_"..info[2].."_"..info[3]]
	end
	levCount = 0
	while levCount < level and data ~=nil and (levCount *2 + 4 )< #info do
		data = data[ info[ levCount * 2 + 4]]
		if not(wholeTable and levCount == level -1) then -- if wholetable is set, then the last catch to get the last sudata. element need to be surpressed to return the whole subtable and not only one element out of it
			data=data[ info[ levCount * 2 + 4] .. "_" ..info[ levCount * 2 + 5]]
		end
		levCount = levCount +1
	end
	return data
end

---------------------- Negative response codes (NRC) --------------------------------------
-- see: http://www.autosar.org  => R4.0 AUTOSAR_SWS_DiagnosticCommunicationManager

nrcCodes = {
	[0x10] = 'General reject',
	[0x11] = 'Service not supported (in active Diag session)',
	[0x12] = 'Subfunction not supported (in active Diag session)',
	[0x13] = 'Incorrect message length or invalid format',
	[0x14] = 'Response to long',
	[0x21] = 'Busy - Repeat request',
	[0x22] = 'Conditions not correct',
	[0x24] = 'Request sequence error',
	[0x25] = 'No response from subnet component',
	[0x26] = 'Failure prevents execution of requested action',
	[0x31] = 'Request out of range',
	[0x33] = 'Securtiy access denied',
	[0x35] = 'Invalid key',
	[0x36] = 'Exceed number of attempts',
	[0x37] = 'Required time delay not expired',
	[0x70] = 'Upload download not accepted',
	[0x71] = 'Transfer data suspended',
	[0x72] = 'General programming failure',
	[0x73] = 'Wrong block sequence counter',
	[0x78] = 'Reqeust correctly received - Response pending',
	[0x7E] = 'Subfunction not supported in active session',
	[0x7F] = 'System internal error',
	[0x81] = 'RPM too high',
	[0x82] = 'RPM too low',
	[0x83] = 'Engine running',
	[0x84] = 'Engine not running',
	[0x85] = 'Engine runtime too low',
	[0x86] = 'Temperature too high',
	[0x87] = 'Temperature too low',
	[0x88] = 'Vehicle speed too high',
	[0x89] = 'Vehicle speed too low',
	[0x8A] = 'Throttle pedal too high',
	[0x8B] = 'Throttle pedal too low',
	[0x8C] = 'Transmission range not in neutral',
	[0x8D] = 'Transmission range not in gear',
	[0x8F] = 'Brake not closed',
	[0x90] = 'Sifter lever not in park',
	[0x91] = 'Torque converter clutch locked',
	[0x92] = 'Voltage too high',
	[0x93] = 'Voltage too low',
}

function selectModuleID(oldvalue,id)
	index = tonumber(oldvalue)

	if index == 0 then
		setModuleID("7DF") -- set legislated OBD/WWH-OBD functional request ID
		setCANFilter(1,"7E8","7FF") -- set CAN-Filter of ECU request/response CAN-ID range
		setSendID("7E8") 	-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - ECM, Engince Control Module)
		ModuleID = oldvalue;
	else
		setModuleID(string.format("%3X",index+0x7E0-1)) -- set legislated OBD/WWH-OBD functional request ID
		setCANFilter(1,string.format("%3X",index+0x7E8-1),"7FF") -- set CAN-Filter of ECU request/response CAN-ID range
		setSendID(string.format("%3X",index+0x7E8-1)) 	-- set default to legislated OBD/WWH-OBD physical response ID
		ModuleID = oldvalue;
	end

	return oldvalue 
end