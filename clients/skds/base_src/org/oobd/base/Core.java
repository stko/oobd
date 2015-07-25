/**
 * \mainpage Welcome to the OOBD Programming Manual
 *
 * \section Introduction
 *
 * This documentation is made for twopurposes: First explain the UISystem
 * functionality, and second of course the general concept of how the different
 * components work together.
 *
 * As the functionality is very specific, while the concept is very gereric, it
 * will be a litte bit tricky to join these both counterparts together. Let's
 * see, if it works.
 *
 *
 * \section The Concept
 *
 * OOBD should be a framework for hopefully all kind of (automotive vehicle)
 * diagnostics, where diagnostic in OOBD is not defined as just monitoring real
 * time data, its defined as sending a question and visualize the answer.
 *
 * When looking more in detail into this basic requierement, you'll find five
 * tasks which are nessecary to fulfill this.
 *
 * Each of these tasks build its own chapter. If you want to understand and
 * program your own user interface, you've only to read and implement the
 * chapter about \ref visualisation, how to create a own \ref scriptengine can
 * be found there etc.
 *
 * \li coordination: Something inside must handle the fundamental things
 * (dynamic module handling, message transfer, file i/o etc.). This is done by
 * the \ref core \li visualisation: Finally somebody wants to see results or
 * wants to do some user input. This is handled by the \ref visualisation
 *
 */
package org.oobd.base;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.*;
import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import java.util.Collection;
import java.util.Collections;
import java.util.prefs.Preferences;

import org.json.JSONException;
import org.oobd.base.support.Onion;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.db.OobdDB;
import org.oobd.base.connector.OobdConnector;
import org.oobd.base.protocol.OobdProtocol;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.scriptengine.ScriptengineLua;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.support.History;
import org.oobd.base.uihandler.*;
import org.oobd.base.visualizer.Visualizer;

/**
 * \defgroup init Initialisation during startup
 *
 * At startup, different objects come to live and tell each other about their
 * existence and their capabilities
 *
 * This is mainly to create the links between the generic core functions and the
 * application depending User interface and UISystem enviroment.
 *
 * This is done in the following steps: \li the Application class itself
 * implements the Interface IFsystem \li the main class, which controls the GUI
 * (like the form, the activity or whatever) implements the Interface IFsystem
 *
 * When the application starts, it first creates an instance of the GUI class.
 * After that, it creates the (single) instance of the OOBD core
 *
 * myCore = new Core(this, GUI_instance)
 *
 *
 * When the Core instance is initiated, it \li announces itself to the GUI
 * object \li announces itself to the GUI object \li tells the GUI about
 * available scriptengines
 *
 *
 * \msc GUI,App,Core; App->Core [label="new Core()"]; App<-Core
 * [label="registerOobdCore()", URL="\ref
 * org::oobd::base::IFsystem.registerOobdCore()"]; GUI<-Core
 * [label="registerOobdCore()", URL="\ref
 * org::oobd::base::IFui.registerOobdCore()"]; GUI<-Core [label="1. Engine
 * found", URL="\ref org::oobd::base::IFui.announceScriptengine()"]; ---
 * [label="for all engines found"]; GUI<-Core [label="..n Engine found",
 * URL="\ref org::oobd::base::IFui.announceScriptengine()"]; \endmsc
 *
 * that's already all.
 *
 * Now the systems waits that the user selects through the GUI one of the
 * announced script engines to work with. How this works can be found in \ref
 * visualisation
 */
/**
 * \defgroup core Kernel, Runtime & Interprocess Functions
 */
/**
 * \brief The Master Control Unit - the Core object
 *
 * The Core object provides all basic functionality and "glues" everything else
 * together
 *
 */
public class Core extends OobdPlugin implements OOBDConstants, CoreTickListener {

