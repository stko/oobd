import time
import bluetooth
import re
import select
import sys

# to install easygui, use "sudo apt-get install python-easygui"
from easygui import *

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
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':1,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':2,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':3,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':4,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 1\r','res':r'.*(:).*','next':5,'err':-1,'descr':'Looking for Serial Nr.','okText':'OK: Serial Nr received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 2\r','res':r'.*1\d{4} mV.*','next':6,'err':-1,'descr':'Check Voltage > 10V','okText':'OK: Voltage > 10V','errText':'Error: No correct Answer'},

{'cmd':'p 1 2 0 0\r','res':r'.*(.).*','next':7,'err':-1,'descr':'Blue LED off','okText':'OK: Blue LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 1 0\r','res':r'.*(.).*','next':8,'err':-1,'descr':'Green LED off','okText':'OK: Green LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 2 0\r','res':r'.*(.).*','next':9,'err':-1,'descr':'Red LED off','okText':'OK: Red LED turned off','errText':'Error: No correct Answer','dialogtext':'Are green and red LEDs off?'},
{'cmd':'p 1 2 0 1\r','res':r'.*(.).*','next':10,'err':-1,'descr':'Blue LED on','okText':'OK: Blue LED turned on','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 1 1\r','res':r'.*(.).*','next':11,'err':-1,'descr':'Green LED on','okText':'OK: Green LED turned on','errText':'Error: No correct Answer','dialogtext':'Is the green LED on?'},
{'cmd':'p 1 2 1 0\r','res':r'.*(.).*','next':12,'err':-1,'descr':'Green LED off','okText':'OK: Green LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 2 1\r','res':r'.*(.).*','next':13,'err':-1,'descr':'Red LED on','okText':'OK: Red LED turned on','errText':'Error: No correct Answer','dialogtext':'Is the red LED on?'},
{'cmd':'p 1 2 2 0\r','res':r'.*(.).*','next':14,'err':-1,'descr':'Red LED off','okText':'OK: Red LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 3 1000\r','res':r'.*(.).*','next':15,'err':-1,'descr':'Buzzer on','okText':'OK: Buzzer turned on','errText':'Error: No correct Answer','dialogtext':'Do you hear the Buzzer?'},
{'cmd':'p 1 2 3 0\r','res':r'.*(.).*','next':16,'err':-1,'descr':'Buzzer off','okText':'OK: Red LED turned off','errText':'Error: No correct Answer'},
#{'cmd':'p 1 2 3 0\r','res':r'.*(.).*','next':21,'err':-1,'descr':'Buzzer off','okText':'OK: Red LED turned off','errText':'Error: No correct Answer'},

# KL-Line Test
{'cmd':'p  1 2 4 0\r','res':r'.*(.).*','next':17,'err':-1,'descr':' K-Line TX OFF (Transmit mode) ','okText':'OK: K-Line TX OFF','errText':'Error: No correct Answer'},
{'cmd':'p  1 2 5 1\r','res':r'.*(.).*','next':18,'err':-1,'descr':' L-Line ON - 0V ','okText':'OK: L-Line ON','errText':'Error: No correct Answer'},
{'cmd':'p  0 0 11\r','res':r'.*(high).*','next':19,'err':-1,'descr':' read K-Line Level ','okText':'OK: K-Line HIGH','errText':'Error: No correct Answer'},
{'cmd':'p  1 2 5 0\r','res':r'.*(.).*','next':20,'err':-1,'descr':' L-Line OFF - 12V (PullUp) ','okText':'OK: L-Line OFF','errText':'Error: No correct Answer'},
{'cmd':'p  0 0 11\r','res':r'.*(low).*','next':21,'err':-1,'descr':' read K-Line Level ','okText':'OK: K-Line LOW','errText':'Error: No correct Answer'},
#{'cmd':'p  0 0 11\r','res':r'.*(low).*','next':32,'err':-1,'descr':' read K-Line Level ','okText':'OK: K-Line LOW','errText':'Error: No correct Answer'},



