dofile("../../tools/lib_lua/serial_dxm.lua")

myfile="test"

function openIOFile(oldvalue,id)
	-- harmless: open a file. If success, the function returns the file path
	newfile = ioInput(myfile,".txt","I want to  open a file")
	if newfile ~="" then
		myfile = newfile
	end
	return "-"..myfile.."-"
end

function openURL(oldvalue,id)
	-- more intesting: Reads a URL
	newfile = ioInput("http://google.de",".egal","html")
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


function readwholeFile(oldvalue,id)
	res = ioRead("*all")..""
	return "-"..res.."-"
end


function donothing(oldvalue,id)
	myedit =oldvalue
	return myedit
end

function donothing2(oldvalue,id)
	myedit =oldvalue
	return myedit
end



---------------------- Main Menu --------------------------------------

-- This function is called at start and at each re- connect, so all neccesary (re-)initalisation needs to be done here
function Start(oldvalue,id)
--	identifyOOBDInterface()
	Main(oldvalue,id)
	return oldvalue
end

function CloseScript(oldvalue,id)
	return oldvalue
end

function Main(oldvalue,id)
	openPage("OOBD File IO Test")
        addElement("Open File", "openIOFile","No input file yet", 0x0, "" )
        addElement("Open URL", "openURL","No input file yet", 0x0, "" )
        addElement("read whole File", "readwholeFile","No input file yet", 0x0, "" )
        addElement("JSON RPC (String)", "jsonStringService","Add 2 + 3 ", 0x0, "" )
        addElement("JSON RPC (Table in)", "jsontableService","Add 3 + 4 ", 0x0, "" )
        addElement("JSON RPC (Table out)", "jsonrpcService","Add 5 + 6 ", 0x0, "" )
	pageDone()
	return oldvalue
end


----------------- Do the initial settings --------------

Start("","")
return

