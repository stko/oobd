
package org.oobd.base;

import java.util.HashMap;
import java.util.MissingResourceException;
import java.io.InputStream;
import org.oobd.base.support.Onion;



/**
 * \brief Interface for the Application to communicate with the OOBD core
 *
 *
 */
public interface IFsystem {

   /**
    * \brief announces the OOBD core to the application
    * \ingroup init
    *
    * @param core the core instance to register to the Application
    */
    public void registerOobdCore(Core core);
    
   /**
    * \brief Load classes for bus, engine, etc...
    *
    * @param path directory to seach in
    * @param classtype reference class for what to search for
    * @return Hashmap of classes with the class names as key
    */
    public HashMap loadOobdClasses(String path, String classPrefix, Class<?> classType);

   /**
    * \brief generates UI specific paths for standard files
    *
    * @param pathID Indentifier of what directory to generate
    * @param filename  the file itself
    * @return complete Path for the wanted filename
    */
    public String generateUIFilePath(int pathID, String filename);

   /**
    * \brief supplies a resource as Inputstream
    *
    * @param pathID Indentifier of what type of file to open, as this drives where to search for
    * @param ResourceName Name of the wanted resource
    * @return InputStream for that resource
    */
    public InputStream generateResourceStream(int pathID, String ResourceName) throws MissingResourceException;

    
    /**
     * \brief supplies objects to bind to system specific hardware
     * 
     * @param typ on
     * @return a object which connects to the system specific hardware or nil
     */
    public Object supplyHardwareHandle(Onion typ);

}
