package org.oobd.mobile;


import org.oobd.mobile.template.TerminalIOStream;
import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * @author Steffen Köhler
 */
public class BTSerial extends Form implements CommandListener, Runnable {

    private StreamConnection connection;
    private List deviceList = null;
    public String URL = null;
    private TerminalIOStream btConnection = null;
    private ConfigForm parent; //Where this form was started from
    private Command BackCommand = null;
    private StringBuffer inBuffer = new StringBuffer();
    Display display;
    Command backCommand;
    Command okCommand;


    public BTSerial() {
        super("Scanning BT...");
        new Thread(this).start();
    }

    public String Connect(String thisURL) {
        if (thisURL != null) {
            try {
                System.out.println("BT-URL: " + URL);
                btConnection = new TerminalIOStream((StreamConnection) Connector.open("btspp://" + URL + ":1", Connector.READ_WRITE));
                //Dialog.show("BT-Connect", "Success!", "OK", null);
                return URL;
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public void Closeconnection() {
        if (btConnection != null) {
            btConnection.close();
            btConnection = null;
        }
    }

    public void getDeviceURL(ConfigForm parent, Display display) {
        this.parent = parent;
        this.display = display;
        deviceList = new List("Available Bluetooth devices", List.EXCLUSIVE);
        deviceList.setCommandListener(this);
        try {
            BluetoothDeviceDiscovery.main();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        int deviceCount = BluetoothDeviceDiscovery.vecDevices.size();
        System.out.println("Devicecount: " + Integer.toString(deviceCount));
        if (deviceCount > -1) {
            for (int i = 0; i < deviceCount; i++) {
                RemoteDevice remoteDevice = (RemoteDevice) BluetoothDeviceDiscovery.vecDevices.elementAt(i);
                deviceList.append(remoteDevice.getBluetoothAddress(),null);
                try {
                    System.out.println("Found device: " + remoteDevice.getFriendlyName(true));
                    /**
                     * for later extensions:
                     * the user friendly name of the device can be found as
                     *
                     * remoteDevice.getFriendlyName(true)
                     */
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
            //deviceList.addItem(new MyRenderer("test dummy"));
            backCommand = new Command("Back", Command.BACK, 0);
            okCommand = new Command("Select", Command.OK, 0);
            deviceList.addCommand(okCommand);
            deviceList.addCommand(backCommand);
            setCommandListener(this);            
            display.setCurrent(deviceList);           
            //new Thread(this).start();
            } else {
            Alert notFound = new Alert("Bluetoothconfig", "No Bluetooth device found!", null, AlertType.ERROR);
            display.setCurrent(notFound);            
        }
    }

    public void commandAction(Command c, Displayable d) {
        if(c==backCommand){

//            System.out.println("Display:" + display.toString());
//            System.out.println("Parent:" + parent.toString());
            display.setCurrent(parent);
        }
        if(c==okCommand){
            int index = deviceList.getSelectedIndex();
            RemoteDevice remoteDevice = (RemoteDevice) BluetoothDeviceDiscovery.vecDevices.elementAt(index);
            parent.setBTname(remoteDevice.getBluetoothAddress());
            URL=remoteDevice.getBluetoothAddress();
            display.setCurrent(parent);
        }
    }


    public void run() {
        System.out.println("Thread has started");
        try {
            while (true) {

                int input;

                if (btConnection != null) {
                    input = btConnection.inStreamReader.read();
                    if (input > 0) {
                        inBuffer.append((char) input);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public void flush() {
        if (btConnection != null) {
            while (read() > (char) 0);
        }

    }

    public boolean isConnected() {
        return btConnection != null;
    }

    public int read() {
        if (btConnection != null) {
            if (inBuffer.length() > 0) {
                char c = inBuffer.charAt(0);
                inBuffer.deleteCharAt(0);
                return (int) c;
            } else {
                return -1;
            }
            /*       if (btConnection != null) {
            return btConnection.read();
            }
             */
        }
        return -2; // -2 stands for "no connection"
    }

    public String readln(int timeout, boolean ignoreEmptyLines) {
        String res = "";
        boolean waitForever = timeout < 1;
        boolean doLoop = true;
        int c;
        int sleepTime = 50;
        while (doLoop) {
            c = read();
            if (c > 0) {
                //if (c != 10 && c != 13) {
                if (c > 31) {
                    res += (char) c;
                }
                if (c == 13) { // CR detected, condition meet
                    //res+=".";
                    doLoop = res.equals("") && ignoreEmptyLines;
                }
            } else {
                if (waitForever) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // the VM doesn't want us to sleep anymore,
                        // so get back to work
                        }

                } else {
                    timeout -= sleepTime;
                    if (timeout <= 0) {
                        doLoop = false;
                    } else {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            // the VM doesn't want us to sleep anymore,
                            // so get back to work
                            }
                    }
                }
            }
        }
        return res;
    }

    public void write(char c) {
        if (btConnection != null) {
            btConnection.write(c);
        }
    }

    public void write(String s) {
        if (btConnection != null) {
            btConnection.write(s);
        }
    }

    public int wait(String conditions, int timeout) {
        boolean waitForever = timeout < 1;
        boolean doLoop = true;
        int c;
        int sleepTime = 50;
        int result = 0;
        Conditions con = new Conditions(conditions);
        while (doLoop) {
            c = read();
            if (c > -1) {
                result = con.checkConditions((char) c);
            }
            if (result > 0) { // condition meet
                doLoop = false;
            } else {
                if (waitForever) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // the VM doesn't want us to sleep anymore,
                        // so get back to work
                        }

                } else {
                    timeout -= sleepTime;
                    if (timeout <= 0) {
                        doLoop = false;
                    } else {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            // the VM doesn't want us to sleep anymore,
                            // so get back to work
                            }
                    }
                }
            }
        }
        return result;
    }

}

class Conditions {

    Vector conVector = new Vector();

    class SingleCondition {

        String text;
        int index = 0, pos = 0;

        SingleCondition(String text, int index) {
            this.text = text;
            this.index = index;
        }

        int check(char c) {
            if (text.charAt(pos) == Character.toLowerCase(c)) {
                pos++;
                if (pos == text.length()) {
                    return index;
                } else {
                    return 0;
                }
            } else {
                // start again
                pos = 0;
                return 0;
            }
        }
    }

    Conditions(String conString) {
        int p = 0;
        int index = 1;
        String out = "";
        if (conString.charAt(conString.length() - 1) != '|') {
            conString += '|';
        }
        while (p < conString.length()) {
            if (conString.charAt(p) != '|') {
                out += conString.charAt(p);
            } else {
                if (out.length() > 0) {
                    conVector.addElement(new SingleCondition(out.toLowerCase(), index));
                    out = "";
                }
                index++;
            }
            p++;
        }
    }

    int checkConditions(char c) {
        int v = 0;
        int result = 0;
        while (v < conVector.size() && result == 0) {
            result = ((SingleCondition) conVector.elementAt(v)).check(c);
            v++;
        }
        return result;
    }
}



