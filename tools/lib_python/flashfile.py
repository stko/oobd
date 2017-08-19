#!/usr/bin/env python
import os
import sys

import flashecu

import binascii


flashFile=flashecu.FlashECU(sys.argv[1])
counter = 1
for block in flashFile.blocks():
	print ("Block {0}: Start Adress {1:0X} Size {2} Bytes".format(counter,block["startAddress"],block["blockSize"]))
	counter+=1
	binary=flashFile.getBinary(block)
	print binascii.hexlify(binary)
print flashFile.header


