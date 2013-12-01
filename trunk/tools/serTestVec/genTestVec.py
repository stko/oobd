import sys
for loop in range(0,100):
	for recv in range(1,2):
		for cmd in range(1,2):
			for prm in range(1,2):
				for prm2 in range(0,1):
					sys.stdout.write("p "+ str(recv) + " "+ str(cmd) + " " + str(prm) + " " + str(prm2) + "\r" )

