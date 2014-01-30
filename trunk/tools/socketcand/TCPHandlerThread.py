import threading, Queue
import time
import socket
import select
import sys
import select


class TCPHandlerThread(threading.Thread):
	""" A TCP thread that does the communication with Kayak


	Ask the thread to stop by calling its join() method.
	"""
	def __init__(self, queue_main, queueTCP, queue_Invader ):
		super(TCPHandlerThread, self).__init__()
		self.queue_main = queue_main
		self.queueTCP = queueTCP
		self.queue_Invader = queue_Invader
		self.stoprequest = threading.Event()
		self.oldTS=0

	def run(self):
		# As long as we weren't asked to stop, we wait for incoming tcp connections
		# and report them to the main task
		# queue. The tasks are taken with a blocking 'get', so no CPU
		# cycles are wasted while waiting.
		# Also, 'get' is given a timeout, so stoprequest is always checked,
		# even if there's nothing in the queue.
		TCP_IP="" # TCP_IP="127.0.0.1"
		TCP_PORT_IN= 29536 # socketcan broadcasted port
		try:
			self.fire=False
			self.TCPSock = socket.socket( socket.AF_INET, # Internet
					socket.SOCK_STREAM ) # TCP
			self.TCPSock.bind( (TCP_IP,TCP_PORT_IN) )
			#self.TCPSock.setblocking(0)
			#self.TCPSock.settimeout(1.0)
			self.TCPSock.listen(1)
			print "listen to TCP"
			while not self.stoprequest.isSet():
				try:
					self.tcpConnection, addr = self.TCPSock.accept() 
					self.tcpConnection.send("< hi >")
					print "connected from %s" % (addr[0]) 
					while not self.stoprequest.isSet():
						readInput, data= self.waitForInput( self.queueTCP, self.tcpConnection)
						print "TCPHandler readinput:%s" % (readInput) 
						if readInput=="sock": 
							print ">%s" % ( data) 
							answer=self.doCmd(data)
							print ("answer:%s"% ( answer))
							if answer:
								self.tcpConnection.send("< %s >"% ( answer))
						elif readInput=="queue":
							cmd, content = data;
							print "TCPHandler queue cmd %s content %s" % ( data) 
							if cmd == "frame":
								self.sendFrame(content)
						elif readInput=="eof":
							print "Connection lost"
							self.queue_Invader.put(("close",""))
							self.tcpConnection.close()
							break
				except:
					print "Unexpected error:", sys.exc_info()[0]
					continue
		finally: 
			self.TCPSock.close()	
		print ' TCPHandlerThread dies..'
		self.queue_main.put("quit")
		
	def join(self, timeout=None):
		self.stoprequest.set()
		super(TCPHandlerThread, self).join(timeout)

	def doCmd(self,  cmdstring ):
		print ("cmdstring:%s"% ( cmdstring))
		param = cmdstring.split()
		if param[0]=="<" and param[len(param)-1]==">":
			cmd=param[1]
			cmd=cmd.upper()
			print ("cmd:%s"% ( cmd))
			if cmd=="OPEN":
				self.queue_Invader.put(("open",param[2]))
				return "ok"
			if cmd=="RAWMODE":
				 self.queue_Invader.put(("rawmode",""))
				 return "ok"
				 
		return None
		
		
	def waitForInput(self, queue, sock):
		TIMEOUT = 0.1   # 100ms
		# Main event loop
		while 1:
			# Get data from socket without blocking if possible
			r, w, x = select.select([sock], [], [], 0.001)
			if r:
				data = sock.recv(1024)
				if not data:    # Hit EOF
					return ("eof",data)
				else:
					return ("sock",data)

			# Get item from queue without blocking if possible
			try:
				item = queue.get_nowait()
				return ("queue",item)
			except Queue.Empty:
				pass

			# If we didn't get anything on this loop, sleep for a bit so we
			# don't max out CPU time
			time.sleep(TIMEOUT)

	def sendFrame(self, inputframe):
		#for i in range(10):
		#	print ("< frame 123 %d.0 11 22 33 44 >"% ( i))
		#	self.tcpConnection.send("< frame 123 %d.0 11 22 33 44 >"% ( i))
		framelines=inputframe.splitlines()
		for frameline in framelines:
			if len(frameline)>10 and frameline[0]=="#":
				lines=frameline.split()
				myFrame="< frame "
				myFrame+=lines[2][2:]+" "
				ts=float(lines[1])/1000
				myFrame+=str(ts)+" "
				dtl=int(lines[4])
				for i in range(dtl):
					myFrame+=lines[5+i]+" "
				myFrame+=">"
				print ("frame to send %s" % myFrame)
				self.tcpConnection.send(myFrame)