import sys

for recv in range(6,11):
	for cmd in range(0,8):
		for prm in range(0,8):
			for prm2 in range(0,1):
				sys.stdout.write("p "+ str(recv) + " "+ str(cmd) + " " + str(prm) + " " + str(prm2) + "\r" )

