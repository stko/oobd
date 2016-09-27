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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.jmdns.*;

import org.json.JSONException;
import static org.oobd.base.OOBDConstants.UDP_PORT;
import org.oobd.base.archive.Archive;
import org.oobd.base.support.Onion;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.db.OobdDB;
import org.oobd.base.connector.OobdConnector;
import org.oobd.base.port.ComPort_Kadaver;
import org.oobd.base.port.ComPort_Telnet;
import org.oobd.base.port.ComPort_Win;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.protocol.OobdProtocol;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.support.OnionWrongTypeException;
import org.oobd.base.uihandler.*;
import org.oobd.crypt.AES.EncodeDecodeAES;
import org.oobd.crypt.AES.PassPhraseProvider;

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
    public static Settings settings = null;
    private String userPassPhrase = "";
    String oobdMacAddress = "-";
    InetAddress oobdIPAddress = null;
    String webRootDir = "";
    String webLibraryDir = "";
    JmDNS jmdns;
    JmDNS jmdnsListener;
    Thread coreThread = null;

    /**
     * \brief The Application creates one single instance of the core class
     * \ingroup init
     *
     * @param mySystemInterface reference to the actual application and runtime
     * enviroment, on which OOBD is actual running on
     * @param name of the Plugin, just for debugging
     * @throws org.oobd.base.Settings.IllegalSettingsException
     *
     */
    public Core(IFsystem mySystemInterface, String name) throws Settings.IllegalSettingsException {
        super(name);
        System.out.println("Java Runtime Version:" + System.getProperty("java.version"));
        if (thisInstance != null) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "Core Creator called more as once!!");
        }
        thisInstance = this;
        id = CoreMailboxName;
        systemInterface = mySystemInterface;
        Settings.init(mySystemInterface);
        Settings.transferSettings(systemInterface.loadPreferences(), true);
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
        startjmDNSDiscovery();
        //jmDNSServiceListen();
        String connectTypeName = Settings.getString(OOBDConstants.PropName_ConnectType, OOBDConstants.PropName_ConnectTypeBT);
        Settings.writeDataPool(DP_ACTUAL_CONNECTION_TYPE, connectTypeName);

        Settings.transferPreferences2System(connectTypeName);

        //-- userInterface.registerOobdCore(this); // Anounce itself at the Userinterface
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
                                    Logger.getLogger(Core.class.getName()).log(Level.CONFIG, "Interface announcement: Scriptengine-ID: {0} visibleName:{1}", new Object[]{element, result
                                    });
                                }
                                break;
                            case "uihandler":
                                uiHandlers.put(element, value);
                                if (result.length() != 0) {
                                    Logger.getLogger(Core.class.getName()).log(Level.CONFIG, "Interface announcement: UIHandler-ID: {0} visibleName:{1}", new Object[]{element, result
                                    });
                                    if (Settings.getString(OOBDConstants.PropName_UIHander, UIHANDLER_WS_NAME).equalsIgnoreCase(result)) {
                                        Onion onion = new Onion();
                                        String seID = createUIHandler(element, onion);

                                        startUIHandler(seID, onion);
                                    }

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
        coreThread = new Thread(this);
        coreThread.start();
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
     * a static help routine which returns the actual running Thread of the Core
     * class
     *
     * @return Thread
     */
    public Thread getThread() {
        return coreThread;
    }

     /**
     * transfers JSON formated preference values into the global settings
     * @param jsonPref JSON formatted preferences
     * @param force forces the data to load without checking for valid admin password
     *
     * @return Thread
     */
    public void  setPrefs(String jsonPrefs, boolean force) throws Settings.IllegalSettingsException {
        Settings.transferSettings(jsonPrefs, force);
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
        return (OobdScriptengine) Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null);
    }

    /**
     * supply Objects which binds to system specific hardware
     *
     * @param typ
     * @return a hardware handle of the requested service_type or nil
     */
    public Object supplyHardwareHandle(Onion typ) {

        try {
            String actualConnectionType = (String) Settings.readDataPool(DP_ACTUAL_CONNECTION_TYPE, "");
            String connectURL = typ.getOnionBase64String("connecturl");
            String[] parts = connectURL.split("://");
            if (parts.length != 2 || "".equals(actualConnectionType)) {
                return null;
            }
            String protocol = parts[0];
            String host = parts[1];
            String proxyHost = Settings.getString(actualConnectionType + "_" + OOBDConstants.PropName_ProxyHost, "");
            int proxyPort = Settings.getInt(actualConnectionType + "_" + OOBDConstants.PropName_ProxyPort, 0);
            if (protocol.toLowerCase().startsWith("ws")) {
                try {
                    Proxy thisProxy = Proxy.NO_PROXY;
                    if (!"".equals(proxyHost) && proxyPort != 0) {
                        thisProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                        System.setProperty("https.proxyHost", proxyHost);
                        System.setProperty("https.proxyPort", Integer.toString(proxyPort));

                    }
                    return new ComPort_Kadaver(new URI(connectURL), thisProxy, proxyHost, proxyPort);

                } catch (URISyntaxException ex) {
                    Logger.getLogger(Core.class.getName()).log(Level.SEVERE, "could not open Websocket Interface", ex);
                    return null;

                }
            } else if ("telnet".equalsIgnoreCase(protocol)) {
                return new ComPort_Telnet(connectURL);
            } else if ("serial".equalsIgnoreCase(protocol)) {
                String osname = System.getProperty("os.name", "").toLowerCase();
                Logger.getLogger(Core.class.getName()).log(Level.CONFIG, "OS detected: " + osname);

                try {
                    return systemInterface.supplyHardwareHandle(typ);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            return null;
        }
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
        OobdScriptengine thisEngine = (OobdScriptengine) Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null);
        if (thisEngine != null) {
            thisEngine.close();
        }
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
        Settings.writeDataPool(DP_LAST_CREATED_SCRIPTENGINE, o);

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
            OobdScriptengine scriptEngine = (OobdScriptengine) Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null);
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
            transferMsg(new Message(Core.getSingleInstance(),
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
        while (keepRunning == true) {
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
        stopScriptEngine();
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
        OobdScriptengine o = (OobdScriptengine) Settings.readDataPool(DP_LAST_CREATED_SCRIPTENGINE, null);
        if (o == null) {
            o = core.createScriptEngine("ScriptengineLua", onion);
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
        while (Settings.readDataPool(DP_RUNNING_SCRIPTENGINE, null) != null) {
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

    /**
     * checks, if the URL points to a lbc ActiveArchive. if yes, starts it and
     * returns the path to the start page read out of the manifest ActiveArchive
     * of that script
     *
     */
    public String startScriptEngineByURL(String resourceName) {

        ArrayList<Archive> files = (ArrayList<Archive>) Settings.readDataPool(DP_LIST_OF_SCRIPTS, null);
        Archive activeArchive = (Archive) Settings.readDataPool(DP_ACTIVE_ARCHIVE, null);
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
                    Settings.writeDataPool(DP_ACTIVE_ARCHIVE, file);
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

            Settings.writeDataPool(DP_ACTIVE_ARCHIVE, activeArchive);
            System.out.println("start scriptengine for " + scriptPath);
            startScriptArchive(activeArchive);
            return activeArchive.getProperty(MANIFEST_STARTPAGE, OOBDConstants.HTML_DEFAULTPAGEURL);
        }
        return resourceName;
    }

    public void startScriptArchive(Archive ActiveArchive) {

        String formatURL;
        String connectTypeName = (String) Settings.readDataPool(DP_ACTUAL_CONNECTION_TYPE, PropName_ConnectTypeBT);

        Class<OOBDPort> value;
        value = getConnectorList().get(connectTypeName);
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
        String connectID = (String) Settings.readDataPool(DP_ACTUAL_CONNECT_ID, "");
        //\todo cancel and error message, if connect id is empty
        String serverURL = (String) Settings.readDataPool(DP_ACTUAL_REMOTECONNECT_SERVER, "");
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
            connectID = Base64Coder.encodeString(connectID); //in Kadaver the remoteId is base64 coded
        }
        connectURL = formatURL;
        connectURL = connectURL.replace("{protocol}", protocol);
        connectURL = connectURL.replace("{connectid}", connectID);
        connectURL = connectURL.replace("{urlpath}", domain);

        try {
            Onion cmdOnion = new Onion("{" + "'scriptpath':'" + ActiveArchive.getFilePath().replace("\\", "/") + "'"
                    + ",'connecturl':'" + Base64Coder.encodeString(connectURL) + "'"
                    + "}");
            Settings.writeDataPool(DP_ACTIVE_ARCHIVE, ActiveArchive);

            startScriptEngine(cmdOnion);
        } catch (JSONException ex) {
            // TODO Auto-generated catch block
            Logger.getLogger(Core.class.getName()).log(Level.WARNING, "JSON creation error with file name:" + ActiveArchive.getFilePath(), ex.getMessage());
        }
    }

    public DatagramSocket getUDPBroadcastSocket() {
        try {
            DatagramSocket socket = null;
            if (socket == null) {
                socket = new DatagramSocket(UDP_PORT);
            }
            socket.setBroadcast(true);
            return socket;
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * \brief supplies Class Array of available Connect classses
     *
     * @param typ on
     * @return a list of connection types classes
     */
    public Hashtable<String, Class> getConnectorList() {
        Hashtable<String, Class> connectClasses = new Hashtable<>();
        connectClasses.put(OOBDConstants.PropName_ConnectTypeBT, ComPort_Win.class);
        connectClasses.put(OOBDConstants.PropName_ConnectTypeRemoteConnect,
                ComPort_Kadaver.class);
        connectClasses.put(OOBDConstants.PropName_ConnectTypeTelnet,
                ComPort_Telnet.class);

        return connectClasses;
    }

    /**
     * \brief creates a new temporary file
     *
     * @param the scriptengine, who's looking for a new data gain
     */
    public void createEngineTempInputFile(OobdScriptengine eng) {
        File f;

        try {
            //do we have to delete a previous first?

            eng.removeTempInputFile();
            // creates temporary file
            f = File.createTempFile("oobd", null, null);

            // deletes file when the virtual machine terminate
            f.deleteOnExit();

            eng.setTempInputFile(f);

        } catch (Exception e) {
            // if any error occurs
            Logger.getLogger(Core.class.getName()).log(Level.WARNING, "could not create temp file! ", e);
        }

    }

    /**
     * \brief returns the (secret) application pass phrase for data decoding
     *
     * @return the application pass phrase
     */
    public char[] getAppPassPhrase() {
        return PassPhraseProvider.getPassPhrase();
    }

    /**
     * \brief returns the (secret) user pass phrase for data decoding
     *
     * @return the user pass phrase
     */
    public String getUserPassPhrase() {
        if (userPassPhrase.equals("")) {
            return "";
        } else {
            try {
                return EncodeDecodeAES.decrypt(new String(
                        getAppPassPhrase()), userPassPhrase);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    /**
     * \brief stores the (secret) user pass phrase for later data decoding
     *
     * @param the user pass phrase
     */
    public void setUserPassPhrase(String upp) {
        try {
            userPassPhrase = EncodeDecodeAES.encrypt(new String(
                    getAppPassPhrase()), upp);
        } catch (Exception e) {
            e.printStackTrace();
            userPassPhrase = "";
        }
    }

    /**
     * \brief reports the URL OOBD runs on
     *
     * @return the URL of the OOBD build in webserver
     */
    public String getOobdURL() {
        InetAddress ip;
        String hostname;
        ip = getSystemIP();
        hostname = ip.getHostName();
        System.out.println("Your current Hostname : " + hostname);
        return "http://" + hostname + ":" + ((Integer) Settings.readDataPool(DP_HTTP_PORT, 8080)).toString();
    }

    /**
     * \brief reports the MAC address OOBD runs on
     *
     * @return the MAC address of local side ip address OOBD runs on
     */
    public String getMACAddress() {
        if (oobdMacAddress.equals("-")) {//not initialized? Then do it first
            getSystemIP();
        }
        return oobdMacAddress;
    }

    /**
     * \brief reports the ip address OOBD runs on
     *
     * @return the local side ip address of the device OOBD runs on
     */
    public InetAddress getSystemIP() {
        if (oobdIPAddress != null) {
            return oobdIPAddress;
        }
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    oobdIPAddress = (InetAddress) ee.nextElement();
                    System.out.println(oobdIPAddress.getHostAddress());
                    if (oobdIPAddress.isSiteLocalAddress()) {
                        System.out.println("Your current local side IP address : " + oobdIPAddress);
                        byte[] myMac = n.getHardwareAddress();
                        oobdMacAddress = "";
                        for (int i = 0; i < myMac.length; i++) {
                            if (!oobdMacAddress.equals("")) {
                                oobdMacAddress += ":";
                            }
                            oobdMacAddress += String.format("%1$02X", myMac[i]);
                        }
                        System.out.println("Your current MAC address : " + oobdMacAddress);
                        return oobdIPAddress;
                    }
                }
            }
            oobdIPAddress = InetAddress.getLocalHost();
            System.out.println("Your current IP address : " + oobdIPAddress);
            return oobdIPAddress;
        } catch (UnknownHostException ex) {
            oobdMacAddress = "-";
            oobdIPAddress = null;
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            oobdMacAddress = "-";
            oobdIPAddress = null;
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
     replaces leading directory alias against their physical location
     */
    String mapDirectory(String[] mapDir, String path) {
        int i = 0;
        while (i < mapDir.length) {
            if (path.toLowerCase().startsWith("/" + mapDir[i].toLowerCase() + "/")) {
                path = path.substring(mapDir[i].length() + 2);
                if (mapDir[i].toLowerCase().equalsIgnoreCase("theme") && path.toLowerCase().startsWith("default/")) { //map the theme folder to  the actual theme
                    path = Settings.readDataPool(DP_WEBUI_ACTUAL_THEME, "default") + "/" + path.substring("default/".length());
                }
                return mapDir[i + 1] + path;
            }
            i += 2;
        }
        return null;
    }

    /**
     * \brief supplies a resource as Inputstream
     *
     * @param pathID Indentifier of what service_type of file to open, as this
     * drives where to search for
     * @param ResourceName Name of the wanted resource
     * @return InputStream for that resource
     */
    public InputStream generateResourceStream(int pathID, String resourceName)
            throws java.util.MissingResourceException {
        Logger.getLogger(Core.class.getName()).log(Level.INFO, "Try to load: " + resourceName
                + " with path ID : " + pathID);
        InputStream resource = null;
        Archive scriptArchive = (Archive) Settings.readDataPool(DP_ACTIVE_ARCHIVE, null);
        try {
            switch (pathID) {
                case OOBDConstants.FT_WEBPAGE:
                    webRootDir = (String) Settings.readDataPool(DP_SCRIPTDIR, "") + "/";
                    webLibraryDir = (String) Settings.readDataPool(DP_WWW_LIB_DIR, "") + "/";
                    // in case the resource name points to a "executable" scriptengine, the engine get started 
                    // and the resourcename is corrected to the html start page to be used
                    resourceName = core.startScriptEngineByURL(resourceName);
                    scriptArchive = (Archive) Settings.readDataPool(DP_ACTIVE_ARCHIVE, null);
                    String mapping = mapDirectory(new String[]{"libs", webLibraryDir + "libs/", "theme", webLibraryDir + "theme/"}, resourceName);
                    if (mapping != null) { //its a mapped request
                        Settings.writeDataPool(DP_LAST_OPENED_PATH, mapping);
                        return new FileInputStream(getSystemIF().getSystemDefaultDirectory(false, mapping));
                    }
                    // let's see, if it's a passthrough request;
                    String[] parts = resourceName.split("/", 3); //remember that resourceName starts with /, so the first split part should be empty
                    if (parts.length > 2) {
                        ArrayList<Archive> files = (ArrayList<Archive>) Settings.readDataPool(DP_LIST_OF_SCRIPTS, null);
                        if (files != null) {
                            for (Archive file : files) {
                                if (parts[1].equals(file.getFileName())) {
                                    Settings.writeDataPool(DP_LAST_OPENED_PATH, parts[2]);
                                    return file.getInputStream(parts[2]);
                                }
                            }
                        }
                    }

                    if (scriptArchive != null) { // if everything else fails, try to load the file out of the package
                        Settings.writeDataPool(DP_LAST_OPENED_PATH, resourceName);
                        return scriptArchive.getInputStream(resourceName);
                    }

                    break;
                case OOBDConstants.FT_PROPS:
                case OOBDConstants.FT_RAW:
                case OOBDConstants.FT_KEY_IMPORT:
                    resource = new FileInputStream(getSystemIF().getSystemDefaultDirectory(false,
                            resourceName));
                    Logger.getLogger(Core.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_DATABASE:
                    if (scriptArchive != null) {
                        resource = scriptArchive.getInputStream(resourceName);
                    }
                    break;
                case OOBDConstants.FT_SCRIPT:
                    // save actual script directory to buffer it for later as webroot directory
//           resource = scriptArchive.getInputStream(scriptArchive.getProperty(OOBDConstants.MANIFEST_SCRIPTNAME, OOBDConstants.MANIFEST_SCRIPTNAME_DEFAULT));
                    resource = scriptArchive.getInputStream(scriptArchive.getProperty(OOBDConstants.MANIFEST_SCRIPTNAME, resourceName));
                    Logger.getLogger(Core.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_KEY:
                    resource = new FileInputStream(getSystemIF().getSystemDefaultDirectory(true, resourceName));
                    Logger.getLogger(Core.class.getName()).log(Level.INFO, "Key File "
                            + resourceName + " loaded");
                    break;

                default:
                    throw new java.util.MissingResourceException("Resource not known",
                            "OOBDApp", resourceName);

            }

        } catch (Exception e) {
            Logger.getLogger(Core.class.getName()).log(Level.INFO, "generateResourceStream: File " + resourceName + " not loaded");
        }
        return resource;
    }

    private void startjmDNSDiscovery() {
        //for usage instructions of jnDNS, see http://home.heeere.com/tech-androidjmdns.html
        new Thread(new Runnable() {
             ServiceListener listener = null;
            @Override
           public void run() {
                try {
                    jmdns = JmDNS.create();
                    // ServiceInfo service_info =  ServiceInfo.create("_http._tcp.local.", "foo._http._tcp.local.", 1234, 0, 0, "path=index.html")
                    //  ServiceInfo service_info = ServiceInfo.create("_http._tcp.", "OOBDesk", 8080, "path=/")
                    // ServiceInfo service_info =   ServiceInfo.create("_http._tcp.", "OOBDesk", 8080, 0,0,values)
                    String service_type = "_http._tcp.";
                    //       String service_name = "http://www.mycompany.com/xyz.html";
                    //String service_name = "OOBD-" + jmdns.getHostName();
                    //String service_name = Core.getSingleInstance().getSystemIF().getOobdURL();
                    String service_name = "OOBD DaaS (" + getMACAddress() + ")";
                    int service_port = (int) Settings.readDataPool(DP_HTTP_PORT, 8080);
                    ServiceInfo service_info = ServiceInfo.create(service_type, service_name, service_port, "");
                    jmdns.registerService(service_info);
                    
                    
                  jmdns.addServiceListener(service_type, listener = new ServiceListener() {
                public void serviceResolved(ServiceEvent ev) {
                    System.out.println("Service resolved: "
                            + ev.getInfo().getQualifiedName()
                            + " port:" + ev.getInfo().getPort());
                }

                public void serviceRemoved(ServiceEvent ev) {
                    System.out.println("Service removed: " + ev.getName());
                }

                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again
                    // (after the first search)
                    jmdnsListener.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });
             
                    
                    
                    System.out.println("jmDNSDiscovery started");
                } catch (IOException ex) {
                    Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();

    }

    private void jmDNSServiceListen() {
        try {
            String service_type = "_http._tcp.";
            ServiceListener listener = null;
            jmdnsListener = JmDNS.create();
            jmdnsListener.addServiceListener(service_type, listener = new ServiceListener() {
                public void serviceResolved(ServiceEvent ev) {
                    System.out.println("Service resolved: "
                            + ev.getInfo().getQualifiedName()
                            + " port:" + ev.getInfo().getPort());
                }

                public void serviceRemoved(ServiceEvent ev) {
                    System.out.println("Service removed: " + ev.getName());
                }

                public void serviceAdded(ServiceEvent event) {
                    // Required to force serviceResolved to be called again
                    // (after the first search)
                    jmdnsListener.requestServiceInfo(event.getType(), event.getName(), 1);
                }
            });

            // jmdns.removeServiceListener(service_type, listener);
            // jmdns.close();
        } catch (IOException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null, ex);
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
