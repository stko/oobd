--[[testsuite.lua
--]]

local BusID = "HS-CAN";
local ModuleID = 0;  -- default functionl request ID for emission related ECUs

dofile("../../tools/lib_lua/lua_utils.lua")
dofile("../../tools/lib_lua/serial_dxm.lua")
dofile("../../tools/lib_lua/lua_uds.lua")



TestData = { 
TestData_0_2B05 = { t = "Bitmapped Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_07 = { by = 0 , bi = 7, t= "Bitmap Byte 0 Bit 7" , lt = "Inactive" ,  ht = "Active"},
sd_33 = { by = 3 , bi = 3, t= "Bitmap Byte 3 Bit 3" , lt = "False" ,  ht = "True"},
}
},
TestData_0_2B2C = { t = "Bitmapped Data w. Write", sev_r = "22", ses_r ="01;03;", sev_w = "2E", ses_w ="03;", sev_ioc = "2F", ses_ioc ="03;", iocp ="00;03;", sd = {
sd_07 = { by = 0 , bi = 7, t= "Bitmap Byte 0 Bit 7" , lt = "Off" ,  ht = "On"},
sd_06 = { by = 0 , bi = 6, t= "Bitmap Byte 0 Bit 6" , lt = "Off" ,  ht = "On"},
}
},



-- read data for each Single value - ASCII

TestData_0_F050 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {   bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "ASCII Len 24" },
}
},
TestData_0_F040 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {   bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "ASCII Len 24 with binary data" },
}
},
TestData_0_F051 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {   bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "ASCII Len 24 with General Response Error" },
}
},
TestData_0_F052 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {   bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "ASCII Len 24 with Timeout" },
}
},
TestData_0_F053 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {   bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "ASCII Len 24 with no Answer" },
}
},
TestData_0_F054 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {   bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "ASCII Len 24 with sequence error" },
}
},



-- read data for UNSIGNED

TestData_0_F220 = { t = "UNSIGNED Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 16 , bitLen = 16 , mult = 0.01 , offset = -35 , min = "-35" , max = "6518.5" , unit = "Count" , dtype = "UNSIGNED" , t = "UNSIGNED 2 Bytes, start at Byte 2, mult 0.1, offset -35"} ,
}
},
TestData_0_F221 = { t = "UNSIGNED Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 16 , bitLen = 16 , mult = 0.01 , offset = -35 , min = "-35" , max = "6518.5" , unit = "Count" , dtype = "UNSIGNED" , t = "UNSIGNED 2 Bytes with General Response Error"} ,
}
},
TestData_0_F222 = { t = "UNSIGNED Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 16 , bitLen = 16 , mult = 0.01 , offset = -35 , min = "-35" , max = "6518.5" , unit = "Count" , dtype = "UNSIGNED" , t = "UNSIGNED 2 Bytes with Timeout"} ,
}
},
TestData_0_F223 = { t = "UNSIGNED Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 16 , bitLen = 16 , mult = 0.01 , offset = -35 , min = "-35" , max = "6518.5" , unit = "Count" , dtype = "UNSIGNED" , t = "UNSIGNED 2 Bytes with no Answer"} ,
}
},
TestData_0_F225 = { t = "UNSIGNED Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 16 , bitLen = 16 , mult = 0.01 , offset = -35 , min = "-35" , max = "6518.5" , unit = "Count" , dtype = "UNSIGNED" , t = "UNSIGNED 2 Bytes, answer too short"} ,
}
},




-- read data for each Single value - Number