    IFui userInterface;
    IFsystem systemInterface;
    OobdUIHandler uiHandler = null;
    HashMap<String, OobdBus> busses; // /<stores all available busses
    HashMap<String, OobdConnector> connectors; // /<stores all available
    // connectors
    HashMap<String, OobdProtocol> protocols; // /<stores all available protocols
    HashMap<String, Class<OobdUIHandler>> uiHandlers; // /<stores all available
    // UI-handlers
    HashMap<String, Class<OobdScriptengine>> scriptengines; // /<stores all
    // available
    // scriptengine classes
    HashMap<String, OobdDB> databases; // /<stores all available
    // database classes
    HashMap<String, OobdScriptengine> activeEngines; // /<stores all active
    // (instanced)
    // scriptengine objects
    HashMap<String, OobdUIHandler> activeUIHandlers; // /<stores all active
    // (instanced)
    // UIHandlers objects
    /**
     * The assingnments - hashtable works as a poor mens registry, where
     * everything, which needs to stored somehow, is kept as a string => object
     * pair
     */
    HashMap<String, Object> assignments;
    String uiHandlerID = "";
    static Core thisInstance = null; // Class variable points to only instance
    CoreTick ticker;
    Preferences props;
    boolean runCore = true;

    /**
     * \brief The Application creates one single instance of the core class
     * \ingroup init
     *
     * @param myUserInterface reference to the View - interface, which is used
     * to handle all visual in- and output
     * @param mySystemInterface reference to the actual application and runtime
     * enviroment, on which OOBD is actual running on
     * @param Name of the Plugin, just for debugging
     *
     */
    public Core(IFui myUserInterface, IFsystem mySystemInterface, String name)  {
        super(name);
        if (thisInstance != null) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Core Creator called more as once!!");
        }
        thisInstance = this;
        id = CoreMailboxName;
        userInterface = myUserInterface;
        systemInterface = mySystemInterface;
        busses = new HashMap<String, OobdBus>();
        connectors = new HashMap<String, OobdConnector>();
        protocols = new HashMap<String, OobdProtocol>();
        uiHandlers = new HashMap<String, Class<OobdUIHandler>>();
        scriptengines = new HashMap<String, Class<OobdScriptengine>>();
        activeEngines = new HashMap<String, OobdScriptengine>();
        activeUIHandlers = new HashMap<String, OobdUIHandler>();
        assignments = new HashMap<String, Object>();
        databases = new HashMap<String, OobdDB>();
        systemInterface.registerOobdCore(this); // Anounce itself at the
        // Systeminterface
        userInterface.registerOobdCore(this); // Anounce itself at the
        // Userinterface
        props = systemInterface.loadPreferences(FT_PROPS,
                OOBDConstants.CorePrefsFileName);

