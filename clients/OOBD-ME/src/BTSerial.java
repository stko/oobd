
import com.sun.lwuit.Form;
import com.sun.lwuit.List;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Label;
import com.sun.lwuit.Button;
import com.sun.lwuit.events.*;
import com.sun.lwuit.list.*;
import com.sun.lwuit.*;
import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.Vector;

/**
 * @author Steffen Köhler
 */
public class BTSerial extends Form implements ActionListener, Runnable {

    private StreamConnection connection;
    private List deviceList = null;
    public String URL = null;
    private TerminalIOStream btConnection = null;
    private Form parent; //Where this form was started from
    private Command BackCommand = null;
    private StringBuffer inBuffer = new StringBuffer();

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

    public String getDeviceURL(final Form parent) {
        this.parent = parent;
        this.show();

        deviceList = new List();
        deviceList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                System.out.println("Listhandler");
                URL = deviceList.getSelectedItem().toString();
                parent.showBack();
            }
        });
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
                deviceList.addItem(new MyRenderer(remoteDevice.getBluetoothAddress()));
                /**
                 * for later extensions:
                 * the user friendly name of the device can be found as
                 *
                 * remoteDevice.getFriendlyName(true)
                 */
            }
            //deviceList.addItem(new MyRenderer("test dummy"));
            this.addComponent(deviceList);
            this.addCommand(BackCommand = new Command("Back"));
            addCommandListener(this);
            return URL;
            //new Thread(this).start();
            } else {
            Dialog.show("BT Scan", "Sorry, no Device found", "Back", null);
            parent.show();
            return null;
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

    public void actionPerformed(ActionEvent ae) {

        Command command = ae.getCommand();
        if (command == BackCommand) {
            URL = null;
            parent.show();
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

class MyRenderer extends Label implements ListCellRenderer {

    private Label focus = new Label("");

    MyRenderer(String text) {
        setText(text);
        focus.getStyle().setBgTransparency(100);
    }

    public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
        setText(value.toString());
        if (isSelected) {
            setFocus(true);
            getStyle().setBgTransparency(100);
            getStyle().setBgColor(255, true);
        } else {
            setFocus(false);
            getStyle().setBgTransparency(0);
        }
        return this;
    }

    public String toString() {
        return getText();
    }

    public Component getListFocusComponent(List list) {
        return focus;
    }
}



