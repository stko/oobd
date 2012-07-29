/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skdsswing;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.HashMap;
import org.oobd.base.*;
import org.oobd.base.OOBDConstants;
import java.net.URL;
import java.net.URLClassLoader;
import org.oobd.base.port.ComPort_Win;

//import java.io.FileInputStream;
import java.io.*;
import org.oobd.base.support.Onion;
import org.oobd.base.port.ComPort_Unix;

/**
 * This class is the connection between the generic oobd system and the enviroment for e.g. IO operations
 * @author steffen
 */
public class SwingSystem implements IFsystem {

    Core core;

    public void registerOobdCore(Core thisCore) {
        core = thisCore;
    }

    public HashMap loadOobdClasses(String path, String classPrefix, Class<?> classType) {
        HashMap<String, Class<?>> myInstances = new HashMap<String, Class<?>>();
        // TODO copy this code section to Swing / ME IFSystem implementation

        File directory = new File(path);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            URL sourceURL = null;
            try {
                // read the path of the directory
                sourceURL = directory.toURI().toURL();
            } catch (java.net.MalformedURLException ex) {
                Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "", ex.getMessage());
            }
            // generate URLClassLoader for that directory

            URLClassLoader loader = new URLClassLoader(new java.net.URL[]{sourceURL}, Thread.currentThread().getContextClassLoader());
            // For each file in dir...
            for (int i = 0; i
                    < files.length; i++) {
                // split file name into name and extension
                Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "File to be load as class: " + files[i].getName());
                String name[] = files[i].getName().split("\\.");
                // only class names without $ are taken
                if (name.length > 1 && name[1].equals("class") && name[1].indexOf("$") == -1) {
                    try {
                        // load the class itself
                        Class<?> source = loader.loadClass(classPrefix + name[0]);
                        // Prüfen, ob die geladene Klasse das Interface implementiert
                        // bzw. ob sie das Interface beerbt
                        // Das Interface darf dabei natürlich nicht im selben Verzeichnis liegen
                        // oder man muss prüfen, ob es sich um ein Interface handelt Class.isInterface()
                        if (classType.isAssignableFrom(source)) {
                            // save unitialized class object in hashmap
                            myInstances.put(name[0], source);
                        }

                    } catch (ClassNotFoundException ex) {
                        // Wird geworfen, wenn die Klasse nicht gefunden wurde
                        Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "couldn't load class", ex);
                        ex.printStackTrace();
                    }

                }
            }
        }// if
        else {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "Directory " + directory.getName() + " does not exist. Class " + classPrefix + " could not be loaded.");
        }
        // returns Hashmap filled with classes found

        return myInstances;

    }

    public String generateUIFilePath(int pathID, String fileName) {

                return fileName;

     }

    public InputStream generateResourceStream(int pathID, String ResourceName) throws java.util.MissingResourceException {
        try {
            return new FileInputStream(ResourceName);
        } catch (FileNotFoundException ex) {
            throw new java.util.MissingResourceException("Resource not found:" + ResourceName, "SwingSystem", ResourceName);
        }
    }

    public Object supplyHardwareHandle(Onion typ) {
        String osname = System.getProperty("os.name", "").toLowerCase();
        Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "OS detected: " + osname);
        try {
            if (osname.startsWith("windows")) {
                return new ComPort_Win();
            } else {
                return new ComPort_Unix();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }
}
