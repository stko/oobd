# -*- coding: utf-8 -*-
import socket

UDP_IP="127.0.0.1"
UDP_PORT_IN=9998
UDP_PORT_OUT=9999

sock_in = socket.socket( socket.AF_INET, # Internet
                      socket.SOCK_DGRAM ) # UDP
sock_out = socket.socket( socket.AF_INET, # Internet
                      socket.SOCK_DGRAM ) # UDP
sock_in.bind( (UDP_IP,UDP_PORT_IN) )

while True:
    msg =bytearray()
    data, addr = sock_in.recvfrom( 1024 ) # buffer size is 1024 bytes
    msg+=data
    print "0x7%02X %02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] , msg[8] , msg[9] , msg[10]) 
    sock_out.sendto( data, (UDP_IP, UDP_PORT_OUT) )
