
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
