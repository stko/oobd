/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

import org.oobd.base.support.Onion;
import java.lang.reflect.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.connector.OobdConnector;
import org.oobd.base.protocol.OobdProtocol;

/**
 * The interface for nearly all interaction between the generic oobd maschine and the different environments
 * @author steffen
 */
public class Core {

    IFui userInterface;
    IFsystem systemInterface;
    HashMap<String, OobdBus> busses; //stores all available busses
    HashMap<String, OobdConnector> connectors; //stores all available busses
    HashMap<String, OobdProtocol> protocols; //stores all available prorocols

    public Core(IFui myUserInterface, IFsystem mySystemInterface) {
        userInterface = myUserInterface;
        systemInterface = mySystemInterface;
        busses = new HashMap<String, OobdBus>();
        connectors = new HashMap<String, OobdConnector>();
        protocols = new HashMap<String, OobdProtocol>();
        //userInterface.sm("Moin");
        Onion testOnion = Onion.generate(null);
        testOnion.setValue("test", "moin");
        testOnion.setValue("test2", "moin2");
        testOnion.setValue("path/test3", "moin3");
        testOnion.setValue("path/test4", "moin4");
        testOnion.setValue("path/path2/test5", "moin5");
        Onion testOnion2 = null;
        try {
            testOnion2 = new Onion(testOnion.toString());
        } catch (org.json.JSONException e) {
        }
        System.out.println(testOnion.toString());
        System.out.println(testOnion2.toString());
        systemInterface.register(this); //Anounce itself at the Systeminterface
        systemInterface.loadConnectors();
        // ----------- load Busses -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses("/home/steffen/Desktop/workcopies/oobd/trunk/clients/skds/org/oobd/ui/swing/build/classes/org/oobd/base/bus", "org.oobd.base.bus.", Class.forName("org.oobd.base.bus.OobdBus"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdBus thisClass = (OobdBus) value.newInstance();
                    thisClass.registerCore(this);
                    busses.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    System.out.println(ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Connectors -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses("/home/steffen/Desktop/workcopies/oobd/trunk/clients/skds/org/oobd/ui/swing/build/classes/org/oobd/base/connector", "org.oobd.base.connector.", Class.forName("org.oobd.base.connector.OobdConnector"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdConnector thisClass = (OobdConnector) value.newInstance();
                    thisClass.registerCore(this);
                    connectors.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    System.out.println(ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Protocols -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses("/home/steffen/Desktop/workcopies/oobd/trunk/clients/skds/org/oobd/ui/swing/build/classes/org/oobd/base/protocol", "org.oobd.base.protocol.", Class.forName("org.oobd.base.protocol.OobdProtocol"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdProtocol thisClass = (OobdProtocol) value.newInstance();
                    thisClass.registerCore(this);
                    protocols.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    System.out.println(ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
    }

    public void register(String msg) {
        userInterface.sm(msg);
    }

  
    /**
     * loads different dynamic classes via an URLClassLoader.
     * Aa an URLClassloader is generic and can handle URLs, file systems and also jar files,
     * this loader is located in the core section of oobd. Just the information about the correct load path
     * is environment specific and needs to come from the systemInterface
     * @param path
     * @param classtype
     * @return
     * @todo loadOobdClasses supports actual only class files in a dir, but not in a jar file. The jar system from "Dynamisches_laden_von_Klassen_example.txt" needs to be implemented also
     * @todo the classloader needs to be extended to support encrypted (=licenced) class files
     * @bug what do do that the procedure also support relative filepaths?
     *
     */
    public HashMap loadOobdClasses(
            String path, String classPrefix, Class classType) {
        // abgekuckt unter http://de.wikibooks.org/wiki/Java_Standard:_Class
        HashMap<String, Class<?>> myInstances = new HashMap<String, Class<?>>();
        File directory = new File(path);
        System.out.println(directory.exists());
        if (directory.exists()) {
            File[] files = directory.listFiles();
            URL sourceURL = null;
            try {
                // Den Pfad des Verzeichnisses auslesen
                sourceURL = directory.toURI().toURL();
            } catch (java.net.MalformedURLException ex) {
                System.out.println(ex.getMessage());
            }
            // Einen URLClassLoader für das Verzeichnis instanzieren
            URLClassLoader loader = new URLClassLoader(new java.net.URL[]{sourceURL}, Thread.currentThread().getContextClassLoader());
            // Für jeden File im Verzeichnis...
            for (int i = 0; i <
                    files.length; i++) {
                // Splittet jeden Dateinamen in Bezeichnung und Endung
                // siehe "regular expression" und String.split()
                String name[] = files[i].getName().split("\\.");
                // Nur Class-Dateien ohne "$" werden berücksichtigt
                if (name.length > 1 && name[1].equals("class") && name[1].indexOf("$") == -1) {
                    try {
                        // Die Klasse laden
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
                        System.out.println(ex.getMessage());
                    }
                }
            }
        }
        // returns Hasmap filled with classes found
        return myInstances;


    }
}

