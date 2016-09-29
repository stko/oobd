package org.oobd.core;

import org.oobd.core.support.Onion;

/**
 * \brief Interface for the Application to communicate with the OOBD core
 *
 *
 */
public interface IFsystem {


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
     * @param privateDir If set, uses system private directory
     * @param fileName the trailing filename to be addded
     * @return complete directory path
     */
    public String getSystemDefaultDirectory(boolean privateDir, String fileName);

    /**
     * \brief loads the settings from the system specific preference store
     *
     * @return last stored settings as JSON String
     */
    public String loadPreferences();

    /**
     * \brief saves the settings in the system specific preference store
     *
     * @param json Settings stored as JSON String
     * @return true, if successfull
     */
    public boolean savePreferences(String json);

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
     * @param Save: Acts as "Save" Dialog, otherways as "Open"
     * @return the path of the choosen file or null, if canceled
     */
    public String doFileSelector(String path, String extension, String message, boolean Save);
}