        // ----------- load Busses -------------------------------
        try {
            Logger.getLogger(Core.class.getName()).log(
                    Level.CONFIG,
                    "Try to load: "
                    + props.get("BusClassPath",
                            "org.oobd.base.bus.BusCom"));
            HashMap<String, Class<?>> classObjects = loadOobdClasses(
                    props.get("BusClassPath", "org.oobd.base.bus.BusCom"),
                    props.get("BusClassPrefix", "org.oobd.base.bus."),
                    Class.forName("org.oobd.base.bus.OobdBus"));
            for (Iterator iter = classObjects.keySet().iterator(); iter
                    .hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdBus thisClass = (OobdBus) value.newInstance();
                    thisClass.registerCore(this);
                    new Thread(thisClass).start();
                    busses.put(element, thisClass);
                    Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                            "Register " + element + " as bus");
                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden
                    // kann
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "InstantiationException: can't instance of Bus "
                            + element);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "IllegalAccessException: can't create instance of Bus "
                            + element);
                }

            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        }
        // ----------- load Connectors -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses(props.get(
                    "ConnectorClassPath",
                    "org.oobd.base.connector.ConnectorLocal"),
                    "org.oobd.base.connector.",
                    Class.forName("org.oobd.base.connector.OobdConnector"));
            for (Iterator iter = classObjects.keySet().iterator(); iter
                    .hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdConnector thisClass = (OobdConnector) value
                            .newInstance();
                    thisClass.registerCore(this);
                    connectors.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden
                    // kann
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "InstantiationException: can't instance of Connector "
                            + element);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "IllegalAccessException: can't create instance of Connector "
                            + element);
                }

            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        }
        // ----------- load Protocols -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses(props.get(
                    "ProtocolClassPath", "org.oobd.base.protocol.ProtocolUDS"),
                    "org.oobd.base.protocol.",
                    Class.forName("org.oobd.base.protocol.OobdProtocol"));
            for (Iterator iter = classObjects.keySet().iterator(); iter
                    .hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdProtocol thisClass = (OobdProtocol) value.newInstance();
                    thisClass.registerCore(this);
                    protocols.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden
                    // kann
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "InstantiationException: can't instance of Protocol "
                            + element);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "IllegalAccessException: can't create instance of Protocol "
                            + element);
                }

            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        }
        // ----------- load Databases -------------------------------
        try {
            HashMap<String, Class<?>> classObjects = loadOobdClasses(props.get(
                    "DatabaseClassPath", "org.oobd.base.db.AVLLookup"),
                    "org.oobd.base.db.",
                    Class.forName("org.oobd.base.db.OobdDB"));
            for (Iterator iter = classObjects.keySet().iterator(); iter
                    .hasNext();) {
                String element = (String) iter.next();
                Class<?> value = (Class<?>) classObjects.get(element);
                try {
                    OobdDB thisClass = (OobdDB) value.newInstance();
                    thisClass.registerCore(this);
                    new Thread(thisClass).start();
                    databases.put(element, thisClass);

                } catch (InstantiationException ex) {
                    // Wird geworfen, wenn die Klasse nicht "instanziert" werden
                    // kann
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "InstantiationException: can't instance of Database "
                            + element);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "IllegalAccessException: can't create instance of Database "
                            + element);
                }

            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        }
        // ----------- load Scriptengines AS CLASSES, NOT AS
        // INSTANCES!-------------------------------
        try {
            scriptengines = loadOobdClasses(
                    props.get("EngineClassPath",
                            "org.oobd.base.scriptengine.ScriptengineLua"),
                    "org.oobd.base.scriptengine.",
                    Class.forName("org.oobd.base.scriptengine.OobdScriptengine"));
            for (Iterator iter = scriptengines.keySet().iterator(); iter
                    .hasNext();) {
                String element = (String) iter.next();
                Class<OobdScriptengine> value = scriptengines.get(element);
                scriptengines.put(element, value);
                // now I need to be a little bit tricky to involve the static
                // class method of an untypized class
                try {
                    Class[] parameterTypes = new Class[]{};
                    java.lang.reflect.Method method = value.getMethod(
                            "publicName", new Class[]{}); // no parameters
                    Object instance = null;
                    String result = (String) method.invoke(instance,
                            new Object[]{}); // no parameters
                    // Android: String.isEmpty() not available
                    // if (!result.isEmpty()) {
                    if (result.length() != 0) {
                        userInterface.announceScriptengine(element, result);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "can't call static method 'publicName' of "
                            + element);
                    ex.printStackTrace();

                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        }
/*        // ----------- load UIHandlers AS CLASSES, NOT AS
        
        // INSTANCES!-------------------------------
        try {
            uiHandlers = loadOobdClasses(props.get("UIHandlerClassPath",
                    "org.oobd.ui.uihandler.UIHandler"),
                    "org.oobd.ui.uihandler.",
                    Class.forName("org.oobd.base.uihandler.OobdUIHandler"));
            for (Iterator iter = uiHandlers.keySet().iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                Class<OobdUIHandler> value = uiHandlers.get(element);
                uiHandlers.put(element, value);
                // now I need to be a little bit tricky to involve the static
                // class method of an untypized class
                try {
                    Class[] parameterTypes = new Class[]{};
                    java.lang.reflect.Method method = value.getMethod(
                            "publicName", new Class[]{}); // no parameters
                    Object instance = null;
                    String result = (String) method.invoke(instance,
                            new Object[]{}); // no parameters
                    // Android: String.isEmpty() not available
                    // if (!result.isEmpty()) {
                    if (result.length() != 0) {
                        userInterface.announceUIHandler(element, result);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.WARNING,
                            "can't call static method 'publicName' of "
                            + element);
                    ex.printStackTrace();

                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        }
*/
        // ----------- load UIHandlers AS CLASSES, NOT AS
        // INSTANCES!-------------------------------
        try {
            {
                BufferedReader br = null;
                String strLine = "";
                br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/org/oobd/base/classloader.cfg")));
                while ((strLine = br.readLine()) != null) {
                    System.out.println(strLine);
                    if (!"".equals(strLine)) {
                        String[] classConfig = strLine.split(":");
                        if ("uihandler".equals(classConfig[0])) {
                            Class<OobdUIHandler> value = (Class<OobdUIHandler>) Class.forName(classConfig[1]);

                            String[] classNameElements = classConfig[1].split("\\.");
                            String element = classNameElements[classNameElements.length - 1];
                            uiHandlers.put(element, value);

 				// now I need to be a little bit tricky to involve the static
                            // class method of an untypized class
                            try {
                                Class[] parameterTypes = new Class[]{};
                                java.lang.reflect.Method method = value.getMethod(
                                        "publicName", new Class[]{}); // no parameters
                                Object instance = null;
                                String result = (String) method.invoke(instance,
                                        new Object[]{}); // no parameters
                                // Android: String.isEmpty() not available
                                // if (!result.isEmpty()) {
                                if (result.length() != 0) {
                                    userInterface.announceUIHandler(element, result);
                                }
                            } catch (Exception ex) {
                                Logger.getLogger(Core.class.getName()).log(
                                        Level.WARNING,
                                        "can't call static method 'publicName' of "
                                        + element);
                                ex.printStackTrace();

                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Error while trying to load class", ex);
        } catch (IOException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }

        // ------------- start the global timer ticker -----------------
        ticker = new CoreTick();
        ticker.setCoreTickListener(this);
        new Thread(ticker).start();
        new Thread(this).start();
    }// constructor

    /**
     * a static help routine which returns the actual running Instance of the
     * Core class
     *
     * @return Core
     */
    public static Core getSingleInstance() {
        return thisInstance;
    }

    /**
     * a help routine which returns the system Interface of the Core class
     *
     * @return Core
     */
    public IFsystem getSystemIF() {
        return systemInterface;
    }

    /**
     * a help routine which returns the UI Interface of the Core class
     *
     * @return UI-Interface
     */
    public IFui getUiIF() {
        return userInterface;
    }

    /**
     * a help routine which returns the UI Handler of the Core class
     *
     * @return Core
     */
    public OobdUIHandler getUiHandler() {
        return uiHandler;
    }

    /**
     * a help routine which returns the Scriptengine of a given ID
     *
     * @return OobdScriptengine
     */
    public OobdScriptengine getScriptEngine(String id) {
        return activeEngines.get(id);
    }

    /**
     * supply Objects which binds to system specific hardware
     *
     * @param typ
     * @return a hardware handle of the requested type or nil
     */
    public Object supplyHardwareHandle(Onion typ) {
        return systemInterface.supplyHardwareHandle(typ);
    }

    /**
     * supply list of system specific connection hardware classes
     *
     * @param typ
     * @return a hardware handle of the requested type or nil
     */
    public Hashtable<String, Class> getConnectorList() {
        return systemInterface.getConnectorList();
    }

    @Override
    public String getPluginName() {
        return OOBDConstants.CoreMailboxName;
    }

    /**
     * \brief create ScriptEngine identified by its public Name
     *
     * Returns a unique ID which is used from now on for all communication
     * between the core and the UI
     *
     * @param id public name of scriptengine to be created
     * @param onion additional params
     * @param classtype
     * @return unique id of this class, made out of its public name and counter.
     * Needed to link UI canvas to this object
     *
     */
    public String createScriptEngine(String id, Onion onion) {
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "Core should create scriptengine: " + id);
        Integer i = 1;
        while (activeEngines.containsKey(id + "." + i.toString())) { // searching
            // for a
            // free
            // id
            i++;
        }
        String seID = id + "." + i.toString();
        OobdScriptengine o = null;
        Class[] argsClass = new Class[3]; // first we set up an pseudo - args -
        // array for the scriptengine-
        // constructor
        argsClass[0] = seID.getClass(); // and fill it with the info of the
        // arguments classes
        argsClass[1] = this.getClass();
        argsClass[2] = IFsystem.class;
        Class classRef = scriptengines.get(id); // then we get the class of the
        // wanted scriptengine
        try {
            Constructor con = classRef.getConstructor(argsClass); // and let
            // Java find
            // the
            // correct
            // constructor
            // matching
            // to the
            // args
            // classes
            Object[] args = {seID, this, systemInterface}; // creating the
            // args-array
            o = (OobdScriptengine) con.newInstance(args); // and finally create
            // the object from
            // the scriptengine
            // class with its
            // unique id as
            // parameter
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeEngines.put(seID, o); // store the new created scriptengine
        return seID;
    }

    /**
     * \brief starts a scriptengine
     *
     * During startup, the core reports all available scriptengines to the User
     * Interface to let the user choose with which one he wants to work with.
     *
     * This engine is then been first created with createScriptEngine(), and
     * when all initalisation is been done, it's started with
     * startScriptEngine() \ingroup visualisation
     *
     * @param onion addional param
     */
    public void startScriptEngine(String id, Onion onion) {
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "Start scriptengine: " + id);
        OobdScriptengine o = activeEngines.get(id);
        o.setStartupParameter(onion);
        Thread t1 = new Thread(o);
        t1.start();
    }

    /**
     * \brief create ScriptEngine identified by its public Name
     *
     * Returns a unique ID which is used from now on for all communication
     * between the core and the UI
     *
     * @param id public name of scriptengine to be created
     * @param onion additional params
     * @param classtype
     * @return unique id of this class, made out of its public name and counter.
     * Needed to link UI canvas to this object
     *
     */
    public String createUIHandler(String id, Onion onion) {
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "Core should create UIHandler: " + id);
        Integer i = 1;
        while (activeUIHandlers.containsKey(id + "." + i.toString())) { // searching
            // for a
            // free
            // id
            i++;
        }
        String seID = id + "." + i.toString();

        OobdUIHandler o = null;
        WSOobdUIHandler o1=null;
        Class[] argsClass = new Class[4]; // first we set up an pseudo - args -
        // array for the scriptengine-
        // constructor
        argsClass[0] = seID.getClass(); // and fill it with the info of the
        // arguments classes
        argsClass[1] = this.getClass();
        argsClass[2] = IFsystem.class;
        argsClass[3] = String.class;
        Class classRef = (Class) uiHandlers.get(id); // then we get the class of
        // the
        // wanted scriptengine
        try {
            Constructor con = classRef.getConstructor(argsClass); // and let
            // Java find
            // the
            // correct
            // constructor
            // matching
            // to the
            // args
            // classes
            Object[] args = {seID, this, systemInterface,new String(OOBDConstants.UIHandlerMailboxName)}; // creating the
            // args-array
      //       o1 =  (WSOobdUIHandler) con.newInstance(args); // and finally create
           o = (OobdUIHandler) con.newInstance(seID, this, systemInterface,OOBDConstants.UIHandlerMailboxName); // and finally create
            // the object from
            // the scriptengine
            // class with its
            // unique id as
            // parameter
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeUIHandlers.put(seID, o1); // store the new created scriptengine
        uiHandlerID = seID;
        return seID;
    }

    /**
     * \brief starts the UIHandler
     *
     * During startup, the core reports all available UIHandler to the User
     * Interface to let the user choose with which one he wants to work with.
     *
     * When all initalisation is been done, it's started with startUIHandler()
     * \ingroup visualisation
     *
     * @param onion addional param
     */
    public void startUIHandler(String id, Onion onion) {
        if (uiHandler == null) {
            Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                    "Start UIHandler: " + id);
            uiHandler = activeUIHandlers.get(id);
            uiHandler.setStartupParameter(onion);
            // uiHandler is not a thread, it's called by the UI-Thread instead,
            // so we
            // Thread t1 = new Thread(uiHandler);
            // t1.start();
            uiHandler.start();
        }
    }

    /**
     * \brief generate the lists of available OOBD classes for scriptengines,
     * busses etc. Loads different dynamic classes via an URLClassLoader. As an
     * URLClassloader is generic and can handle URLs, file systems and also jar
     * files, this loader is located in the core section of oobd. Just the
     * information about the correct load path is environment specific and needs
     * to come from the systemInterface
     *
     * @param path directory to seach in
     * @param classtype reference class for what to search for
     * @return Hashmap
     * @todo loadOobdClasses supports actual only class files in a dir, but not
     * in a jar file. The jar UISystem from
     * "Dynamisches_laden_von_Klassen_example.txt" needs to be implemented also
     * @todo the classloader needs to be extended to support encrypted
     * (=licenced) class files
     * @bug what do do that the procedure also support relative filepaths?
     *
     */
    public HashMap loadOobdClasses(String path, String classPrefix,
            Class classType) {
        // inspired by http://de.wikibooks.org/wiki/Java_Standard:_Class
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "Scanne Directory: " + path);
        HashMap<String, Class<?>> myInstances = systemInterface
                .loadOobdClasses(path, classPrefix, classType);
        return myInstances;
    }

    /**
     * main entry point for all actions required by the different components.
     * Can be called either with a onion or with an json-String containing the
     * onion data
     *
     * @param json string representing the onion data
     * @return true if the message should be replied to sender
     */
    public boolean actionRequest(String jsonString) {
        try {
            Logger.getLogger(Core.class.getName()).log(Level.INFO,
                    "required Action:" + jsonString);
            return actionRequest(new Onion(jsonString.replace('\'', '"')));
        } catch (org.json.JSONException ex) {
            Logger.getLogger(Core.class.getName()).log(
                    Level.SEVERE,
                    "could not convert JSONstring \"" + jsonString
                    + "\" into Onion", ex);
            return false;
        }
    }

    /**
     * main entry point for all actions required by the different components.
     * Can be called either with a onion or with an json-String containing the
     * onion data
     *
     * @param json string representing the onion data
     * @return true if the message should be replied to sender
     */
    public boolean actionRequest(Onion myOnion) {
        try {
            if (myOnion.isType(CM_CHANNEL)) {
                this.getMsgPort().sendAndWait(
                        new Message(Core.getSingleInstance(), BusMailboxName,
                                new Onion("" + "{'type':'" + CM_BUSTEST + "',"
                                        + "'command':'connect',"
                                        + "'connecturl':'"
                                        + myOnion.getString("connecturl")
                                        + "'}")), 35000); // 35 secs to
                // connect
                // to a
                // device
                // (BT has
                // ~30sec
                // Timeout)
                return true;
            }
        } catch (org.json.JSONException e) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "JSON exception..");
            return false;
        }
        return false;
    }

    /**
     * generic hashtable to store several relational data assignments during
     * runtine
     *
     * @param id string identifier
     * @param subclass string sub identifier
     * @param data object reference to store
     */
    public void setAssign(String id, String subclass, Object data) {
        assignments.put(id + ":" + subclass, data);
    }

    /**
     * get entry from assigment
     *
     * @param id string identifier
     * @param subclass string sub identifier
     */
    public Object getAssign(String id, String subclass) {
        return assignments.get(id + ":" + subclass);
    }

    /**
     * remove entry from assignment table
     *
     * @param id string identifier
     * @param subclass string sub identifier
     */
    public void removeAssign(String id, String subclass) {
        assignments.remove(id + ":" + subclass);
    }

    /**
     * \brief transfer a message to the receiver
     *
     * @param msg a message, containing sender, receipient and the message
     * itself
     * @return true, if receipient was known
     *
     * \ingroup core
     */
    public boolean transferMsg(Message msg) {
        Logger.getLogger(Core.class.getName()).log(
                Level.INFO,
                "Msg: " + msg.sender + " ==> " + msg.rec + " content:"
                + msg.getContent().toString());
        if (OOBDConstants.CoreMailboxName.equals(msg.rec)) { // is the core the
            // receiver?
            this.sendMsg(msg);
            return true;
        } else {// find receipient
            OobdPlugin receiver = activeEngines.get(msg.rec);
            if (receiver == null
                    && OOBDConstants.UIHandlerMailboxName.equals(msg.rec)) {
                receiver = activeUIHandlers.get(uiHandlerID);
            }
            if (receiver == null) {
                receiver = busses.get(msg.rec);
            }
            if (receiver == null) {
                receiver = connectors.get(msg.rec);
            }
            if (receiver == null) {
                receiver = protocols.get(msg.rec);
            }
            if (receiver == null) {
                receiver = databases.get(msg.rec);
            }
            if (receiver != null) {
                receiver.sendMsg(msg);
                return true;
            } else {
                Logger.getLogger(Core.class.getName()).log(
                        Level.WARNING,
                        "Coudn't send msg to " + msg.rec
                        + ":Receiver not in database");
                return false;
            }
        }
    }

    /**
     * a small help routine to output text from elements who don't have an own
     * mailbox
     *
     * @param output the output text
     */
    public void outputText(String output) {
        try {
            core.transferMsg(new Message(Core.getSingleInstance(),
                    UIHandlerMailboxName, new Onion("{" + "'type':'"
                            + CM_WRITESTRING + "'," + "'owner':" + "{'name':'"
                            + Core.getSingleInstance().getId() + "'},"
                            + "'command':'serDisplayWrite'," + "'data':'"
                            + Base64Coder.encodeString(output) + "'" + "}")));
        } catch (JSONException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * forwards parameter requests to the UI to
     *
     * @todo extend this function to allow all type of parameter inputs + return
     * values to the caller
     *
     * @param msg the parameter description
     */
    public Onion requestParamInput(Onion msg) {
        Onion answer = null;
        try {
            Onion thisOnion = new Onion("{" + "'type':'" + CM_PARAM + "',"
                    + "'owner':" + "{'name':'"
                    + Core.getSingleInstance().getId() + "'}" + "}");
            thisOnion.put(CM_PARAM, msg.getJSONArray(CM_PARAM));
            Message answerMsg = this.getMsgPort().sendAndWait(
                    new Message(Core.getSingleInstance(), UIHandlerMailboxName,
                            thisOnion), -1);
            if (answerMsg != null) {
                answer = answerMsg.getContent();
            }
        } catch (JSONException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    /**
     * a small help routine to output text from elements who don't have an own
     * mailbox
     *
     * @param msg the text to show
     */
    public void userAlert(String msg, String windowName) {
        try {
            requestParamInput(new Onion("{'" + CM_PARAM + "' : [{ "
                    + "'type':'String'," + "'title':'"
                    + Base64Coder.encodeString("Info") + "'," + "'default':'"
                    // + Base64Coder.encodeString(connectURLDefault)
                    + "',"
                    + "'window':'"
                    + windowName
                    + "',"
                    + "'tooltip':'" + Base64Coder.encodeString(msg)
                    + "'" + "}]}"));

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * The Central Timer
     *
     * in here all functions are called which needs a regular run
     *
     */
    public void coreTick() {
        ticker.enable(false);
        // updateVisualizers();
        ticker.enable(true);
    }

    /**
     * \brief the Core thread
     */
    public void run() {
        Message thisMsg;
        while (runCore == true) {
            while ((thisMsg = msgPort.getMsg(100)) != null) { // just waiting
                // and handling
                // messages
                if (actionRequest(thisMsg.content) == true) {
                    try {
                        thisMsg.content.setValue("replyID",
                                thisMsg.content.getInt("msgID"));
                    } catch (JSONException ex) {
                        Logger.getLogger(Core.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                    msgPort.replyMsg(thisMsg, thisMsg.content);
                }

            }
            // transferMsg(new Message(this, "ScriptengineTerminal.1", null));
        }
    }
}

/**
 *
 * \brief Helpclass for Core timer events
 */
class CoreTick implements Runnable {

    boolean keepRunning = true;
    boolean enableTicks = true;
    private static final int LONG_TIME = 100; /* 0.1 Seconds */

    CoreTickListener l = null; /*
     * Currently only one listener. There could be
     * many
     */


    public void run() {
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                " Start Core tick thread");
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
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "End Core tick thread");
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
