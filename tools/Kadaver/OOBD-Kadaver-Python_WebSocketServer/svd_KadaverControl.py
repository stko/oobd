#!/usr/local/bin/python
# -*- coding: utf-8 -*-

from json import loads , dumps
from base64 import encodestring, decodestring
from websocket import create_connection
from time import gmtime, strftime, sleep
import sys
import ssl
import os
from optparse import OptionParser


lasterror=""
result=""
actStep=""
loopCount=1
myPID=str(os.getpid())
textNoErr="all Ok"
alright=False
errorCount=0

parser = OptionParser(usage="usage: %prog [options]", version="%prog 1.0")
parser.add_option("--url", default='ws://localhost:9000', type='string', action="store", dest="url", help="hostname (ws://localhost:9000)")
parser.add_option("--cert", default='', type='string', action="store", dest="cert", help="cert file for SSL verification")
parser.add_option("--loop", default=False, action="store_true", dest="loop", help="run in a loop instead only once")
parser.add_option("--singleshot", default=False, action="store_true", dest="singleShot", help="runs until error condition is reached")
parser.add_option("--debug", default=False, action="store_true", dest="debug", help="debug output")
parser.add_option("--maxerror", default=3,  type='int', action="store", dest="errorCount", help="Nr of continious errors before set error flag")

(options, args) = parser.parse_args()


if options.cert!='':
	
	#context = ssl.SSLContext(ssl.PROTOCOL_SSLv23)
	#context.verify_mode = ssl.CERT_REQUIRED
	#context.check_hostname = True
	#context.load_verify_locations(sys.argv[2])
	sslOptions=sslopt={
		"ca_certs" : options.cert,
		"ssl_version": ssl.PROTOCOL_TLSv1,
		"check_hostname": False
		}
else:
	sslOptions=sslopt={"cert_reqs": ssl.CERT_NONE}
	
while(1):
	try:
		actStep="(1) init socket 1"
		ws_dongle = create_connection(options.url, timeout=3,sslopt=sslOptions)
		actStep="(2) init socket 2"
		ws_master = create_connection(options.url, timeout=3,sslopt=sslOptions)
		if options.debug:
			print ("init channel")
		actStep="(3) send dongle init"
		ws_dongle.send(dumps({"reply": encodestring("init channel"), "channel" : encodestring(myPID) }))
		if options.debug:
			print ("send master init")
		actStep="(4) send master init"
		ws_master.send(dumps({"msg": encodestring("send master request"), "channel" : encodestring(myPID) }))
		if options.debug:
			print ("Receiving at dongle side")
		actStep="(5) receive master init"
		result =  ws_dongle.recv()
		if options.debug:
			print ("Received as dongle:'%s'" % result)
			print ("Answer from  dongle")
		actStep="(6) send dongle answer"
		ws_dongle.send(dumps({"reply": encodestring("Answer from  dongle"), "channel" : encodestring(myPID) }))
		if options.debug:
			print ("Receiving as master")
		actStep="(7) receive dongle answer"
		result =  ws_master.recv()
		if options.debug:
			print ("Received as master:'%s'" % result)
		actStep="(8) close dongle socket"
		ws_dongle.close()
		actStep="(9) close master socket"
		ws_master.close()
		error=textNoErr
		errorCount=0
	except Exception as n:
		errorCount+=1
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
	if (lasterror!=error or options.debug):
		print ("%s\tLoop:\t%d\tErrorcount:\t%d\t%s" % (strftime("%Y-%m-%d %H:%M:%S", gmtime()),loopCount, errorCount, error))
		if options.debug:
			print ("lastreceived:'%s' at step: %s" % (result,actStep))
	lasterror=error
	alright = error==textNoErr
	if (options.loop!=True): # in case, we don't want to run endless
		if (alright or (not alright and errorCount> options.errorCount)): #it's either ok or the error level is rechead
			if (not options.singleShot): # we dont want to wait until the error level is reached, so we quit
				break
			else: # otherways we need to check if we really have an error
				if( not alright and errorCount> options.errorCount): #error reached?
					break # quit the program
	loopCount+=1
	sleep(5)
if (alright):
	exit(0)
else:
	exit(1)
