
openPage = openPageCall
addElement = addElementCall
pageDone = pageDoneCall
openChannel = openChannelCall
dbLookup = dbLookupCall

serFlush = serFlushCall
serWrite = serWriteCall
serWait = serWaitCall
serReadLn = serReadLnCall
serDisplayWrite = serDisplayWriteCall
openXCVehicleData =openXCVehicleDataCall


function sendOpenXC(oldvalue,id)
	if id=="1" then openXCVehicleData({timestamp= 1332794087.675514, name= "longitude", value= -83.237427}) end
	if id=="2" then openXCVehicleData({timestamp= 1351176963.426318, name= "door_status", value= "passenger", event= true}) end
	if id=="3" then openXCVehicleData({timestamp= 1351176963.438087, name= "fine_odometer_since_restart", value= 0.0}) end
	if id=="4" then openXCVehicleData({timestamp= 1351176963.438211, name= "brake_pedal_status", value= false}) end
	if id=="5" then openXCVehicleData({timestamp= 1351176963.438318, name= "transmission_gear_position", value= "second"}) end

	return "done"
end



---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- coonect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	return oldvalue
end

function Main(oldvalue,id)
	openPage("OOBD openXC Test")
        addElement("Send longitude", "sendOpenXC","-83.237427", 0x4, "1" )
        addElement("Send door_status", "sendOpenXC","passenger", 0x4, "2" )
        addElement("Send fine_odometer_since_restart", "sendOpenXC","0.0", 0x4, "3" )
        addElement("Send brake_pedal_status", "sendOpenXC","false", 0x4, "4" )
        addElement("Send transmission_gear_position", "sendOpenXC","second", 0x4, "5" )
 	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