TestData_0_D100 = { t = "Active Diagnostic Session", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = { bitPos = 0 , bitLen = 8, dtype = "ENUM", t = "", ev = { 
ev_1 = { bv = 1 , t = "Default Session"},

ev_5 = { bv = 5 , t = "Supplier Specific Diagnostic Session 0x60"},
}},
}
},
TestData_0_F162 = { t = "Software Download Specification Version", sd = {
sd_00 = { bpos = 0, blen = 1, bitPos = 0 , bitLen = 8, dtype = "ENUM", t = "", ev = { 
ev_0 = { bv = 0 , t = "SWDL 31808456 Issue P01"},
ev_1 = { bv = 1 , t = "SWDL 31808456 Issue 001"},

}},
}
},
TestData_0_F163 = { t = "Numeric Enum Data", sd = {
sd_00 = { bpos = 0, blen = 1, bitPos = 0 , bitLen = 8, dtype = "ENUM", t = "", ev = { 
ev_1 = { bv = 1 , t = "Enum Byte 0 Value 1"},
ev_3 = { bv = 3 , t = "GGDS 31810161 Issue 003"},
}},
}
},
TestData_0_D91C = { t = "RSC/ARP Rollover Stability Control/Active Rollover Protection", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 0 , bitLen = 16 , mult = 1 , offset = 0 , min = "0" , max = "65535" , unit = "counts, counts, counts" , dtype = "UNSIGNED" , t = "RSC/ARP Rollover Stability Control/Active Rollover Protection"} ,
}
},

TestData_0_2B0D = { t = "Brake Fluid Line Hydraulic Pressure", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bitPos = 0 , bitLen = 16 , mult = 0.333333333333 , offset = 0 , min = "-10922.6666666557" , max = "10922.3333333224" , unit = "Bar" , dtype = "SIGNED", t = "Brake Fluid Line Hydraulic Pressure"} ,
}
},



-- read data for each Packeted value - Number

TestData_0_D10E = { t = "HSCAN Network Management State", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = { bitPos = 0 , bitLen = 8, dtype = "ENUM", t = "HSCAN Network Management State - Latest State", ev = { 
ev_0x00 = { bv = 0x00 , t = "Reserved"},
ev_0x01 = { bv = 0x01 , t = "Off"},
}},
}
},
TestData_0_F166 = { t = "NOS Message Database #1 Version Number", sd = {
sd_00 = {  bitPos = 0 , bitLen = 8 , mult = 1 , offset = 0 , min = "-" , max = "-" , unit = "" , dtype = "BCD", t = "Year"} ,
}
},
TestData_0_F180 = { t = "Boot Software Identification", sd = {
sd_00 = {  bitPos = 0 , bitLen = 8 , mult = 1 , offset = 0 , min = "0" , max = "255" , unit = "Count" , dtype = "UNSIGNED" , t = "Number Of Modules (ISO14229 compatibility - always set to 01h)"} ,
sd_10 = {  bitPos = 0 , bitLen = 192 , dtype = "ASCII", t = "Boot Software Identifcation Data (Part no. of the bootloader)" },
}
},
}

-- DTC list
DTCs = {
DTCs_4001 =  "Test DTC" ,
}

-- data for selfTests()
selftest = {
selftest_310202 =  "On-Demand Self-Test" ,
}






--[[

function readBMPDiDByTable(value, id)

function readBMPDiD(oldvalue, id)

function writeBMPDiD(oldvalue, id)

function getIDValue(id) -- function delete prefix id0x of id

function readAscDiD(oldvalue,id)

function CalcNumDiD( byteNr , nrOfBytes, multiplier, offset, unit)

function sCalcNumDiD( bitLen, bitPos, byteNr , nrOfBytes, multiplier, offset, unit, endianess)

function CalcNumDiD_any( bitLen, bitPos, byteNr , multiplier, offset, endianess)

function str2float(x, datatype)

function readNumDiD(oldvalue,id)

function calculatePacketedDiD(content, udsBuffer, bytepos)

function readPacketedDiD(oldvalue,id)

function readPacketedRTDDiD(oldvalue,id)

function doSelfTest(oldvalue,id)

function evaluateDTCs(title)

function showDTC(oldvalue,id)

function createXmlDTCs(type)

function getXmlDtc(oldvalue,id)

function getNrOfDTC()

function deleteDTC()



--]]



