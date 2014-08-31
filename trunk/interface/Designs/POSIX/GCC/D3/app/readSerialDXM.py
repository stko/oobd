import serial
import sys
import time

port = serial.Serial("/tmp/DXM", baudrate=115200, timeout=3.0)
port.write("\r\r\r")
time.sleep(1)
port.write("p 1 1 0 0\r")
time.sleep(1)
port.write("p 7 1 4\r")
while True:
    rcv = port.read(10)
    sys.stdout.write(rcv)
