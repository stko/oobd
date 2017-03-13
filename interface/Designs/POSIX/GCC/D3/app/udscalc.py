# -*- coding: utf-8 -*-
import sys
a=sys.argv[1]
print a
msg =bytearray()
msg+=a
l=len(msg)
print l
for i in range(len(msg)) :
  sys.stdout.write("0x%02X, " % ( msg[i]))
  
print