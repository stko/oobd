/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.io.*;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.support.Onion;

/**
 * 
 * @author steffen
 */
public interface OOBDPort {

    public PortInfo[] getPorts();

    public boolean connect(Onion options, OobdBus receiveListener);
    // Must be called when an unexpected IO exception happens to clear close the connection

    public void close();

   // public InputStream getInputStream();

   // public OutputStream getOutputStream();

//    public boolean available();

    public String connectInfo();
    
    public void attachShutDownHook();

    public void write(String s);
}
