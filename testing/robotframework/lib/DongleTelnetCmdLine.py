import os.path
import socket
import sys
import re
import time

_ser= None

class DongleTelnetCmdLine(object):

	def __init__(self):
		self._answer = ''

	def open_port(self, host, port):
		sys.stderr.write("open "+host+" on port ")
		sys.stderr.write(port)
		sys.stderr.write("\n")
		global _ser
		_ser = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		_ser.settimeout(0.2)
     
		# connect to remote host
		try :
			_ser.connect((host, port))
		except :
			raise AssertionError("could not open telnet port!")
		self._flush()
			
	def read_stdin(self,txt):
		self._answer=raw_input(txt)

	def close_port(self):
		sys.stderr.write("close port ")
		global _ser
		if _ser:
			_ser.shutdown(socket.SHUT_RDWR)
		_ser.close

	def send_dongle_command(self,cmd):
		global _ser
		self._answer = ""; #cmd+'+cr+'
		if _ser is None:
			raise AssertionError("command: telnet port is not open!")
		#sys.stderr.write("cmd "+cmd)
		self._doLine(cmd+"\r")

	def answer_should_match(self, expected_answer):
		#sys.stderr.write("result: "+self._answer)		
		matchObj = re.match( expected_answer , self._answer, re.M|re.I)
		if matchObj:
			pass
		else:
			raise AssertionError("Expected answer to be '%s' but was '%s'."
		  		% (expected_answer, self._answer))


	def answer_should_be_equal(self, expected_answer):
		if self._answer==expected_answer:
			pass
		else:
			raise AssertionError("Expected answer to be '%s' but was '%s'."
		  		% (expected_answer, self._answer))



	def _doLine(self,line):
		global _ser
		if _ser is None:
			raise AssertionError("telnet port is not open!")
		else:
#			sys.stderr.write("\n"+time.strftime("%Y-%m-%d %H:%M:%S", time.gmtime())+" Start\n") 
			_ser.send(line.encode())      # write a string
			maxDelay=50
			while maxDelay>0:
				try:
					s = _ser.recv(4096)          # read buffer
					#sys.stderr.write("recv:"+s+ " with a maxDelay of ")
					s=s.replace("\r","+cr+")
					s=s.replace("\n","+lf+")
					#sys.stderr.write(maxDelay)
					#sys.stderr.write("\n")
					self._answer +=s
					if True and s[-1:]==">": # end of feedback
						maxDelay=0 # end loop
					else:
						maxDelay =50 #rewind timeout
					
				except :
					maxDelay -=1 #reduce timeout


	def _flush(self):
		global _ser
		if not(_ser is None):
			try:
				s = _ser.recv(4096)          # read buffer
			except :
				pass

