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

TestData_0_FF50 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "ASCII Len 24" },
}
},


TestData_0_FF40 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "ASCII Len 24 with binary data" },
}
},


TestData_0_FF51 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "ASCII Len 24 with General Response Error" },
}
},


TestData_0_FF52 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "ASCII Len 24 with Timeout" },
}
},


TestData_0_FF53 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "ASCII Len 24 with no Answer" },
}
},


TestData_0_FF54 = { t = "Ascii Data", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "ASCII Len 24 with sequence error" },
}
},



-- read data for each Single value - Number

TestData_0_D100 = { t = "Active Diagnostic Session", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = { bpos = 0, blen = 1, Bpos = 0 , Blen = 8, dtype = "ENUM", t = "", ev = { 
ev_1 = { bv = 1 , t = "Default Session"},

ev_5 = { bv = 5 , t = "Supplier Specific Diagnostic Session 0x60"},
}},
}
},
TestData_0_F162 = { t = "Software Download Specification Version", sd = {
sd_00 = { bpos = 0, blen = 1, Bpos = 0 , Blen = 8, dtype = "ENUM", t = "", ev = { 
ev_0 = { bv = 0 , t = "SWDL 31808456 Issue P01"},
ev_1 = { bv = 1 , t = "SWDL 31808456 Issue 001"},

}},
}
},
TestData_0_F163 = { t = "Numeric Enum Data", sd = {
sd_00 = { bpos = 0, blen = 1, Bpos = 0 , Blen = 8, dtype = "ENUM", t = "", ev = { 
ev_1 = { bv = 1 , t = "Enum Byte 0 Value 1"},
ev_3 = { bv = 3 , t = "GGDS 31810161 Issue 003"},
}},
}
},
TestData_0_D91C = { t = "RSC/ARP Rollover Stability Control/Active Rollover Protection", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 2 , Bpos = 0 , Blen = 16 , mult = 1 , offset = 0 , min = "0" , max = "65535" , unit = "counts, counts, counts" , dtype = "UNSIGNED" , t = "RSC/ARP Rollover Stability Control/Active Rollover Protection"} ,
}
},

TestData_0_2B0D = { t = "Brake Fluid Line Hydraulic Pressure", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = {  bpos = 0 , blen = 2 , Bpos = 0 , Blen = 16 , mult = 0.333333333333 , offset = 0 , min = "-10922.6666666557" , max = "10922.3333333224" , unit = "Bar" , dtype = "SIGNED", t = "Brake Fluid Line Hydraulic Pressure"} ,
}
},



-- read data for each Packeted value - Number

TestData_0_D10E = { t = "HSCAN Network Management State", sev_r = "22", ses_r ="01;03;", sd = {
sd_00 = { bpos = 0, blen = 1, Bpos = 0 , Blen = 8, dtype = "ENUM", t = "HSCAN Network Management State - Latest State", ev = { 
ev_0x00 = { bv = 0x00 , t = "Reserved"},
ev_0x01 = { bv = 0x01 , t = "Off"},
}},
}
},
TestData_0_F166 = { t = "NOS Message Database #1 Version Number", sd = {
sd_00 = {  bpos = 0 , blen = 1 , Bpos = 0 , Blen = 8 , mult = 1 , offset = 0 , min = "-" , max = "-" , unit = "" , dtype = "BCD", t = "Year"} ,
}
},
TestData_0_F180 = { t = "Boot Software Identification", sd = {
sd_00 = {  bpos = 0 , blen = 1 , Bpos = 0 , Blen = 8 , mult = 1 , offset = 0 , min = "0" , max = "255" , unit = "Count" , dtype = "UNSIGNED" , t = "Number Of Modules (ISO14229 compatibility - always set to 01h)"} ,
sd_10 = {  bpos = 1 , blen = 24 , Bpos = 0 , Blen = 192 , dtype = "ASCII", t = "Boot Software Identifcation Data (Part no. of the bootloader)" },
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





















---------------------- Vehicle Info Menu --------------------------------------






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
	openPage("OOBD-ME Main")
	addElement("Sensor Data >", "createCMD01Menu",">>>",0x1, "")
	addElement("Greetings", "greet","",0x0, "")
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return