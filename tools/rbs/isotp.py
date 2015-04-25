# -*- coding: utf-8 -*-
import sys
import socket
import time

import struct


# CAN frame packing/unpacking (see `struct can_frame` in <linux/can.h>)
can_frame_fmt = "=IB3x8s"
 
def build_can_frame(can_id, data):
	can_dlc = len(data)
	data = data.ljust(8, b'\x00')
	return struct.pack(can_frame_fmt, can_id, can_dlc, data)

def dissect_can_frame(frame):
	can_id, can_dlc, data = struct.unpack(can_frame_fmt, frame)
	return (can_id, can_dlc, data[:can_dlc])

def initcan(canbus):
	# create a raw socket and bind it to the given CAN interface
	global s
	s = socket.socket(socket.AF_CAN, socket.SOCK_RAW, socket.CAN_RAW)
	s.bind((canbus,))
 


def sendSingleFrame(can_id,data):
	msg =bytearray()
#	msg[2]=data["e"]
	#	print dump(data)
	for i in range(len(data)):
		msg.extend([data[i]])
	for i in range(len(data),7):
		msg.extend([0])
	global s
	try:
#		s.send(build_can_frame(can_id, b'\x01\x02\x03'))
		s.send(build_can_frame(can_id, msg))
	except socket.error:
		print('Error sending CAN frame')

	print ("sended: 0x%02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( can_id, 8, msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] ) )

def recvTele(timeout):
	state=0
	msg =bytearray()
	s.settimeout(None) # blocking mode, waits forever
	while state != 99:
		cf, addr = s.recvfrom( 16 ) # buffer size is 1024 bytes
		print('Received: can_id=%x, can_dlc=%x, data=%s' % dissect_can_frame(cf))
		can_id, can_dlc, data=dissect_can_frame(cf)
		
		print ("received: 0x%02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( can_id, can_dlc, data[0] , data[1] , data[2] , data[3] , data[4] , data[5] , data[6] , data[7] ) )
		frameType=(data[0] & 0xF0 ) / 16
		print (frameType)

		##############  Single Frame ###############
		if frameType == 0: 
			i = 1
			pid= "%02X%02X%02X" % ( data[i+0] , data[i+1] , data[i+2])
			print ("Single Frame")
			msg.extend(data[1:data[0]+1])
			state=99;
		############ First Frame  #################
		if frameType == 1: 
			i = 1
			nrOfBytes = (data[0] & 0x0f ) *256 + data[1]
			msg.extend(data[2:])
			print ("First Frame, expecting ",nrOfBytes, " Bytes")
			nrOfBytes = nrOfBytes -6 # 6 bytes aready received
			pid= "%02X%02X%02X" % ( data[i+0] , data[i+1] , data[i+2])
			# send FlowControl, wait for consecutive frames
			sendSingleFrame(can_id+8,[0x30,0x30,0x0F,0x00])

		################# Consecutive Frame #############
		if frameType == 2: # consecutive frame
			if nrOfBytes> 6:
				msg.extend(data[1:])
			else:
				msg.extend(data[1:nrOfBytes+1])
			nrOfBytes = nrOfBytes - 7 # just count the number of received bytes
			print ("Consecutive Frame, bytes remaining: ", nrOfBytes)
			if nrOfBytes <=0:
				print ("last Consecutive frame received, sending answer")
				state=99 # send the answer   

		######### Flow Control #############
		#if frameType == 3: # not supported yet

		print ("pid:",pid)
	return  ("%04X" % 	can_id) , msg


def sendTele(can_id,data, timeout=0.2):

	state=0
	global s
	s.settimeout(timeout) # time to wait for a Consecutive frame
	isMultiFrame=False
	FFalreadySend=False
	CFcount=1
	while state != 99:
		msg =bytearray()
		if len(data)<8 and not isMultiFrame:
			msg.extend([len(data)])
			msg.extend(data)
			sendSingleFrame(int(can_id)+8,msg)
			state=99;
		else:
			print("sendTele 2...")
			isMultiFrame=True
			if not FFalreadySend:
				msg.extend([0x10+(len(data) >> 4)])
				msg.extend([len(data) % 16])
				msg.extend(data[:6])
				print("sendTele 3...")
				sendSingleFrame(int(can_id)+8,msg)
				print("sendTele 4...")
				FFalreadySend=True
				cf, addr = s.recvfrom( 16 )# wait for FC
				print("sendTele 5...")
			else:
				msg.extend([0x20+CFcount])
				CFcount+=1
				if CFcount>15:
					CFcount=0
				msg.extend(data[:7])
				data=data[7:]
				sendSingleFrame(int(can_id)+8,msg)
				if len(data)<1:
					state=99
		print("sendTele 6...")


