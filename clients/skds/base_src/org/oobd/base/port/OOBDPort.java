/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.port;

import java.io.*;
import org.oobd.base.support.Onion;

/**
 * 
 * @author steffen
 */
public interface OOBDPort {

	public  PortInfo[] getPorts();
	
	public boolean connect(Onion options);
	// Must be called when an unexpected IO exception happens to clear close the connection
	public OOBDPort resetConnection();

	public OOBDPort close();

	public InputStream getInputStream();

	public OutputStream getOutputStream();

	public boolean available();
        
           public void attachShutDownHook();
        
}
