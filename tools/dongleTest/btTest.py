import time
import bluetooth
import re
import select
import sys

# to install easygui, use "sudo apt-get install python-easygui"
from easygui import *


if len(sys.argv) < 2:
    sys.exit('Usage: %s BT-MAC (00:00...)' % sys.argv[0])

BTMAC=sys.argv[1]
secsToWait=3
nrOfTrials=10
count=1
while(count < nrOfTrials):
	try:
		count+=1
		gaugeSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
		gaugeSocket.connect((BTMAC, 1))
		#gaugeSocket.setblocking(0)
		print "Connected - wait ", secsToWait, " secs"
		gaugeSocket.close()
		time.sleep(secsToWait)
	except bluetooth.btcommon.BluetoothError as error:
		gaugeSocket.close()
		print "Could not connect: ", error, "; Retrying in ", secsToWait, "secs..."
		time.sleep(secsToWait)




gaugeSocket.close()


def exitHandler(signum = 0, frame = 0):
	print("Kill Process..")
	gaugeSocket.close()
	sys.exit(0)
