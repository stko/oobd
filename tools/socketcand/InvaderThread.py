import threading
import Queue
import sys
import time
import select
import bluetooth


class InvaderThread(threading.Thread):
	""" A  thread which handles the serial communication with the dongle
	and provides the dongle initialisation routine

	"""
	def __init__(self, queue_main, queueTCP, queue_Invader ):
		super(InvaderThread, self).__init__()
		self.queue_main = queue_main
		self.queueTCP = queueTCP
		self.queue_Invader = queue_Invader
		self.stoprequest = threading.Event()
		self.btSocket = None

	def run(self):
		try:
			print "talk to Caninvader"
			while not self.stoprequest.isSet():
				readInput, data= self.waitForInput( self.queue_Invader, self.btSocket)
				#print "InvaderThread readinput:%s" % (readInput) 
				if readInput=="queue":
					print "InvaderThread queue data:%s" % (data[0]) 
					if data[0]=="open": 
						self.doOpen(data[1])
					elif data[0]=="rawmode":
						self.doRawMode()
					elif data[0]=="close":
						self.doClose()
				elif readInput=="sock":
					#print "InvaderThread invader text:\n%s\n" % (data) 
					if data[0]=="#": # its a listen frame
						#print "annouce frame:\n%s\n" % (data)
						self.queueTCP.put(("frame",data))
				elif readInput=="eof":
					elf.queue_main.put("cancel")			
		except:
			print "Unexpected error:", sys.exc_info()[0],sys.exc_info()[1]
		finally: 
			if self.btSocket != None:
				self.btSocket.close()
		print ' InvaderThread dies..'
		self.queue_main.put("quit")



	def join(self, timeout=None):
		self.stoprequest.set()
		super(InvaderThread, self).join(timeout)


	def waitForInput(self, queue, sock):
		TIMEOUT = 0.1   # 100ms
		# Main event loop
		while 1:

		
			# Get data from socket without blocking if possible
			if sock != None:
				recvDone=False
				res=self.readln()
				if res!="":
					print res
					return ("sock", res)
			else:
				return ("eof","")
			# Get item from queue without blocking if possible
			try:
				item = queue.get_nowait()
				return ("queue",item)
			except Queue.Empty:
				pass

			# If we didn't get anything on this loop, sleep for a bit so we
			# don't max out CPU time
			time.sleep(TIMEOUT)

	def doOpen(self, bus ):
		print ' OpenBus %s'% bus

	def doClose(self):
		print ' doClose'
		self.echoWrite("p 7 1 0 0\r") # disable listen

		
	def doRawMode(self):
		print ' doRawmode'
		self.echoWrite("p 1 1 0 0\r")
		self.echoWrite("p 8 2 2 0\r") # bus in listen mode
		self.echoWrite("p 7 1 1 0\r")# CAN transceiver: Loop back combined with silent mode
		self.echoWrite("p 8 2 3\r")
		self.echoWrite("p 8 3 3\r")
		self.echoWrite("p 8 4 0\r")
		self.echoWrite("p 8 11 1 0\r")# open all filters
		
		

		#self.echoWrite("p 1 1 0 0\r")
		#self.echoWrite("p 7 0 0\r")
		#self.echoWrite("p 8 4 0\r")
		#self.echoWrite("p 8 3 3\r")
		#self.echoWrite("p 8 2 3\r")
		#self.echoWrite("p 8 10 1 $000\r")
		#self.echoWrite("p 8 11 1 $000\r")
		#self.echoWrite("p 7 1 1")

		
		
		#self.queueTCP.put(("frame","blabla"))
		

	def readln(self):
		recvDone=False
		res=""
		while(not recvDone):
			ready = select.select([self.btSocket], [], [], 0.5) #waiting 0.5 sec for an answer
			if ready[0]:
				res+=self.btSocket.recv(10)
				recvDone=res[-1:]=="\r" or res[-1:]=="\n"
			else:
				recvDone=True
		if res!="":
			print res
			return res
		else:
			return ''

	def flush(self):
		ready = select.select([self.btSocket], [], [], 0.05) #waiting 0.05 sec for an answer
		if ready[0]:
			self.btSocket.recv(1024)
			
			
	def echoWrite(self, cmd):
		self.flush()
		print "echoWrite ", cmd
		self.btSocket.send(cmd)
		#print len(cmd)
		res=self.btSocket.recv(len(cmd))


	def connect(self, macAdress):
		counter=3
		while(counter>=0):
			try:
				self.btSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
				self.btSocket.connect((macAdress, 1))
				#self.btSocket.setblocking(0)
				print "Connected to ", macAdress
				break;
			except bluetooth.btcommon.BluetoothError as error:
				self.btSocket.close()
				self.btSocket=None
				print "Could not connect: %s; %d trials left, Retrying in 3s..." % (error, counter)
				counter-=1
				time.sleep(3)
		return self.btSocket;

		
	def connectBT(self, macAddress):
		self.btSocket = self.connect(macAddress)
		if self.btSocket != None:
			try:
				self.btSocket.send('\r\r\r')
				ready = select.select([self.btSocket], [], [], 0.5) #waiting 1 sec for an answer
				if ready[0]:
					res=self.btSocket.recv(1024)
			except bluetooth.btcommon.BluetoothError as error:
				print "Caught inital BluetoothError: ", error
			runstatus=3
			while(runstatus>0):
				self.echoWrite("\r\n")
				res=self.readln()
				print "Read: ", res
				lines=res.splitlines()
				if len(lines)>0 and lines[len(lines)-1]==">":
					self.flush()
					print "Responce from Dongle detected, asking for Version string"
					self.echoWrite("p 0 0\r")
					res=self.readln()
					lines=res.splitlines()
					#print "res=",lines
					if lines[len(lines)-1]==">":
						version = lines[0].split()
						print "Dongle Version:", version
						if version[0] == "OOBD":
							print "OOBD Firmware identified - continue"
							return (True, "CANInvader",lines[0])
				runstatus -= 1 # reduce the trial conter by 1


			self.btSocket.close()
			return (False, "-","-")
