package org.oobd.mobile;

import org.oobd.mobile.template.TerminalIOStream;
import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * @author axel
 */
public class BTSerial extends Form implements CommandListener, Runnable {

    public String deviceURL = null;
    private StreamConnection connection;
    private List deviceList = null;
    private String deviceName = null;
    private TerminalIOStream btConnection = null;
    private ConfigForm parent; //Where this form was started from
    private Command BackCommand = null;
    private StringBuffer inBuffer = new StringBuffer();
    private Display display;
    private Command backCommand;
    private Command okCommand;
    private MobileLogger log;
    private OOBD_MEv2 mainMidlet;
    private boolean friendlyOK;

    public BTSerial(OOBD_MEv2 mainMidlet) {
        super("Scanning BT...");
        log = mainMidlet.getLog();
        this.mainMidlet = mainMidlet;
        new Thread(this).start();
    }

    public String Connect(String thisURL) {
        if (thisURL != null) {
            try {
                deviceURL = thisURL;

                btConnection = new TerminalIOStream((StreamConnection) Connector.open("btspp://" + deviceURL + ":1", Connector.READ_WRITE), log);
                //Dialog.show("BT-Connect", "Success!", "OK", null);
                log.log(0, "Connected to MAC: " + deviceURL + " with the name: " + deviceName);
                return deviceURL;
            } catch (IOException ex) {
                log.log(3, ex.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    public void Closeconnection() {
        if (btConnection != null) {
            try {
                btConnection.close();
                btConnection = null;
                log.log(0, "BT connection closed");
            } catch (Exception ex) {
                log.log(3, "a: " + ex.toString());
            }
        }
    }

    public void getDeviceURL(ConfigForm parent, Display display) {
        this.parent = parent;
        this.display = display;

        try {
            BluetoothDeviceDiscovery.main();
        } catch (IOException ex) {
            log.log(3, ex.toString());
        }
        log.log(0, "This BT-DeviceURL: " + BluetoothDeviceDiscovery.localDevice.getBluetoothAddress());
//        if (!BluetoothDeviceDiscovery.localDevice.isPowerOn())
//        log.log("BT-Device Poweredon:"+LocalDevice.isPowerOn());
        int deviceCount = BluetoothDeviceDiscovery.vecDevices.size();
        log.log(0, "Bluetooth devicecount: " + deviceCount);
        System.out.println("Devicecount: " + Integer.toString(deviceCount));
        if (deviceCount > -1) {

            deviceList = new List("Please choose:", List.EXCLUSIVE);
            deviceList.setTicker(new Ticker("Adding Bluetooth devices to list..."));
            display.setCurrent(deviceList);
            deviceList.setCommandListener(this);

            if (deviceCount > 0) {
//                log.log("BT devicecout = "+ deviceCount);
                for (int i = 0; i < deviceCount; i++) {
                    RemoteDevice remoteDevice = (RemoteDevice) BluetoothDeviceDiscovery.vecDevices.elementAt(i);

                    friendlyOK = false;
                    try {
                        deviceList.append(remoteDevice.getFriendlyName(true), null);
                        friendlyOK = true;
                    } catch (IOException ex) {
                        log.log(0, "Friendly name not available" + ex.toString());
                        log.log(3, ex.getMessage());
                    }
                    if (!friendlyOK) {
                        deviceList.append(remoteDevice.getBluetoothAddress(), null);
                    }
                }

                //deviceList.addItem(new MyRenderer("test dummy"));
                backCommand = new Command("Back", Command.BACK, 0);
                okCommand = new Command("OK", Command.OK, 0);
                deviceList.addCommand(okCommand);
                deviceList.addCommand(backCommand);
                deviceList.setTicker(null);
                setCommandListener(this);
                display.setCurrent(deviceList);
            } else {
                Alert notFound = new Alert("Bluetoothconfig", "No Bluetooth devices found. ", null, AlertType.ERROR);
                notFound.setTimeout(Alert.FOREVER);
                display.setCurrent(notFound, parent);
            }
        } else {
            Alert notFound = new Alert("Bluetoothconfig", "Bluetooth not activated or not available!", null, AlertType.ERROR);
            notFound.setTimeout(Alert.FOREVER);
            display.setCurrent(notFound, parent);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCommand) {

//            System.out.println("Display:" + display.toString());
//            System.out.println("Parent:" + parent.toString());
            display.setCurrent(parent);
        }
        if (c == okCommand) {
            int index = deviceList.getSelectedIndex();
            RemoteDevice remoteDevice = (RemoteDevice) BluetoothDeviceDiscovery.vecDevices.elementAt(index);
            try {
                deviceName = remoteDevice.getFriendlyName(true);
            } catch (IOException ex) {
                log.log(3, ex.toString());
            }
            if (deviceName != null) {
                parent.setBTname(deviceName);
            } else {
                parent.setBTname(remoteDevice.getBluetoothAddress());
            }
            deviceURL = remoteDevice.getBluetoothAddress();

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
        } catch (Exception ex) {
            log.log(3, ex.toString());
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

                if (result > 0) { // condition meet
                    doLoop = false;
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



