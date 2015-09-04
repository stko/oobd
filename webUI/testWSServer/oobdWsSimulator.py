'''

The MIT License(MIT)

Copyright(c) 2013 Dave P.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

		The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
'''


import signal, sys, ssl, logging
from SimpleWebSocketServer import WebSocket, SimpleWebSocketServer, SimpleSSLWebSocketServer
from optparse import OptionParser
from json import loads, dumps
from base64 import encodestring, decodestring
from pprint import pprint
from decimal import Decimal

logging.basicConfig(format = '%(asctime)s %(message)s', level = logging.DEBUG)

global server

class viz:
	def __init__(self, msg):
		firstValue= decodestring(msg['value'])
		if firstValue.lower() in ["i","n","y"]:
			self.type="boolean"
			self.value=firstValue.lower()
		else:
			pprint(firstValue.split(None, 1))
			values =firstValue.split(None, 1)
			try:
				self.maxValue=Decimal(values[0]);
				self.value=self.maxValue;
				self.type="num"
				if len(values)>1:
					self.unit=values[1]
				else:
					self.unit=""
			except:
				self.type="text"
				self.text=firstValue
				self.value=0
				
	def getValue(self):
		if self.type=="none":
			return "none"
		elif self.type=="boolean":
			if self.value=="i":
				self.value="n"
			elif self.value=="n":
				self.value="y"
			elif self.value=="y":
				self.value="i"
			else:
				self.value="i"
			return self.value
		elif self.type=="num":
			self.value=self.value+self.maxValue/10
			if self.value>self.maxValue:
				self.value=0
			if self.unit !="":
				return str(self.value) + " " + self.unit
			else:
				return str(self.value)
		elif self.type=="text":
			self.value=self.value+1
			if self.value>10:
				self.value=0
			return "{0}-{1}".format(self.text,self.value)
			
			
class SimpleEcho(WebSocket):


	def handleMessage(self):
		if self.data is None:
			self.data = ''

		try:
			self.sendMessage(str(self.data))
		except Exception as n:
			print n

	def handleConnected(self):
		print self.address, 'connected'

	def handleClose(self):
		print self.address, 'closed'


class SimpleChat(WebSocket):
	clients={}
	
	def guessAnswer(self,msg):
		pprint(msg)
		if not msg['name'] in self.clients.keys():
			print msg['name']+ " not found"
			self.clients[msg['name']]=viz(msg)
			
		return self.clients[msg['name']].getValue()


	def handleMessage(self):
		if self.data is not None:
			try:
				print 'message: ' + str(self.data)
				thisMsg = loads(str(self.data))
				pprint(thisMsg)
				self.sendMessage('{"type":"VALUE" , "to":{"name":"'+thisMsg['name'].encode("utf-8")+'"}, "value":"' + encodestring(self.guessAnswer(thisMsg)).replace('\n', '') + '"}')
				#self.sendMessage('{"type":"VALUE" , "to":{"name":"gauge_speed:"}, "value":"' + encodestring("120").replace('\n', '') + '"}')# for client in self.server.connections.itervalues(): #print 'actual client: ' + client.channel#
			except Exception as n:
				print "Exception: ", n


	def handleConnected(self):
		self.clients={}
		print self.address, 'connected'
		self.sendMessage('{"type":"WRITESTRING" ,"data":"' + encodestring("Connected to OOBD").replace('\n', '') + '"}')
		self.sendMessage('{"type":"WSCONNECT"}')

	def handleClose(self):
		print self.address, 'closed'

if __name__ == "__main__":

	parser = OptionParser(usage = "usage: %prog [options]", version = "%prog 1.0")
	parser.add_option("--host",
	default = '', type = 'string', action = "store", dest = "host", help = "hostname (localhost)")
	parser.add_option("--port",
	default = 8443, type = 'int', action = "store", dest = "port", help = "port (9000)")
	parser.add_option("--example",
	default = 'chat', type = 'string', action = "store", dest = "example", help = "echo, chat")
	parser.add_option("--ssl",
	default = 0, type = 'int', action = "store", dest = "ssl", help = "ssl (1: on, 0: off (default))")
	parser.add_option("--cert",
	default = './cert.pem', type = 'string', action = "store", dest = "cert", help = "cert (./cert.pem)")
	parser.add_option("--ver",
	default = ssl.PROTOCOL_TLSv1, type = int, action = "store", dest = "ver", help = "ssl version")

	(options, args) = parser.parse_args()

	cls = SimpleEcho
	if options.example == 'chat':
		cls = SimpleChat

	if options.ssl == 1:
		server = SimpleSSLWebSocketServer(options.host, options.port, cls, options.cert, options.cert, version = options.ver)
	else :
		server = SimpleWebSocketServer(options.host, options.port, cls)

	def close_sig_handler(signal, frame):
		server.close()
		sys.exit()

	signal.signal(signal.SIGINT, close_sig_handler)
	server.serveforever()
