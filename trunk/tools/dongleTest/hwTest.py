import time
import bluetooth
import re
import select
# to install easygui, use "sudo apt-get install python-easygui"
from easygui import *

testSet=[
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':1,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':2,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':3,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 0\r','res':r'.*(OBD).*','next':4,'err':4,'descr':'Looking for OOBD ID','okText':'OK: OOBD String received','errText':'Error: No correct Answer'},
{'cmd':'p 0 0 1\r','res':r'.*(:).*','next':5,'err':-1,'descr':'Looking for Serial Nr.','okText':'OK: Serial Nr. received','errText':'Error: No correct Answer'},

{'cmd':'p 1 2 0 0\r','res':r'.*(.).*','next':6,'err':-1,'descr':'Blue LED off','okText':'OK: Blue LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 1 0\r','res':r'.*(.).*','next':7,'err':-1,'descr':'Green LED off','okText':'OK: Green LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 2 0\r','res':r'.*(.).*','next':8,'err':-1,'descr':'Red LED off','okText':'OK: Red LED turned off','errText':'Error: No correct Answer','dialogtext':'Are green and red LEDs off?'},
{'cmd':'p 1 2 0 1\r','res':r'.*(.).*','next':9,'err':-1,'descr':'Blue LED on','okText':'OK: Blue LED turned on','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 1 1\r','res':r'.*(.).*','next':10,'err':-1,'descr':'Green LED on','okText':'OK: Green LED turned on','errText':'Error: No correct Answer','dialogtext':'Are the green LED on?'},
{'cmd':'p 1 2 1 0\r','res':r'.*(.).*','next':11,'err':-1,'descr':'Green LED off','okText':'OK: Green LED turned off','errText':'Error: No correct Answer'},
{'cmd':'p 1 2 2 1\r','res':r'.*(.).*','next':12,'err':-1,'descr':'Red LED on','okText':'OK: Red LED turned on','errText':'Error: No correct Answer','dialogtext':'Are the red LED on?'},
{'cmd':'p 1 2 2 0\r','res':r'.*(.).*','next':13,'err':-1,'descr':'Red LED off','okText':'OK: Red LED turned off','errText':'Error: No correct Answer'},

# mid speed test
{'cmd':'p 8 2 0\r','res':r'.*(.).*','next':14,'err':-1,'descr':'deactivate Bus','okText':'OK: Bus deactivated','errText':'Error: No correct Answer'},
{'cmd':'p 8 3 1\r','res':r'.*(.).*','next':15,'err':-1,'descr':'set CAN 125kb / 11b','okText':'OK: Bus set','errText':'Error: No correct Answer'},
{'cmd':'p 8 4 1\r','res':r'.*(.).*','next':16,'err':-1,'descr':'set Channel 0','okText':'OK: Channel set','errText':'Error: No correct Answer'},
{'cmd':'p 8 2 3\r','res':r'.*(.).*','next':17,'err':-1,'descr':'set Bus active','okText':'OK: Bus activated','errText':'Error: No correct Answer'},
{'cmd':'p 6 5 $741\r','res':r'.*(.).*','next':18,'err':-1,'descr':'set Module ID','okText':'OK: Module ID set','errText':'Error: No correct Answer'},
{'cmd':'p 8 10 1 $07FF\r','res':r'.*(.).*','next':19,'err':-1,'descr':'set CAN Filter','okText':'OK: CAN Filter set','errText':'Error: No correct Answer'},
{'cmd':'p 8 11 1 $0000\r','res':r'.*(.).*','next':20,'err':-1,'descr':'set CAN Mask ','okText':'OK: CAN Mask set','errText':'Error: No correct Answer'},
{'cmd':'p 6 9 $749\r','res':r'.*(.).*','next':21,'err':-1,'descr':'set Send ID','okText':'OK: Send ID set','errText':'Error: No correct Answer'},
{'cmd':'1002\r','res':r'^(50).*','next':-1,'err':22,'descr':'send Tester present','okText':'OK: Answer from Module','errText':'Error: No correct Answer'},
{'cmd':'1002\r','res':r'^(50).*','next':-1,'err':23,'descr':'send Tester present','okText':'OK: Answer from Module','errText':'Error: No correct Answer'},
{'cmd':'1002\r','res':r'^(50).*','next':-1,'err':-1,'descr':'send Tester present','okText':'OK: Answer from Module','errText':'Error: No correct Answer'},
]

def echoWrite(cmd):
	gaugeSocket.send(cmd)
	#print len(cmd)
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

def connect():
	while(True):
		try:
			gaugeSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
			gaugeSocket.connect(('00:12:6F:22:4E:8E', 1))
			#gaugeSocket.setblocking(0)
			break;
		except bluetooth.btcommon.BluetoothError as error:
			gaugeSocket.close()
			print "Could not connect: ", error, "; Retrying in 10s..."
			time.sleep(10)
	return gaugeSocket;

gaugeSocket = connect()
try:
	gaugeSocket.send('\r\r\r')
	gaugeSocket.recv(1024)
except bluetooth.btcommon.BluetoothError as error:
	print "Caught inital BluetoothError: ", error
state=0
while(state!=-1):    
	try:
		thisTest=testSet[state]
		state=testCommand(thisTest)
		#time.sleep(1)
	except bluetooth.btcommon.BluetoothError as error:
		print "Caught BluetoothError: ", error
		time.sleep(5)
		gaugeSocket = connect()

gaugeSocket.close()


def exitHandler(signum = 0, frame = 0):
	print("Kill Process..")
	gaugeSocket.close()
	sys.exit(0)
