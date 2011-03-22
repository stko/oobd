
package org.oobd.base;

import java.util.HashMap;


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


}
