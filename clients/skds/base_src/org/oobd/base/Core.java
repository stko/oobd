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
import java.util.prefs.Preferences;

import org.json.JSONException;
import org.oobd.base.archive.Archive;
import org.oobd.base.archive.Factory;
import org.oobd.base.support.Onion;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.db.OobdDB;
import org.oobd.base.connector.OobdConnector;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.protocol.OobdProtocol;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.uihandler.*;

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
    //Preferences props;
    boolean runCore = true;
    final ArrayList dataPoolList = new ArrayList(DP_ARRAY_SIZE);

    /**
     * \brief The Application creates one single instance of the core class
     * \ingroup init
     *
     * @param myUserInterface reference to the View - interface, which is used
     * to handle all visual in- and output
     * @param mySystemInterface reference to the actual application and runtime
     * enviroment, on which OOBD is actual running on
     * @param name of the Plugin, just for debugging
     *
     */
    public Core(IFui myUserInterface, IFsystem mySystemInterface, String name) {
        super(name);
        System.out.println("Java Runtime Version:" + System.getProperty("java.version"));
        if (thisInstance != null) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Core Creator called more as once!!");
        }
        thisInstance = this;
        id = CoreMailboxName;
        userInterface = myUserInterface;
        systemInterface = mySystemInterface;
