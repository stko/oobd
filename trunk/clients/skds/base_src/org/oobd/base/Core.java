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
import org.oobd.base.scriptengine.OobdScriptengine;

/**
 * The interface for nearly all interaction between the generic oobd maschine and the different environments
 * @author steffen
 */
public class Core implements Constants {

    IFui userInterface;
    IFsystem systemInterface;
    HashMap<String, OobdBus> busses; //stores all available busses
    HashMap<String, OobdConnector> connectors; //stores all available busses
    HashMap<String, OobdProtocol> protocols; //stores all available protocols
    HashMap<String, Class<?>> scriptengines; //stores all available scriptengines
    HashMap<String, OobdScriptengine> activeEngines; //stores all active scriptengines

    public Core(IFui myUserInterface, IFsystem mySystemInterface) {
        userInterface = myUserInterface;
        systemInterface = mySystemInterface;
        busses = new HashMap<String, OobdBus>();
        connectors = new HashMap<String, OobdConnector>();
        protocols = new HashMap<String, OobdProtocol>();
        scriptengines = new HashMap<String, Class<?>>();
        activeEngines = new HashMap<String, OobdScriptengine>();

        //userInterface.sm("Moin");
        Onion testOnion = Onion.generate();
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
        Debug.msg("core",DEBUG_BORING,testOnion.toString());
        Debug.msg("core",DEBUG_BORING,testOnion2.toString());
        systemInterface.registerOobdCore(this); //Anounce itself at the Systeminterface
        userInterface.registerOobdCore(this); //Anounce itself at the Userinterface


             File dir1 = new File (".");
     File dir2 = new File ("..");
     try {
       System.out.println ("Current dir : " + dir1.getCanonicalPath());
       System.out.println ("Parent  dir : " + dir2.getCanonicalPath());
       }
     catch(Exception e) {
       e.printStackTrace();
       }



        // ----------- load Busses -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses("../../org/oobd/ui/swing/build/classes/org/oobd/base/bus", "org.oobd.base.bus.", Class.forName("org.oobd.base.bus.OobdBus"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdBus thisClass = (OobdBus) value.newInstance();
                    thisClass.registerCore(this);
                    busses.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    Debug.msg("core",DEBUG_ERROR,ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Connectors -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses("../../org/oobd/ui/swing/build/classes/org/oobd/base/connector", "org.oobd.base.connector.", Class.forName("org.oobd.base.connector.OobdConnector"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdConnector thisClass = (OobdConnector) value.newInstance();
                    thisClass.registerCore(this);
                    connectors.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    Debug.msg("core",DEBUG_ERROR,ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Protocols -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses("../../org/oobd/ui/swing/build/classes/org/oobd/base/protocol", "org.oobd.base.protocol.", Class.forName("org.oobd.base.protocol.OobdProtocol"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdProtocol thisClass = (OobdProtocol) value.newInstance();
                    thisClass.registerCore(this);
                    protocols.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    Debug.msg("core",DEBUG_ERROR,ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Scriptengines AS CLASSES, NOT AS INSTANCES!-------------------------------
        try {
            scriptengines = loadOobdClasses("../../org/oobd/ui/swing/build/classes/org/oobd/base/scriptengine", "org.oobd.base.scriptengine.", Class.forName("org.oobd.base.scriptengine.OobdScriptengine"));
            for (Iterator iter = scriptengines.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = scriptengines.get(element);
                scriptengines.put(element, value);
                // now I need to be a little bit tricky to involve the static class method of an untypized class
                try {
                    Class[] parameterTypes = new Class[]{};
                    java.lang.reflect.Method method = value.getMethod("publicName", new Class[]{}); //no parameters
                    Object instance = null;
                    String result = (String) method.invoke(instance, new Object[]{}); // no parameters
                    if (!result.isEmpty()) {
                        announceScriptEngine(element, result);
                    }
                } catch (Exception e) {
                }
            }
        } catch (ClassNotFoundException e) {
        }
    }

    public void register(String msg) {
        userInterface.sm(msg);
    }

    /**
     * create ScriptEngine identified by its public Name. Returns a unique ID which is used from now on for all communication between the core and the UI
     * @param id public name of scriptengine to be created
     * @param classtype
     * @return unique id of this class, made out of its public name and counter. Needed to link UI canvas to this object
     *
     */
    public String createScriptEngine(String id) {
        Debug.msg("core",DEBUG_INFO,"Core should create scriptengine: " + id);
        Integer i = 1;
        while (activeEngines.containsKey(id + "." + i.toString())) {
            i++;
        }
        String seID = id + "." + i.toString();
        OobdScriptengine o = null;
        Class[] argsClass = new Class[2]; // first we set up an pseudo - args - array for the scriptengine- constructor
        argsClass[0] = seID.getClass(); // and fill it with the info, that the argument for the constructor will be first a String
        argsClass[1] = this.getClass(); // and fill it with the info, that the argument for the constructor will be first a String
        Class classRef = scriptengines.get(id); // then we get the class of the wanted scriptengine
        try {
            Constructor con = classRef.getConstructor(argsClass); // and let Java find the correct constructor with one string as parameter
            Object[] args = {seID, this}; //we will an args-array with our String parameter
            o = (OobdScriptengine) con.newInstance(args); // and finally create the object from the scriptengine class with its unique id as parameter
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeEngines.put(seID, o);
        return seID;
    }

    /**
     * create ScriptEngine identified by its public Name. Returns a unique ID which is used from now on for all communication between the core and the UI
     * @param id public name of scriptengine to be created
     * @param classtype
     * @return unique id of this class, made out of its public name and counter. Needed to link UI canvas to this object
     *
     */
    public void startScriptEngine(String id) {
        Debug.msg("core",DEBUG_BORING,"Start scriptengine: " + id);
        OobdScriptengine o = activeEngines.get(id);
        o.start();
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
        if (directory.exists()) {
            File[] files = directory.listFiles();
            URL sourceURL = null;
            try {
                // Den Pfad des Verzeichnisses auslesen
                sourceURL = directory.toURI().toURL();
            } catch (java.net.MalformedURLException ex) {
                Debug.msg("core",DEBUG_WARNING,ex.getMessage());
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
                        Debug.msg("core",DEBUG_ERROR,ex.getMessage());
                    }

                }
            }
        }
        // returns Hasmap filled with classes found
        return myInstances;
    }

    /**
     * tells the UserInterface about the existence of a scriptengine, e.g. to add this to a selection menu
     * @param id the key of the scriptengines hash array where the loaded instances are been stored
     * @param visibleName
     */
    void announceScriptEngine(String id, String visibleName) {
        userInterface.announceScriptengine(id, visibleName);
    }

    /**
     * main entry point for all actions required by the different components. 
     * Can be called either with a onion or with an json-String containing the onion data
     * @param json string representing the onion data
     */
    public void actionRequest(String jsonString) {
        try {
               Debug.msg("core",DEBUG_BORING,"required Action:" +jsonString);
             actionRequest(new Onion(jsonString));
        } catch (org.json.JSONException e) {
               Debug.msg("core",DEBUG_ERROR, "could not convert JSONstring \"" +jsonString+"\" into Onion");
        }
    }


    /**
     * main entry point for all actions required by the different components. 
     * Can be called either with a onion or with an json-String containing the onion data
     * @param json string representing the onion data
     */
    public void actionRequest(Onion myOnion) {
        try {
            Debug.msg("core",DEBUG_BORING,"type is:" +myOnion.getString("type"));
            if (doCompare(myOnion.getString("type"), ACTION)) {
                Debug.msg("Core",DEBUG_INFO,"action requested");
            }

        } catch (org.json.JSONException e) {
        }
    }


    /**
     * doCompare is just a developing help function to have a single point where all necessary if x equaly y tests
     * are been made to change this maybe later against something which is quicker as a time consuming string comparison
     * Can be called either with a onion or with an json-String containing the onion data
     * @param String 1
     * @param String 2
     * @return true, if Strings are equal
     */
    boolean doCompare(String s1, String s2) {
        return s1.matches(s2);
    }
}

