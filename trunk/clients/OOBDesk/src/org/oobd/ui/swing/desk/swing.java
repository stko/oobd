/*
 * swing.java
 */
package org.oobd.ui.swing.desk;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.oobd.base.Core;
import org.oobd.ui.swing.support.SwingSystem;

/**
 * The main class of the application.
 */
public class swing extends SingleFrameApplication {

    Core oobdCore;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        swingView thisUserInterface = new swingView(this);
        show(thisUserInterface);
       oobdCore = new Core(thisUserInterface, new SwingSystem(), "Core");
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
