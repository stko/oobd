import os.path
import serial
import sys
import re
import time


class DongleCmdLine(object):

	def __init__(self):
		self._answer = ''
		self._ser = None

	def open_port(self, device):
		sys.stderr.write("open "+device)
		self._ser = serial.Serial(device, 19200, timeout=1)
		if self._ser is None:
			raise AssertionError("could not open serial line!")
		else:
			self._flush()
			
	def close_port(self):
		sys.stderr.write("close port ")
		self._ser.close

	def send_dongle_command(self,cmd):
		self._answer = cmd+'+cr+'
		if self._ser is None:
			raise AssertionError("command: serial line is not open!")
		sys.stderr.write("cmd "+cmd)
		self._doLine(cmd+"\r")

	def answer_should_match(self, expected_answer):
		sys.stderr.write("result: "+self._answer)		
		matchObj = re.match( expected_answer , self._answer, re.M|re.I)
		if matchObj:
			pass
		else:
			raise AssertionError("Expected answer to be '%s' but was '%s'."
		  		% (expected_answer, self._answer))



	def _doLine(self,line):
		if self._ser is None:
			raise AssertionError("serial line is not open!")
		else:
			self._ser.write(line)      # write a string
			maxDelay=50
			while maxDelay>0:
				nrOfBytes=self._ser.inWaiting();
				if nrOfBytes > 0:
					s = self._ser.read(nrOfBytes)          # read buffer
					s=s.replace("\r","+cr+")
					sys.stderr.write("\n### "+s+"\n") 
					self._answer +=s
					if True and s[-1:]==">": # end of feedback
						maxDelay=1 # end loop
					else:
						maxDelay =50 #rewind timeout
					
				else:
					time.sleep(0.1) # wait 0.1 seconds
					maxDelay -=1 #reduce timeout



	def _flush(self):
		if not(self._ser is None):
			nrOfBytes=50
			while nrOfBytes>0:
				nrOfBytes=self._ser.inWaiting();
				if nrOfBytes > 0:
					s = self._ser.read(nrOfBytes)          # read buffer


