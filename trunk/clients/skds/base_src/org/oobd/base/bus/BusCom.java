/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.bus;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.support.Onion;
import org.json.JSONException;

import java.io.IOException;
import java.util.*;


public class BusCom extends OobdBus implements OOBDConstants {

    static Enumeration portList;
    static boolean outputBufferEmptyFlag = false;
       ComReader reader = null;

    public BusCom() {
        super("Buscom");
    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
    }

    @Override
    public String getPluginName() {
        return "b:Com";
    }

    public void run() {

         reader = new ComReader();
        while (keepRunning == true) {
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            //System.out.println("Buscom Message abgeholt:" + on.toString());
            String command = on.getOnionString("command");
            if ("serWrite".equalsIgnoreCase(command)) {
                String data = Base64Coder.decodeString(on.getOnionString("data"));
                Logger.getLogger(BusCom.class.getName()).log(Level.INFO,
                        "busCom serWrite: >" + data + "<");
                reader.write(data);
            } else if ("serFlush".equalsIgnoreCase(command)) {
                Logger.getLogger(BusCom.class.getName()).log(Level.INFO,
                        "busCom serFlush ");
                reader.flush();

            } else if ("serWait".equalsIgnoreCase(command)) {
                try {
                    Integer result = reader.wait(Base64Coder.decodeString(on.getOnionString("data")), on.getInt("timeout"));
                    Logger.getLogger(BusCom.class.getName()).log(Level.INFO,
                            "busCom serWait: " + result);
                    replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS
                            + "'," + "'owner':" + "{'name':'" + getPluginName()
                            + "'}," + "'result':" + result + "," + "'replyID':"
                            + on.getInt("msgID") + "}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            } else if ("connect".equalsIgnoreCase(command)) {
                reader.close();
                Boolean result = reader.connect((OOBDPort) getCore().supplyHardwareHandle(on),on, this);
                try {
                    replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS
                            + "'," + "'owner':" + "{'name':'" + getPluginName()
                            + "'}," + "'result':" + result.toString() + ","
                            + "'replyID':" + on.getInt("msgID") + "}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
                            null, ex);
                }

            } else if ("close".equalsIgnoreCase(command)) {
                reader.close();

            } else if ("serReadLn".equalsIgnoreCase(command)) {
                try {
                    String result = reader.readln(on.getInt("timeout"),
                            on.optBoolean("ignore"));
                    Logger.getLogger(BusCom.class.getName()).log(Level.INFO,
                            "busCom readline: " + result);
                    replyMsg(msg, new Onion("" + "{'type':'" + CM_RES_BUS
                            + "'," + "'owner':" + "{'name':'" + getPluginName()
                            + "'}," + "'replyID':" + on.getInt("msgID") + ","
                            + "'result':'" + Base64Coder.encodeString(result)
                            + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
            }
        }
    }

    @Override
    public void receiveString(String msg) {
         if (reader != null){
            reader.receiveString(msg);
        }
    }
}
class ComReader implements Runnable {

    public String URL = null;
    private StringBuffer inBuffer = new StringBuffer();
    OOBDPort comHandle;

    public ComReader() {

        //new Thread(this).start();
    }

    public boolean connect(OOBDPort portHandle, Onion options, OobdBus bus) {
        if (comHandle != null) {
            comHandle.close();
            comHandle = null;
        }
        if (portHandle.connect(options, bus )) {
            comHandle = portHandle;
            return true;
        } else {
            comHandle = null;
            return false;
        }
    }

    public void close() {
        if (comHandle != null) {
            try {
                comHandle.close();
                comHandle = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

public synchronized void receiveString(String msg){
    inBuffer.append(msg);
}    
    
    public synchronized void write(char c) {
        if (comHandle != null && comHandle.getOutputStream() != null) {
            try {
                comHandle.getOutputStream().write(c);
            } catch (IOException ex) {
                Logger.getLogger(ComReader.class.getName()).log(Level.WARNING,
                        null, ex);
            }
        }
    }

    public synchronized void write(String s) {
        if (comHandle != null) {
                comHandle.write(s); 
                
        }else{
            System.err.println ("NO comhandle:"+s);
           
        }
    }


    /**
     * \todo this routine actual polls, but as e.g. shown in the purejavacomm-
     * Demo (), the reveice can also be done as an EventListener
     */
    public void run() {
        while (true) {

            int input;
            int n;

            try {
                if (false && comHandle != null && comHandle.getInputStream() != null) {
                    n = comHandle.getInputStream().available();
                    if (n > 0) {
                        byte[] buffer = new byte[n];

                        n = comHandle.getInputStream().read(buffer, 0, n);

                        for (int i = 0; i < n; ++i) {
                            inBuffer.append((char) buffer[i]);
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            // the VM doesn't want us to sleep anymore,
                            // so get back to work
                            Logger.getLogger(ComReader.class.getName()).log(
                                    Level.WARNING, null, ex);

                        }
                    }
                } else {
                    // as this thread runs in an unstopped endless loop, as
                    // long
                    // there's no serial port open, we need to slow him down
                    // here...
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // the VM doesn't want us to sleep anymore,
                        // so get back to work
                    }
                }

            } catch (Exception ex) {
                Logger.getLogger(ComReader.class.getName()).log(Level.WARNING,
                        "Unexpected error: Close down socket", ex);
                close();
            }
        }
    }

    public void flush() {
        if (comHandle != null && comHandle.getInputStream() != null) {
            while (read() > (char) 0)
				;
        }

    }

    public boolean isConnected() {
        return comHandle != null;
    }

    public synchronized int read() {
  //      if (comHandle != null && comHandle.getInputStream() != null) {
            if (inBuffer.length() > 0) {
                char c = inBuffer.charAt(0);
                inBuffer.deleteCharAt(0);
                return (int) c;
            } else {
                return -1;

            } /*
             * if (btConnection != null) { return btConnection.read(); }
             */
//        }
//        return -2; // -2 stands for "no connection"
    }

    public String readln(int timeout, boolean ignoreEmptyLines) {
        String res = "";
        boolean waitForever = timeout < 1;
        boolean doLoop = true;
        int c;
        long timeOutInMillis = System.currentTimeMillis() + timeout;
        int sleepTime = 2;
        while (doLoop && isConnected()) {
            c = read();
            if (c > 0) {
                // if (c != 10 && c != 13) {
                if (c > 31) {
                    res += (char) c;
                }
                if (c == 13) { // CR detected, condition meet - LF (0x10) is
                    // completely ignored
                    // res+=".";
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
                    if (timeOutInMillis <= System.currentTimeMillis()) {
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
        Logger.getLogger(ComReader.class.getName()).log(Level.INFO,
                "Serial input:" + res);
        return res;
    }

    public int wait(String conditions, int timeout) {
        boolean waitForever = timeout < 1;
        boolean doLoop = true;
        int c;
        int sleepTime = 5;
        int result = 0;
        long timeOutInMillis = System.currentTimeMillis() + timeout;
        Conditions con = new Conditions(conditions);
        while (doLoop && isConnected()) {
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
                    if (timeOutInMillis <= System.currentTimeMillis()) {
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
                    conVector.addElement(new SingleCondition(out.toLowerCase(),
                            index));
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
