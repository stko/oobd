MYPORT = 42000 # socketcand broadcast 

import sys, time
from socket import *
import threading, Queue

class UDPBroadcastThread(threading.Thread):
    """ A IUDPBroadcastThread that takes the interval in secs as start parameter

        Ask the thread to stop by calling its join() method.
    """
    def __init__(self , queue_main , interval,name , description):
	super(UDPBroadcastThread, self).__init__()
	self.queue_main = queue_main
	self.interval = interval
	self.name = name
	self.description = description
	self.stoprequest = threading.Event()

    def run(self):
	my_str = """<CANBeacon name="%s" type="adapter" description="%s">
    <URL>can://127.0.0.1:29536</URL>
    <Bus name="HSCAN"/>
    <Bus name="MSCAN"/>
</CANBeacon>"""% (self.name, self.description)
	queryMsg = str.encode(my_str)
	type(queryMsg)
	s = socket(AF_INET, SOCK_DGRAM)
	s.connect(('8.8.8.8', 80))
	host = s.getsockname()[0]
	s.close()
	s = socket(AF_INET, SOCK_DGRAM)
	print 'UDPBroadcastThread ip:%s' % host[0]
	s.bind((host, 0))
	s.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)
	# Send UDP broadcast packets
	while not self.stoprequest.isSet():
		try:
			s.sendto(queryMsg, ('<broadcast>', MYPORT))
			time.sleep(self.interval)
		except Queue.Empty:
			continue
	print 'UDPBroadcastThread dies..' 
	self.queue_main.put("quit")
	
    def join(self, timeout=None):
        self.stoprequest.set()
        super(UDPBroadcastThread, self).join(timeout)
