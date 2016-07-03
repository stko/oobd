#!/usr/bin/env python
# coding: utf-8

# install:
# sudo pip install python-jsonrpc
# sudo apt-get install libttspico-utils aplay


# extended text input syntax: http://dafpolo.free.fr/telecharger/svoxpico/SVOX_Pico_Manual.pdf

import pyjsonrpc
import subprocess



class RequestHandler(pyjsonrpc.HttpRequestHandler):

  @pyjsonrpc.rpcmethod
  def add(self, a, b):
      """Test method"""
      return a + b

  @pyjsonrpc.rpcmethod
  def say(self, text):
      """Say method"""
      subprocess.call(['pico2wave', '--lang=de-DE', '--wave=/tmp/test.wav', '"'+text+'"'])
      subprocess.call(['aplay',  '/tmp/test.wav'])
      subprocess.call(['rm',  '/tmp/test.wav'])
      return "OK"


# Threading HTTP-Server
http_server = pyjsonrpc.ThreadingHttpServer(
    server_address = ('localhost', 10023),
    RequestHandlerClass = RequestHandler
)
print "Starting HTTP server ..."
print "URL: http://localhost:10023"
http_server.serve_forever()