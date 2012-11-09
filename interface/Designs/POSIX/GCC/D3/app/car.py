# -*- coding: utf-8 -*-
import socket
from yaml import load, dump
import time




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
  print dump(data)
  for i in range(len(d)):
    print d[i]
    msg[3+i]=d[i]
  for i in range(len(d),7):
    msg[4+i]=0
  global UDP_IP
  global UDP_PORT_OUT
  global sock_out
  sock_out.sendto( msg, (UDP_IP, UDP_PORT_OUT) )
  print "sended: 0x7%02X %02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] , msg[8] , msg[9] , msg[10]) 
  time.sleep(0.01 * data["t"])



while True:
    nextStep=0
    msg =bytearray()
    data, addr = sock_in.recvfrom( 1024 ) # buffer size is 1024 bytes
    msg+=data
    print "received: 0x7%02X %02X %d %02X %02X %02X %02X %02X %02X %02X %02X" % ( msg[0] , msg[1] , msg[2] , msg[3] , msg[4] , msg[5] , msg[6] , msg[7] , msg[8] , msg[9] , msg[10]) 
    if msg[0] == 0xD0:
      msg[0]=0xE8 # changing functional address to answer address of ECU
    else:
      msg[0]=msg[0] | 8 # changing the physical address from ECU to tester
    frameType=(msg[3] & 0xF0 ) / 16
    print frameType
    
    ##############  Single Frame ###############
    if frameType == 0: 
      i = 4
      pid= "%02X%02X%02X" % ( msg[i+0] , msg[i+1] , msg[i+2])
      print "Single Frame"
      nextStep=1 # send the answer
      
    ############ First Frame  #################
    if frameType == 1: 
      i = 5
      nrOfBytes = (msg[3] & 0x0f ) *256 + msg[4]
      print "First Frame, expecting ",nrOfBytes, " Bytes"
      nrOfBytes = nrOfBytes -6 # 6 bytes aready received
      pid= "%02X%02X%02X" % ( msg[i+0] , msg[i+1] , msg[i+2])
      nextStep=0 # send FlowControl, wait for consecutive frames
      sendTele(msg,{'d':[0x30,0x30,0x00,0x00],'t':2})
      
    ################# Consecutive Frame #############
    if frameType == 2: # consecutive frame
      nextStep=0 # do nothing, just wait
      nrOfBytes = nrOfBytes - 7 # just count the number of received bytes
      print "Consecutive Frame, bytes remaining: ", nrOfBytes
      if nrOfBytes <=0:
	print "last Consecutive frame received, sending answer"
	nextStep=1 # send the answer   
	
    ######### Flow Control #############
    if frameType == 3: # 
      nextStep=2 # send remaining Consecutive frames
      
    print pid
    if len(simu)>0:
      try:
	dArray = simu[pid]
      except:
	dArray=simu["default"]
	print pid, "not found, send default"
      # print dump(dArray[0])
      print "next step: ", nextStep
      if nextStep == 1: #Single Frame or end of Consecutive Frame series -> send the PID answer
	sendTele(msg,dArray[0]) 
      if nextStep == 2: # send remaining consecute frames
	print "Array Len: ", len(dArray)
	for i in range(len(dArray)):
	  print "i: ", i
	  if i > 0 :
	    sendTele(msg,dArray[i])
    else:
      print pid, "nothing configured, just reply msg"
      sock_out.sendto( msg, (UDP_IP, UDP_PORT_OUT) )
