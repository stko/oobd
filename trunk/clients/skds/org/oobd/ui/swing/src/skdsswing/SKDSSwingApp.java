/*
 * SKDSSwingApp.java
 */

package skdsswing;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.oobd.base.Core;
/**
 * The main class of the application.
 */
public class SKDSSwingApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        SKDSSwingView thisUserInterface=new SKDSSwingView(this);
        show(thisUserInterface);
         Core thiscore=new Core(thisUserInterface, new SwingSystem());
   }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of SKDSSwingApp
     */
    public static SKDSSwingApp getApplication() {
        return Application.getInstance(SKDSSwingApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(SKDSSwingApp.class, args);
    }
}
