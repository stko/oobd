package skdsswing.bus;

import java.io.*;
import java.util.Vector;
import gnu.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComReader implements Runnable {

    public String URL = null;
    private StringBuffer inBuffer = new StringBuffer();
    SerialPort serialPort;
    InputStream inStream;
    OutputStream outStream;
    InputStreamReader inStreamReader;
    OutputStreamWriter outStreamWriter;

    public ComReader() {
        new Thread(this).start();
    }

    public void connect(CommPortIdentifier portId) {
        try {
            serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
            inStream = serialPort.getInputStream();
            inStreamReader = new InputStreamReader(inStream);
            outStream = serialPort.getOutputStream();
            outStreamWriter = new OutputStreamWriter(outStream, "iso-8859-1");
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
                inStreamReader.close();
                inStreamReader = null;
                inStream.close();
                inStream = null;
                outStreamWriter.close();
                outStreamWriter = null;
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
        if (serialPort != null) {
            try {
                outStreamWriter.write(c);
                outStreamWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void write(String s) {
        if (serialPort != null) {
            try {
                System.out.println("Serial output:" + s);
                outStreamWriter.write(s);
                outStreamWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized char readChar() {
        int inChar = -1;
        if (serialPort != null) {
            try {
                if (inStream.available() > 0) {
                    inChar = inStreamReader.read();
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
            return (!(serialPort != null && inStream.available() == 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void run() {
        System.out.println("Thread has started");
        while (true) {

            int input;

            if (serialPort != null) {
                try {
                    input = inStreamReader.read();
                    if (input > 0) {
                        inBuffer.append((char) input);
                    }
                } catch (Exception e) {
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

    public int read() {
        if (serialPort != null) {
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
        System.out.println("Serial input:" + res);
        return res;
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






