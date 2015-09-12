-- m4_define(`DEBUGPRINT', `m4_ifdef(`DEBUG', if "DBGUSER" ==$1 and DBGLEVEL >=$2 then  print(string.format(m4_shift(m4_shift( $@ ))) ) end)')


-- taken from  http://stackoverflow.com/questions/1426954/split-string-in-lua
function Split(pString, pPattern)
   DEBUGPRINT("nexulm", 1, "lua_utils.lua - Split,%02d: %s", "00", "enter function Split")
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
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - translateTableID,%02d: %s", "00", "enter function translateTableID")
	local info = Split(id, "_")
	return info[3] , _G[info[1]][info[1].."_"..info[2].."_"..info[3]] --nice trick to get a variable just by name :-)
end


-- helper function to get a subtable just out of the ID. If wholeTable is true, then the function returns the whole subtable, if wholeTable is false, it only return that particual element out of the subtable which is indexed by the string
-- returns ID,  data
function getSubTable(id, level,wholeTable)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - getSubTable,%02d: %s", "00", "enter function getSubTable")
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


--[[
isPermitted(permissionName): Checks, if permissionname is included in permission file "ScriptPermissions.perms"

in there the global array scriptPermissions hold the key -> values, which works like flags. With the utility function isPermitted() is can be checked, if a flag is set here or not.

Attention: Only the presence of a flag is checked, but not it's value! So also combinations like MyFlag = 0 , MyFlag = "No" or MyFlag = FALSE would give a positive result, as the key "MyFlag" itself exists. The only exeption is a MyFlag= nil, as this in fact deletes the whole entry.

--]]


function isPermitted(permissionName)
	return scriptPermissions ~= nil and scriptPermissions[permissionName:lower()] ~= nil
end 


--[[
setLocale(oldvalue,id): Sets global LOCALE variable to "id". If not set, LOCALE is set to "en_en" at first call of getLocalePrintf
--]]
function setLocale(oldvalue,id)
	LOCALE=id
	return "language set to "..id
end 

--[[
getLocalePrintf( category, textID, default)
help function to support text messages coming out of localized databases
At first step, it tries to open a oobd-db, where the filename consists of categorie_LOCALE.oobd
LOCALE is a global variable, which shall contain the locale country code like de_de. If not set, en_en is used as default.

Then it tries to find the entry "textID"

if this fails, it's tried again with the english version of that categorie db, means categorie_en_en.oobd

if that also failes, it returns the default string

The return value, either found in the db or the default can be used as simple string, but the main purpose is to return the format string
of a string.format() call, so by that localized texts can be generated which can also include some variables like e.g.

return string.format(getLocalePrintf("nrc",string.format("0x%x",udsBuffer[3]), "NRC: 0x%02X"),udsBuffer[3])

--]]

function getLocalePrintf( category, textID, default)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - getLocalePrintf,%02d: %s", "00", "enter function getLocalePrintf")
	if dbLookup ~= nil then -- db functionality implemented?
		if LOCALE==nil then -- default language not set ?
			LOCALE="en_en" -- then set it to english
		end
		newArray= dbLookup(category.."_"..LOCALE..".oodb",textID) -- look up for the given category db and for the given LOCALE
		if newArray ~= nil and newArray.len > 0 then -- if something was found
			index=tostring(newArray.header["Template"])
			return newArray.data["1"][index]
		else -- not found: either the LOCALE DB does not exist or the entry was not found
			if LOCALE ~="en_en" then -- if the LOCALE is already english, then there's no need to search again
				newArray= dbLookup(category.."_en_en.oodb",textID) --another trial, this time in english
				if newArray ~= nil and newArray.len > 0 then
					index=tostring(newArray.header["Template"])
					return newArray.data["1"][index]
				else --not found
					return default -- just return the default
				end
			else --english was already tried
				return default -- just return the default
			end
		end
	else -- no db functionality available
		return default -- just return the default
	end
end


function selectModuleID(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - selectModuleID,%02d: %s", "00", "enter function selectModuleID")
	index = tonumber(oldvalue)

	if index == 0 or index == 1 or index == 2 then
		if hardwareID == 0 or hardwareID == 1 then	-- if ELM327/DXM1 is selected so default is set autoprobing diag protocol on
     		setBus("autoprobing")					-- set automatic protocol detection
		else
			setBus("HS-CAN")						-- set HS-CAN 500kbit/s, 11bit
		end
		activateBus()
		if index == 1 then
			setModuleID("7E0")						-- set legislated OBD/WWH-OBD physical request 11bit CAN-ID for ECM
			setSendID("7E8") 						-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - ECM, Engince Control Module)
		elseif index == 2 then
			setModuleID("7E1")						-- set legislated OBD/WWH-OBD physical request 11bit CAN-ID for TCM
			setSendID("7E9") 						-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - ECM, Engince Control Module)
		else	-- index == 0
			setModuleID("7DF")						-- set legislated OBD/WWH-OBD functional request 11bit CAN-ID
			setSendID("7E8") 						-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - ECM, Engince Control Module)
		end
		setCANFilter(1,"7E0","7F0")					-- set CAN-Filter of ECU request/response CAN-ID range
		ModuleID = oldvalue;
	elseif index == 9 or index == 10 or index == 11 then	-- 29bit identifier
		DEBUGPRINT("nexulm", 1, "lua_utils.lua - selectModuleID,%02d: %s", "01", "index 9, setBus(500b29)")
		setBus("500b29")						-- set HS-CAN 500kbit/s, 29bit
		activateBus()
		if index == 10 then
			setModuleID("18DA10F1")					-- set legislated OBD/WWH-OBD physical request 29bit CAN-ID for ECM#1		
		elseif index == 11 then
			setModuleID("18DA18F1")					-- set legislated OBD/WWH-OBD physical request 29bit CAN-ID	for TCM#1
		else 	-- index == 9
			setModuleID("18DB33F1")					-- set legislated OBD/WWH-OBD functional request 29bit CAN-ID
		end
		setSendID("18DAF110")					-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - ECM, Engince Control Module)
		setCANFilter(1,"18DAF100","1FFFFF00")	-- set CAN-Filter of ECU response 29bit CAN-ID range only
		ModuleID = oldvalue;
	else
		if hardwareID == 0 or hardwareID == 1 then	-- if ELM327/DXM1 is selected so default is set autoprobing diag protocol on
     		setBus("autoprobing")					-- set automatic protocol detection
		else
			setBus("HS-CAN")						-- set HS-CAN 500kbit/s, 11bit for all other supported OBD2 Dongle
		end
		activateBus()
		setModuleID(string.format("%3X",index+0x7E0-1))				-- set legislated OBD/WWH-OBD physical request ID
		setSendID(string.format("%3X",index+0x7E8-1)) 				-- set default to legislated OBD/WWH-OBD physical response ID
		setCANFilter(1,string.format("%3X",index+0x7E8-1),"7FF")	-- set CAN-Filter of ECU request/response CAN-ID range
		ModuleID = oldvalue;
	end

	return oldvalue 
end