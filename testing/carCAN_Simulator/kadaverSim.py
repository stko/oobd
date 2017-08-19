import os.path
import websocket
import sys, getopt , thread
import re
import ssl
import time
import socket
from uuid import getnode as get_mac
import select
from json import loads, dumps
from base64 import encodestring,decodestring, b64decode, b64encode
from pprint import pprint

class kadaverSim(object):


	def __init__(self,wsURL,connectID, host, port):




		def on_error(ws, error):
			print (error)

		def on_close(ws):
			print ("### closed ###")

		def on_open(ws):
			print ("### connected ###")
			try:
				self.send_kadaver_message("OOBD Rocks")
				print ("### connected2 ###")
			except Exception as n:
				print "Exception: " , n
				
		def on_message(ws, message):
			print (message)
			try:
				thisMsg=loads(message)
				if "msg" in thisMsg:
					self._serPortSocket.send(decodestring(thisMsg["msg"]).encode('utf-8'))
				if "echo" in thisMsg and "msg" in thisMsg:
					thisMsg["echo"]="client"
					thisMsg["reply"]=""
					thisMsg.pop("msg", None)
					self._wsSocket.send(dumps(thisMsg))
			except Exception as n:
				print "Exception: " , n


		def serialThread(*args):
			print ('serial:')
			while True:
				socket_list = [sys.stdin, self._serPortSocket]
					
				# Get the list sockets which are readable
				read_sockets, write_sockets, error_sockets = select.select(socket_list , [], [])
				print ('select:')
					
				for sock in read_sockets:
					#incoming message from remote server
					print ('Got-1:')
					if sock == self._serPortSocket:
						print ('Got-2:')
						data = sock.recv(4096)
						if data : # otherways the socket would be closed
							sys.stdout.write(data)
							print ('Got:', data)
							self.send_kadaver_message(data)
					else :
						#msg = sys.stdin.readline()
						#_serPortSocket.send(msg)
						pass


		def close_kadaver(self):
			if not self._wsSocket is None:
				sys.stderr.write("close kadaver\n")
				self._wsSocket.close()



		def _doLine(self):
			if self._wsSocket is None:
				raise AssertionError("_doLine: Websocket is not open!")
			else:
				try:
					s = self._wsSocket.recv()          # read buffer
					self._answer +=s
				except Exception as e:
					raise AssertionError("something went wrong :-("+repr(e)+"\n")
			
		### start the init- code itself	
		self._wsURL=wsURL
		self._connectID=connectID
		sys.stderr.write("open "+wsURL+"\n")
		self._wsSocket = websocket.WebSocketApp(wsURL,
			on_message = on_message,
			on_error = on_error,
			on_close = on_close)
		if self._wsSocket is None:
			raise AssertionError("could not open kadaver- Websocket!")
		
		self._serPortSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self._serPortSocket.settimeout(2)
     
		# connect to remote host
		try :
			self._serPortSocket.connect((host, port))
		except :
			raise AssertionError("could not open telnet port!")
		

		
		
		self._flush()





		thread.start_new_thread(serialThread, ())
		self._wsSocket.on_open = on_open
		# disable SSL cert check finally found on https://pypi.python.org/pypi/websocket-client/
		# maybe I should just read the original documentaion more often first before googeling around ;-)
		self._wsSocket.run_forever(sslopt={"cert_reqs": ssl.CERT_NONE})

	
	def send_kadaver_message(self,message):
		thisMsg=loads('{ "reply" : "" , "channel" : "" }')
		thisMsg["reply"]=b64encode(message)
		thisMsg["channel"]=b64encode(self._connectID)
		pprint (thisMsg)
		self._flush()
		if self._wsSocket is None:
			raise AssertionError("send_kadaver_message: Websocket is not open!")
		self._wsSocket.send(dumps(thisMsg))      # write a string
		sys.stderr.write("send msg:"+dumps(thisMsg))
			
	def _flush(self):
		if self._wsSocket is None:
			raise AssertionError("_flush: Websocket is not open!")
		else:
			try:

				s="dummy"
				while s != "":
					s= self._wsSocket.recv()
			except Exception as e:
					pass
					#raise AssertionError("flush error :-("+repr(e)+"\n")

		
		
			
def main(argv):
	wsHost = 'wss://oobd.luxen.de/websockssl/'
	connectID = hex(get_mac())
	telnetHost = 'localhost'
	telnetPort = 1234
	try:
		opts, args = getopt.getopt(argv,"h:c:d:p:",["host=","connect-id=","dongle-host=","dongle-port="])
	except getopt.GetoptError:
		print ("kadaverSim.py -h <ServerURL> -c <connectID> -d <dongleHost> -p <donglePort>")
		print ("kadaverSim.py --host=<ServerURL> --connect-id=<connectID> --dongle-host<dongleHost> --dongle-port<donglePort>")
		sys.exit(2)
	for opt, arg in opts:
		if opt in ("-h", "--host"):
			wsHost = arg
		elif opt in ("-c", "--connect-id"):
			connectID = arg
		elif opt in ("-d", "--dongle-host"):
			telnetHost = arg
		elif opt in ("-s", "--dongle-port"):
			telnetPort = int(arg)
	print('connectID  is "', connectID ,'"')
	print('wsHost is "', wsHost ,'"')
	print('telnet host is "', telnetHost ,'"')
	print('telnet Port is "', telnetPort ,'"')
	mySocket= kadaverSim( wsHost, connectID , telnetHost, telnetPort)


if __name__ == "__main__":
	main(sys.argv[1:])
