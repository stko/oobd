"""
A simple Python script to send messages to a sever over Bluetooth using 
Python sockets (with Python 3.3 or above). 
"""

import socket

serverMACAddress = '00:1f:e1:dd:08:3d'
port = 3
s = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
s.connect((serverMACAddress,port))
while 1:
	text = input()
	if text == "quit":
		break
	s.send(bytes(text, 'UTF-8'))
s.close()

