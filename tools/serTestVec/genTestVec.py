import sys
for loop in range(0,10):
	for recv in range(8,9):
		for cmd in range(6,7):
			for prm in range(0,4):
				for prm2 in range(0,1):
					sys.stdout.write("p "+ str(recv) + " "+ str(cmd) + " " + str(prm) + " " + str(prm2) + "\r" )

