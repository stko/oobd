/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.uihandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.oobd.base.*;
import org.oobd.base.support.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.base.visualizer.Visualizer;

/**
 * generic abstract for the implementation of scriptengines
 *
 * @author steffen
 */
abstract public class LocalOobdUIHandler extends OobdUIHandler {

    final HashMap<String, ArrayList<Visualizer>> visualizers = new HashMap<String, ArrayList<Visualizer>>();// /<stores all available visalizers
    IFui userInterface;

    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "LocalOobdUIHandler";
    }

    public LocalOobdUIHandler(String myID, Core myCore, IFsystem mySystem, String name) {
        super(myID, myCore, mySystem, name);
        userInterface = myCore.getUiIF();
        Core.getSingleInstance().writeDataPool(OOBDConstants.DP_WEBUI_WS_READY_SIGNAL, true);
        Logger.getLogger(LocalOobdUIHandler.class.getName()).log(Level.CONFIG, "Local UIHandler  object created: {0}", id);

    }

    Onion actionRequest(Onion myOnion) {
        try {
            if (myOnion.isType(CM_VISUALIZE)) {
                userInterface.visualize(myOnion);
                return null;
            }
            if (myOnion.isType(CM_VALUE)) {
                handleValue(myOnion);
                return null;
            }
            if (myOnion.isType(CM_IOINPUT)) {
                openTempFile(myOnion);
                return new Onion();
            }
            if (myOnion.isType(CM_UPDATE)) {
                core.transferMsg(new Message(this, myOnion.getString("to"), myOnion));

                return null;
            }

            if (myOnion.isType(CM_PAGE)) {
                userInterface.openPage(myOnion.getOnionString("owner"),
                        myOnion.getOnionString("name"), 1, 1);
                return null;
            }
            if (myOnion.isType(CM_PAGEDONE)) {
                userInterface.openPageCompleted(
                        myOnion.getOnionString("owner"),
                        myOnion.getOnionString("name"));
                return null;
            }
            if (myOnion.isType(CM_WRITESTRING)) {
                String modifier = myOnion.getOnionString("modifier"); // an absolutely work around. Here's is why: https://github.com/stko/oobd/issues/164
                if (modifier == null) {
                    modifier = "";
                }
                userInterface.sm(Base64Coder.decodeString(myOnion.getOnionString("data")), Base64Coder.decodeString(modifier));
                return null;
            }
            if (myOnion.isType(CM_PARAM)) {
                return userInterface.requestParamInput(myOnion);
            }
        } catch (org.json.JSONException e) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "JSON exception..");
            return null;
        }
        return null;
    }

    public void handleMsg() {
        Message thisMsg;
        while ((thisMsg = this.getMsgPort().getMsg(0)) != null) { // just waiting
            // and handling
            // messages
            Onion answer = actionRequest(thisMsg.getContent());
            if (answer != null) {
                try {
                    thisMsg.setContent(thisMsg.getContent().setValue("replyID",
                            thisMsg.getContent().getInt("msgID")));
                } catch (JSONException ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.SEVERE, null, ex);

                }
                thisMsg.getContent().setValue("answer", answer);
                msgPort.replyMsg(thisMsg, thisMsg.getContent());
            }

        }
        updateVisualizers();
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
        String owner = value.getOnionString("owner/name"); // who's the owner of
        // that value?
        if (owner == null) {
            Logger.getLogger(Core.class.getName()).log(Level.WARNING,
                    "onion id does not contain name");
        } else {
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
            String owner = value.getOnionString("owner/name"); // who's the owner of
            // that value?
            if (owner == null) {
                Logger.getLogger(Core.class.getName()).log(Level.WARNING,
                        "onion id does not contain name");
                return;
            }
            String filePath = Base64Coder.decodeString(value.getOnionString("filepath"));
            String fileExtension = Base64Coder.decodeString(value.getOnionString("extension"));
            String fileMessage = Base64Coder.decodeString(value.getOnionString("message"));
            if (fileMessage.equalsIgnoreCase("html")) {
                myFileName = filePath;
                URL url = new URL(filePath);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int HttpResult = conn.getResponseCode();

                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    myInputStream = new InputStreamReader(conn.getInputStream(), "utf-8");
                } else {
                    System.err.println(conn.getResponseMessage());
                }
            } else {
                if (fileMessage.equalsIgnoreCase("json")) {
                    myFileName = filePath;
                    URL url = new URL(filePath);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestMethod("POST");
                    conn.connect();
                    OutputStream os = conn.getOutputStream();
                    os.write(fileExtension.getBytes("UTF-8"));
                    os.close();
                    int HttpResult = conn.getResponseCode();

                    if (HttpResult == HttpURLConnection.HTTP_OK) {
                        myInputStream = new InputStreamReader(conn.getInputStream(), "utf-8");
                    } else {
                        System.err.println(conn.getResponseMessage());
                    }
                } else {
                    if ("direct".equalsIgnoreCase(fileMessage)) {
                        myFileName = filePath;
                    } else {
                        myFileName = getCore().getSystemIF().doFileSelector(filePath, fileExtension, fileMessage, false);
                    }
                    if (myFileName != null) {
                        try {
                            myFileName=getCore().getSystemIF().generateUIFilePath(FT_SCRIPT, myFileName);
                            myInputStream = new FileReader(getCore().getSystemIF().generateUIFilePath(FT_SCRIPT, myFileName));
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(LocalOobdUIHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }

            if (myInputStream != null) {
                OobdScriptengine actEngine = getCore().getScriptEngine(owner);
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
