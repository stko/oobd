package org.oobd.base.port;

public class PortInfo {
	String deviceName;
	String friendlyName;

	public PortInfo(String device, String name) {
		deviceName = device;
		friendlyName = name;
	}

	public String getDevice() {
		return deviceName;
	}

	public String toString() {
		return "(" + deviceName + ") " + friendlyName;
	}

}
