import sys
import serial
import time


def doLine(line):
	print "->"+line
	ser.write(line)      # write a string
	maxDelay=5
	while maxDelay>0:
		nrOfBytes=ser.inWaiting();
		if nrOfBytes > 0:
			s = ser.read(nrOfBytes)          # read buffer
			print s
			maxDelay =5 #rewind timeout
		else:
			time.sleep(0.1) # wait 0.1 seconds
			maxDelay -=1 #reduce timeout



if len(sys.argv) != 3:
    sys.exit('''Usage: %s serialPort inputfile

sends the inputfile to serialport and prints input and output

if inputfile = "-", stdin is used as input ''' % sys.argv[0])

ser = serial.Serial(sys.argv[1], 19200, timeout=1)
print sys.argv[1]
if sys.argv[2]=="-":
	f=sys.stdin
else:
	f = open(sys.argv[2], 'rb')
thisLine=""
c = f.read(1)
while c != "":
	thisLine +=c
	if c =="\r":
		doLine(thisLine)     # write a string
		thisLine=""
	c = f.read(1)
f.close()
ser.close()


