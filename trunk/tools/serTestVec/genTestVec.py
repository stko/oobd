import sys

for recv in range(0,11):
	for cmd in range(0,8):
		for prm in range(0,8):
			for prm2 in range(0,1):
				print("p %d %d %d %d \r" % (recv,cmd,prm,prm2))  

