#!/bin/sh
# starts the python script as virtual car connected by a simulated CAN-Bus, realized as UDP packet transfer
echo Start OOBD Simulator
python car.py &
echo generate /tmp/DXM and /tmp/OOBD as virtual COM ports
echo connect your terminal program to /tmp/DXM
echo Press CRTL+C to stop
socat PTY,link=/tmp/DXM PTY,link=/tmp/OOBD