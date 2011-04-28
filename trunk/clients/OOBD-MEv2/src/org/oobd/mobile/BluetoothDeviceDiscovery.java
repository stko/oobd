package org.oobd.mobile;

import java.io.IOException;
import java.util.Vector;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
/**
 * Class that discovers all bluetooth devices in the neighbourhood
 * and displays their name and bluetooth address.
 */
public class BluetoothDeviceDiscovery implements DiscoveryListener{
	//object used for waiting
        public static LocalDevice localDevice;

	public static Object lock=new Object();
	//vector containing the devices discovered
	public static Vector vecDevices=new Vector();
	//main method of the application
	public static void main() throws IOException {
		//create an instance of this class
		BluetoothDeviceDiscovery bluetoothDeviceDiscovery=new BluetoothDeviceDiscovery();
		//display local device address and name
		localDevice = LocalDevice.getLocalDevice();
		//System.out.println("Address: "+localDevice.getBluetoothAddress());
	//	System.out.println("Name: "+localDevice.getFriendlyName());
		//find devices
		DiscoveryAgent agent = localDevice.getDiscoveryAgent();
	//	System.out.println("Starting device inquiry...");
		agent.startInquiry(DiscoveryAgent.GIAC, bluetoothDeviceDiscovery);

		try {
			synchronized(lock){
				lock.wait();
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}//end main
	//methods of DiscoveryListener
	/**
	 * This call back method will be called for each discovered bluetooth devices.
	 */
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		//System.out.println("Device discovered: "+btDevice.getBluetoothAddress());
		//add the device to the vector
		if(!vecDevices.contains(btDevice)){
			vecDevices.addElement(btDevice);
         //  sterm.joro.append(btDevice.getBluetoothAddress(), null);

		}
	}
	//no need to implement this method since services are not being discovered
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
	}
	//no need to implement this method since services are not being discovered
	public void serviceSearchCompleted(int transID, int respCode) {
	}
	/**
	 * This callback method will be called when the device discovery is
	 * completed.
	 */

	public void inquiryCompleted(int discType) {
		synchronized(lock){
			lock.notify();
		}
		//switch (discType) {
		//	case DiscoveryListener.INQUIRY_COMPLETED :
		//		System.out.println("INQUIRY_COMPLETED");

		//		break;
		//	case DiscoveryListener.INQUIRY_TERMINATED :
		//		System.out.println("INQUIRY_TERMINATED");
		//		break;
		//	case DiscoveryListener.INQUIRY_ERROR :
		//		System.out.println("INQUIRY_ERROR");
		//		break;
		//	default :
		//		System.out.println("Unknown Response Code");
		//		break;
		//}
	}//end method
}//end class
