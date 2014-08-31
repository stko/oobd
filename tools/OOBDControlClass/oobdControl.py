
# Copyright (C) 2014 Moritz Martinius <moritzmar@googlemail.com>
# 
# oobdControl.py is free software: you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# oobdControl.py is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along
# with this program.  If not, see <http://www.gnu.org/licenses/>.
#
# todo:
# - lots of commenting
# - remove referencing to ini files
# - checking if postive CAN-response is received is garbage



from bluetooth import *
import time
import configparser
import sys
import traceback
import binascii
import select
import logging
import types

# Setup config file
cfg = configparser.ConfigParser()
cfg.read("oobd_control.ini")



class OOBDControl(object):
	
	def __init__(self):
		self.socket = BluetoothSocket(RFCOMM)
		self.connectionStatus = False
		self.filterCanID=[1,"000"]
		self.filterMask=[1,"000"]
		self.currentReqId=""
		
	
	def connect(self):
		if self.connectionStatus == True:
			logging.info('You are already connected, aborting connection attempt...')
			return False
		else:
			for retries in range(1,int(cfg['MAIN']['MAX_RETRIES'])+1):
				try:
					logging.info('Connecting to Dongle (Attempt '+str(retries)+')')
					self.socket = BluetoothSocket(RFCOMM)
					if self.socket.connect((cfg['DONGLE']['MAC'], 1)): # connect to MAC-Address in the config at port 1
						self.socket.setblocking(0)		# See below
						self.socket.settimeout(0)		# Nasty bug! Doesn't work with the MS BT stack, bug in PyBluez, see Issue 40 http://code.google.com/p/pybluez/issues/detail?id=40
					time.sleep(1) # Dirty workaround for bug above
					while True:
						recvBuf = self.sendCtrlSeq(['p 0 0 0'])
						time.sleep(0.5) # Dirty workaround for bug above
						if "OOBD" in str(recvBuf[0]): 
							self.connectionStatus = True
							logging.info('Connected to OOBD-Dongle!')
							break
						else:
							self.socket.close()
							self.connectionStatus = False
							time.sleep(1)
							logging.error("Probably not an OOBD-Dongle or devices not booted yet...")
						raise(IOError)
				except:
					time.sleep(1)
					logging.error('Connection Error:'+traceback.format_exc())
					continue
				break
				
	
	def sendCtrlSeq(self, seq):
		res = []
		
		logging.info('Sending Commands:'+ str(seq))
		
		for command in seq:
			try:
				res.append(self.sendRawData(data=command+"\r", timeout=0.3))
			except:
				logging.error("Something went wrong sending or receiving over the BT socket: "+traceback.format_exc())
				return False
		logging.info("Result: "+str(res))
		return res
	
	def disconnect(self):
		logging.info('Disconnecting from dongle...')
		
		self.sendCtrlSeq(["p 0 99 0 0"]) # reboots the device, just in case...
		
		self.socket.close()
		
		self.connectionStatus = False

	def formatAnswer(self, recv):
		if(isinstance(recv, list)):
			outBuf = []
			for inBuf in recv:
				strippedCR = inBuf.replace(b"\r", b"")
				outBuf.append(((strippedCR).decode(encoding='UTF-8'))[0:-2].replace(" ", "").lower())
			return outBuf
		else:
			return False
	
	#configureCAN(parameters=[MS_HS, speed, filter_start, filter_stop])...
	def configureCAN(self, ms_hs="hs", reqId="000", speed=["11b", 500], filterCanID=[1,"000"], filterMask=[1,"0000"]):
		configString = ["p 8 2 0"]
		self.filterCanID=filterCanID
		self.filterMask=filterMask
		self.currentReqId=reqId
		
		if ms_hs == "ms":
			configString.append("p 8 4 1 0")
		elif ms_hs == "hs":
			configString.append("p 8 4 0 0")
		
		
		if speed[0] == "11b": # 11b addressing
			if speed[1] == 125:
				configString.append("p 8 3 0 0")
			elif speed[1] == 250:
				configString.append("p 8 3 2 0")
			elif speed[1] == 500:
				configString.append("p 8 3 3 0")
			elif speed[1] == 1000:
				configString.append("p 8 3 4 0")
			
			configString.append("p 8 2 3")
			configString.append("p 6 5 $"+reqId)
			
			if filterCanID:
				configString.append("p 8 10 "+str(filterCanID[0])+" $"+filterCanID[1])
			if filterMask:
				configString.append("p 8 11 "+str(filterMask[0])+" $"+filterMask[1])
		elif speed[0] == "29b":
			if speed[1] == 125:
				configString.append("p 8 3 5 0")
			elif speed[1] == 250:
				configString.append("p 8 3 6 0")
			elif speed[1] == 500:
				configString.append("p 8 3 7 0")
			elif speed[1] == 1000:
				configString.append("p 8 3 8 0")
			
			configString.append("p 8 2 3")
			configString.append("p 6 5 $"+reqId)
			
			if filterCanID:
				configString.append("p 8 12 "+str(filterCanID[0])+" $"+filterCanID[1])
			if filterMask:
				configString.append("p 8 13 "+str(filterMask[0])+" $"+filterMask[1])
		logging.info("Config String sent to dongle: "+str(configString))
		self.sendCtrlSeq(configString)
		return True
	
	def sendRawData(self, data, timeout):
		logging.debug("RawData: "+ data)
		self.socket.send(bytes((data), "utf-8").decode("unicode_escape")) 
		if select.select([self.socket], [], [], 1): # If socket is ready
			time.sleep(timeout)
			return self.socket.recv(1024)
	
	
	def sendCanData(self, seq, reqId=None, checkAnswer=False):
		res = []
		reqId = reqId or self.currentReqId # Python doesn't accept variables as default parameters, use this workaround to set ReqId if one is omitted, else use currentReqId
		self.sendCtrlSeq(["p 6 5 $"+reqId])
		logging.info('Sending CAN Commands:'+ str(seq))
		for command in seq:
			try:
				currentAnswer = self.sendRawData(data=command+"\r", timeout=1)
				res.append(currentAnswer)
				if checkAnswer == True:
					commandSectionLen = len(command.replace(" ", "").lower())
					commandAnswer = ((self.formatAnswer([currentAnswer]))[0])[0:commandSectionLen*2]
					if (int(commandAnswer[:commandSectionLen], 16) == (int(commandAnswer[-1*commandSectionLen:], 16) - int("400000", 16))):
						logging.info("Command "+command+" has been executed succesfull")
					else:
						logging.error("Error executing command: "+command)
					
			except:
				logging.error("Something went wrong sending or receiving over the BT socket: "+traceback.format_exc())
				return False
		logging.info("Result: "+str(res))
		return res
	
	def TesterPresent(self, active=True, reqId=None, interval=250):
		reqId = reqId or self.currentReqId # Python doesn't accept variables as default parameters, use this workaround to set ReqId if one is omitted, else use currentReqId
		
		if active == True:
			self.sendCtrlSeq(["p 6 8 "+str(interval)+" 0", "p 6 6 $"+reqId+" 0"])
		else:
			self.sendCtrlSeq(["p 6 7 $"+reqId+" 0"])
