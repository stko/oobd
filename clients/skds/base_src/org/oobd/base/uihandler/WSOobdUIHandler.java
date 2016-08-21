/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.uihandler;

import org.oobd.base.*;
import org.oobd.base.OOBDConstants.*;
import org.oobd.base.support.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.visualizer.Visualizer;

import java.util.Map;
import java.io.IOException;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Hashtable;
import static org.oobd.base.OOBDConstants.DP_RUNNING_SCRIPTENGINE;
import static org.oobd.base.OOBDConstants.DP_SCRIPTDIR;
import static org.oobd.base.OOBDConstants.DP_ACTIVE_ARCHIVE;
import static org.oobd.base.OOBDConstants.DP_HTTP_HOST;
import static org.oobd.base.OOBDConstants.DP_HTTP_PORT;
import org.oobd.base.archive.Archive;
import org.oobd.base.archive.Factory;
import org.oobd.base.port.OOBDPort;

// the NanoHTTPD FileUpload dependencies have been commented as they do not work at all and caused proGuard errors
//import fi.iki.elonen.NanoFileUpload;
//import org.apache.commons.fileupload.FileItem;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
/**
 * generic abstract for the implementation of scriptengines
 *
 * @author steffen
 */
// taken from https://github.com/TooTallNate/Java-WebSocket
abstract public class WSOobdUIHandler extends OobdUIHandler {

    protected Onion myStartupParam;
    protected static ChatServer wsServer;
    OOBDHttpServer myWebServer;
    final HashMap<String, ArrayList<Visualizer>> visualizers = new HashMap<String, ArrayList<Visualizer>>();// /<stores
    // all
    // available
    // visalizers
    IFui userInterface;
    public static String ownerEngine;
    public static Message lastOutstandingQuestion = null;
    public static MessagePort wsMsgPort;
    OobdUIHandler myself;

    public static String publicName() {
        /*
         * the abstract class also needs to have this method, because
         * it'wsServer also loaded during dynamic loading, and the empty return
         * string* is the indicator for this abstract class
         */
        return "WebUIHandler";
    }

    public WSOobdUIHandler(String myID, Core myCore, IFsystem mySystem,
            String name) {
        super(myID, myCore, mySystem, name);
        wsMsgPort = msgPort;
        id = myID;
        core = myCore;
        UISystem = mySystem;
        myself = this;
        Logger.getLogger(WSOobdUIHandler.class.getName()).log(Level.CONFIG,
                "Web UIHandler  object created: {0}", id);

    }

