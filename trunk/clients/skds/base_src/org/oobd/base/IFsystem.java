
package org.oobd.base;


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
}