--- this function is a copy of the lib_uds readPacketedDiD(9 function, but without the openpage/addelements commands

function testreadPacketedDiD(oldvalue,id)
	DEBUGPRINT("nexulm", 1, "lua_uds.lua - testreadPacketedDiD,%02d: %s", "00", "enter function testreadPacketedDiD")
	local did, data = translateTableID( id )
	if (data.sev_r == udsService_Read_Data_By_LocalIdentifier) then	 -- if ReadDataByLocalID the leading byte "00" is cut off
		did = string.sub(did,3)
	end
	subTable=getSubTable(id, 1,true) 
	if subTable == nil then
		return string.format(getLocalePrintf("lua_uds",strID_DidNotDefined, "DID %s is not defined"),id)
	end
	if data.sev_r == nil then  -- check if data.sev_r is missing
		data.sev_r = udsService_Read_Data_By_Identifier;
	end
	return udsServiceRequest(data.sev_r, did , {} , 0 , function ()
		-- openPage(data.t)   -- title/description of DID
		for key,content in pairs(data.sd) do
			if key~="dummy" then
				bytepos = math.floor(content.bitPos/8)+4
				res=calculatePacketedDiD(content, udsBuffer, bytepos)
			--	addElement(content.t, "nothing",res,0x00, "")
			end
		end
		-- addElement("<< Packeted Data","PacketedData_Menu","<",0x10, "")
		-- pageDone()
	end )
end


function testDidData(oldvalue,id)
	return udsServiceRequest("22", "AABB" , {0xCC,0xDD} , 0 , function ()
		return array2str(udsBuffer)
	end )
end



---- testing the database 

function testlocale(oldvalue,id)
	if id=="1" then  -- no locale set
		setLocale(oldvalue,nil)
		return getLocalePrintf("testlocale","004", "locale not found")
	end
	if id=="2" then  -- default locale set
		setLocale(oldvalue,"en_en")
		return getLocalePrintf("testlocale","004", "locale not found")
	end
	if id=="3" then  -- german locale set
		setLocale(oldvalue,"de_de")
		return getLocalePrintf("testlocale","004", "locale not found")
	end
	if id=="4" then  -- klingonian locale set
		setLocale(oldvalue,"kl_kl")
		return getLocalePrintf("testlocale","004", "locale not found")
	end
	if id=="5" then  -- klingonian locale set with unknown ID
		setLocale(oldvalue,"kl_kl")
		return getLocalePrintf("testlocale","foo", "locale not found")
	end
end


---- testing the database 

function testdb(oldvalue,id)
	local newArray
	local index
	local textID
	local dbName
	local colum
	local nr
	if id=="1" then  -- all correct
		dbName = "testsuiteData"
		textID = "005"
		colum = "Value"
		nr = "1"
	end
	if id=="2" then -- database does not exist
		dbName = "foo"
		textID = "005"
		colum = "Value"
		nr = "1"
	end
	if id=="3" then -- textID does not exist
		dbName = "testsuiteData"
		textID = "foo"
		colum = "Value"
		nr = "1"
	end
	if id=="4" then -- read from second column
		dbName = "testsuiteData"
		textID = "005"
		colum = "Remark"
		nr = "1"
	end
	if id=="5" then -- read from existing column
		dbName = "testsuiteData"
		textID = "005"
		colum = "foo"
		nr = "1"
	end
	if id=="6" then  -- read second row
		dbName = "testsuiteData"
		textID = "005"
		colum = "Value"
		nr = "2"
	end
	if id=="7" then  -- read second row from second column
		dbName = "testsuiteData"
		textID = "005"
		colum = "Remark"
		nr = "2"
	end
	if id=="8" then  -- read not existing row
		dbName = "testsuiteData"
		textID = "005"
		colum = "Value"
		nr = "99"
	end
	if id=="9" then  -- read not existing row from second column
		dbName = "testsuiteData"
		textID = "005"
		colum = "Remark"
		nr = "99"
	end
	if id=="10" then  -- read not existing row from non existing column
		dbName = "testsuiteData"
		textID = "005"
		colum = "foo"
		nr = "99"
	end
	if dbLookup ~= nil then -- db functionality implemented?
		newArray= dbLookup(dbName..".oodb",textID) -- look up for the given category db and for the given LOCALE
		return getDBEntry( newArray, colum ,  nr , textID.." not found" )
	else -- no db functionality available
		return "noDBlookup"
	end
end


--- writes something, sends the output clear command and write something else

function clearOutput(oldvalue,id)
	serDisplayWrite("try to clear output")
	serDisplayWrite("foo","clear")
	serDisplayWrite("if you read only this, then it works..")
	return oldvalue
end



--- sends the output clear command,write content and saves it with file Dialog

function saveOutputAs(oldvalue,id)
	serDisplayWrite("foo","clear")
	serDisplayWrite(oldvalue)
	serDisplayWrite(oldvalue,"saveas")
	return oldvalue
end

--- sends the output clear command,write content and saves it directly

function saveOutput(oldvalue,id)
	serDisplayWrite("foo","clear")
	serDisplayWrite(oldvalue)
	serDisplayWrite(oldvalue,"save")
	return oldvalue
end

--- sends the output clear command,write content and saves it directly

function saveBuffer1(oldvalue,id)
	serDisplayWrite("foo","clearall") -- clears all buffers
	serDisplayWrite("buffer1","setbuffer") -- test the create buffer function
	serDisplayWrite(oldvalue)
	serDisplayWrite(oldvalue,"saveas") -- test the save function
	serDisplayWrite("display","setbuffer")
	serDisplayWrite("foo","clear")
	serDisplayWrite(oldvalue)
	serDisplayWrite("written and saved")
	serDisplayWrite("buffer1","setbuffer")
	serDisplayWrite("foo","clear")
	serDisplayWrite(oldvalue.."_clear","save")
	serDisplayWrite("buffer2","setbuffer")
	serDisplayWrite("foo","clear")
	serDisplayWrite("one line\n")
	serDisplayWrite(oldvalue.."_append","save")
	serDisplayWrite("second line\n")
	serDisplayWrite(oldvalue.."_append","append")
	serDisplayWrite("foo","close")
	serDisplayWrite("display","setbuffer")
	serDisplayWrite("buffer1 cleared and saved as"..oldvalue.."_clear")
	serDisplayWrite("buffer2 created, wrote, saved, wrote and append as"..oldvalue.."_append")
	return oldvalue
end

--- sends a buffer sequence 

function bufferSequence(oldvalue,id)
	serDisplayWrite("foo","clearall") -- clears all buffers
	serDisplayWrite("buffer3","setbuffer") 
	serDisplayWrite("write in buffer3-1")
	serDisplayWrite("buffer4","setbuffer")
	serDisplayWrite("foo","clear")
	serDisplayWrite("write in buffer4-1")
	serDisplayWrite("buffer3","setbuffer")
	serDisplayWrite("write in buffer3-2")
	serDisplayWrite("buffer4","setbuffer")
	serDisplayWrite("write in buffer4-2")
	serDisplayWrite("buffer3","setbuffer")
	serDisplayWrite("foo","clear")
	serDisplayWrite("buffer4","setbuffer")
	serDisplayWrite("foo","close")
	return oldvalue
end

function writeSlackMsg(oldvalue,id)
	-- this is just a test, for all Slack options visit https://api.slack.com/incoming-webhooks
	-- contacting slack as jsonrpc server, suppling the parameters as lua table
	jsonrpcparams={username="RobotFrame",text="This is a script message"}
	newfile = ioInput(oldvalue,jsonrpcparams,"json")
	-- reading the whole result as text string
	res = ioRead("*all")
	return "-"..res.."-"
end




myfile="test"

function openIOFile(oldvalue,id)
	-- harmless: open a file. If success, the function returns the file path
	newfile = ioInput(oldvalue,"","direct")
	if newfile ~="" then
		oldvalue = newfile
	end
	return "-"..oldvalue.."-"
end

function openURL(oldvalue,id)
	-- more intesting: Reads a URL
	newfile = ioInput("https://httpbin.org/html",".egal","html")
	return "-"..newfile.."-"
end

function jsonStringService(oldvalue,id)
	-- contacting a jsonrpc server, suppling the parameters as json string
	ioInput("http://www.raboof.com/projects/jayrock/demo.ashx",' { "method": "add", "params": {"a":2,"b":3}, "id": 1}',"json")
	-- reading the whole result as tex string
	res = ioRead("*all")..""
	return "-"..res.."-"
end

function jsontableService(oldvalue,id)
	-- contacting a jsonrpc server, suppling the parameters as lua table
	jsonrpcparams={method="add",params={a=3,b=4},id=2}
	newfile = ioInput("http://www.raboof.com/projects/jayrock/demo.ashx",jsonrpcparams,"json")
	-- reading the whole result as text string
	res = ioRead("*all")
	return "-"..res.."-"
end

function jsonrpcService(oldvalue,id)
	-- contacting a jsonrpc server, suppling the parameters as lua table
	jsonrpcparams={method="add",params={a=5,b=6},id=2}
	ioInput("http://www.raboof.com/projects/jayrock/demo.ashx",jsonrpcparams,"json")
	-- reading the whole result as lua table..
	res = ioRead("*json")
	 -- here we certainly will need some convinience functions for the json-rpc format
	
	if res ~=nil then
		if res.result ~=nil then
			return "-"..res.result.."-"
		else
			return res.error
		end
	else
		return "no valid answer :-("
	end
end


function readFirstLine(oldvalue,id)
	return ioRead("*line")
end




function hashHoleFile(oldvalue,id)
	return ioRead("*sha256")
end

---------------------- Dialog tests --------------------------------------



function userAlert(oldvalue,id)
	msgBox("alert","Alert Test", "main text", "default value")
	return "done"
end



function userConfirm(oldvalue,id)
	return msgBox("confirm","Confirm Test", "main text", "default value")
end


function userPrompt(oldvalue,id)
	return msgBox("prompt","Prompt Test", "Please enter 123", "default value")
end

--------------- crash tests -------------------------



function callCrash(oldvalue,id)
	callNotExistingFunction() -- this function does not exist
	return "Still Alive after callCrash"
end



---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	identifyOOBDInterface()
	
	-- do all the bus settings in once
	deactivateBus()
	echoWrite("p 1 1 1 0\r") -- activate Diagnostic protocol
	activateBus()
	-- settings for module specific Request/Response IDs are done within the Combobox from the Mainscreen (selectModuleID)
	-- start the mail loop
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	deactivateBus()
	return oldvalue
end

function Main(oldvalue,id)
	openPage("OOBD Testsuite")
	addElement("Sensor Data >", "createCMD01Menu",">>>",0x1, "")
	addElement("Greetings", "greet","",0x0, "")
	addElement("clear output", "clearOutput","",0x0, "")
	addElement("Save output As", "saveOutputAs","/media/ram/saveOutputAs.txt",0x0, "")
	addElement("Save output", "saveOutput","/media/ram/saveOutput.txt",0x0, "")
	addElement("create Buffer1", "saveBuffer1","/media/ram/buffer1.txt",0x0, "")
	addElement("User alert", "userAlert","",0x0, "")
	addElement("User confirm", "userConfirm","",0x0, "")
	addElement("User prompt", "userPrompt","",0x0, "")
	addElement("buffer sequence", "bufferSequence","",0x0, "")
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return