
import javax.microedition.io.StreamConnection;
import java.io.*;

/**
 *
 * @author Steffen
 */
public class TerminalIOStream {

    InputStream inStream;
    OutputStream outStream;
    InputStreamReader inStreamReader;
    OutputStreamWriter outStreamWriter;
    StreamConnection connection;
    boolean setUp = false;

    /** Creates a new instance of TerminalOutputStream */
    public TerminalIOStream(StreamConnection connection) {
        try {
            inStream = connection.openInputStream();
            inStreamReader = new InputStreamReader(inStream);
            outStream = connection.openOutputStream();
            outStreamWriter = new OutputStreamWriter(outStream, "iso-8859-1");
            setUp = true;
            this.connection = connection;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void write(char c) {
        if (setUp) {
            try {
                outStreamWriter.write(c);
                outStreamWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public synchronized void write(String s) {
        if (setUp) {
            try {
                outStreamWriter.write(s);
                outStreamWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (char) 0;

    }

    public synchronized boolean isEmpty() {
        try {
            return (!(setUp && inStream.available() == 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void close() {
        if (setUp) {
            try {
                inStreamReader.close();
                inStreamReader = null;
                inStream.close();
                inStream = null;
                outStreamWriter.close();
                outStreamWriter = null;
                outStream.close();
                outStream = null;

                connection.close();
                connection = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
