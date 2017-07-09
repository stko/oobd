#!/bin/bash
# do the inital settings for a virtual car connected by a real or virtual CAN-Bus, realized by socketCAN devices

echo "A serial connection can be made with,"
echo "socat PTY,link=/tmp/DXM PTY,link=/tmp/OOBD"
echo "--------"
echo "or a telnet connection (when running telnet in a console,"
echo "it can be quit with Ctrl+] and 'quit' )"
echo "socat  PTY,link=/tmp/OOBD tcp-listen:8081,fork"
echo
echo "and start the virtual car with"
echo "python3 carCAN.py oobdcan0"
echo "or play simulated CAN bus traffic with"
echo "./canplayer -l i -I canbusdata.sim"

BR=125000
BUS=oobdcan0
PS3='Please take your choice: '
options=("bus can0" "bus oobdcan0" "bitrate 500k" "bitrate 125k" "(re)start bus" "show can state" "start CanSim" "stop CanSim" "Quit")
select opt in "${options[@]}"
	do
		case $opt in
			"bus can0")
				echo "you chose bus Can0"
				BUS=can0
				echo "bus set to $BUS"

			;;
			"bus oobdcan0")
				echo "you chose bus oobdcan0"
				BUS=oobdcan0
				echo "bus set to $BUS"

			;;
			"bitrate 125k")
				echo "you chose bitrate 125k"
				BR=125000
				echo "bitrate set to $BR"

			;;
			"bitrate 500k")
				echo "you chose bitrate 500k"
				BR=500000
				echo "bitrate set to $BR"

			;;
			"(re)start bus")
				echo "you chose (re)start bus"
				if [ "$BUS" == "oobdcan0"  ] ; then
					if ! ip link show oobdcan0  ; then
						sudo modprobe can
						sudo modprobe can_raw
						sudo modprobe can_bcm
						sudo modprobe vcan
						sudo ip link add dev oobdcan0 type vcan
						sudo ip link set up oobdcan0
					fi
				else
					if ip link show $BUS ; then
						sudo ifconfig $BUS down
					fi
					sudo ip link set $BUS type can bitrate $BR triple-sampling on
					sudo ifconfig $BUS up
				fi

			;;
			"start CanSim")
				echo "you chose start CanSim"
				python3 carCAN.py $BUS &
				cspid=$!
			;;
			"show can state")
				echo "you chose show can state"
				ip -details link show can0
				ip -details link show oobdcan0
			;;
			"stop CanSim")
				echo "you chose stop CanSim"
				if [ -z "$cspid" ] ; then
					echo "CanSim is not running?!"
				else
					kill $cspid
					unset cspid
				fi
			;;
			"Quit")
				break
			;;
			*) echo invalid option;;
		esac
	done
