import time

import bluetooth

import re

import select

import sys

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

#print len(cmd)

ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer

if ready[0]:

    res=gaugeSocket.recv(len(cmd))

def testCommand(thisTest):

print thisTest['descr']

print thisTest['cmd']

echoWrite(thisTest['cmd'])

ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer

if ready[0]:

    res=gaugeSocket.recv(1024)

    print res

    matchObj = re.match( thisTest['res'] , res, re.M|re.I)

    if matchObj:

        if 'dialogtext' in thisTest:

            if ccbox(thisTest['dialogtext'], "Please Confirm"):     # show a Continue/Cancel dialog

                print thisTest['okText']

                return thisTest['next']

            else:

                print thisTest['errText']

                return thisTest['err']

        else:

            print thisTest['okText']

            return thisTest['next']

    else:

        print thisTest['errText']

        return thisTest['err']

else:

    print "Empty read Buffer!"        

    return thisTest['err']

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


if len(sys.argv) < 2:

    sys.exit('Usage: %s Bluetooth-MAC (00:12:..)' % sys.argv[0])

BTMAC=sys.argv[1]

gaugeSocket = connect(BTMAC)

try:

	gaugeSocket.send('\r\r\r')
	
	ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer
	
	if ready[0]:
	
	    res=gaugeSocket.recv(1024)

except bluetooth.btcommon.BluetoothError as error:

print "Caught inital BluetoothError: ", error

echoWrite("\r\n")

res=readln()

lines=res.splitlines()

print "res=",lines

if len(lines)>0 and lines[len(lines)-1]==">":

 print "Normal Line detected"

 echoWrite("p 0 0\r")

 res=readln()

 lines=res.splitlines()

 print "res=",lines

 if lines[len(lines)-1]==">":

    version = lines[0].split()

    print version

    if version[0] == "OOBD":

       print "start flashing"

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

       print "res=",lines

#in case the bootloader is active

if len(lines)>0 and lines[len(lines)-1]=="OOBD-Flashloader>":

      print "hard core flashing starts.."

      if ccbox("ok to flash?", "Please Confirm"):

          try:

   

   

             ymodem = YMODEM(getc, putc)

   

             echoWrite("1")

   

             print "no way back.."

   

             print 'Modem instance', ymodem

   

             status = ymodem.send(['OOBD_Firmware_SVN415.bin'])

   

             print  'sent', status

   

          except:

   

                 print "Unexpected error:", sys.exc_info()[0]

   

gaugeSocket.close()


def exitHandler(signum = 0, frame = 0):

print("Kill Process..")

gaugeSocket.close()

sys.exit(0)


