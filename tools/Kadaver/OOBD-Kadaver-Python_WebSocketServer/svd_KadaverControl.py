import xmlrpclib
#server = xmlrpclib.Server('http://localhost:9001/RPC2')
from json import loads , dumps
from base64 import encodestring, decodestring
from websocket import create_connection
from time import gmtime, strftime, sleep
import sys
import os

lasterror=""
result=""
debug=False
actStep=""
loopCount=1
myPID=str(os.getpid())

if len(sys.argv)!=2:
	wsServerName="ws://localhost:9000/"
else:
	wsServerName=sys.argv[1]
while(1):
	try:
		actStep="(1) init socket 1"
		ws_dongle = create_connection(wsServerName, timeout=3)
		actStep="(2) init socket 2"
		ws_master = create_connection(wsServerName, timeout=3)
		if debug:
			print "init channel"
		actStep="(3) send dongle init"
		ws_dongle.send(dumps({"reply": encodestring("init channel"), "channel" : encodestring(myPID) }))
		if debug:
			print "send master init"
		actStep="(4) send master init"
		ws_master.send(dumps({"msg": encodestring("send master request"), "channel" : encodestring(myPID) }))
		if debug:
			print "Receiving at dongle side"
		actStep="(5) receive master init"
		result =  ws_dongle.recv()
		if debug:
			print "Received as dongle:'%s'" % result
			print "Answer from  dongle"
		actStep="(6) send dongle answer"
		ws_dongle.send(dumps({"reply": encodestring("Answer from  dongle"), "channel" : encodestring(myPID) }))
		if debug:
			print "Receiving as master"
		actStep="(7) receive dongle answer"
		result =  ws_master.recv()
		if debug:
			print "Received as master:'%s'" % result
		actStep="(8) close dongle socket"
		ws_dongle.close()
		actStep="(9) close master socket"
		ws_master.close()
		error="all Ok"
	except Exception as n:
		error=  str(n) + " at step " + actStep
		try:
			ws_dongle
		except NameError:
			ws_dongle = None
		if ws_dongle != None:
			ws_dongle.close()
		try:
			ws_master
		except NameError:
			ws_master = None
		if ws_dongle != None:
			ws_master.close()
		if debug:
			print "lastreceived:'%s' at step: %s" % (result,actStep)
	if (lasterror!=error):
		print strftime("%Y-%m-%d %H:%M:%S", gmtime()),"Loop:",loopCount, error
	lasterror=error
	loopCount+=1
	sleep(5)

