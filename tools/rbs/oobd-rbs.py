# -*- coding: utf-8 -*-
import sys
import socket
import time
import struct
import xml.etree.ElementTree as ET
import pprint

import isotp
import mdx

if len(sys.argv) < 3:
	print(sys.argv[0]+": The OOBD Rest Bus Simulator")
	print('A part of the OOBD Open Onboard Diagnostic tool set - visit www.OOBD.org for more')
	print()
	print('Usage: '+sys.argv[0]+' <socketcandevice> <mdx_busname> file[s]')
	print('<socketcandevice>: SocketCAN CAN device name (can0, slcan0 etc.) on which is listening to')
	print('<mdx_busname>: the bus name as shown in the MDX file to which the CAN device is bound to')
	print('file(s): one or many mdx files which are read into memory sorted by bus and module id')
	sys.exit(0)
 
isotp.initcan(sys.argv[1])
myBus=sys.argv[2]
#isotp.recvTele(100)
modules={}
for i in range(3,len(sys.argv)):
	module = mdx.MDX(sys.argv[i])
	bus, moduleID = module.getBusData()
	print(bus, moduleID, module.name )
	#module.answerDiD("22","did_0202")
	try:
		modules[bus] # do we know that bus already
	except Exception as n:
		modules[bus]={} # create empty bus
	modules[bus][moduleID]=module
pprint.pprint(modules[sys.argv[2]])	


can_id, msg = isotp.recvTele(50)
service = "%02X" % ( msg[0])
did= "%02X%02X" % ( msg[1] , msg[2])
print (can_id, msg)
print (service, did)
try:
	answer=modules[myBus]["0x"+can_id].answerDiD(service,"did_"+did)
	print ("Answer len:", len(answer))

except Exception as n:
		print ("not found - bus: ",myBus, "can_id: ", can_id)
		pprint.pprint(modules[myBus]["0x"+can_id])