import sys
import serial
import time
import os
import subprocess

def writeEcho(line):
	ser_out.write(line)      # write a string
	sys.stdout.write("->"+line.replace("\r","\n"))
	maxDelay=5
	res=""
	ser_out.flush()
	while maxDelay>0:
		#nrOfBytes=ser_in.inWaiting();
		#if nrOfBytes > 0:
		s = ser_in.read(1)          # read buffer
		if s!="":
			res+=s
			if res.find(line)>-1: # input found again
				sys.stdout.write("<-"+res.replace("\r","\n"))
				return 1
		else:
			time.sleep(0.1) # wait 0.1 seconds
			maxDelay -=1 #reduce timeout
	return 0


def readLn():
	maxDelay=5
	res=""
	while maxDelay>0:
		#nrOfBytes=ser_in.inWaiting();
		#if nrOfBytes > 0:
		s = ser_in.read(1)          # read buffer
		if s!="":
			if s!="\r":
				if s!="\a":
					res+=s
			else:
				sys.stdout.write("<-"+res+"\n")
				return res
		else:
			time.sleep(0.1) # wait 0.1 seconds
			maxDelay -=1 #reduce timeout
	return ""


	
	# looks in given directory for a file with given design and given extension with a higher version number as given version
def findnewerVersion(design, version, directory, extension):
	bestfile=""
	bestversion=0
	for files in os.listdir(directory):
		if files.endswith("."+extension):
			fileParam=files.split("_")
			fileVersion=fileParam[1].split(".")
			fileVersion=fileVersion[0]
			if design==fileParam[0]:
				try:
					i = int(fileVersion)
				except ValueError:
					i = 0
				if i>version and i> bestversion:
					bestfile=files
					bestversion=i
	return bestfile


def flashFirmware(fwFileName,fhin, fhout):
	# shot the dongle into an reset
	writeEcho("p 99 2\r")
	# fire some f's to make the bootloader jump into its menu
	for i in range(5):
		 ser_out.write("f")
		 time.sleep(0.1) # wait 0.1 seconds
	ser_out.write("\r")
	ser_out.write("\r")
	readLn()
	#request flash upload
	ser_out.write("1\r")
	ser_out.write("1\r")
	ser_out.write("1\r")
	ser_out.write("1\n")
	ser_out.write("1\n")
	time.sleep(1) # wait 1 seconds
	subprocess.call(["sb" , "-vv" , "-b" , fwFileName],stdin=fhin,stdout=fhout)	 
		 
if len(sys.argv) != 5:
    sys.exit('''Usage: %s command serialPort bootloader-dir firmware-dir

tries to update bootloader and/or  firmware on OOBD cup connected to given port ''' % sys.argv[0])

#ser = serial.Serial(sys.argv[2], 19200, timeout=1)
ser_in=open(sys.argv[2], 'r')
ser_out=open(sys.argv[2], 'w')
if ser_in==None or  ser_out==None:	
	sys.exit('''Fatal Error: Serial Device %s could not been opened\nProgram will terminate\n'''% sys.argv[2])

notDone=1
while (notDone):
	print "User Info: Send a few CR first to clean the input buffer"
	writeEcho("\r")
	readLn()
	writeEcho("\r")
	readLn()
	writeEcho("\r")
	readLn()
	print "User Info: Read OOBD Identifier"
	writeEcho("p 0 0\r")
	oobdIdentifier=readLn()

	sys.stdout.write(oobdIdentifier+"\n")
	oobdIDs=oobdIdentifier.split(None,4)
	if len(oobdIDs)==0 or oobdIDs[0]!="OOBD":	
		sys.exit('''Fatal Error: The connected device can not be identied as OOBD cup\n %s will terminate\n'''% sys.argv[0])

	print "User Info: Searching for newer firmware"
	newFirmwareFile=findnewerVersion(oobdIDs[1],int(oobdIDs[2]),sys.argv[4],"oobdfw")
	if newFirmwareFile=="":	
		sys.exit('''No never firmware file available, so no update possible
	%s will terminate\n'''% sys.argv[0])
	print "new firmware found:"+newFirmwareFile
	flashFirmware(newFirmwareFile,ser_in,ser_out)
	
	
	notDone=0



ser_in.close()
ser_out.close()


