/*
 * swing.java
 */
package org.oobd.ui.swing.desk;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.oobd.OOBD;


/**
 * The main class of the application.
 */
public class swing extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        swingView thisUserInterface = new swingView(this);
        show(thisUserInterface);
        try {
            new OOBD(new SwingSystem());
        } catch (OOBD.IllegalSettingsException ex) {
            Logger.getLogger(swing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     *
     * @return the instance of swing
     */
    public static swing getApplication() {
        return Application.getInstance(swing.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(swing.class, args);
    }
}
