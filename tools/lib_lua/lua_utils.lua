-- m4_define(`DEBUGPRINT', `m4_ifdef(`DEBUG', if "DBGUSER" ==$1 and DBGLEVEL >=$2 then  print(string.format(m4_shift(m4_shift( $@ ))) ) end)')




-------------------- CRC32 ---
--Copyright (c) 2007-2008 Neil Richardson (nrich@iinet.net.au)
--
--Permission is hereby granted, free of charge, to any person obtaining a copy 
--of this software and associated documentation files (the "Software"), to deal
--in the Software without restriction, including without limitation the rights 
--to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
--copies of the Software, and to permit persons to whom the Software is 
--furnished to do so, subject to the following conditions:
--
--The above copyright notice and this permission notice shall be included in all
--copies or substantial portions of the Software.
--
--THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
--IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
--FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
--AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
--LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
--OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
--IN THE SOFTWARE.
local max = 2^32 -1

local CRC32 = {
    0,79764919,159529838,222504665,319059676,
    398814059,445009330,507990021,638119352,
    583659535,797628118,726387553,890018660,
    835552979,1015980042,944750013,1276238704,
    1221641927,1167319070,1095957929,1595256236,
    1540665371,1452775106,1381403509,1780037320,
    1859660671,1671105958,1733955601,2031960084,
    2111593891,1889500026,1952343757,2552477408,
    2632100695,2443283854,2506133561,2334638140,
    2414271883,2191915858,2254759653,3190512472,
    3135915759,3081330742,3009969537,2905550212,
    2850959411,2762807018,2691435357,3560074640,
    3505614887,3719321342,3648080713,3342211916,
    3287746299,3467911202,3396681109,4063920168,
    4143685023,4223187782,4286162673,3779000052,
    3858754371,3904687514,3967668269,881225847,
    809987520,1023691545,969234094,662832811,
    591600412,771767749,717299826,311336399,
    374308984,453813921,533576470,25881363,
    88864420,134795389,214552010,2023205639,
    2086057648,1897238633,1976864222,1804852699,
    1867694188,1645340341,1724971778,1587496639,
    1516133128,1461550545,1406951526,1302016099,
    1230646740,1142491917,1087903418,2896545431,
    2825181984,2770861561,2716262478,3215044683,
    3143675388,3055782693,3001194130,2326604591,
    2389456536,2200899649,2280525302,2578013683,
    2640855108,2418763421,2498394922,3769900519,
    3832873040,3912640137,3992402750,4088425275,
    4151408268,4197601365,4277358050,3334271071,
    3263032808,3476998961,3422541446,3585640067,
    3514407732,3694837229,3640369242,1762451694,
    1842216281,1619975040,1682949687,2047383090,
    2127137669,1938468188,2001449195,1325665622,
    1271206113,1183200824,1111960463,1543535498,
    1489069629,1434599652,1363369299,622672798,
    568075817,748617968,677256519,907627842,
    853037301,1067152940,995781531,51762726,
    131386257,177728840,240578815,269590778,
    349224269,429104020,491947555,4046411278,
    4126034873,4172115296,4234965207,3794477266,
    3874110821,3953728444,4016571915,3609705398,
    3555108353,3735388376,3664026991,3290680682,
    3236090077,3449943556,3378572211,3174993278,
    3120533705,3032266256,2961025959,2923101090,
    2868635157,2813903052,2742672763,2604032198,
    2683796849,2461293480,2524268063,2284983834,
    2364738477,2175806836,2238787779,1569362073,
    1498123566,1409854455,1355396672,1317987909,
    1246755826,1192025387,1137557660,2072149281,
    2135122070,1912620623,1992383480,1753615357,
    1816598090,1627664531,1707420964,295390185,
    358241886,404320391,483945776,43990325,
    106832002,186451547,266083308,932423249,
    861060070,1041341759,986742920,613929101,
    542559546,756411363,701822548,3316196985,
    3244833742,3425377559,3370778784,3601682597,
    3530312978,3744426955,3689838204,3819031489,
    3881883254,3928223919,4007849240,4037393693,
    4100235434,4180117107,4259748804,2310601993,
    2373574846,2151335527,2231098320,2596047829,
    2659030626,2470359227,2550115596,2947551409,
    2876312838,2788305887,2733848168,3165939309,
    3094707162,3040238851,2985771188,
}

function xor(a, b)
    local calc = 0    

    for i = 32, 0, -1 do
	local val = 2 ^ i
	local aa = false
	local bb = false

	if a == 0 then
	    calc = calc + b
	    break
	end

	if b == 0 then
	    calc = calc + a
	    break
	end

	if a >= val then
	    aa = true
	    a = a - val
	end

	if b >= val then
	    bb = true
	    b = b - val
	end

	if not (aa and bb) and (aa or bb) then
	    calc = calc + val
	end
    end

    return calc
end


function orByte(a, b) -- or's two value, for speed reasons limited to byte size
    local calc = 0    

    for i = 8, 0, -1 do
	local val = 2 ^ i
	local aa = false
	local bb = false

	if a == 0 then
	    calc = calc + b
	    break
	end

	if b == 0 then
	    calc = calc + a
	    break
	end

	if a >= val then
	    aa = true
	    a = a - val
	end

	if b >= val then
	    bb = true
	    b = b - val
	end

	if  (aa or bb) then
	    calc = calc + val
	end
    end

    return calc
end

function lshift(num, left)
    local res = num * (2 ^ left)
    return res % (2 ^ 32)
end

function rshift(num, right)
    local res = num / (2 ^ right)
    return math.floor(res)
end

function Hash(byteBuffer,len,offset)
    local crc = max
    
    local i = 0
    while len > 0 do
	local byte = byteBuffer[i+offset]

	crc = xor(lshift(crc, 8), CRC32[xor(rshift(crc, 24), byte) + 1])

	i = i + 1
	len = len - 1
    end

    return string.format("%04X",crc)
end


function hasBit(value, bitNr) -- bitNr starts with 0
	bitValue=2 ^ bitNr
	return value % (bitValue + bitValue) >= bitValue -- found in the internet: nice trick to do bitoperations in Lua, which does not have such functions by default
end

function bitor(x, y)
  local p = 1
  while p < x do p = p + p end
  while p < y do p = p + p end
  local z = 0
  repeat
    if p <= x or p <= y then
      z = z + p
      if p <= x then x = x - p end
      if p <= y then y = y - p end
    end
    p = p * 0.5
  until p < 1
  return z
end





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
	local levCount = 0
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
debug helper to print tables

found on http://stackoverflow.com/questions/9168058/lua-beginner-table-dump-to-console
-- Print contents of `tbl`, with indentation.
-- `indent` sets the initial level of indentation.
--]]




function tprint (tbl, indent)
  if not indent then indent = 0 end
  for k, v in pairs(tbl) do
--    formatting = string.rep("  ", indent) .. k .. ": "
    local formatting =  k .. ": "
    if type(v) == "table" then
      print(formatting)
      tprint(v, indent+1)
    elseif type(v) == 'boolean' then
      print(formatting .. tostring(v))      
    else
      print(formatting .. v)
    end
  end
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
getPrefs(name,default): Reads from the global preferences the value of "name". If not found, default is returned
--]]
function getPrefs(name,default)
	if globalPrefs ~= nil and globalPrefs[name]~=nil then
		return globalPrefs[name]
	else
		return default
	end
end 


--[[
setPrefs(name,value): Sets the in global preferences  "name" to "value"
--]]
function setPrefs(name,value)
	if globalPrefs == nil  then
		 globalPrefs= {}
	end
	globalPrefs[name]=value
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
		local newArray
		local index
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
	local index = tonumber(oldvalue)
	local ModuleID
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