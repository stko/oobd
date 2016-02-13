import os.path
import websocket
import sys, getopt , thread
import re
import ssl
import time
import serial
from json import loads, dumps
from base64 import encodestring,decodestring, b64decode, b64encode
from pprint import pprint

class kadaverSim(object):

	def __init__(self,wsURL,connectID,serialPort):
		self._wsURL=wsURL
		self._connectID=connectID
		self._serialPort=serialPort
		sys.stderr.write("open "+wsURL+"\n")

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
					self._serPortSocket.write(decodestring(thisMsg["msg"]).encode('utf-8'))
				if "echo" in thisMsg and "msg" in thisMsg:
					thisMsg["echo"]="client"
					thisMsg["reply"]=""
					thisMsg.pop("msg", None)
					self._wsSocket.send(dumps(thisMsg))
			except Exception as n:
				print "Exception: " , n

		self._wsSocket = websocket.WebSocketApp(wsURL,
                              on_message = on_message,
                              on_error = on_error,
                              on_close = on_close)
		if self._wsSocket is None:
			raise AssertionError("could not open kadaver- Websocket!")
		self._serPortSocket = serial.Serial(serialPort, 38400, timeout=10)

		def serialThread(*args):
			while True:
				# from pySerial 3.0 onwards: nrOfChars=self._serPortSocket.in_waiting
				nrOfChars=self._serPortSocket.inWaiting()
				if nrOfChars>0:
					_input = self._serPortSocket.read(nrOfChars)
					if len(_input) > 0:
						print ('Got:', _input)
						self.send_kadaver_message(_input)
				else:
					time.sleep(0.01)

		thread.start_new_thread(serialThread, ())
		self._wsSocket.on_open = on_open
		# disable SSL cert check finally found on https://pypi.python.org/pypi/websocket-client/
		# maybe I should just read the original documentaion more often first before googeling around ;-)
		self._wsSocket.run_forever(sslopt={"cert_reqs": ssl.CERT_NONE})

	def close_kadaver(self):
		if not self._wsSocket is None:
			sys.stderr.write("close kadaver\n")
			self._wsSocket.close()

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

	def answer_should_match(self, expected_answer):
		self._answer = ""
		self._doLine()
		try: # do a sanity check first, if the given string is really a well formed JSON string
			pattern = loads(expected_answer)
		except:
			raise AssertionError("expected answer is not a valid JSON string!: "+expected_answer+"\n")
		try: # do a sanity check first, if the received string is really a well formed JSON string
			answer = loads(self._answer)
		except:
			raise AssertionError("RECEIVED answer is not a valid JSON string!: "+self._answer+"\n")
		if not self.compareDicts(pattern, answer):
			raise AssertionError("Expected answer to be '%s' but was '%s'."
		  		% (expected_answer, self._answer))
		return 'SUCCESS'


	def compareDicts(self,patternDict, inputDict):
		for attr, value in patternDict.items():
			if not attr in inputDict:
				return False
			elif type(value) != type(inputDict[attr]):
				return False
			elif isinstance (value, dict) and not self.compareDicts(value,inputDict[attr]):
				return False
			else:
				if isinstance( value, str) or isinstance( value, unicode) :
					inputValue = inputDict[attr]
					regCompareFlag=False
					if value[:1]=="%":
						regCompareFlag = True
						value=value[1:]
					if value[:1]=="#":
						value=value[1:]
						try:
							inputValue=b64decode(inputValue).decode('utf-8')
						except:
							raise AssertionError("RECEIVED jason element " + attr + "is not a valid BASE64 string!: "+inputDict[attr]+"\n")
							return False
					if regCompareFlag:
						matchObj = re.match( value , inputValue, re.M|re.I)
						if not matchObj:
							sys.stderr.write (" REGEX string compare for " + str(value) + "against " + str(inputValue) + " failed\n" )
							return False
					elif value != inputValue:
						sys.stderr.write (" normal string compare for " + str(value) + " against " + str(inputValue) + " failed\n" )
						return False

				elif  value != inputDict[attr]:
					sys.stderr.write ("other type, direct compare for " + str(value) + "against " +str(inputDict[attr]) + " failed\n" )
					return False
		return True
			
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

	def _doLine(self):
		if self._wsSocket is None:
			raise AssertionError("_doLine: Websocket is not open!")
		else:
			try:
				s = self._wsSocket.recv()          # read buffer
				self._answer +=s
			except Exception as e:
				raise AssertionError("something went wrong :-("+repr(e)+"\n")
			
			
def main(argv):
	wsHost = 'wss://oobd.luxen.de/websockssl/'
	connectID = '1234'
	serialPort = '/tmp/DXM'
	try:
		opts, args = getopt.getopt(argv,"h:c:s:",["host=","connect-id=","serial-port="])
	except getopt.GetoptError:
		print ("kadaverSim.py -h <ServerURL> -c <connectID>")
		print ("kadaverSim.py --host=<ServerURL> --connect-id=<connectID>")
		sys.exit(2)
	for opt, arg in opts:
		if opt == '-h':
			wsHost = arg
		elif opt in ("-c", "--connect-id"):
			connectID = arg
		elif opt in ("-s", "--serial-port"):
			serialPort = arg
	print('connectID  is "', connectID ,'"')
	print('wsHost is "', wsHost ,'"')
	print('serial Port is "', serialPort ,'"')
	mySocket= kadaverSim( wsHost, connectID , serialPort)


if __name__ == "__main__":
	main(sys.argv[1:])
