-- m4_define(`DEBUGPRINT', `m4_ifdef(`DEBUG', if "DBGUSER" ==$1 and DBGLEVEL >=$2 then  print(string.format(m4_shift(m4_shift( $@ ))) ) end)')


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
msgBox = msgBoxCall
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
	DEBUGPRINT("stko", 1, "serial_dxm.lua - serReadLn,%02d: %s %d, %d", "00", "read from input: ", res, readcount)
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
	DEBUGPRINT("stko", 1, "serial_dxm.lua - serWrite,%02d: %s %d", "00", "Serwrite: ", data)
end

function serFlush()
end

--]]


--[[
function openPage(title)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - openPage,%02d: %s %s", "00", "Start Menu generation: ", title)
end

function addElement(title, func , intial,flags , id)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s", "00", "<---")
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s %s", "01", "title: ", title)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s %s", "02", "function: ", func)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s %s", "03", "id: ", id)
	DEBUGPRINT("stko", 1, "serial_dxm.lua - addElement,%02d: %s", "00", "--->")
end

function pageDone()
	DEBUGPRINT("stko", 1, "serial_dxm.lua - pageDone,%02d: %s", "00", "Show Menu")
end



--]]

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
	return "see output window"
end


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
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - getSubTable,%02d: %s", "05", "exit function getSubTable")
	return data
end


-- returns substring nr. "index" of of a string of space separated substring

function getStringPart(text, index)
	DEBUGPRINT("nexulm", 1, "serial_dxm.lua - getStringPart,%02d: %s", "00", "enter function getStringPart")
	if text==nil then
		return ""
	end
	local start, finish = string.find(text," ")
	local loop = 0
	local first = ""
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

-- formats "num" to a float string with "idp" number of digits

function round(num, idp)
  return tonumber(string.format("%." .. (idp or 0) .. "f", num))
end

-- translate DTC bytes into their offical notation
-- as descripted on http://en.wikipedia.org/wiki/OBD-II_PIDs (30.4.11)

function translateDTC(highByte, lowByte)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - translateDTC,%02d: %s", "00", "enter function translateDTC")
	local hNibble= (highByte -(highByte % 16)) / 16 -- tricky to do an integer devide with luas float numbers..
	local lNibble =highByte % 16
	local start = "??"
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
	elseif hNibble == 15 then start = "U3"
	end
	return start..string.format("%X",lNibble)..string.format("%02X",lowByte)
end

function notyet(oldvalue,id)
	return "not implemented yet"
end

function nothing(oldvalue,id)
	return oldvalue
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
	if LOCALE ~= nil then
		return "language set to "..id
	else
		return "no default Language"
	end
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
	if dbLookup ~= nil then -- db functionality implemented?
		if LOCALE==nil then -- default language not set ?
			LOCALE="en_en" -- then set it to english
		end
		newArray= dbLookup(category.."_"..LOCALE..".oodb",textID) -- look up for the given category db and for the given LOCALE
		if newArray ~= nil and newArray.len > 0 then -- if something was found
			return getDBEntry(newArray, "Template","1",default)
		else -- not found: either the LOCALE DB does not exist or the entry was not found
			if LOCALE ~="en_en" then -- if the LOCALE is already english, then there's no need to search again
				newArray= dbLookup(category.."_".."en_en"..".oodb",textID) -- look up for the given category db and for the given LOCALE
				return getDBEntry(newArray, "Template","1",default)
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
--	local ModuleID -- if local ModuleID is set the combo box selected value won't be reused
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
			setSendID("7E9") 						-- set default to legislated OBD/WWH-OBD physical response ID (ECU #1 - TCM, Transmission Control Module)
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
			setSendID("18DAF110")					-- set legislated OBD/WWH-OBD physical response 29bit CAN-ID for ECM#1
		elseif index == 11 then
			setModuleID("18DA1EF1")					-- set legislated OBD/WWH-OBD physical request 29bit CAN-ID	for TCM#1
			setSendID("18DAF11E")					-- set legislated OBD/WWH-OBD physical response 29bit CAN-ID for ECM#1
		else 	-- index == 9
			setModuleID("18DB33F1")					-- set legislated OBD/WWH-OBD functional request 29bit CAN-ID
			setSendID("18DAF133")					-- set legislated OBD/WWH-OBD fucntional response 29bit CAN-ID
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



-- helper function to read out of a dbtable, which is an result of precesing dbLookup, by taking care about the different error cases
function getDBEntry(db, header, nr, default)
		if db ~= nil and db.len > 0 then
			local index = db.header[header]
			if index==nil then
				return default
			end
			index=tostring(index)
			index = getArrayData(db.data, {nr, index})
			if index==nil then
				return default
			end
			return index
		else --not found
			return default -- just return the default
		end

end

--[[
    getArrayData returns the value out of a multidimensional array like result = array[a][b][c], but in opposite to such a direct access
    it returns a clear nil, but it does not throw any java exceptions in case the addressing is invalid.

	so its been called as getArrayData(array, {a,b,c})
--]]

function getArrayData(myArray, indexer)
	for i = 1 , #indexer do
		print ("search for index"..indexer[i])
		if myArray[indexer[i]]==nil then
			return nil
		else
			myArray=myArray[indexer[i]]
		end
	end
	return myArray
	
end

 
--[[
    array2str returns the string represenation of the given byte array
--]]

function array2str(byteArray)
	ans="";
	for i = 1 , #byteArray do
		ans = ans .. string.format("%02X",byteArray[i])
	end
	return ans
	
end



-- translates XML tag arguments into array. (unused) part of primitiveXML2Table()
function xmlTagParseArgs(s)
	local arg = {}
	string.gsub(s, "([%-%w]+)=([\"'])(.-)%2", function (w, _, a)
			arg[w] = a
		end)
	return arg
end

--[[
translates primitive XML constructs into lua table. 
Caution: it supports only the basic XML format stile like <parent><child>value1</child><child>value2</child></parent> ,
but no arguments, and no other fancy XLM feature at all

returns lua table like
{parent = { { child = { {"value1"} , {"value2"} } } }

in case of an error, it returns {}

--]]
function primitiveXML2Table(s)
	local stack = {} ---------- stack for the elements
	local name = {} ------ Stack for the tag name
	local top = {}
	table.insert(stack, top)
	local ni, c, label, xarg, empty
	local i, j = 1, 1
	while true do
		ni, j, c, label, xarg, empty = string.find(s, "<(%/?)([%w_:]+)(.-)(%/?)>", i)
		if not ni then break end
		local text = string.sub(s, i, ni - 1) -- what stands bevore the actual found tag?
		if not string.find(text, "^%s*$") then -- some text?
			table.insert(top, text) -- store it
		end
		if empty == "/" then -- empty element tag
			-- table.insert(top, {label=label, xarg=parseargs(xarg), empty=1}) -- empty? Don't save that
		elseif c == "" then -- start tag
			--if xarg=="" then                  arguments do not work somehow, so commented out
				top = {}
			--else
			--	top = { xarg=xmlTagParseArgs(xarg)}
			--end
			table.insert(stack, top) -- new level: push new table on stack
			table.insert(name, label) -- push actual element name on name stack for later assignment, when coming back from stack
		else -- end tag
			local toclose = table.remove(stack) -- pull result from stack
			top = stack[#stack]
			local lastName = table.remove(name) -- pull this level name from stack again
			if #stack < 1 then
				return {} -- error("nothing to close with " .. label)
			end
			-- the original source had some error handling, but we dont...
			--[[ if toclose.label ~= label then
				error("trying to close "..toclose.label.." with "..label)
			      end
			--]]
			-- adding the childtable to the actual table by using the actual name as identifier
			if top[lastName] == nil then -- is there already a table to add to?
				top[lastName] = {} -- if not, create ome
			end
			table.insert(top[lastName], toclose)
		end
		i = j + 1
	end
	--[[  some text left? We don't care
	  local text = string.sub(s, i)
	  if not string.find(text, "^%s*$") then
	    table.insert(stack[#stack], text)
	  end
	  --]]
	if #stack > 1 then
		return {} -- error("unclosed " .. stack[#stack].label)
	end
	return stack[1]
end

local codetable =
{
--  ['Å'] = "\197\160", -- Š
  ['Å½'] = "\197\189", -- Ž
  ['Å¡'] = "\197\161", -- š
  ['Å“'] = "\197\147", -- œ
  ['Å¾'] = "\197\190", -- ž
  ['Å¸'] = "\197\184", -- Ÿ
  ['Â§'] = "\194\167", -- §
  ['Âµ'] = "\194\181", -- µ
  ['Ã€'] = "\195\128", -- À
  ['Ã'] = "\195\129", -- Á
	['Ã‚'] = "\195\130", -- Â
	['Ãƒ'] = "\195\131", -- Ã
  ['Ã„'] = "\195\132", -- Ä
  ['Ã…'] = "\195\133", -- Å
  ['Ã†'] = "\195\134", -- Æ
  ['Ã‡'] = "\195\135", -- Ç
  ['Ãˆ'] = "\195\136", -- È
  ['Ã‰'] = "\195\137", -- É
  ['ÃŠ'] = "\195\138", -- Ê
  ['Ã‹'] = "\195\139", -- Ë
  ['ÃŒ'] = "\195\140", -- Ì
--  ['Ã'] = "\195\141", -- Í
  ['ÃŽ'] = "\195\142", -- Î
--  ['Ã'] = "\195\143", -- Ï
--  ['Ã'] = "\195\144", -- Ð
  ['Ã‘'] = "\195\145", -- Ñ
  ['Ã’'] = "\195\146", -- Ò
--  ['Ã“'] = "\195\147", -- Ó
  ['Ã”'] = "\195\148", -- Ô
  ['Ã•'] = "\195\149", -- Õ
  ['Ã–'] = "\195\150", -- Ö
  ['Ã—'] = "\195\151", -- ×
  ['Ã˜'] = "\195\152", -- Ø
  ['Ã™'] = "\195\153", -- Ù
  ['Ãš'] = "\195\154", -- Ú
  ['Ã›'] = "\195\155", -- Û
  ['Ãœ'] = "\195\156", -- Ü
--  ['Ã'] = "\195\157", -- Ý
  ['Ãž'] = "\195\158", -- Þ
  ['ÃŸ'] = "\195\159", -- ß
--  ['Ã'] = "\195\160", -- à
  ['Ã¡'] = "\195\161", -- á
  ['Ã¢'] = "\195\162", -- â
  ['Ã£'] = "\195\163", -- ã
  ['Ã¤'] = "\195\164", -- ä
  ['Ã¥'] = "\195\165", -- å
  ['Ã¦'] = "\195\166", -- æ
  ['Ã§'] = "\195\167", -- ç
  ['Ã¨'] = "\195\168", -- è
  ['Ã©'] = "\195\169", -- é
  ['Ãª'] = "\195\170", -- ê
  ['Ã«'] = "\195\171", -- ë
  ['Ã¬'] = "\195\172", -- ì
  ['Ã­'] = "\195\173", -- í
  ['Ã®'] = "\195\174", -- î
  ['Ã¯'] = "\195\175", -- ï
  ['Ã°'] = "\195\176", -- ð
  ['Ã±'] = "\195\177", -- ñ
  ['Ã²'] = "\195\178", -- ò
  ['Ã³'] = "\195\179", -- ó
  ['Ã´'] = "\195\180", -- ô
  ['Ãµ'] = "\195\181", -- õ
  ['Ã¶'] = "\195\182", -- ö
  ['Ã·'] = "\195\183", -- ÷
  ['Ã¸'] = "\195\184", -- ø
  ['Ã¹'] = "\195\185", -- ù
  ['Ãº'] = "\195\186", -- ú
  ['Ã»'] = "\195\187", -- û
  ['Ã¼'] = "\195\188", -- ü
  ['Ã½'] = "\195\189", -- ý
  ['Ã¾'] = "\195\190", -- þ
  ['Ã¿'] = "\195\191", -- ÿ
}

--[[
  encode UTF8(strUTF8) usable for ioRead, ioWrite, ioInput, ... library if file with e.g. german characters like ä,ü,ö,... must be converted via:
	----- snip -----
	ioInput(myfile,".txt", "direct")
	
	while true do
		res = ioRead("*line")
			...
	end
	----- snap -----
]]--
function encodeUTF8(strUTF8)
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - encodeUTF8,%02d: %s", "00", "enter function encodeUTF8")
	if strUTF8 == nil or type(strUTF8) ~= "string" then
		return ''
	end

	local newstr = strUTF8
	-- Encode known UTF8 characters
	for character, entity in pairs(codetable) do
		newstr = string.gsub(newstr, character, entity)
	end
	DEBUGPRINT("nexulm", 1, "lua_utils.lua - encodeUTF8,%02d: newstr: %s", "05", newstr)
	return newstr
end