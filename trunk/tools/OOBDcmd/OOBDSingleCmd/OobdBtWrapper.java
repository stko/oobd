//package org.oobd.tools.oobdflash;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;
import java.io.*;
import javax.microedition.io.*;
import com.intel.bluetooth.RemoteDeviceHelper;


/**
*
* Minimal Services Search example.
*/
public class OobdBtWrapper {


    public static final Vector<OOBDDongleDescriptor> serviceFound = new Vector();
    public static DiscoveryListener listener;

    public static Vector<OOBDDongleDescriptor>  discover()  {

        // First run RemoteDeviceDiscovery and use discoved device
        System.out.print(".");
        RemoteDeviceDiscovery.discover();

        serviceFound.clear();

        UUID serviceUUID =  new UUID(0x1101);
        //UUID serviceUUID = "1e0ca4ea-299d-4335-93eb-27fcfe7fa848";


        final Object serviceSearchCompletedEvent = new Object();

        listener = new DiscoveryListener() {

            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                try {
                    RemoteDeviceHelper.authenticate(btDevice, "1234");
                } catch (IOException CantAuthenticate) {
                }
            }

            public void inquiryCompleted(int discType) {
            }

            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                for (int i = 0; i < servRecord.length; i++) {
                    String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    if (url == null) {
                        continue;
                    }
                    try {
                        serviceFound.add(new OOBDDongleDescriptor(servRecord[i].getHostDevice().getFriendlyName(false), servRecord[i].getHostDevice().getBluetoothAddress(), url));
                        System.out.print(".");

                    } catch ( IOException e ) {
                        System.err.print(e.toString());
                    }
                }
            }

            public void serviceSearchCompleted(int transID, int respCode) {
                System.out.print(".");
                synchronized(serviceSearchCompletedEvent) {
                    serviceSearchCompletedEvent.notifyAll();
                }
            }

        };

        UUID[] searchUuidSet = new UUID[] { serviceUUID };
        int[] attrIDs =  new int[] {
            //0x0100 // Service name
            0x0003 // Service name
        };
        System.out.print(".");
        for(Enumeration en = RemoteDeviceDiscovery.devicesDiscovered.elements(); en.hasMoreElements(); ) {
            RemoteDevice btDevice = (RemoteDevice)en.nextElement();
            System.out.print(".");
            synchronized(serviceSearchCompletedEvent) {
                try {
                    System.out.print(".");
                    LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
                    serviceSearchCompletedEvent.wait();
                } catch ( IOException ex ) {
                    ex.printStackTrace();
                } catch ( InterruptedException ex ) {
                    ex.printStackTrace();
                }
            }
        }
        return serviceFound;

    }
}

class RemoteDeviceDiscovery {

    public static final Vector<RemoteDevice> devicesDiscovered = new Vector();

    static Vector<RemoteDevice> discover()   {

        try {
            final Object inquiryCompletedEvent = new Object();

            devicesDiscovered.clear();

            DiscoveryListener listener = new DiscoveryListener() {

                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    System.out.print(".");

                    try {
                        String name= btDevice.getFriendlyName(false);
                        System.out.print(".");
                        if (name.matches("^OOBD.*")) {
                            devicesDiscovered.addElement(btDevice);
                        }
                    } catch (IOException cantGetDeviceName) {
                    }
                }

                public void inquiryCompleted(int discType) {
                    System.out.print(".");
                    synchronized(inquiryCompletedEvent) {
                        inquiryCompletedEvent.notifyAll();
                    }
                }

                public void serviceSearchCompleted(int transID, int respCode) {
                }

                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                }
            };

            synchronized(inquiryCompletedEvent) {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                if (started) {
                    System.out.print(".");
                    inquiryCompletedEvent.wait();
                    System.out.print(".");
                }
            }

        } catch ( IOException ex ) {
            ex.printStackTrace();
        } catch ( InterruptedException ex ) {
            ex.printStackTrace();
        }
        return devicesDiscovered;
    }

}


class OOBDDongleDescriptor {
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


