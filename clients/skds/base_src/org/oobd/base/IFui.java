package org.oobd.base;

import org.oobd.base.support.Onion;

/**
 * \brief Interface for the application object who does the graphical User
 * Interface to communicate with the OOBD core
 */
public interface IFui {



 



    /**
     * \brief sends a JSON data set to the openXC interface \ingroup
     * visualisation
     *
     * @param onion
     */
    public void openXCVehicleData(Onion onion);
}
