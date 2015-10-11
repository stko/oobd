import sys
import serial
from time import sleep

if len(sys.argv)!=2:
	print ("Usage: "+ sys.argv[0] + " port (e.g. /dev/ttyS0)")
	exit(0)


ser = serial.Serial(sys.argv[1], 38400, timeout=10)
input = ""

while True:
	data = ser.read(1)
	if len(data) > 0:
		input += data
		if data=="\r":
			print 'Got:', input
			input = ""
		ser.write(data)

ser.close()

