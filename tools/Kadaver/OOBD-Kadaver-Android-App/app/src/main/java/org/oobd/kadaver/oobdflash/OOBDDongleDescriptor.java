package org.oobd.kadaver.oobdflash;
//package org.oobd.tools.oobdflash;



/**
*
* Minimal Services Search example.
*/
public class OOBDDongleDescriptor {
	public String friendlyName;
	public String BluetoothAddress;
	public String url;
	public String hardwareID;
	public String revision;
	public String design;
	public String layout;
	
	OOBDDongleDescriptor(String friendlyName, String BluetoothAddress, String url) {
		this.friendlyName = friendlyName;
		this.BluetoothAddress = BluetoothAddress;
		this.url = url;
	}
}

