package skdsswing.bus;

import java.io.*;
import java.util.Vector;
//import javax.comm.*;// for SUN's serial/parallel port libraries
//import gnu.io.*;
import purejavacomm.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ComReader implements Runnable {

    public String URL = null;
    private StringBuffer inBuffer = new StringBuffer();
    SerialPort serialPort = null;
    InputStream inStream = null;
    OutputStream outStream = null;
    // InputStreamReader inStreamReader;
    // OutputStreamWriter outStreamWriter;

    public ComReader() {
        new Thread(this).start();
    }

    public void connect(CommPortIdentifier portId) throws IOException {
        try {
            serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);

            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            serialPort.enableReceiveTimeout(5);

            inStream = serialPort.getInputStream();
            // inStreamReader = new InputStreamReader(inStream);
            outStream = serialPort.getOutputStream();
            //outStreamWriter = new OutputStreamWriter(outStream, "iso-8859-1");
            serialPort.enableReceiveTimeout(5);
        } catch (UnsupportedCommOperationException ex) {
            throw new IOException("Unsupported serial port parameter");

        } catch (PortInUseException ex) {
            Logger.getLogger(ComReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void Closeconnection() {
        if (serialPort != null) {
            close();
            serialPort = null;
        }
    }

    public void close() {
        if (serialPort != null) {
            try {
//                inStreamReader.close();
//                inStreamReader = null;
                inStream.close();
                inStream = null;
                //               outStreamWriter.close();
                //               outStreamWriter = null;
                outStream.close();
                outStream = null;

                serialPort.close();
                serialPort = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void write(char c) {
        if (outStream != null) {
            try {
                //outStreamWriter.write(c);
                outStream.write(c);
                outStream.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void write(String s) {
        if (outStream != null) {
            try {
                Logger.getLogger(ComReader.class.getName()).log(Level.INFO, "Serial output:" + s);
                outStream.write(s.getBytes(), 0, s.length());
                outStream.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized char readChar() {
        int inChar = -1;
        if (inStream != null) {
            try {
                if (inStream.available() > 0) {
                    inChar = inStream.read();
                    return (char) inChar;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (char) 0;

    }

    public synchronized boolean isEmpty() {
        try {
            return (!(inStream != null && inStream.available() == 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * \todo this routine actual polls, but as e.g. shown in the purejavacomm- Demo (),
     * the reveice can also be done as an EventListener
     */
    public void run() {
        while (true) {

            int input;
            int n;

            if (serialPort != null) {
                try {
                    if (inStream != null) {
                        n = inStream.available();
                        if (n > 0) {
                           byte[] buffer = new byte[n];
                            n = inStream.read(buffer, 0, n);
                            for (int i = 0; i < n; ++i) {
                                 inBuffer.append((char) buffer[i]);
                            }
                        } else {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                // the VM doesn't want us to sleep anymore,
                                // so get back to work
                                e.printStackTrace();

                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // as this thread runs in an unstopped endless loop, as long there's no serial port open, we need to slow him down here...
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // the VM doesn't want us to sleep anymore,
                    // so get back to work
                }
            }
        }
    }

    public void flush() {
        if (serialPort != null) {
            while (read() > (char) 0);
        }

    }

    public boolean isConnected() {
        return serialPort != null;
    }

    public synchronized int read() {
        if (serialPort != null) {
            if (inBuffer.length() > 0) {
                char c = inBuffer.charAt(0);
                inBuffer.deleteCharAt(0);
                return (int) c;
            } else {
                return -1;


            } /*       if (btConnection != null) {
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
                if (c == 10) { // LF detected, condition meet
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
        Logger.getLogger(ComReader.class.getName()).log(Level.INFO, "Serial input:" + res);
        return res;
    }

    public int wait(String conditions, int timeout) {
        boolean waitForever = timeout < 1;
        boolean doLoop = true;
        int c;
        int sleepTime = 5;
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






