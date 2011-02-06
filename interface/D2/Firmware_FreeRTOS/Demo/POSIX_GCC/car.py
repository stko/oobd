# -*- coding: utf-8 -*-
import socket
from yaml import load, dump





try:
    from yaml import CLoader as Loader
    from yaml import CDumper as Dumper
except ImportError:
    from yaml import Loader, Dumper


      

simu=load(open("carsim.yaml"), Loader=Loader)

print dump(simu)

quit
UDP_IP="127.0.0.1"
UDP_PORT_IN=9998
UDP_PORT_OUT=9999

sock_in = socket.socket( socket.AF_INET, # Internet
                      socket.SOCK_DGRAM ) # UDP
sock_out = socket.socket( socket.AF_INET, # Internet
                      socket.SOCK_DGRAM ) # UDP
sock_in.bind( (UDP_IP,UDP_PORT_IN) )


def sendTele(msg,data):
  d=data["d"]
  for i in range(len(d)):
    msg[4+i]=d[i]
  msg[3]=len(d)
  for i in range(len(d),7):
    msg[4+i]=0
  global UDP_IP
  global UDP_PORT_OUT
  global sock_out
  sock_out.sendto( msg, (UDP_IP, UDP_PORT_OUT) )
  print "sended.."



while True:
    nextStep=0
    msg =bytearray()
    data, addr = sock_in.recvfrom( 1024 ) # buffer size is 1024 bytes
    msg+=data
    print "0x7%02X %02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] , msg[8] , msg[9] , msg[10]) 
    frameType=(msg[3] & 0xF0 ) % 16
    print frameType
    if frameType == 0: #single frame
      i = 4
      pid= "%02X%02X%02X" % ( msg[i+0] , msg[i+1] , msg[i+2])
      print "Single Frame"
      nextStep=1 # send the answer
    if frameType == 1: #first frame
      i = 5
      nrOfBytes = (msg[3] & 0x0f ) *256 + msg[4]
      pid= "%02X%02X%02X" % ( msg[i+0] , msg[i+1] , msg[i+2])
      nextStep=2 # send FrameControl
      send(msg,{'d':[0x30,0x00,0x00]})
    if frameType == 2: # consecutive frame
      nrOfBytes = nrOfBytes - (msg[3] & 0x0f ) # just count the number of received bytes
      if nrOfBytes <=0:
	nextStep=1 # send the answer
    if frameType == 3: # FlowControl - triggers the output of the remaining msg
      nextStep=3
    print pid
    msg[0]=msg[0]+8 # changing the physical address from ECU to tester
    if len(simu)>0:
      try:
	dArray = simu[pid]
	print dump(dArray[0])
	if nextStep == 1: #Single Frame or end of Consecutive Frame series -> send the PID answer
	  if len(dArray)>1: # if more as a single frame is requested to send
	    outputBlock=1 # indicates more frames  to be send
	  else:
	    outputBlock =0 # indicates that no more frames have to be send
	sendTele(msg,dArray[0]) 
	if nextStep == 2: #
	  for i ,v  in enumerate(dArray):
	    print i,v
      except:
	print pid, "not found, just reply msg"
	sock_out.sendto( msg, (UDP_IP, UDP_PORT_OUT) )
    else:
      print pid, "nothing configured, just reply msg"
      sock_out.sendto( msg, (UDP_IP, UDP_PORT_OUT) )
