#!/bin/sh
# do the inital settings for a virtual car connected by a virtual CAN-Bus, realized by socketCAN device oobdcan0
echo Set CAN Devices for OOBD Simulator

sudo modprobe can
sudo modprobe can_raw
sudo modprobe can_bcm
sudo modprobe vcan
sudo ip link add dev oobdcan0 type vcan
sudo ip link set up oobdcan0
echo Device name used: oobdcan0
