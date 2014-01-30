#http://css.dzone.com/articles/python-threads-communication



import os, time
import threading, Queue
import signal
import sys
import TCPHandlerThread
import UDPBroadcastThread
import InvaderThread

def signal_handler(signal, frame):
        print 'You pressed Ctrl+C!'
	invader_thread.join()
	udp_broadcast_thread.join()
	tcphandler_thread.join()
        
        
def exitHandler(signum = 0, frame = 0):
	print("Kill Process..")
	sys.exit(0)



def main(args):
	if len(sys.argv) != 2:
		sys.exit('Usage: %s Bluetooth-MAC (00:12:..) ' % sys.argv[0])
	BTMAC=sys.argv[1]

	# Create a single input and a single output queue for all threads.
	queue_main = Queue.Queue()
	queueTCP = Queue.Queue()
	queue_Invader = Queue.Queue()

	# Create the "invader pool" dictonary
	signal.signal(signal.SIGINT, signal_handler)

	# initial value for die counter
	die_count = 3
	# start the UDPBroadcastThread

	invader_thread=InvaderThread.InvaderThread( queue_main=queue_main, queueTCP=queueTCP, queue_Invader =queue_Invader)
	btconnect, name, description=invader_thread.connectBT(sys.argv[1]);
	if btconnect:
		invader_thread.start()
		udp_broadcast_thread=UDPBroadcastThread.UDPBroadcastThread( queue_main=queue_main, interval=2,name=name,description=description)
		udp_broadcast_thread.start()
		tcphandler_thread=TCPHandlerThread.TCPHandlerThread( queue_main=queue_main, queueTCP= queueTCP, queue_Invader =queue_Invader )
		tcphandler_thread.start()
		# just waiting for the threads to send quit messages
		while die_count > 0:
			# Blocking 'get' from a Queue.
			result = queue_main.get()
			print 'main thread input %s' % (result)
			if result == "quit":
				die_count-=1
			if result == "cancel":
				invader_thread.join()
				udp_broadcast_thread.join()
				tcphandler_thread.join()
#
if __name__ == '__main__':
	import sys
	main(sys.argv[1:])