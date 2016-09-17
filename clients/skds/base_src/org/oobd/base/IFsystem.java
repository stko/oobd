package org.oobd.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.prefs.Preferences;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.support.Onion;
import org.oobd.base.port.OOBDPort;

/**
 * \brief Interface for the Application to communicate with the OOBD core
 *
 *
 */
public interface IFsystem {

    /**
     * \brief announces the OOBD core to the application \ingroup init
     *
     * @param core the core instance to register to the Application
     */
    public void registerOobdCore(Core core);
    
    
      /**
     * \brief sends a JSON data set to the openXC interface \ingroup
     * visualisation
     *
     * @param onion
     */
    public void openXCVehicleData(Onion onion);

    /**
     * \brief generates UI specific paths for standard files
     *
     * @param private If set, uses system private directory
     * @return complete  directory path
     */
    public String getSystemDefaultDirectory(boolean privateDir, String fileName);


  
    
    /**
     * \brief loads a Property file as the IO & Exeption handling for loading
     * propertys are not trivial, it's put into a helper function
     *
     * @param pathID Indentifier of what type of file to open, as this drives
     * where to search for
     * @param filename the wanted proberty file itself
     * @return an empty (if new) of filled property object
     */
    public Preferences loadPreferences(int pathID, String filename);

    /**
     * \brief saves a Property file as the IO & Exeption handling for saving
     * propertys are not trivial, it's put into a helper function
     *
     * @param pathID Indentifier of what type of file to open, as this drives
     * where to search for
     * @param filename the wanted proberty file itself
     * @param prop the proberty to save
     */
    public boolean savePreferences(int pathID, String filename, Preferences prop);


    /**
     * \brief supplies objects to bind to system specific hardware
     *
     * @param typ on
     * @return a object which connects to the system specific hardware or nil
     */
    public Object supplyHardwareHandle(Onion typ);

  


    /**
     * \brief generates and handles a Fileselector- Dialog
     *
     * @param path : where to start the search
     * @param extension : display filter in the file selector box
     * @param message : title of the box, id supported
     * @param save: Acts as "Save" Dialog, otherways as "Open"
     * @return the path of the choosen file or null, if canceled
     */
    public String doFileSelector(String path, String extension, String message, Boolean Save);
}

