# -*- coding: utf-8 -*-
import sys
import socket
import time
import pprint

import struct

# to get yaml, do sudo apt-get install python3-yaml


from yaml import load, dump

pp = pprint.PrettyPrinter(indent=4)

try:
	from yaml import CLoader as Loader
	from yaml import CDumper as Dumper
except ImportError:
	from yaml import Loader, Dumper


      

simu=load(open("carsimCAN.yaml"), Loader=Loader)

#print (dump(simu))

quit
      
L1 = simu.keys()
#for x in L1: 
#	print (x)
print ("Start")
# CAN frame packing/unpacking (see `struct can_frame` in <linux/can.h>)
can_frame_fmt = "=IB3x8s"
 
def build_can_frame(can_id, data):
	can_dlc = len(data)
	data = data.ljust(8, b'\x00')
	return struct.pack(can_frame_fmt, can_id, can_dlc, data)

def dissect_can_frame(frame):
	can_id, can_dlc, data = struct.unpack(can_frame_fmt, frame)
	return (can_id, can_dlc, data[:can_dlc])

if len(sys.argv) != 2:
	print('Provide CAN device name (can0, slcan0 etc.)')
	sys.exit(0)


def sendTele(data):
	pp.pprint(data)
	msg =bytearray()
	d=data["d"]
	time.sleep(0.01 * data["t"])
#	msg[2]=data["e"]
	#	print dump(data)
	for i in range(0,8):
	#	    print d[i]
		msg+=b'\x00'
	for i in range(len(d)):
	#	    print d[i]
		msg[0+i]=d[i]
	#	msg+=d[i] & 0xFF
	for i in range(len(d),7):
		msg[1+i]=0
	global s
	global can_id
	global can_dlc
	try:
