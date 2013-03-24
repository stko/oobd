
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
	info = Split(id, "_")
	-- print ("split into: tablename "..info[1].." and id "..info[2])
	data =_G[info[1]]
	--print (data)
	data= data[info[1].."_"..info[2]]
	--[[
	print (data)
	for n ,v in pairs(data) do
		print (n,v)
	end
	--]]
	return info[2] , _G[info[1]][info[1].."_"..info[2]] --nice trick to get a variable just by name :-)
end
