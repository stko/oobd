import os.path
import websocket
import sys
import re
import time
from json import loads, dumps
from base64 import encodestring,decodestring, b64decode
from pprint import pprint

class webUIClient(object):

	def __init__(self):
		self._answer = ''
		self._wsSocket = None

	def open_webUI(self, wsURL, timeout):
		sys.stderr.write("open "+wsURL+"\n")
		self._wsSocket = websocket.WebSocket()
		if self._wsSocket is None:
			raise AssertionError("could not open webUI- Websocket!")
		self._wsSocket.connect(wsURL)
		self._wsSocket.settimeout(timeout)

	def close_webUI(self):
		sys.stderr.write("close webUI\n")
		self._wsSocket.close()

	def send_webUI_command(self,cmd):
		self._flush()
		try: # do a sanity check first, if the given string is really a well formed JSON string
			loads(cmd)
		except:
			raise AssertionError("command is not a valid JSON string!: "+cmd+"\n")
		if self._wsSocket is None:
			raise AssertionError("send_webUI_command: Websocket is not open!")
		sys.stderr.write("webUI cmd: "+cmd+"\n")
		self._wsSocket.send(cmd)      # write a string

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
			#sys.stderr.write("Type of value:" +type(value).__name__+"\n");
			#sys.stderr.write("Type of inputDict:" +type(inputDict[attr]).__name__+"\n");
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
							sys.stderr.write (" REGEX string compare for " + value + "against " + inputValue + " failed\n" )
							return False
					elif value != inputValue:
						sys.stderr.write("normal string test")
						sys.stderr.write (" normal string compare for " + value + " against " + inputValue + " failed\n" )
						return False

				elif  value != inputDict[attr]:
					sys.stderr.write ("other type, direct compare for " + value + "against " +inputDict[attr] + " failed\n" )
					return False
		return True
			
	def _flush(self):
		timeout = self._wsSocket.gettimeout()
		self._wsSocket.settimeout(0.3)
		try:

			s="dummy"
			while s != "":
				s= self._wsSocket.recv()
		except Exception as e:
				pass
				#raise AssertionError("flush error :-("+repr(e)+"\n")
		self._wsSocket.settimeout(timeout)

	def _doLine(self):
		if self._wsSocket is None:
			raise AssertionError("_doLine: Websocket is not open!")
		else:
			try:
				s = self._wsSocket.recv()          # read buffer
				self._answer +=s
				sys.stderr.write("Answer "+self._answer+"\n") 
			except Exception as e:
				raise AssertionError("something went wrong :-("+repr(e)+"\n")
