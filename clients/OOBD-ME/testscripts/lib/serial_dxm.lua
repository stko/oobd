
-- include the basic connectivity



--- use the following lines for debugging in lua editor
---[[
openPage = openPageCall
addElement = addElementCall
pageDone = pageDoneCall
--]]
---[[
serFlush = serFlushCall
serWrite = serWriteCall
serWait = serWaitCall
serReadLn = serReadLnCall
serDisplayWrite = serDisplayWriteCall
--]]

--[[
readcount= 1
input = {}
input[1]="Searching"
input[2]="41 00 FF FF FF FF"
input[3]=">"
input[4]="41 14 FF FF FF FF"
input[5]=">"


function serReadLn()
	res= input[readcount]
	print ("read from input: ",res, readcount)
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
	print ("Serwrite:" ,data)
end

function serFlush()
end

--]]


--[[
function openPage(title)
	print("Start Menu generation ", title);
end

function addElement(title, func , intial,flags , id)
	print("<---");
	print("title: ", title);
	print("function: ", func);
	print("id: ", id);
	print("--->");
end

function pageDone()
	print("Show Menu");
end



--]]

-- define the receive buffer

udsBuffer = {}
udslen =0

-- the global receiving routine. Trys to read single- or multiframe answers from the dxm and stores it in udsbuffer, setting the reveived length in udslen

function receive()
	udsLen=0
	answ=""
	answ=serReadLn(2000, true)
	if answ == "" then
		return 0
	else
		doLoop = true
		byteCount= udsLen+1 -- auf Abbruchbedingung setzen
		while doLoop and  answ ~="" do
			nChar = string.byte(answ)
			if nChar >=48 and nChar <=57 then  -- an 1. Stelle steht eine Zahl-> positive Antwort
				if string.sub(answ,2,2) == ":" then
					answ = string.sub(answ,4) -- wegschneiden des Zaehlers am Anfang
					while byteCount <=udsLen  and answ ~="" do
						byteStr= string.sub(answ,1,2)
						answ = string.sub(answ,4)
						udsBuffer[byteCount]=tonumber(byteStr,16)
						byteCount = byteCount + 1
					end
					if byteCount >=udsLen then
						doLoop=false
						serWait(">",2000)
					else
						answ=serReadLn(2000, true)
					end
				else
					if string.sub(answ,3,3) == " " then -- singleframe
						udsLen=1
						while answ ~="" do
							byteStr= string.sub(answ,1,2)
							answ = string.sub(answ,4)
							udsBuffer[udsLen]=tonumber(byteStr,16)
							udsLen = udsLen +1
						end
						udsLen = udsLen - 1
						doLoop = false
					else -- Multiframe
						-- die Längenangabe
						udsLen=tonumber(answ,16)
						byteCount=1
						answ=serReadLn(2000, true)

					end
				end
			else
				answ=serReadLn(2000, true)
			end

		end
	end
	return  udsLen
end
