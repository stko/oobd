#!/bin/sh
# do the inital settings for a virtual car connected by a virtual CAN-Bus, realized by socketCAN device oobdcan0
echo Set CAN Devices for OOBD Simulator
if ip link show oobdcan0 ; then
	echo "CAN - Device oobdcan0 is already active, skip activation"
else
	sudo modprobe can
	sudo modprobe can_raw
	sudo modprobe can_bcm
	sudo modprobe vcan
	sudo ip link add dev oobdcan0 type vcan
	sudo ip link set up oobdcan0
fi

echo do not forget to create a serial connection with
echo socat PTY,link=/tmp/DXM PTY,link=/tmp/OOBD
echo and start the virtual car with 
echo python3 carCAN.py oobdcan0
echo or play simulated CAN bus traffic with
echo ./canplayer -l i -I canbusdata.sim

