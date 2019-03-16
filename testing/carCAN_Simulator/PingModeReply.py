# -*- coding: utf-8 -*-
import sys
import socket
import pprint
import struct
import time

quit

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

def sendTele(can_id, hex_data):
	global s
	msg =bytearray.fromhex(hex_data)
	try:
		s.send(build_can_frame(can_id, msg))
	except socket.error:
		print('Error sending CAN frame')
	msg = msg.ljust(8, b'\x00')
	print ("sended: 0x%02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( can_id, can_dlc, msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] ) )


while True:
	# create a raw socket and bind it to the given CAN interface
	s = socket.socket(socket.AF_CAN, socket.SOCK_RAW, socket.CAN_RAW)
	notConnected=True
	while notConnected:
		try:
			s.bind((sys.argv[1],))
			notConnected=False
		except:
			print ("wait for can device to come up..")
			time.sleep(0.5)
	canDeviceIsAvailable=True;
	while canDeviceIsAvailable:
		msg =bytearray()
		try:
			cf, addr = s.recvfrom( 16 ) # buffer size is 1024 bytes
		except:
			print ("wait..")
			time.sleep(0.5)
			canDeviceIsAvailable=False;
			break
		print("(Re-)Connected to CAN")
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
		if teleLength < 3 : 
			pid= "%02X%02X" % ( msg[1] , msg[2] )
		else:
			pid= "%02X%02X%02X" % ( msg[1] , msg[2] , msg[3])
		if pid=="190102":
			sendTele(0x7ec,"065901ff0000")
			sendTele(0x72b,"065901ca0000")
			sendTele(0x72e,"065901ca0000")
			sendTele(0x7cf,"065901ca0000")
			sendTele(0x7ee,"065901ff0000")
			sendTele(0x7d8,"065901cb0000")
			sendTele(0x74e,"065901fb0000")
			sendTele(0x7e8,"065901ff0000")
			sendTele(0x73e,"065901fb0000")

