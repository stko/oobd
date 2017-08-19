#!/usr/bin/python
# -*- coding: utf-8 -*-
'''
#-----------------for development only: Load the can module from the local subfolder
import os, sys, inspect

# realpath() will make your script run, even if you symlink it :)
cmd_folder = os.path.realpath(os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0]))
if cmd_folder not in sys.path:
	sys.path.insert(0, cmd_folder)


# use this if you want to include modules from a subfolder
cmd_subfolder = os.path.realpath(os.path.abspath(os.path.join(os.path.split(inspect.getfile( inspect.currentframe() ))[0],"pysotp")))
if cmd_subfolder not in sys.path:
	sys.path.insert(0, cmd_subfolder)
from Can import *	
#---------------------------	
'''

from pysotp import *
from modules import modules

import time
import struct
from flashecu import download
import binascii

from parameterlist import parameters


try:
	from FordStuff import key_from_seed
	import FordStuff

except ImportError:
	# dummy declarations, in case FordStuff is not installed
	def key_from_seed(a,b,c): 
		return False
	
	class FordStuff: 
		secret_keys = {
		}

class SampleModule(IsoTp):	
	def send(self,request,trials=3):
		while trials>0:
			try:
				hexDump=request
				if isinstance(hexDump, bytearray):
					#print type(hexDump).__name__
					hexDump=binascii.hexlify(hexDump)
					hexDump=" ".join("%s"%hexDump[i:i+2] for i in range(0,len(hexDump),2))
				answer=self.iso_tp_send_and_receive( request,500)
				while (answer != None and answer[0]==0x7f and answer[2]==0x78):
					answer=self.iso_tp_receive( 500)
				dump=binascii.hexlify(answer)
				dump=" ".join("%s"%dump[i:i+2] for i in range(0,len(dump),2))
				print("\n:({2}) {0} ->\n({3}) {1}".format(hexDump,dump,len(request),len(answer)))
				return answer
			except Exception as inst:
				#print(":({2}) {0} -> error:{1}".format(hexDump,inst,len(request)))
				#print type(inst)     # the exception instance
				#print inst.args      # arguments stored in .args
				#print inst           # __str__ allows args to be printed directly		print(": {0} -> -".format(request))
				#return bytearray()
				trials-=1
		print(":({1}) {0} -> Timeout".format(hexDump,len(request)))
		return bytearray()
		
	def accessMode(self, opMode, seclevel):
		# first of all switch into the requested session mode
		udsBuffer=self.send("10 {:02X}".format(opMode))
		if len(udsBuffer)>0:
			if udsBuffer[0]!=0x50:
				print(": {0}".format(binascii.hexlify(udsBuffer)))
				return -1
		else:
			return -2
		if seclevel == 0 :
			return 0 # just changed the mode, nothing more to do
		udsBuffer=self.send("27 {:02X}".format(seclevel))
		if len(udsBuffer)>0:
			if udsBuffer[0]==0x67:
				if udsBuffer[1] == seclevel:
					if udsBuffer[ 2 ] == 0  and  udsBuffer[ 3 ] == 0  and  udsBuffer[ 4 ] == 0:
					# access already granted previously..
						return seclevel
					else:
						# to let FordStuff act correcty, we need to put the code to use first into the right place before call the seed calculation
						if "seccodes" in self.module and  seclevel in self.module["seccodes"]:
							FordStuff.secret_keys [self.module["tx"]]=self.module["seccodes"][seclevel]
						else:
							FordStuff.secret_keys [self.module["tx"]]="00 00 00 00 00"
						seed=key_from_seed(self.module["tx"],"{:02X} {:02X} {:02X}".format( udsBuffer[ 2 ] , udsBuffer[ 3 ] , udsBuffer[ 4 ] ),1)
						udsBuffer=self.send("27 {:02X} {:02X} {:02X} {:02X}".format( seclevel + 1 , seed[ 0 ] , seed[ 1 ] , seed[ 2 ] ) )
						if len(udsBuffer)>0:
							if udsBuffer[ 0 ] == 0x67 and udsBuffer[ 1 ] == seclevel + 1 :
								return seclevel
							else:
								return -4
						else:
							return -5
				else:
					return -6
			else:
				print ("Error response: {:02X} {:02X}".format(udsBuffer[0],udsBuffer[2]))
				return -7
		else:
			return -8


	def simplesend(self,prefix,mask, byteseq):
		answer=self.send( prefix+" "+mask+" "+byteseq)
		print(": {0} -> {1}".format(request,binascii.hexlify(answer)))
		time.sleep( 0.3 )
		#self.iso_tp_send_and_receive(prefix+" "+mask+" 00 00 00 00",500)


def busOff():
	global broadcast
	broadcast.send('10 82',trials=1)	
	time.sleep(0.6)
	broadcast.send('3E 80',trials=1)	
	time.sleep(0.07)
	broadcast.send('10 82',trials=1)
	time.sleep(0.6)

def busOn():
	global broadcast
	broadcast.send('11 81',trials=1)



can_isotp_start()
#raw = RawCan()
#raw.raw_can_map_channel('my_channel_1')
#raw.raw_can_send('my_channel_1', '01 23 XX 45')
myModule = SampleModule("can0",modules["MBCM"])
broadcast = SampleModule("can0",modules["broadcast"])

bootloader=str(myModule.send('22 F1 80')[4:]).strip('\0')
print (">{0}<".format(bootloader))
try:
	print parameters[bootloader]['flashfile']
except:
	exit(1) # no config for this file available

busOff()
time.sleep(2)



udsBuffer=myModule.send('22 D1 00') # read ECU state
if udsBuffer[3]!=1: #not in Op Mode?
	myModule.send('10 01') # go into op mode
udsBuffer=myModule.send('22 D1 2B') # read config address
config_address=udsBuffer[3:7]
vin_buffer=myModule.send('22 F1 14') # read VIN
struct.pack_into("13s",vin_buffer,14,"OOBD was here")
print binascii.hexlify(vin_buffer)
old_config=myModule.send('22 F1 06') # read actual config
if len(old_config)>0:
	success=False
	for tryouts in range(0,5):
		if myModule.accessMode( 2, 1 )>-1:
			success=True
			break
	if success:
		print "Sec Access 1 granted"
		flashFile=download(myModule,parameters[bootloader]['flashfile'],128)
		try:
			print flashFile.header["call"]
			print binascii.hexlify(struct.pack(">i",flashFile.header["call"]))
			myModule.send(bytearray.fromhex('31 01 03 01') +struct.pack(">i",flashFile.header["call"]))# activate SBL
		except:
			pass
		