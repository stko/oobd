import time
import bluetooth
import re
import select
import sys
import subprocess

from modem import YMODEM
# to install easygui, use "sudo apt-get install python-easygui"
from easygui import *

def getc(size, timeout=5):
	r, w, e = select.select([gaugeSocket], [], [], timeout)
	if r: return gaugeSocket.recv(size)

def putc(data, timeout=1):
	r, w, e = select.select([], [gaugeSocket], [], timeout)
	if w: return gaugeSocket.send(data)


def readln():
	ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer
	if ready[0]:
		res=gaugeSocket.recv(1024)
		print res
		return res
	else:
		print "Empty read Buffer!"        
		return ''

def echoWrite(cmd):
	gaugeSocket.send(cmd)
	ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer
	if ready[0]:
		res=gaugeSocket.recv(len(cmd))


def connect(macAdress):
	while(True):
		try:
			gaugeSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
			gaugeSocket.connect((macAdress, 1))
			#gaugeSocket.setblocking(0)
			break;
		except bluetooth.btcommon.BluetoothError as error:
			gaugeSocket.close()
			print "Could not connect: ", error, "; Retrying in 10s..."
			time.sleep(10)
	return gaugeSocket;


if len(sys.argv) != 3:
	sys.exit('Usage: %s Bluetooth-MAC (00:12:..) firmwarefile.bin' % sys.argv[0])
BTMAC=sys.argv[1]
gaugeSocket = connect(BTMAC)
try:
	gaugeSocket.send('\r\r\r')
	ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer
	if ready[0]:
		res=gaugeSocket.recv(1024)
except bluetooth.btcommon.BluetoothError as error:
	print "Caught inital BluetoothError: ", error
runstatus=2
while(runstatus>0):
	echoWrite("\r\n")
	res=readln()
	lines=res.splitlines()
	if len(lines)>0 and lines[len(lines)-1]==">":
		print "Responce from Dongle detected, asking for Version string"
		echoWrite("p 0 0\r")
		res=readln()
		lines=res.splitlines()
		#print "res=",lines
		if lines[len(lines)-1]==">":
			version = lines[0].split()
			print "Dongle Version:", version
			if version[0] == "OOBD":
				print "OOBD Firmware identified - try to activate Bootloader"
				echoWrite("p 0 99 2\r")
				time.sleep(0.1)
				#gaugeSocket.send("fff")
				echoWrite("fff")
				res=readln()
				echoWrite("\r\r\r")
				time.sleep(1)
				res=readln()
				echoWrite("\r")
				time.sleep(1)
				res=readln()
				lines=res.splitlines()

	#in case the bootloader is active
	if len(lines)>0 and lines[len(lines)-1]=="OOBD-Flashloader>":
		print "Bootloader active, flashing can start.."
		if ccbox("ok to flash?", "Please Confirm"):
			try:
				# ymodem = YMODEM(getc, putc)
				echoWrite("1")
				print "start transfer  - no way back.."
				# print 'Modem instance', ymodem
				# status = ymodem.send([sys.argv[2]])

				status   = subprocess.call(['sb',  sys.argv[2]], stdin=gaugeSocket, stdout=gaugeSocket)

				print  'sent status', status
				if status==0:
					#start the firmware
					gaugeSocket.send("3")
				runstatus = 0
			except:
				print "Unexpected error:", sys.exc_info()[0]
	else: # the dongle seems not answer
		runstatus -= 1 # reduce the trial conter by 1
	if runstatus==1: # it seems serious, the dongle does not answer. Last chance is to kill a potential running ymodem- transfer..
		print("Try to send YMODEM cancel signal 0x18 a few times..")
		gaugeSocket.send("\x18\x18\x18\x18\x18\x18")


gaugeSocket.close()


def exitHandler(signum = 0, frame = 0):
	print("Kill Process..")
	gaugeSocket.close()
	sys.exit(0)

