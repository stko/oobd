#!/bin/sh
# starts the python script as virtual car connected by a simulated CAN-Bus, realized as UDP packet transfer
# supplies the virtual device /tmp/vboxcom1 as serial pipe for a VirtualBox COM port
# start your VirtualBox VM first, than this script
echo Start OOBD Simulator for the Virtualbox COM Port

python car.py &
export pythonPid=$!

socat UNIX-CONNECT:/tmp/vboxcom1 PTY,link=/tmp/OOBD &
export socatPid=$!

echo Press CRTL+C to stop
./OOBD_POSIX.bin

# Cleanup
kill $pythonPid
kill $socatPid