# high speed test
{'cmd':'p 8 2 0\r','res':r'.*(.).*','next':22,'err':-1,'descr':'deactivate Bus','okText':'OK: Bus deactivated','errText':'Error: No correct Answer'},
{'cmd':'p 8 3 0\r','res':r'.*(.).*','next':23,'err':-1,'descr':'set CAN 500kb / 11b','okText':'OK: Bus set','errText':'Error: No correct Answer'},
{'cmd':'p 8 4 0\r','res':r'.*(.).*','next':24,'err':-1,'descr':'set Channel 0','okText':'OK: Channel set','errText':'Error: No correct Answer'},
{'cmd':'p 8 2 3\r','res':r'.*(.).*','next':25,'err':-1,'descr':'set Bus active','okText':'OK: Bus activated','errText':'Error: No correct Answer'},
{'cmd':'p 6 5 $737\r','res':r'.*(.).*','next':26,'err':-1,'descr':'set Module ID 737 (Airbag)','okText':'OK: Module ID set','errText':'Error: No correct Answer'},
{'cmd':'p 8 10 1 $07FF\r','res':r'.*(.).*','next':27,'err':-1,'descr':'set CAN Filter','okText':'OK: CAN Filter set','errText':'Error: No correct Answer'},
{'cmd':'p 8 11 1 $0000\r','res':r'.*(.).*','next':28,'err':-1,'descr':'set CAN Mask ','okText':'OK: CAN Mask set','errText':'Error: No correct Answer'},
{'cmd':'p 6 9 $73f\r','res':r'.*(.).*','next':29,'err':-1,'descr':'set Send ID','okText':'OK: Send ID set','errText':'Error: No correct Answer'},
{'cmd':'1002\r','res':r'^(50).*','next':32,'err':30,'descr':'send Tester present','okText':'OK: Answer from Cluster','errText':'Error: No correct Answer from Airbag'},
{'cmd':'1002\r','res':r'^(50).*','next':32,'err':31,'descr':'send Tester present','okText':'OK: Answer from Cluster','errText':'Error: No correct Answer from Airbag'},
{'cmd':'1002\r','res':r'^(50).*','next':32,'err':-1,'descr':'send Tester present','okText':'OK: Answer from Cluster','errText':'Error: No correct Answer from Airbag'},
# mid speed test
{'cmd':'p 8 2 0\r','res':r'.*(.).*','next':33,'err':-1,'descr':'deactivate Bus','okText':'OK: Bus deactivated','errText':'Error: No correct Answer'},
{'cmd':'p 8 3 1\r','res':r'.*(.).*','next':34,'err':-1,'descr':'set CAN 125kb / 11b','okText':'OK: Bus set','errText':'Error: No correct Answer'},
{'cmd':'p 8 4 1\r','res':r'.*(.).*','next':35,'err':-1,'descr':'set Channel 1','okText':'OK: Channel set','errText':'Error: No correct Answer'},
{'cmd':'p 8 2 3\r','res':r'.*(.).*','next':36,'err':-1,'descr':'set Bus active','okText':'OK: Bus activated','errText':'Error: No correct Answer'},
{'cmd':'p 6 5 $741\r','res':r'.*(.).*','next':37,'err':-1,'descr':'set Module ID','okText':'OK: Module ID set','errText':'Error: No correct Answer'},
{'cmd':'p 8 10 1 $07FF\r','res':r'.*(.).*','next':38,'err':-1,'descr':'set CAN Filter','okText':'OK: CAN Filter set','errText':'Error: No correct Answer'},
{'cmd':'p 8 11 1 $0000\r','res':r'.*(.).*','next':39,'err':-1,'descr':'set CAN Mask ','okText':'OK: CAN Mask set','errText':'Error: No correct Answer'},
{'cmd':'p 6 9 $749\r','res':r'.*(.).*','next':40,'err':-1,'descr':'set Send ID','okText':'OK: Send ID set','errText':'Error: No correct Answer'},
{'cmd':'1002\r','res':r'^(50).*','next':-1,'err':41,'descr':'send Tester present','okText':'OK: Answer from DCU','errText':'Error: No correct Answer from DCU'},
{'cmd':'1002\r','res':r'^(50).*','next':-1,'err':42,'descr':'send Tester present','okText':'OK: Answer from DCU','errText':'Error: No correct Answer from DCU'},
{'cmd':'1002\r','res':r'^(50).*','next':-1,'err':-1,'descr':'send Tester present','okText':'OK: Answer from DCU','errText':'Error: No correct Answer from DCU'},
]

def echoWrite(cmd):
	gaugeSocket.send(cmd)
	#print len(cmd)
	res=gaugeSocket.recv(len(cmd))

def testCommand(thisTest):
	print thisTest['descr']
	print thisTest['cmd']
	echoWrite(thisTest['cmd'])
	recvDone=False
	res=""
	while(not recvDone):
		ready = select.select([gaugeSocket], [], [], 1) #waiting 1 sec for an answer
		if ready[0]:
		    	res+=gaugeSocket.recv(1024)
			recvDone=res[-2:]=="\r>"
		else:
			recvDone=True
	if res!="":
		matchObj = re.match( thisTest['res'] , res, re.M|re.I)
		print res.replace("\r","\r\n")

		if matchObj:
			if 'dialogtext' in thisTest:
				if ccbox(thisTest['dialogtext'], "Please Confirm"):     # show a Continue/Cancel dialog
					print  bcolors.OKGREEN + thisTest['okText'] + bcolors.ENDC
					return thisTest['next']
				else:
					print bcolors.FAIL + thisTest['errText'] + bcolors.ENDC
					return thisTest['err']
			else:
				print bcolors.OKGREEN + thisTest['okText'] + bcolors.ENDC
				return thisTest['next']
		else:
			print  bcolors.FAIL + thisTest['errText'] + bcolors.ENDC
			return thisTest['err']
	else:
		print  bcolors.FAIL + "Empty read Buffer!" + bcolors.ENDC	
		return thisTest['err']

def connect(macAdress):
	while(True):
		try:
			gaugeSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
			gaugeSocket.connect((macAdress, 1))
			print "Connected to",macAdress
			time.sleep(0.5)
			#gaugeSocket.setblocking(0)
			break;
		except bluetooth.btcommon.BluetoothError as error:
			gaugeSocket.close()
			print "Could not connect: ", error, "; Retrying in 10s..."
			time.sleep(10)
	return gaugeSocket;


if len(sys.argv) < 2:
    sys.exit('Usage: %s database-name' % sys.argv[0])

BTMAC=sys.argv[1]
gaugeSocket = connect(BTMAC)
try:
	gaugeSocket.send('\r\r\r')
	gaugeSocket.recv(1024)
except bluetooth.btcommon.BluetoothError as error:
	print bcolors.FAIL + "Caught inital BluetoothError: ", error , bcolors.ENDC
state=0
while(state!=-1):    
	try:
		thisTest=testSet[state]
		state=testCommand(thisTest)
		#time.sleep(1)
	except bluetooth.btcommon.BluetoothError as error:
		print bcolors.FAIL + "Caught BluetoothError: ", error , bcolors.ENDC
		time.sleep(5)
		gaugeSocket = connect()

gaugeSocket.close()


def exitHandler(signum = 0, frame = 0):
	print("Kill Process..")
	gaugeSocket.close()
	sys.exit(0)
