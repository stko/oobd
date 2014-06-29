import time
import bluetooth
import re
import select
import sys
from sys import stderr



def print_stderr(*args, **kwargs):
    print(*args, file=stderr, **kwargs)

class bcolors:
	HEADER = '\033[95m'
	OKBLUE = '\033[94m'
	OKGREEN = '\033[92m'
	WARNING = '\033[93m'
	FAIL = '\033[91m'
	ENDC = '\033[0m'

	def disable(self):
		self.HEADER = ''
		self.OKBLUE = ''
		self.OKGREEN = ''
		self.WARNING = ''
		self.FAIL = ''
		self.ENDC = ''



testSet=[
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':-2,'err':1,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':-2,'err':2,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':-2,'err':3,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':-2,'err':-1,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},

]

def echoWrite(cmd):
	gaugeSocket.send(cmd)
	res=gaugeSocket.recv(len(cmd))


def doSingleCmd(cmd, regex):
	echoWrite(cmd)
	recvDone=False
	res=""
	while(not recvDone):
		ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer
		if ready[0]:
			res+=gaugeSocket.recv(1024).decode("utf-8")
			recvDone=res[-2:]=="\r>"
		else:
			recvDone=True
	print_stderr("Received:",res.replace("\r","\n"))
	if res!="":
		matchObj = re.search( regex , res, re.M|re.I)
		if res[-3:]==".\r>":
			res=res[:-3]
		res=res.replace("\r","")

		if matchObj:
			return {'err':0,'text':res}
		else:
			return {'err':1,'text':res}
	else:
		return {'err':1,'text':''}





def testCommand(thisTest):
	print_stderr ("Description:", thisTest['descr'])
	print_stderr ("Command:", thisTest['cmd'])
	singleCMD=doSingleCmd(thisTest['cmd'],thisTest['res'])
	if singleCMD['err']==0:
		print_stderr (bcolors.OKGREEN + thisTest['okText'] + bcolors.ENDC)
		return thisTest['next']
	else:
		print_stderr  (bcolors.FAIL + thisTest['errText'] + bcolors.ENDC)
		return thisTest['err']

def connect(macAdress):
	try:
		port = 1
		gaugeSocket=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
		gaugeSocket.connect((macAdress, port))

		#so it would like with the build in bluetooth support of python >=3.3, but that does not work
		# under windows :-(
		# in the header: import socket
		#gaugeSocket = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
		#gaugeSocket.connect((macAdress,port))

		print_stderr ("Connected to",macAdress)
		time.sleep(0.5)
		#gaugeSocket.setblocking(0)
	except socket.error as message: 
		if gaugeSocket: 
			gaugeSocket.close() 
		gaugeSocket=None
		print_stderr ("Could not connect: ", message)
	return gaugeSocket;


if len(sys.argv) < 2:
    sys.exit('''
Usage: %s BT-MAC-Address \'[cmdset[,cmdset]]\'
for detailed information goto http://www.oobd.org/doku.php?id=doc:tools_oobdcmd
''' % sys.argv[0])

BTMAC=sys.argv[1]
gaugeSocket = connect(BTMAC)
if gaugeSocket == None:
	exit(1) # error code for failed connect
try:
	gaugeSocket.send('\r\r\r')
	gaugeSocket.recv(1024)
except socket.error as error:
	print_stderr (bcolors.FAIL + "Caught inital BluetoothError: ", error , bcolors.ENDC)
	exit(1) # error code for failed connect
state=0
while(state!=-1 and state!=-2 ):    
	try:
		thisTest=testSet[state]
		state=testCommand(thisTest)
	except socket.error as error:
		print_stderr (bcolors.FAIL + "Caught BluetoothError: ", error , bcolors.ENDC)
		exit(1) # error code for failed connect

if state != -2:
	exit(2) # error code for failed init

if len(sys.argv) > 2:
	print_stderr("Do Command Sequence")
	iniString=sys.argv[2]
	cmdSets=re.split(",",iniString)
	try:
		for cmdSet in cmdSets:
			print_stderr ("cmdSet:",cmdSet)
			cmdDetails=re.split("\|",cmdSet)
			if len(cmdDetails) != 2:
				print_stderr("Error: Wrong command format in", cmdSet)
				exit(3)
			print_stderr ("cmd:",cmdDetails[0])
			print_stderr ("regex:",cmdDetails[1])
			singleCMD=doSingleCmd(bytes(cmdDetails[0], "utf-8").decode("unicode_escape"), cmdDetails[1])
			if singleCMD['err']!=0:
				print_stderr("Error: Command sequence failed on ", cmdSet)
				exit(3)
	except socket.error as error:
		print_stderr (bcolors.FAIL + "Caught BluetoothError: ", error , bcolors.ENDC)
		exit(1) # error code for failed connect

if len(sys.argv) > 2 and len(sys.argv) < 4 : # no additional UDS-command given, so output the results from init
	if singleCMD != None: # we have a result
		print (singleCMD['text']) # this time only a print(), not a print_stderr(), because the result shall go to stdout


gaugeSocket.close()
exit(0) 

def exitHandler(signum = 0, frame = 0):
	print_stderr("Kill Process..")
	gaugeSocket.close()
	sys.exit(0)
