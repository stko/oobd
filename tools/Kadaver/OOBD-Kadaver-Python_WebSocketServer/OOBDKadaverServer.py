'''
The MIT License (MIT)

Copyright (c) 2013 Dave P.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Adapted as OOBD server by Steffen Koehler for the OOBD TEam 2015

'''

import signal, sys, ssl, logging
from SimpleWebSocketServer import WebSocket, SimpleWebSocketServer, SimpleSSLWebSocketServer
from optparse import OptionParser
from json import loads , dumps
from base64 import encodestring, decodestring

logging.basicConfig(format='%(asctime)s %(message)s', level=logging.DEBUG)

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

   def handleMessage(self):
      if self.data is not None:
	try:
		thisMsg=loads(str(self.data))
		print 'message: '+str(self.data)
		try:
			thisMsg['reply'] # checks if variable exists
			thiscmd=decodestring(thisMsg['reply'])
			thiscmd.replace("\r","\n")
			print decodestring(thisMsg['channel'])+"<"+thiscmd+"\n"
			self.channel='r'+thisMsg['channel']
			prefix="s"
		except Exception as n:
			thiscmd=decodestring(thisMsg['msg'])
			thiscmd.replace("\r","\n")
			print decodestring(thisMsg['channel'])+">"+thiscmd+"\n"
			self.channel='s'+thisMsg['channel']
			prefix="r"
		for client in self.server.connections.itervalues():
#			print 'actual client: '+ client.channel
			try:
				if client != self and client.channel == prefix+thisMsg['channel'] :
					client.sendMessage(str(self.data))
			except Exception as n:
				print "Send Exception: " ,n
		try:
			thisMsg['echo'] # checks if variable exists
			print "echo msg on channel "+decodestring(thisMsg['channel'])+"\n"
			thisMsg['echo'] ="server"
			self.sendMessage(dumps(thisMsg))
		except Exception as n:
			pass
			
	except Exception as n:
		print "Exception: " , n



   def handleConnected(self):
      print self.address, 'connected'
 
   def handleClose(self):
      print self.address, 'closed'
 

if __name__ == "__main__":

   parser = OptionParser(usage="usage: %prog [options]", version="%prog 1.0")
   parser.add_option("--host", default='', type='string', action="store", dest="host", help="hostname (localhost)")
   parser.add_option("--port", default=9000, type='int', action="store", dest="port", help="port (9000)")
   parser.add_option("--example", default='chat', type='string', action="store", dest="example", help="echo, chat")
   parser.add_option("--ssl", default=0, type='int', action="store", dest="ssl", help="ssl (1: on, 0: off (default))")
   parser.add_option("--cert", default='./cert.pem', type='string', action="store", dest="cert", help="cert (./cert.pem)")
   parser.add_option("--ver", default=ssl.PROTOCOL_TLSv1, type=int, action="store", dest="ver", help="ssl version")
   
   (options, args) = parser.parse_args()

   cls = SimpleEcho
   if options.example == 'chat':
      cls = SimpleChat	

   if options.ssl == 1:
      server = SimpleSSLWebSocketServer(options.host, options.port, cls, options.cert, options.cert, version=options.ver)
   else:	
      server = SimpleWebSocketServer(options.host, options.port, cls)

   def close_sig_handler(signal, frame):
      server.close()
      sys.exit()

   signal.signal(signal.SIGINT, close_sig_handler)

   server.serveforever()
