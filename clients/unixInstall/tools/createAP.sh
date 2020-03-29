#!/bin/bash
# taken from: https://cdn-learn.adafruit.com/downloads/pdf/setting-up-a-raspberry-pi-as-a-wifi-access-point.pdf
if [ -z "$1" ]
	then
		SSID=OOBD
	else
		SSID=$1

fi
if [ -z "$2" ]
	then
		PASSWORD=oobdoobd
	else
		SSID=$1

fi


# Create a minimized dhcpd connfiguration
############################################################################
cat << 'EOF' | sudo tee  /etc/dhcp/dhcpd.conf
ddns-update-style none;
default-lease-time 600;
max-lease-time 7200;
authoritative;
log-facility local7;
subnet 192.168.42.0 netmask 255.255.255.0 {
range 192.168.42.10 192.168.42.50;
option broadcast-address 192.168.42.255;
option routers 192.168.42.1;
default-lease-time 600;
max-lease-time 7200;
option domain-name "local";
option domain-name-servers 8.8.8.8, 8.8.4.4;
}
EOF
############################################################################


# Create a minimized dhcpd configuration
############################################################################
cat << 'EOF' | sudo tee   /etc/default/isc-dhcp-server
INTERFACES="wlan0"
EOF
############################################################################

#sudo ifdown wlan0

 # write a new network configuration
############################################################################
cat << 'EOF' | sudo tee   /etc/network/interfaces
# interfaces(5) file used by ifup(8) and ifdown(8)

# Please note that this file is written to be used with dhcpcd
# For static IP, consult /etc/dhcpcd.conf and 'man dhcpcd.conf'

# Include files from /etc/network/interfaces.d:
source-directory /etc/network/interfaces.d

auto lo
iface lo inet loopback

iface eth0 inet dhcp

allow-hotplug wlan0

iface wlan0 inet static
  address 192.168.42.1
  netmask 255.255.255.0

EOF
############################################################################

sudo ifconfig wlan0 192.168.42.1

sudo mkdir -p /etc/hostapd/
# Create the hostapd connfiguration
############################################################################
cat << EOF | sudo tee   /etc/hostapd/hostapd.conf
interface=wlan0
# which is the correct driver for non-RPI 3 models?
#driver=rtl871xdrv
ssid=$SSID
country_code=US
hw_mode=g
channel=6
macaddr_acl=0
auth_algs=1
ignore_broadcast_ssid=0
wpa=2
wpa_passphrase=$PASSWORD
wpa_key_mgmt=WPA-PSK
wpa_pairwise=CCMP
wpa_group_rekey=86400
ieee80211n=1
wme_enabled=1
EOF
############################################################################

# make the config known
echo DAEMON_CONF="/etc/hostapd/hostapd.conf" | sudo tee  -a /etc/default/hostapd
sudo sed  -i '/DAEMON_CONF=/c\DAEMON_CONF=/etc/hostapd/hostapd.conf' /etc/init.d/hostapd

# this would be only needed for a permanent change. so it's listed here only for completeness
# echo "net.ipv4.ip_forward=1" | sudo tee  -a /etc/sysctl.conf

# modify the routing tables
sudo sh -c "echo 1 > /proc/sys/net/ipv4/ip_forward "

sudo iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
sudo iptables -A FORWARD -i eth0 -o wlan0 -m state --state RELATED,ESTABLISHED -j ACCEPT
sudo iptables -A FORWARD -i wlan0 -o eth0 -j ACCEPT

#view table content with
# sudo iptables -t nat -S
# sudo iptables -S

# for permanent use, this could be saved with
#sudo sh -c "iptables-save > /etc/iptables/rules.v4"

# we must kill wpa_supplicant first, otherways it would cause the hostapd daemin to hang up (by receiving Signal 15 from wpa_supplicant)
sudo killall wpa_supplicant


#now finally start the new hotspot
sudo systemctl daemon-reload
## bug: hostapd service is not working, so as workaround I start hostapd manually
 sudo service hostapd start
#nohup sudo /usr/sbin/hostapd -d -t  -B /etc/hostapd/hostapd.conf -f /oobd/aplog
sudo service isc-dhcp-server start
