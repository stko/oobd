/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.Collection;
import java.util.Properties;
import org.oobd.base.support.Onion;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.connector.OobdConnector;
import org.oobd.base.protocol.OobdProtocol;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.visualizer.Visualizer;

/**
 * The interface for nearly all interaction between the generic oobd maschine and the different environments
 * @author steffen
 */
public class Core extends OobdPlugin implements OOBDConstants, CoreTickListener {

    IFui userInterface;
    IFsystem systemInterface;
    HashMap<String, OobdBus> busses; //stores all available busses
    HashMap<String, OobdConnector> connectors; //stores all available busses
    HashMap<String, OobdProtocol> protocols; //stores all available protocols
    HashMap<String, Class<?>> scriptengines; //stores all available scriptengines
    HashMap<String, OobdScriptengine> activeEngines; //stores all active scriptengines
    HashMap<String, Object> assignments; //stores all active assignments
    HashMap<String, ArrayList<Visualizer>> visualizers;
    static Core thisInstance; //Class variable points to only instance
    CoreTick ticker;
    Properties props;

    public Core(IFui myUserInterface, IFsystem mySystemInterface) {
        thisInstance = this;
        userInterface = myUserInterface;
        systemInterface = mySystemInterface;
        busses = new HashMap<String, OobdBus>();
        connectors = new HashMap<String, OobdConnector>();
        protocols = new HashMap<String, OobdProtocol>();
        scriptengines = new HashMap<String, Class<?>>();
        activeEngines = new HashMap<String, OobdScriptengine>();
        assignments = new HashMap<String, Object>();
        visualizers = new HashMap<String, ArrayList<Visualizer>>();

        //userInterface.sm("Moin");
        props = new Properties();
        try {
            props.load(new FileInputStream(OOBDConstants.CorePrefsFileName));
        } catch (IOException ignored) {
        }

        Onion testOnion = Onion.generate();
        testOnion.setValue("test", "moin");
        testOnion.setValue("test2", "moin2");
        testOnion.setValue("path/test3", "moin3");
        testOnion.setValue("path/test4", "moin4");
        testOnion.setValue("path/path2/test5", "moin5");

        try {
            Debug.msg("core", DEBUG_BORING, testOnion.getOnionObject("test").toString());
            Debug.msg("core", DEBUG_BORING, testOnion.getOnionObject("test2").toString());
            Debug.msg("core", DEBUG_BORING, testOnion.getOnionObject("path/test3").toString());
            Debug.msg("core", DEBUG_BORING, testOnion.getOnionObject("path/path2/test5").toString());
        } catch (OnionNoEntryException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        Onion testOnion2 = null;
        try {
            testOnion2 = new Onion(testOnion.toString());
        } catch (org.json.JSONException e) {
        }
        Debug.msg("core", DEBUG_BORING, testOnion.toString());
        Debug.msg("core", DEBUG_BORING, testOnion2.toString());
        systemInterface.registerOobdCore(this); //Anounce itself at the Systeminterface
        userInterface.registerOobdCore(this); //Anounce itself at the Userinterface


        File dir1 = new File(".");
        File dir2 = new File("..");
        try {
            System.out.println("Current dir : " + dir1.getCanonicalPath());
            System.out.println("Parent  dir : " + dir2.getCanonicalPath());
        } catch (Exception e) {
            e.printStackTrace();
        }



        // ----------- load Busses -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses(props.getProperty("BusClassPath", "../../org/oobd/ui/swing/build/classes/org/oobd/base/bus"), "org.oobd.base.bus.", Class.forName("org.oobd.base.bus.OobdBus"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdBus thisClass = (OobdBus) value.newInstance();
                    thisClass.registerCore(this);
                    busses.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    Debug.msg("core", DEBUG_ERROR, ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Connectors -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses(props.getProperty("ConnectorClassPath", "../../org/oobd/ui/swing/build/classes/org/oobd/base/connector"), "org.oobd.base.connector.", Class.forName("org.oobd.base.connector.OobdConnector"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdConnector thisClass = (OobdConnector) value.newInstance();
                    thisClass.registerCore(this);
                    connectors.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    Debug.msg("core", DEBUG_ERROR, ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Protocols -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses(props.getProperty("ProtocolClassPath", "../../org/oobd/ui/swing/build/classes/org/oobd/base/protocol"), "org.oobd.base.protocol.", Class.forName("org.oobd.base.protocol.OobdProtocol"));
            for (Iterator iter = classObjects.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdProtocol thisClass = (OobdProtocol) value.newInstance();
                    thisClass.registerCore(this);
                    protocols.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden kann
                    Debug.msg("core", DEBUG_ERROR, ex.getMessage());
                } catch (IllegalAccessException e) {
                }

            }
        } catch (ClassNotFoundException e) {
        }
        // ----------- load Scriptengines AS CLASSES, NOT AS INSTANCES!-------------------------------
        try {
            scriptengines = loadOobdClasses(props.getProperty("EngineClassPath", "../../org/oobd/ui/swing/build/classes/org/oobd/base/scriptengine"), "org.oobd.base.scriptengine.", Class.forName("org.oobd.base.scriptengine.OobdScriptengine"));
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
        // ------------- start the global timer ticker -----------------
        ticker = new CoreTick();
        ticker.setCoreTickListener(this);
        new Thread(ticker).start();
    }

    /**
     * a help routine returns actual Instance of Core class
     * @return Core
     */
    public static Core getSingleInstance() {
        return thisInstance;
    }

    public void register(String msg) {
        userInterface.sm(msg);
    }

    /**
     * add generated visualizers to global list
     */
    public void addVisualizer(String owner, Visualizer vis) {
        if (visualizers.containsKey(owner)) {
            ((ArrayList) visualizers.get(owner)).add(vis);
        } else {
            ArrayList ar = new ArrayList();
            ar.add(vis);
            visualizers.put(owner, ar);
        }
    }

    @Override
    public String getPluginName() {
        return OOBDConstants.CoreMailboxName;
    }

    /**
     * create ScriptEngine identified by its public Name. Returns a unique ID which is used from now on for all communication between the core and the UI
     * @param id public name of scriptengine to be created
     * @param classtype
     * @return unique id of this class, made out of its public name and counter. Needed to link UI canvas to this object
     *
     */
    public String createScriptEngine(String id) {
        Debug.msg("core", DEBUG_INFO, "Core should create scriptengine: " + id);
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
        Debug.msg("core", DEBUG_BORING, "Start scriptengine: " + id);
        OobdScriptengine o = activeEngines.get(id);
        Thread t1 = new Thread(o);
        t1.start();
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
    public HashMap loadOobdClasses(String path, String classPrefix, Class classType) {
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
                Debug.msg("core", DEBUG_WARNING, ex.getMessage());
            }
// Einen URLClassLoader für das Verzeichnis instanzieren

            URLClassLoader loader = new URLClassLoader(new java.net.URL[]{sourceURL}, Thread.currentThread().getContextClassLoader());
            // Für jeden File im Verzeichnis...
            for (int i = 0; i
                    < files.length; i++) {
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
                        Debug.msg("core", DEBUG_ERROR, ex.getMessage());
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
            Debug.msg("core", DEBUG_BORING, "required Action:" + jsonString);
            actionRequest(new Onion(jsonString.replace('\'', '"')));
        } catch (org.json.JSONException e) {
            Debug.msg("core", DEBUG_ERROR, "could not convert JSONstring \"" + jsonString + "\" into Onion");
        }
    }

    public void handleValue(Onion value) {
        String owner = value.getOnionString("owner/name");
        if (owner == null) {
            Debug.msg("core", DEBUG_WARNING, "onion id does not contain name");
        } else {
            Collection c = visualizers.values();
            //obtain an Iterator for Collection
            Iterator itr;
            ArrayList affectedVisualizers = visualizers.get(owner);
            if (affectedVisualizers != null) {
                Iterator visItr = affectedVisualizers.iterator();
                while (visItr.hasNext()) {
                    Visualizer vis = (Visualizer) visItr.next();
                    vis.setValue(value);
                }
            }
        }
    }

    /**
     * main entry point for all actions required by the different components. 
     * Can be called either with a onion or with an json-String containing the onion data
     * @param json string representing the onion data
     */
    public void actionRequest(Onion myOnion) {
        try {
            Debug.msg("core", DEBUG_BORING, "type is:" + myOnion.getString("type"));
            if (myOnion.isType(CM_VISUALIZE)) {
                Debug.msg("Core", DEBUG_INFO, "visualitation requested");
                userInterface.visualize(myOnion);
            }
            if (myOnion.isType(CM_VALUE)) {
                Debug.msg("Core", DEBUG_INFO, "visualitation requested");
                handleValue(myOnion);
            }
            if (myOnion.isType(CM_UPDATE)) {
                Debug.msg("Core", DEBUG_INFO, "forward UPDATE request to"+myOnion.getString("to"));
                   transferMsg(new Message(this, myOnion.getString("to"), myOnion));

            }

            if (myOnion.isType(CM_CANVAS)) {
                Debug.msg("Core", DEBUG_INFO, "Canvas requested");
                String dummy = myOnion.getOnionString("owner");

                userInterface.addCanvas(myOnion.getOnionString("owner"), myOnion.getOnionString("name"));
            }

        } catch (org.json.JSONException e) {
        }
    }

    /**
     * generic hashtable to store several relational data assignments during runtine
     * Can be called either with a onion or with an json-String containing the onion data
     * @param id string identifier
     * @param subclass string sub identifier
     * @param data object reference to store
     */
    public void setAssign(String id, String subclass, Object data) {
        assignments.put(id + ":" + subclass, data);
    }

    /**
     * get entry from assigment
     * @param id string identifier
     * @param subclass string sub identifier
     */
    public Object getAssign(String id, String subclass) {
        return assignments.get(id + ":" + subclass);
    }

    /**
     * remove entry from assignment table
     * @param id string identifier
     * @param subclass string sub identifier
     */
    public void removeAssign(String id, String subclass) {
        assignments.remove(id + ":" + subclass);
    }

    /** updates all visualizers
     *
     */
    public void updateVisualizers() {

        Collection c = visualizers.values();
        //obtain an Iterator for Collection
        Iterator itr;

        //iterate through HashMap values iterator
        // run through the 3 update states: 0: start 1: update data 2: finish
        for (int i = 0; i < 3; i++) {
            itr = c.iterator();
            while (itr.hasNext()) {
                ArrayList engineVisualizers = (ArrayList) itr.next();
                Iterator visItr = engineVisualizers.iterator();
                while (visItr.hasNext()) {
                    Visualizer vis = (Visualizer) visItr.next();
                    vis.doUpdate(i);
                }
            }
        }
    }

    public boolean transferMsg(Message msg) {
        if (OOBDConstants.CoreMailboxName.equals(msg.rec)){
            this.sendMsg(msg);
            return true;
        }else{
        OobdPlugin receiver = activeEngines.get(msg.rec);
        if (receiver == null) {
            receiver = busses.get(msg.rec);
        }
        if (receiver == null) {
            receiver = connectors.get(msg.rec);
        }
        if (receiver == null) {
            receiver = protocols.get(msg.rec);
        }
        if (receiver != null) {
            receiver.sendMsg(msg);
            return true;
        } else {
            return false;
        }
        }
    }

    /** the central timer for the core
     *
     */
    public void coreTick() {
        ticker.enable(false);
        System.out.println("Tick..");
        Message thisMsg;
        while((thisMsg=msgPort.getMsg(false))!=null){
            actionRequest(thisMsg.content);

        }
        //transferMsg(new Message(this, "ScriptengineTerminal.1", null));
        updateVisualizers();
        ticker.enable(true);
    }

    public void run() {
    }
}

class CoreTick implements Runnable {

    boolean keepRunning = true;
    boolean enableTicks = true;
    private static final int LONG_TIME = 1000; /* 1 Seconds */

    CoreTickListener l = null; /* Currently only one listener. There could be many*/


    public void run() {
        System.out.println(" End Core tick thread");
        while (keepRunning) {
            try {
                Thread.currentThread().sleep(LONG_TIME);
                if (enableTicks) {
                    l.coreTick();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("End Core tick thread");
    }

    public void cancel() {
        keepRunning = false;
    }

    public void enable(boolean allow) {
        enableTicks = allow;
    }

    public void setCoreTickListener(CoreTickListener l) {
        this.l = l;
    }
}

interface CoreTickListener {

    public void coreTick();
}
