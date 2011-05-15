package org.oobd.mobile.template;


import javax.microedition.io.StreamConnection;
import java.io.*;
import org.oobd.mobile.MobileLogger;

/**
 *
 * @author Steffen
 */
public class TerminalIOStream {

    InputStream inStream;
    OutputStream outStream;
    public InputStreamReader inStreamReader;
    OutputStreamWriter outStreamWriter;
    StreamConnection connection;
    boolean setUp = false;
    private MobileLogger log;

    /** Creates a new instance of TerminalOutputStream */
    public TerminalIOStream(StreamConnection connection, MobileLogger log) {
        this.log = log;
        try {
            inStream = connection.openInputStream();
            inStreamReader = new InputStreamReader(inStream);
            outStream = connection.openOutputStream();
            outStreamWriter = new OutputStreamWriter(outStream, "iso-8859-1");
            setUp = true;
            this.connection = connection;
        } catch (IOException ex) {
            log.log(ex.toString());
        }
    }

    public synchronized void write(char c) {
        if (setUp) {
            try {
                outStreamWriter.write(c);
                outStreamWriter.flush();
            } catch (IOException ex) {
                log.log(ex.toString());
            }
        }
    }

    public synchronized void write(String s) {
        if (setUp) {
            try {
                outStreamWriter.write(s);
                outStreamWriter.flush();
            } catch (IOException ex) {
                log.log(ex.toString());
            }
        }
    }

    public synchronized char read() {
        int inChar = -1;
        if (setUp) {
            try {
                if (inStream.available() > 0) {
                    inChar = inStreamReader.read();
                    return (char) inChar;
                }
            } catch (Exception ex) {
                log.log(ex.toString());
            }
        }
        return (char) 0;

    }

    public synchronized boolean isEmpty() {
        try {
            return (!(setUp && inStream.available() == 0));
        } catch (Exception ex) {
            log.log(ex.toString());
        }
        return true;
    }

    public void close() {
        if (setUp) {
            try {
                log.log("Trying to close inStreamReader");
                inStreamReader.close();
                inStreamReader = null;
                log.log("inStreamReader closed");
                
                inStream.close();
                inStream = null;
                log.log("inStream closed");
                
                outStreamWriter.close();
                outStreamWriter = null;
                log.log("outStreamWriter closed");
                
                outStream.close();
                outStream = null;
                log.log("outStream closed");                

                connection.close();
                connection = null;
            } catch (Exception ex) {
                log.log("b: "+ex.toString());
            }
        } else {
            log.log("Variable 'setup' in TerminalIOStream not set");
        }
    }
}
