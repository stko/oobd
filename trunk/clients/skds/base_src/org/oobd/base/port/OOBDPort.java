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
    


    public boolean connect(Onion options);
    public boolean close();
    public InputStream getInputStream();
    public OutputStream getOutputStream();
    public boolean available();
}