    public void start() {
        System.err.println("Start WEB SERVER");

        WebSocketImpl.DEBUG = false;
        try {
            wsServer = new ChatServer(UISystem.getSystemIP(), (int) Core
                    .getSingleInstance().readDataPool(DP_WSOCKET_PORT, 8443));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        wsServer.start();
        System.out.println("ChatServer started on port: " + wsServer.getPort());

        try {
            myWebServer = new OOBDHttpServer();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }

        /*
         * BufferedReader sysin = new BufferedReader(new
         * InputStreamReader(System.in)); while (true) { try { String in =
         * sysin.readLine();
         * 
         * wsServer.sendToAll(in); if (in.equals("exit")) { wsServer.stop();
         * break; } else if (in.equals("restart")) { wsServer.stop();
         * wsServer.start(); break; } } catch (Exception ex) {
         * ex.printStackTrace(); } }
         */
        /*
         * OOBDWebSocketServer webSocketServer = new OOBDWebSocketServer();
         * webSocketServer.setHost("localhost"); webSocketServer.setPort(8443);
         * try { webSocketServer.setKeyStoreResource(new
         * FileResource(OOBDWebSocketServer
         * .class.getResource("/keystore.jks")));
         * webSocketServer.setKeyStorePassword("password");
         * webSocketServer.setKeyManagerPassword("password");
         * webSocketServer.addWebSocket(MyWebSocket.class, "/");
         * webSocketServer.initialize(); webSocketServer.start(); // set
         * userInterface here (somehow..) } catch (Exception ex) {
         * Logger.getLogger(WSOobdUIHandler.class.getName()).log(Level.SEVERE,
         * null, ex); }
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    OobdUIHandler uiHandler = core.getUiHandler();
                    if (uiHandler == myself) {
                        Message thisMsg;
                        while ((thisMsg = myself.getMsgPort().getMsg(-1)) != null) { // just
                            // waiting
                            // and handling
                            // messages
                            lastOutstandingQuestion = actionRequest(thisMsg,
                                    thisMsg.getContent());
                        }

                    }
                }
            }
        }).start();
    }

    /*
     * return the incoming msg, in case we've to wait for answer to reply to
     * that message, otherways null
     */
    Message actionRequest(Message incomingMsg, Onion myOnion) {
        if (myOnion.isType(CM_VISUALIZE)) {
            // userInterface.visualize(myOnion);
            ownerEngine = myOnion.getOnion(OOBDConstants.FN_OWNER)
                    .getOnionString(OOBDConstants.FN_NAME, null);
            wsServer.sendToAll(myOnion.toString());
            return null;
        }
        if (myOnion.isType(CM_VALUE)) {
            // handleValue(myOnion);
            wsServer.sendToAll(myOnion.toString());
            return null;
        }
        if (myOnion.isType(CM_IOINPUT)) {
            openTempFile(myOnion);
            try {
                incomingMsg.setContent(incomingMsg.getContent().setValue(
                        "replyID", incomingMsg.getContent().getInt("msgID")));
            } catch (JSONException ex) {
                Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null,
                        ex);

            }
            incomingMsg.getContent().setValue("answer", new Onion());
            msgPort.replyMsg(incomingMsg, incomingMsg.getContent());
            return null;
        }
        if (myOnion.isType(CM_UPDATE)) {
            try {
				// core.transferMsg(new Message(this, myOnion.getString("to"),
                // myOnion));
                // core.transferMsg(new Message(this, ownerEngine, myOnion));
                myOnion.put("to", ownerEngine);
                core.transferMsg(new Message(this, myOnion.getString("to"),
                        myOnion));

                return null;
            } catch (JSONException ex) {
                Logger.getLogger(WSOobdUIHandler.class.getName())
                        .log(Level.INFO,
                                "failed Update Request, Scriptengine is not running yet");
            }
        }

        if (myOnion.isType(CM_PAGE)) {
			// userInterface.openPage(myOnion.getOnionString("owner"),
            // myOnion.getOnionString("name"), 1, 1);
            ownerEngine = myOnion.getOnionString("owner", null);
            wsServer.sendToAll(myOnion.toString());
            return null;
        }
        if (myOnion.isType(CM_PAGEDONE)) {
			// userInterface.openPageCompleted(
            // myOnion.getOnionString("owner"),
            // myOnion.getOnionString("name"));
            wsServer.sendToAll(myOnion.toString());
            return null;
        }
        if (myOnion.isType(CM_WRITESTRING)) {
            // userInterface.sm(Base64Coder.decodeString(myOnion.getOnionString("data")));
            wsServer.sendToAll(myOnion.toString());
            return null;
        }
        if (myOnion.isType(CM_DIALOG_INFO)) {
            // userInterface.sm(Base64Coder.decodeString(myOnion.getOnionString("data")));
            wsServer.sendToAll(myOnion.toString());
            return null;
        }
        if (myOnion.isType(CM_PARAM) && wsServer != null
                && wsServer.connections() != null
                && !wsServer.connections().isEmpty()) {
            wsServer.sendToAll(myOnion.toString());
            return incomingMsg;
        } else {
            try {
                incomingMsg.setContent(incomingMsg.getContent().setValue(
                        "replyID", incomingMsg.getContent().getInt("msgID")));
            } catch (JSONException ex) {
                Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            incomingMsg.getContent().setValue("answer", "");
            WSOobdUIHandler.wsMsgPort.replyMsg(incomingMsg,
                    incomingMsg.getContent());
        }
        return null;
    }

    @Override
    public void handleMsg() {
        /* on localUIHandler the GUI triggers the update, while on wsHandler,
         * the update is done by an internal thread, started in the start() initialisation
		
         Message thisMsg;
         while ((thisMsg = this.getMsgPort().getMsg(0)) != null) { // just
         // waiting
         // and handling
         // messages
         lastOutstandingQuestion = actionRequest(thisMsg,
         thisMsg.getContent());
         }
         // updateVisualizers();
		 
         */
    }

    /**
     * \brief add generated visualizers to global list
     *
     * several owners (=scriptengines) do have their own visualizers. This is
     * stored in the visualizers hash
     *
     * @param owner who owns the visualizer
     * @param vis the visualizer
     */
    @Override
    public void addVisualizer(String owner, Visualizer vis) {
        if (visualizers.containsKey(owner)) {
            ((ArrayList) visualizers.get(owner)).add(vis);
        } else {
            ArrayList ar = new ArrayList();
            ar.add(vis);
            visualizers.put(owner, ar);
        }
    }

    /**
     * \brief Tells Value to all visualizers of a scriptengine
     *
     * @param value Onion containing value and scriptengine
     *
     */
    public void handleValue(Onion value) {
        try {
            String owner = value.getOnionString("owner/name"); // who's the owner of

            ArrayList affectedVisualizers = visualizers.get(owner); // which
            // visualizers
            // belong to
            // that
            // owner
            if (affectedVisualizers != null) {
                Iterator visItr = affectedVisualizers.iterator();
                while (visItr.hasNext()) {
                    Visualizer vis = (Visualizer) visItr.next();
                    vis.setValue(value); // send the value to all visualisers of
                    // that owner
                }
            }
        } catch (OnionWrongTypeException | OnionNoEntryException ex) {
            Logger.getLogger(Core.class.getName()).log(Level.WARNING,
                    "onion id does not contain name");
        }
    }

    /**
     * \brief Tells Value to all visualizers of a scriptengine
     *
     * @param value Onion containing value and scriptengine
     *
     */
    public void openTempFile(Onion value) {
        InputStreamReader myInputStream = null;
        String myFileName = null;
        try {
            String owner; 
            try {
                owner = value.getOnionString("owner/name"); // who's the
            } catch (OnionWrongTypeException | OnionNoEntryException ex) {
                Logger.getLogger(Core.class.getName()).log(Level.WARNING,
                        "onion id does not contain name");
                return;

            }
            String filePath = Base64Coder.decodeString(value
                    .getOnionString("filepath",""));
            String fileExtension = Base64Coder.decodeString(value
                    .getOnionString("extension",""));
            String fileMessage = Base64Coder.decodeString(value
                    .getOnionString("message",""));
            if (fileMessage.equalsIgnoreCase("html")) {
                myFileName = filePath;
                URL url = new URL(filePath);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int HttpResult = conn.getResponseCode();

                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    myInputStream = new InputStreamReader(
                            conn.getInputStream(), "utf-8");
                } else {
                    System.err.println(conn.getResponseMessage());
                }
            } else {
                if (fileMessage.equalsIgnoreCase("json")) {
                    myFileName = filePath;
                    URL url = new URL(filePath);
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type",
                            "application/json; charset=utf8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestMethod("POST");
                    conn.connect();
                    OutputStream os = conn.getOutputStream();
                    os.write(fileExtension.getBytes("UTF-8"));
                    os.close();
                    int HttpResult = conn.getResponseCode();

                    if (HttpResult == HttpURLConnection.HTTP_OK) {
                        myInputStream = new InputStreamReader(
                                conn.getInputStream(), "utf-8");
                    } else {
                        System.err.println(conn.getResponseMessage());
                    }
                } else {
                    if ("direct".equalsIgnoreCase(fileMessage)) {
                        myFileName = filePath;
                    } else {
                        myFileName = getCore().getSystemIF().doFileSelector(
                                filePath, fileExtension, fileMessage, false);
                    }
                    if (myFileName != null) {
                        try {
                            myFileName = getCore().getSystemIF()
                                    .generateUIFilePath(FT_SCRIPT, myFileName);
                            myInputStream = new FileReader(getCore()
                                    .getSystemIF().generateUIFilePath(
                                            FT_SCRIPT, myFileName));
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(LocalOobdUIHandler.class.getName())
                                    .log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            if (myInputStream != null) {
                OobdScriptengine actEngine = getCore().getScriptEngine();
                getCore().getSystemIF().createEngineTempInputFile(actEngine);

                actEngine.fillTempInputFile(myInputStream);
            } else {
                myFileName = "";
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        }
        value.setValue("result", Base64Coder.encodeString(myFileName));
    }

    /**
     * \brief updates all visualizers
     *
     * to not having several UI refreshes in parallel, update requests are only
     * be collected for each visualizer and only been refreshed when the central
     * core raises this update event.
     *
     *
     *
     */
    public void updateVisualizers() {
        synchronized (visualizers) { // Collection<ArrayList<Visualizer>> c =
            // Collections
            // .synchronizedCollection(visualizers.values());
            Collection<ArrayList<Visualizer>> c = visualizers.values();
			// synchronized (c) {
            // obtain an Iterator for Collection
            Iterator<ArrayList<Visualizer>> itr;

			// iterate through HashMap values iterator
            // run through the 3 update states: 0: start 1: update data 2:
            // finish
            for (int i = 0; i < 3; i++) {
                itr = c.iterator();
                while (itr.hasNext()) {
                    ArrayList<Visualizer> engineVisualizers = itr.next();
                    boolean somethingToRemove = false;
                    Iterator<Visualizer> visItr = engineVisualizers.iterator();
                    // synchronized (visItr) {
                    while (visItr.hasNext()) {
                        Visualizer vis = visItr.next();
                        if (vis != null) {
                            synchronized (vis) {
                                if (vis.getRemoved()) {
                                    somethingToRemove = true;
                                } else {
                                    vis.doUpdate(i);
                                }
                            }
                        }
                    }
                    // }
                    synchronized (engineVisualizers) {
                        if (somethingToRemove) {
                            int del = 0;
                            while (del < engineVisualizers.size()) {
                                if (engineVisualizers.get(del).getRemoved()) {
                                    engineVisualizers.remove(del);
                                }
                                del++;
                            }

                        }
                    }
                }
            }
            // }
        }
    }

}

class ChatServer extends WebSocketServer {

    public ChatServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port));
    }

    public ChatServer(InetSocketAddress address) {
        super(address);
    }

    public ChatServer(InetAddress address, int port) {
        // super(new InetSocketAddress(port));
        super(new InetSocketAddress(address, port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println(conn.getRemoteSocketAddress().getAddress()
                .getHostAddress()
                + " entered the room!");
        conn.send("{\"type\":\"WSCONNECT\",\"script\":\""
                + Core.getSingleInstance().readDataPool(
                        OOBDConstants.DP_RUNNING_SCRIPT_NAME, "") + "\"}");
        conn.send("{\"type\":\"WRITESTRING\" ,\"data\":\""
                + Base64Coder.encodeString("Connected to OOBD") + "\"}");
        Core.getSingleInstance().writeDataPool(
                OOBDConstants.DP_WEBUI_WS_READY_SIGNAL, true);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println(conn + " has left the room!");
        Core.getSingleInstance().writeDataPool(
                OOBDConstants.DP_WEBUI_WS_READY_SIGNAL, false);
        if (WSOobdUIHandler.lastOutstandingQuestion != null) {
            try {
                WSOobdUIHandler.lastOutstandingQuestion
                        .setContent(WSOobdUIHandler.lastOutstandingQuestion
                                .getContent().setValue(
                                        "replyID",
                                        WSOobdUIHandler.lastOutstandingQuestion
                                        .getContent().getInt("msgID")));
            } catch (JSONException ex) {
                Logger.getLogger(Core.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
            WSOobdUIHandler.lastOutstandingQuestion.getContent().setValue(
                    "answer", "");
            WSOobdUIHandler.wsMsgPort.replyMsg(
                    WSOobdUIHandler.lastOutstandingQuestion,
                    WSOobdUIHandler.lastOutstandingQuestion.getContent());
            WSOobdUIHandler.lastOutstandingQuestion = null;
        }

        // Core.getSingleInstance().stopScriptEngine();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("WS from Socket: " + message);
        try {
            Onion webvis = new Onion(message);
            if ("PARAM".equalsIgnoreCase(webvis.getOnionString("type",""))) {
                if (WSOobdUIHandler.lastOutstandingQuestion != null) {
                    try {
                        WSOobdUIHandler.lastOutstandingQuestion
                                .setContent(WSOobdUIHandler.lastOutstandingQuestion
                                        .getContent()
                                        .setValue(
                                                "replyID",
                                                WSOobdUIHandler.lastOutstandingQuestion
                                                .getContent().getInt(
                                                        "msgID")));
                    } catch (JSONException ex) {
                        Logger.getLogger(Core.class.getName()).log(
                                Level.SEVERE, null, ex);
                    }
                    WSOobdUIHandler.lastOutstandingQuestion.getContent()
                            .setValue("answer", webvis);
                    WSOobdUIHandler.wsMsgPort.replyMsg(
                            WSOobdUIHandler.lastOutstandingQuestion,
                            WSOobdUIHandler.lastOutstandingQuestion
                            .getContent());
                    WSOobdUIHandler.lastOutstandingQuestion = null;
                }
            } else {
                Core.getSingleInstance().transferMsg(
                        new Message(Core.getSingleInstance(),
                                OOBDConstants.UIHandlerMailboxName,
                                new Onion(""
                                        + "{"
                                        + "'type':'"
                                        + OOBDConstants.CM_UPDATE
                                        + "',"
                                        + "'vis':'"
                                        + webvis.getOnionString("name","")
                                        + "',"
                                        + "'to':'"
                                        + WSOobdUIHandler.ownerEngine
                                        + "',"
                                        + "'optid':'"
                                        + webvis.getOnionString("optid","")
                                        + "',"
                                        + "'actValue':'"
                                        + webvis.getOnionString("actValue","")
                                        + "',"
                                        + "'updType':"
                                        + Integer.toString(webvis
                                                .getInt("updType")) + "}")));
            }
        } catch (JSONException ex) {
            Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE,
                    null, ex);
        }

    }

    @Override
    public void onFragment(WebSocket conn, Framedata fragment) {
        System.out.println("received fragment: " + fragment);
    }

    public static void main(String[] args) throws InterruptedException,
            IOException {
        WebSocketImpl.DEBUG = true;
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ex) {
        }
        ChatServer s = new ChatServer(port);
        s.start();
        System.out.println("ChatServer started on port: " + s.getPort());

        BufferedReader sysin = new BufferedReader(new InputStreamReader(
                System.in));
        while (true) {
            String in = sysin.readLine();
            s.sendToAll(in);
            if (in.equals("exit")) {
                s.stop();
                break;
            } else if (in.equals("restart")) {
                s.stop();
                s.start();
                break;
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
			// some errors like port binding failed may not be assignable to a
            // specific websocket
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void sendToAll(String text) {
        System.out.println("WS to Socket: " + text);
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }

    /**
     * Close all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     * @throws InterruptedException When socket related I/O errors occur.
     */
    public void closeAllConnects() {
        System.out.println("close all websockets");
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.close();
            }
        }
    }
}

class OOBDHttpServer extends NanoHTTPD {

    public static final String MIME_DEFAULT_BINARY = "application/octet-stream";

    // NanoFileUpload uploader;
    public String uri;

    public Method method;

    public Map<String, String> header;

    public Map<String, String> parms;

    // public Map<String, List<FileItem>> files;
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    /*
     * @SuppressWarnings("serial") private static final Map<String, String>
     * MIME_TYPES = new HashMap<String, String>() {
     * 
     * { put("css", "text/css"); put("htm", "text/html"); put("html",
     * "text/html"); put("xml", "text/xml"); put("xsl", "text/xsl"); put("java",
     * "text/x-java-source, text/java"); put("md", "text/plain"); put("txt",
     * "text/plain"); put("asc", "text/plain"); put("gif", "image/gif");
     * put("jpg", "image/jpeg"); put("jpeg", "image/jpeg"); put("png",
     * "image/png"); put("svg", "image/svg+xml"); put("mp3", "audio/mpeg");
     * put("m3u", "audio/mpeg-url"); put("mp4", "video/mp4"); put("ogv",
     * "video/ogg"); put("flv", "video/x-flv"); put("mov", "video/quicktime");
     * put("swf", "application/x-shockwave-flash"); put("js",
     * "application/javascript"); put("pdf", "application/pdf"); put("doc",
     * "application/msword"); put("ogg", "application/x-ogg"); put("zip",
     * "application/octet-stream"); put("exe", "application/octet-stream");
     * put("class", "application/octet-stream"); put("m3u8",
     * "application/vnd.apple.mpegurl"); put("ts", " video/mp2t"); } };
     */
    public OOBDHttpServer() throws IOException {
		// super(8080);
        // uploader = new NanoFileUpload(new DiskFileItemFactory());
        super(((InetAddress) Core.getSingleInstance().readDataPool(
                DP_HTTP_HOST,
                Core.getSingleInstance().getSystemIF().getSystemIP()))
                .getHostAddress(), (int) Core.getSingleInstance().readDataPool(
                        DP_HTTP_PORT, 8080));

        this.MIME_TYPES = new HashMap<String, String>() {

            {
                put("css", "text/css");
                put("htm", "text/html");
                put("html", "text/html");
                put("xml", "text/xml");
                put("xsl", "text/xsl");
                put("java", "text/x-java-source, text/java");
                put("md", "text/plain");
                put("txt", "text/plain");
                put("asc", "text/plain");
                put("gif", "image/gif");
                put("jpg", "image/jpeg");
                put("jpeg", "image/jpeg");
                put("png", "image/png");
                put("svg", "image/svg+xml");
                put("mp3", "audio/mpeg");
                put("m3u", "audio/mpeg-url");
                put("mp4", "video/mp4");
                put("ogv", "video/ogg");
                put("flv", "video/x-flv");
                put("mov", "video/quicktime");
                put("swf", "application/x-shockwave-flash");
                put("js", "application/javascript");
                put("pdf", "application/pdf");
                put("doc", "application/msword");
                put("ogg", "application/x-ogg");
                put("zip", "application/octet-stream");
                put("exe", "application/octet-stream");
                put("class", "application/octet-stream");
                put("m3u8", "application/vnd.apple.mpegurl");
                put("ts", " video/mp2t");
            }
        };

        start();
        System.out.println("\nRunning! Point your browsers to "
                + Core.getSingleInstance().getSystemIF().getOobdURL() + " \n");
    }

    public static void main(String[] args) {
        try {
            new OOBDHttpServer();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
        this.uri = session.getUri();
        this.method = session.getMethod();
        this.header = session.getHeaders();
        /*
         * if (NanoFileUpload.isMultipartContent(session)) {
         * 
         * }
         */
        System.err.println("url path:" + session.getUri());

        Map<String, String> postFiles = new HashMap<>();

        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                session.parseBody(postFiles);
            } catch (IOException ioe) {
                System.err.println("SERVER INTERNAL ERROR: IOException: "
                        + ioe.getMessage());
            } catch (ResponseException re) {
                System.err.println("SERVER INTERNAL ERROR: IOException: "
                        + re.getMessage());
            }
        }
        // get the POST body
        String postBody = session.getQueryParameterString();
        // or you can access the POST request's parameters
        String postParameter = session.getParms().get("parameter");
        this.parms = session.getParms();

        if (parms.get("theme") != null && !"".equals(parms.get("theme"))) {
            Core.getSingleInstance().writeDataPool(
                    OOBDConstants.DP_WEBUI_ACTUAL_THEME, parms.get("theme"));
        }
        if (parms.get("pgppw") != null && !"".equals(parms.get("pgppw"))) {
            Core.getSingleInstance().getSystemIF()
                    .setUserPassPhrase(parms.get("pgppw"));
        }
        if (parms.get("rcid") != null && !"".equals(parms.get("rcid"))) {
            Core.getSingleInstance().writeDataPool(
                    OOBDConstants.DP_ACTUAL_CONNECT_ID, parms.get("rcid"));
        }
        if (parms.get("connectType") != null
                && !"".equals(parms.get("connectType"))) {
            String connectTypeName = parms.get("connectType");
            Class<OOBDPort> value = Core.getSingleInstance().getConnectorList()
                    .get(connectTypeName);
            if (value != null) {
                Core.getSingleInstance().getUiIF()
                        .transferPreferences2System(connectTypeName);
                Core.getSingleInstance().writeDataPool(
                        OOBDConstants.DP_ACTUAL_CONNECTION_TYPE,
                        connectTypeName);
            }
        }
        if ("/".equals(session.getUri()) || "/#".equals(session.getUri())) {
            Core.getSingleInstance().stopScriptEngine(); // back to start: stop
            // actual script
            // engine
            Core.getSingleInstance().writeDataPool(
                    OOBDConstants.DP_ACTIVE_ARCHIVE, null); // set active
            // Archive to null
            String catalog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<?xml-stylesheet type=\"text/xsl\" href=\"/theme/default/xslt/start.xsl\"?>\n"
                    + "<catalog>\n";
            Hashtable<String, Class> connectionTypes = Core.getSingleInstance()
                    .getConnectorList();
            String actualConnectTypeName = (String) Core.getSingleInstance()
                    .readDataPool(OOBDConstants.DP_ACTUAL_CONNECTION_TYPE, "");
            for (String connectionName : connectionTypes.keySet()) {
                if (connectionName.equalsIgnoreCase(actualConnectTypeName)) {
                    catalog += "<connection selected =\"yes\">"
                            + encodeHTML(connectionName) + "</connection>\n";
                } else {
                    catalog += "<connection>" + encodeHTML(connectionName)
                            + "</connection>\n";
                }
            }
            // prepare a list of available themes
            String actualTheme = (String) Core.getSingleInstance()
                    .readDataPool(OOBDConstants.DP_WEBUI_ACTUAL_THEME,
                            "default");
            File themeDirectory = new File((String) Core.getSingleInstance()
                    .readDataPool(OOBDConstants.DP_WWW_LIB_DIR, "") + "/theme");
            if (themeDirectory.exists()) {
                File[] files = themeDirectory.listFiles();
                for (File dirFile : files) {
                    if (dirFile.isDirectory()) {
                        String dirName = dirFile.getName();
                        if (actualTheme.equalsIgnoreCase(dirName)) {
                            catalog += "<theme selected =\"yes\">\n";
                        } else {
                            catalog += "<theme>\n";
                        }
                        catalog += encodeHTML(dirName);
                        catalog += "</theme>\n";
                    }
                }
            }
            ArrayList<Archive> files = Factory.getDirContent((String) Core
                    .getSingleInstance().readDataPool(
                            OOBDConstants.DP_SCRIPTDIR, null));
            for (Archive file : files) {
                catalog += "<script>\n";
                catalog += "<fileid>" + encodeHTML(file.getID())
                        + "</fileid>\n";
                catalog += "<filename>" + encodeHTML(file.toString())
                        + "</filename>\n";
                catalog += generateOptionalTag(file, "title", "title");
                catalog += generateOptionalTag(file, "name", "name");
                catalog += generateOptionalTag(file, "shortname", "shortname");
                catalog += generateOptionalTag(file, "description",
                        "description");
                catalog += generateOptionalTag(file, "version", "version");
                catalog += generateOptionalTag(file, "copyright", "copyright");
                catalog += generateOptionalTag(file, "author", "author");
                catalog += generateOptionalTag(file, "security", "security");
                catalog += generateOptionalTag(file, "date", "date");
                catalog += generateOptionalTag(file, "icon", "icon");
                catalog += generateOptionalTag(file, "screenshot", "screenshot");
                catalog += generateOptionalTag(file, "url", "url");
                catalog += generateOptionalTag(file, "email", "email");
                catalog += generateOptionalTag(file, "phone", "phone");
                catalog += "</script>\n";
            }
            catalog += "</catalog>";
            return newFixedLengthResponse(Response.Status.OK, "text/xml",
                    catalog);
        } 
        else if ("/settings.json".equals(session.getUri())){
            return newFixedLengthResponse(Response.Status.OK, "text/xml",
                    "{\"properties\":{\"make\":{\"type\":\"string\",\"enum\":[\"Tooooyota\",\"BMW\",\"Honda\",\"Ford\",\"Chevy\",\"VW\"]},\"model\":{\"type\":\"string\"},\"year\":{\"type\":\"integer\",\"enum\":[1995,1996,1997,1998,1999,2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014],\"default\":2008}}}");
        }else {
            InputStream myFileStream = Core
                    .getSingleInstance()
                    .getSystemIF()
                    .generateResourceStream(OOBDConstants.FT_WEBPAGE,
                            session.getUri());
            String mimeType = getMimeTypeForFile(session.getUri());
            /*
             * if (myFileStream != null && myFileStream instanceof
             * FileInputStream ) { mimeType = getMimeTypeForFile((String)
             * Core.getSingleInstance
             * ().readDataPool(OOBDConstants.DP_LAST_OPENED_PATH, mimeType)); }
             */
            if (myFileStream != null) {
                mimeType = getMimeTypeForFile((String) Core.getSingleInstance()
                        .readDataPool(OOBDConstants.DP_LAST_OPENED_PATH,
                                mimeType));
                if (WSOobdUIHandler.wsServer != null
                        && "text/html".equalsIgnoreCase(mimeType)) { // we load
                    // a new
                    // page,
                    // so we
                    // have
                    // to
                    // kill
                    // the
                    // websocket
                    // to
                    // make
                    // sure
                    // the
                    // communication
                    // goes
                    // to
                    // the
                    // new
                    // page
                    WSOobdUIHandler.wsServer.closeAllConnects();
                }
                System.err.println("load " + session.getUri() + " as mimetype "
                        + mimeType);
                return newChunkedResponse(Response.Status.OK, mimeType,
                        myFileStream);
            } else {

                return newFixedLengthResponse(Response.Status.NOT_FOUND,
                        NanoHTTPD.MIME_HTML, "OOBD reports: File not found");
            }
        }
    }

    String generateOptionalTag(Archive file, String key, String tag) {
        String result;
        result = file.getProperty(key, "");
        if (!"".equals(result)) {
            result = "<" + tag + ">" + encodeHTML(result) + "</" + tag + ">\n";
        }
        return result;
    }

    String encodeHTML(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