#		s.send(build_can_frame(can_id, b'\x01\x02\x03'))
		s.send(build_can_frame(can_id, msg))
	except socket.error:
		print('Error sending CAN frame')

	print ("sended: 0x%02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( can_id, can_dlc, msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] ) )

def generateFrame(bytesSended,typeID, service):
	
	'''
	
	generic frame generation for all 0xFxxx DiDs
	
	typeID definitions:
	HNibble: Length of Answer
	0 :   1 Byte
	1 :   2 Byte
	2 :   4 Byte
	3 :   8 Byte
	4 :  16 Byte
	5 :  32 Byte ASCII
	6 : 256 Bytes
	7: 4905 Bytes

	LNibble: Error types
	0 : correct Answer
	1 : General Responce Error 7F
	2 : Timeout (200ms)
	3 : no Answer
	4 : Sequence error (only if multiframe)
	5 : Answer too short (dlc = norminal length -1 )
	6 : Answer too long (dlc  = norminal length + 1 )
	'''
	tClass=int(typeID[:1], 16)
	tType=int(typeID[1:], 16)
	print (tClass,tType)
	serviceResponse = int(service, 16) +0x40
	telegram={}
	telegram["t"]=2
	telegram["e"]=0
	telegram["d"]=[]
	if tType == 2:
		telegram["t"]=20 # timeout = 200 ms
	if tType == 1: # generate general response
		dlc=4
		length=4
		bytesSended=length
		telegram["d"].append(dlc)
		telegram["d"].append(0x7f)
		telegram["d"].append(0xFF)
		telegram["d"].append(int(typeID,16))
		telegram["d"].append(0xFF)
	else:
		if tClass < 6:
			length = 2 ** tClass
		elif tClass == 6:
			length = 256
		else:
			length = 4095-3
		dlc=length+3
		print ("length, dlc: ", length,dlc)
		if bytesSended == 0: # initial  frame
			if tType == 5:
				dlc -=1 # make answer too short
			if tType == 6:
				dlc +=1 # make answer too short
			if length < 7: # single frame
				telegram["d"].append(dlc)
				telegram["d"].append(serviceResponse)
				telegram["d"].append(0xFF)
				telegram["d"].append(int(typeID,16))
				start=4
				
			else: # multi frames
				telegram["d"].append(16 + dlc // 256)
				telegram["d"].append(dlc % 256)
				telegram["d"].append(serviceResponse)
				telegram["d"].append(0xFF)
				telegram["d"].append(int(typeID,16))
				start=5
			while start<8 and bytesSended <= length :
				if tClass != 5: # if no ASCII type
					contentValue = bytesSended % 16
					telegram["d"].append( contentValue * 16 + (15 - contentValue))
				else:
					contentValue = bytesSended % 26
					telegram["d"].append( contentValue + 65 )
				start +=  1
				bytesSended += 1
			while start<8 : # fill frame
				telegram["d"].append( 0 )
				start +=  1
		else: # consecute frames 
			if tType==4: # create wrong sequence count
				sequenceCount=((bytesSended- 2) // 7 + 1 + 1 ) % 16
			else:
				sequenceCount=((bytesSended- 2) // 7 + 1 ) % 16
			telegram["d"].append(32 + sequenceCount )
			print ("bytesSended, Sequence count" , bytesSended ,sequenceCount )
			start=1
			while start<8 and bytesSended <= length :
				if tClass != 5: # if no ASCII type
					contentValue = bytesSended % 16
					telegram["d"].append( contentValue * 16 + (15 - contentValue))
				else:
					contentValue = bytesSended % 26
					telegram["d"].append( contentValue + 65 )
				start +=  1
				bytesSended += 1
			while start<8 : # fill frame
				telegram["d"].append( 0 )
				start +=  1

	print ("generic send, type=",tType)
	pp.pprint(telegram)
	if tType != 3 : # no answer
		sendTele(telegram)
#	sendTele({'d':[0x30,0x30,0x0F,0x00],'t':2,'e':0})
	if length <= bytesSended : # done?
		bytesSended = -1 # stops further sends
	return bytesSended
      
      
  
while True:
	# create a raw socket and bind it to the given CAN interface
	s = socket.socket(socket.AF_CAN, socket.SOCK_RAW, socket.CAN_RAW)
	notConnected=True
	while notConnected:
		try:
			s.bind((sys.argv[1],))
			notConnected=False
		except:
			print ("wait..")
			time.sleep(0.5)
	


	canDeviceIsAvailable=True;
	while canDeviceIsAvailable:
		nextStep=0
		msg =bytearray()
		try:
			cf, addr = s.recvfrom( 16 ) # buffer size is 1024 bytes
		except:
			print ("wait..")
			time.sleep(0.5)
			canDeviceIsAvailable=False;
			break
		can_id, can_dlc, data=dissect_can_frame(cf)
		if can_id == 0x7DF:
			can_id=0x7E0 # changing functional address to answer address of ECU
		if can_id & 0x700 !=0x700 or can_id & 0x8 !=0: 
			#print ("No diagnostic tester frame: CAN ID 0x%02X discarded" % ( can_id) )
			continue
		msg+=data # if it's no diagnotic frame, don't handle it
		print ("received: 0x%02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( can_id, can_dlc, msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] ) )
		can_id=can_id | 8 # changing the physical address from ECU to tester
		frameType = (msg[0] & 0xF0 ) / 16
		teleLength = msg[0] & 0x0F 
		print (frameType)

		##############  Single Frame ###############
		if frameType == 0: 
			i = 1
			if teleLength < 3 : 
				pid= "%02X%02X" % ( msg[i+0] , msg[i+1] )
			else:
				pid=""
				for count in range(teleLength):
					pid+= "%02X" % ( msg[i+count]) 
			print ("Single Frame")
			nextStep=1 # send the answer
			bytesSended=0

		############ First Frame  #################
		if frameType == 1: 
			i = 1
			nrOfBytes = (msg[0] & 0x0f ) *256 + msg[1]
			print ("First Frame, expecting ",nrOfBytes, " Bytes")
			nrOfBytes = nrOfBytes -6 # 6 bytes aready received
			if teleLength < 3 : 
				pid= "%02X%02X" % ( msg[i+0] , msg[i+1] )
			else:
				pid= "%02X%02X%02X" % ( msg[i+0] , msg[i+1] , msg[i+2])
			nextStep=0 # send FlowControl, wait for consecutive frames
			sendTele({'d':[0x30,0x30,0x0F,0x00],'t':2,'e':0})

		################# Consecutive Frame #############
		if frameType == 2: # consecutive frame
			nextStep=0 # do nothing, just wait
			nrOfBytes = nrOfBytes - 7 # just count the number of received bytes
			print ("Consecutive Frame, bytes remaining: ", nrOfBytes)
			if nrOfBytes <=0:
				print ("last Consecutive frame received, sending answer")
				nextStep=1 # send the answer   

		######### Flow Control #############
		if frameType == 3: # 
			nextStep=2 # send remaining Consecutive frames

		print ("pid:",pid)
		if len(simu)>0:
			try:
				dArray = simu["pid_"+pid]
			except:
				dArray=simu["pid_default"]
				print (pid, "not found, send default")
			# print dump(dArray[0])
			print ("next step: ", nextStep)
			
			
			#und hier kommt jetzt die generische Datenerzeugung..
			print ("len",len(pid), "slice",  pid[4:6])
			if len(pid)==6 and pid[2:3]=="F": # generic test case generation
				print ("generic frame, bytesSended=",bytesSended)
				if nextStep == 1: #Single Frame or end of Consecutive Frame series -> send the PID answer
					bytesSended=generateFrame(bytesSended,pid[4:6],pid[:2])
				if nextStep == 2: # send remaining consecute frames
					while bytesSended>-1:
						bytesSended=generateFrame(bytesSended,pid[4:6],pid[:2])
			else:
				if nextStep == 1: #Single Frame or end of Consecutive Frame series -> send the PID answer
					sendTele(dArray[0]) 
				if nextStep == 2: # send remaining consecute frames
					print ("Array Len: ", len(dArray))
					for i in range(len(dArray)):
						print ("i: ", i)
						if i > 0 :
							sendTele(dArray[i])
		else:
			print (pid, "nothing configured, just reply msg")
			try:
				s.send(cf)
			except socket.error:
				print('Error sending CAN frame')