//        busses = new HashMap<String, OobdBus>();
        busses = new HashMap<>();
        connectors = new HashMap<>();
        protocols = new HashMap<>();
        uiHandlers = new HashMap<>();
        scriptengines = new HashMap<>();
        activeUIHandlers = new HashMap<>();
        assignments = new HashMap<>();
        databases = new HashMap<>();
        // absolutely scary, but the datapool array list need to be filled once to not
        // crash later when trying to (re)assign a value...
        for (int i = 0; i < DP_ARRAY_SIZE; i++) {
            dataPoolList.add(null);
        }
        systemInterface.registerOobdCore(this); // Anounce itself at the Systeminterface
        userInterface.registerOobdCore(this); // Anounce itself at the Userinterface
        try {
            BufferedReader br;
            String strLine;
            br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/org/oobd/base/classloader.cfg")));
            while ((strLine = br.readLine()) != null) {
                if (!"".equals(strLine)) {
                    String[] classConfig = strLine.split(":");
                    boolean loadAsClass = false;
                    boolean loadAsInstance = false;
                    String result = "";
                    //Class<OobdScriptengine> value = (Class<OobdScriptengine>) Class.forName(classConfig[1]);
                    Class value = Class.forName(classConfig[1]);
                    String[] classNameElements = classConfig[1].split("\\.");
                    String element = classNameElements[classNameElements.length - 1];
                    OobdPlugin thisClass = null;
                    switch (classConfig[0]) {
                        case "scriptengine":
                        case "uihandler":
                            loadAsClass = true;
                            break;
                        case "oobddb":
                        case "protocol":
                        case "bus":
                        case "connector":
                            loadAsInstance = true;
                            break;

                    }
                    if (loadAsInstance) {
                        try {
                            thisClass = (OobdPlugin) value.newInstance();
                            thisClass.registerCore(this);
                            new Thread(thisClass).start();

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
                        // after creation, save results
                        switch (classConfig[0]) {
                            case "oobddb":
                                databases.put(element, (OobdDB) thisClass);
                                break;
                            case "protocol":
                                protocols.put(element, (OobdProtocol) thisClass);
                                break;
                            case "bus":
                                busses.put(element, (OobdBus) thisClass);
                                break;
                            case "connector":
                                connectors.put(element, (OobdConnector) thisClass);
                                break;
                        }
                    }
                    if (loadAsClass) {
                             //scriptengines.put(element, value);

                        // now I need to be a little bit tricky to involve the static
                        // class method of an untypized class
                        try {
                            java.lang.reflect.Method method = value.getMethod(
                                    "publicName", new Class[]{}); // no parameters
                            Object instance = null;
                            result = (String) method.invoke(instance,
                                    new Object[]{}); // no parameters
                            // Android: String.isEmpty() not available
                            // if (!result.isEmpty()) {
                            // if (result.length() != 0) {
                            //     userInterface.announceScriptengine(element, result);
                            // }
                        } catch (Exception ex) {
                            Logger.getLogger(Core.class.getName()).log(
                                    Level.WARNING,
                                    "can't call static method 'publicName' of "
                                    + element);
                            ex.printStackTrace();

                        }
                        // after creation, save results
                        switch (classConfig[0]) {
                            case "scriptengine":
                                scriptengines.put(element, value);
                                if (result.length() != 0) {
                                    userInterface.announceScriptengine(element, result);
                                }
                                break;
                            case "uihandler":
                                uiHandlers.put(element, value);
                                if (result.length() != 0) {
                                    userInterface.announceUIHandler(element, result);
                                }
                                break;
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
    public OobdScriptengine getScriptEngine() {
        return (OobdScriptengine) readDataPool(OOBDConstants.DP_RUNNING_SCRIPTENGINE, null);
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
     * ends a scriptEngine call this only at end of program or when reaching the
     * main page During normal operation, the engine change is handled by the
     * startEngine()routine already
     *
     * @param id
     *
     */
    public void stopScriptEngine() {
        OobdScriptengine thisEngine = (OobdScriptengine) readDataPool(OOBDConstants.DP_RUNNING_SCRIPTENGINE, null);
        if (thisEngine != null) {
            thisEngine.close();
        }
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
    public OobdScriptengine createScriptEngine(String id, Onion onion) {
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "Core should create scriptengine: " + id);
        OobdScriptengine o = null;
        Class[] argsClass = new Class[3]; // first we set up an pseudo - args -
        // array for the scriptengine-
        // constructor
        argsClass[0] = id.getClass(); // and fill it with the info of the
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
            Object[] args = {id, this, systemInterface}; // creating the
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
        writeDataPool(OOBDConstants.DP_LAST_CREATED_SCRIPTENGINE, o);

        return o;
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
        Class[] argsClass = new Class[3]; // first we set up an pseudo - args -
        // array for the scriptengine-
        // constructor
        argsClass[0] = seID.getClass(); // and fill it with the info of the
        // arguments classes
        argsClass[1] = this.getClass();
        argsClass[2] = IFsystem.class;
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
            Object[] args = {seID, this, systemInterface}; // creating the
            // args-array
            o = (OobdUIHandler) con.newInstance(seID, this, systemInterface); // and finally create
            // the object from
            // the scriptengine
            // class with its
            // unique id as
            // parameter
        } catch (Exception e) {
            e.printStackTrace();
        }
        activeUIHandlers.put(seID, o); // store the new created scriptengine
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
                if ("connect".equalsIgnoreCase(myOnion.getString("command"))) {
                    this.getMsgPort().sendAndWait(
                            new Message(Core.getSingleInstance(), BusMailboxName,
                                    new Onion("" + "{'type':'" + CM_BUSTEST + "',"
                                            + "'command':'" + myOnion.getString("command") + "',"
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
            }
        } catch (org.json.JSONException e) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "JSON exception..");
            return false;
        }

        return false;
    }

    /**
     * @brief add an object to the global data pool, used for most of the
     * variables used in OOBD
     *
     * @param id a numeric identifier, defined in OOBDConstants in DP_ (Data
     * Pool) section
     * @param data object reference to be stored
     */
    public void writeDataPool(int id, Object data) {
        synchronized (dataPoolList) {
            if (id >= dataPoolList.size()) {
                dataPoolList.ensureCapacity(id + 1);
            }
            dataPoolList.set(id, data);
        }
    }

    /**
     * @brief gets an object to the global data pool, used for most of the
     * variables used in OOBD
     *
     * @param id a numeric identifier, defined in OOBDConstants in DP_ (Data
     * Pool) section
     * @param defaultObject object returned, if object is null
     * @return Object
     */
    public Object readDataPool(int id, Object defaultObject) {
        synchronized (dataPoolList) {
            try {
                Object data = dataPoolList.get(id);
                if (data == null) {
                    return defaultObject;
                }
                return data;
            } catch (IndexOutOfBoundsException ex) {
                return defaultObject;
            }
        }
    }

    /**
     * @brief removes an object from the global data pool, used for most of the
     * variables used in OOBD
     *
     * @param id a numeric identifier, defined in OOBDConstants in DP_ (Data
     * Pool) section
     * @return removed object
     */
    public Object removeDataPool(int id, Object defaultObject) {
        synchronized (dataPoolList) {
            return dataPoolList.remove(id);
        }
    }

    /**
     * generic hashtable to store several relational data assignments during
     * runtine
     *
     * @param id string identifier
     * @param subclass string sub identifier
     * @param data object reference to store
     * @todo depreciated
     */
    public void setAssign(String id, String subclass, Object data) {
        assignments.put(id + ":" + subclass, data);
    }

    /**
     * get entry from assigment
     *
     * @param id string identifier
     * @param subclass string sub identifier
     * @todo depreciated
     */
    public Object getAssign(String id, String subclass) {
        return assignments.get(id + ":" + subclass);
    }

    /**
     * remove entry from assignment table
     *
     * @param id string identifier
     * @param subclass string sub identifier
     * @todo depreciated
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
    public
            boolean transferMsg(Message msg) {
        Logger.getLogger(Core.class
                .getName()).log(
                        Level.INFO,
                        "Msg: " + msg.sender + " ==> " + msg.rec + " content:"
                        + msg.getContent().toString());
        if (OOBDConstants.CoreMailboxName.equals(msg.rec)) { // is the core the
            // receiver?
            this.sendMsg(msg);
            return true;
        } else {// find receipient
            OobdPlugin receiver = null;
            OobdScriptengine scriptEngine = (OobdScriptengine) readDataPool(OOBDConstants.DP_RUNNING_SCRIPTENGINE, null);
            if (scriptEngine != null && scriptEngine.getId().equals(msg.rec)) {
                receiver = scriptEngine;
            }
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
            Logger.getLogger(Core.class
                    .getName()).log(Level.SEVERE, null, ex);
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
    public Onion requestParamInput(OobdPlugin sender, Onion msg) {
        Onion answer = new Onion();
        try {
            Onion thisOnion = new Onion("{" + "'type':'" + CM_PARAM + "',"
                    + "'owner':" + "{'name':'"
                    + sender.getId() + "'}" + "}");
            thisOnion.put(CM_PARAM, msg.get(CM_PARAM));
            Message answerMsg = sender.getMsgPort().sendAndWait(
                    new Message(sender, UIHandlerMailboxName,
                            thisOnion), -1);
            if (answerMsg != null) {
                answer = answerMsg.getContent();

            }
        } catch (JSONException ex) {
            Logger.getLogger(Core.class
                    .getName()).log(Level.SEVERE, null, ex);
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
            Onion msgOnion = new Onion("{'" + CM_DIALOG_INFO + "' : { "
                    + "'type':'String'," + "'title':'"
                    + Base64Coder.encodeString("Info") + "'," + "'default':'"
                    // + Base64Coder.encodeString(connectURLDefault)
                    + "',"
                    + "'window':'"
                    + Base64Coder.encodeString(windowName)
                    + "',"
                    + "'tooltip':'" + Base64Coder.encodeString(msg)
                    + "'" + "}}");
            Onion thisOnion = new Onion("{" + "'type':'" + CM_DIALOG_INFO + "',"
                    + "'owner':" + "{'name':'"
                    + Core.getSingleInstance().getId() + "'}" + "}");
            thisOnion.put(CM_DIALOG_INFO, msgOnion.get(CM_DIALOG_INFO));
            transferMsg(new Message(Core.getSingleInstance(), UIHandlerMailboxName,
                    thisOnion));

        } catch (JSONException ex) {
            Logger.getLogger(Core.class
                    .getName()).log(Level.SEVERE, null, ex);
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
                        Logger.getLogger(Core.class
                                .getName()).log(
                                        Level.SEVERE, null, ex);
                    }
                    msgPort.replyMsg(thisMsg, thisMsg.content);

                }

            }
            // transferMsg(new Message(this, "ScriptengineTerminal.1", null));
        }
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
    public void startScriptEngine(Onion onion) {
        OobdScriptengine o = (OobdScriptengine) readDataPool(OOBDConstants.DP_LAST_CREATED_SCRIPTENGINE, null);
        if (o == null) {
            o = createScriptEngine("ScriptengineLua", onion);
        } else {
            o.close();// stop the old engine
            o = createScriptEngine("ScriptengineLua", onion);
        }
        if (o == null) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Could not create Scriptengine! ");
            return;
        }
        Logger.getLogger(Core.class.getName()).log(Level.CONFIG,
                "Start scriptengine: " + id);
        while (core.readDataPool(DP_RUNNING_SCRIPTENGINE, null) != null) {
            try {
                Thread.sleep(100);
                System.out.println("core StartScriptEngine: Old Scriptengine not finished yet");
            } catch (InterruptedException ex) {
            }
        }
        o.setStartupParameter(onion);
        Thread t1 = new Thread(o);
        t1.start();
    }
    /*
     checks, if the URL points to a lbc ActiveArchive. if yes, starts it and returns the path to the start page read out of the manifest ActiveArchive of that script
     */

    public String startScriptEngineByURL(String resourceName) {

        ArrayList<Archive> files = (ArrayList<Archive>) readDataPool(DP_LIST_OF_SCRIPTS, null);
        Archive activeArchive = (Archive) readDataPool(DP_ACTIVE_ARCHIVE, null);
        String scriptPath = "";
        if (activeArchive != null && resourceName.endsWith(".lbc")) {//lets see if the innerpath points to a lbc ActiveArchive
            if (activeArchive.fileExist(resourceName)) {
                scriptPath = resourceName; //define the script path relative to the open Archive
                //this is a lbc, so lets try to load its own manifest
                activeArchive.relocateManifest(resourceName);
            }
        }
        if (files != null && scriptPath.equals("")) { // if the resource does not point to an archive internal lbc ActiveArchive, see if it matches to another archive
            for (Archive file : files) {
                if (("/" + file.getID()).equalsIgnoreCase(resourceName)) {
                    activeArchive = file;
                    writeDataPool(DP_ACTIVE_ARCHIVE, file);
                    scriptPath = activeArchive.getProperty(OOBDConstants.MANIFEST_SCRIPTNAME, "");
                    if (!scriptPath.equals("") && !activeArchive.fileExist(scriptPath)) {
                        scriptPath = "";
                    }
                    //lets see if the archive gives us a startpage
                    resourceName = activeArchive.getProperty(OOBDConstants.MANIFEST_STARTPAGE, resourceName);
                }
            }
        }
        if (!scriptPath.equals("") && activeArchive != null) { //if only the root ActiveArchive name is given or the innerpath really points to a .lbc ActiveArchive

            writeDataPool(DP_ACTIVE_ARCHIVE, activeArchive);
            System.out.println("start scriptengine for " + scriptPath);
            startScriptArchive(activeArchive);
            return activeArchive.getProperty(MANIFEST_STARTPAGE, OOBDConstants.HTML_DEFAULTPAGEURL);
        }
        return resourceName;
    }

    public void startScriptArchive(Archive ActiveArchive) {

        String formatURL;
        String connectTypeName = (String) core.readDataPool(OOBDConstants.DP_ACTUAL_CONNECTION_TYPE, OOBDConstants.PropName_ConnectTypeBT);

        Class<OOBDPort> value;
        value = systemInterface.getConnectorList().get(connectTypeName);
        try { // tricky: try to call a static method of an interface, where a
            // interface don't have static values by definition..

            java.lang.reflect.Method method = value.getMethod("getUrlFormat", new Class[]{}); // no parameters
            Object instance = null;
            formatURL = (String) method.invoke(instance, new Object[]{}); // no parameters

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Core.class.getName())
                    .log(Level.WARNING,
                            "can't call static methods 'getUrlFormat' of "
                            + value.getName());
            ex.printStackTrace();
            return;
        }

        String connectURL;
        String protocol = "";
        String domain = "";
        String userID = "";
        String serverURL = (String) readDataPool(DP_ACTUAL_REMOTECONNECT_SERVER, "");
        if (!"".equals(serverURL)) {
            String[] parts = serverURL.split("://");
            if (parts.length != 2) {
                //\todo error message JOptionPane.showMessageDialog(null, "The Remote Connect URL is not a valid URL", "Wrong Format", JOptionPane.WARNING_MESSAGE);
                return;
            }
            protocol = parts[0];
            domain = parts[1];
        }

        if (connectTypeName.equalsIgnoreCase("Kadaver")) {
            if (UIHANDLER_WS_NAME.equalsIgnoreCase((String) readDataPool(DP_ACTUAL_UIHANDLER, ""))) {
                userID = (String) readDataPool(DP_REMOTE_CONNECT_ID, "");
                //\todo cancel and error message, if connect id is empty
                userID = Base64Coder.encodeString(userID);
            } else {
                try {
                    Onion answer = getUiIF().requestParamInput(new Onion("{" + "'" + CM_PARAM + "' : [{ " + "'type':'String',"
                            + "'title':'" + Base64Coder.encodeString("Enter the Connect Number") + "',"
                            + "'default':'" + Base64Coder.encodeString((String) readDataPool(OOBDConstants.DP_REMOTE_CONNECT_ID, "")) + "',"
                            + "'message':'" + Base64Coder.encodeString("Please ask the person, who's connecting the dongle to the vehicle for the Connect Number displayed by his software") + "'"
                            + "}]}"));
                    if (answer == null) {
                        //\todo error message JOptionPane.showMessageDialog(null, "For Remote Connect you need to enter the Connect Number", "Missing Value", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    userID = answer.getOnionBase64String("answer");
                    if (userID == null || userID.equals("")) {
                        //\todo error message JOptionPane.showMessageDialog(null, "For Remote Connect you need to enter the Connect Number", "Missing Value", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    writeDataPool(OOBDConstants.DP_REMOTE_CONNECT_ID, userID);
                    userID = Base64Coder.encodeString(userID); // in the later URL the connect

                } catch (JSONException ex) {
                    Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
        }
        connectURL = formatURL;
        connectURL = connectURL.replace("{device}", (String) readDataPool(OOBDConstants.DP_ACTUAL_DONGLE_PORT_ID, ""));
        connectURL = connectURL.replace("{protocol}", protocol);
        connectURL = connectURL.replace("{user}", userID);
        connectURL = connectURL.replace("{urlpath}", domain);

        try {
            Onion cmdOnion = new Onion("{" + "'scriptpath':'" + ActiveArchive.getFilePath().replace("\\", "/") + "'"
                    + ",'connecturl':'" + Base64Coder.encodeString(connectURL) + "'"
                    + "}");
            core.writeDataPool(DP_ACTIVE_ARCHIVE, ActiveArchive);

            startScriptEngine(cmdOnion);
        } catch (JSONException ex) {
            // TODO Auto-generated catch block
            Logger.getLogger(Core.class.getName()).log(Level.WARNING, "JSON creation error with file name:" + ActiveArchive.getFilePath(), ex.getMessage());
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
