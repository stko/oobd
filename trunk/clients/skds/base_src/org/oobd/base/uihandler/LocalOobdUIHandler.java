/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.uihandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.oobd.base.*;
import org.oobd.base.support.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.visualizer.Visualizer;

/**
 * generic abstract for the implementation of scriptengines
 * @author steffen
 */
abstract public class LocalOobdUIHandler extends OobdUIHandler {

    final HashMap<String, ArrayList<Visualizer>> visualizers = new HashMap<String, ArrayList<Visualizer>>();// /<stores all available visalizers
    IFui userInterface;

    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "";
    }

    public LocalOobdUIHandler(String myID, Core myCore, IFsystem mySystem, String name) {
        super(myID, myCore, mySystem, name);
        userInterface = myCore.getUiIF();

        Logger.getLogger(LocalOobdUIHandler.class.getName()).log(Level.CONFIG, "Local UIHandler  object created: {0}", id);

    }

    boolean actionRequest(Onion myOnion) {
        System.out.println("UI Handler action request type:" + myOnion.getOnionString("type"));
        try {
            if (myOnion.isType(CM_VISUALIZE)) {
                userInterface.visualize(myOnion);
                return false;
            }
            if (myOnion.isType(CM_VALUE)) {
                handleValue(myOnion);
                return false;
            }
            if (myOnion.isType(CM_UPDATE)) {
                core.transferMsg(new Message(this, myOnion.getString("to"), myOnion));

                return false;
            }

            if (myOnion.isType(CM_PAGE)) {
                userInterface.openPage(myOnion.getOnionString("owner"),
                        myOnion.getOnionString("name"), 1, 1);
                return false;
            }
            if (myOnion.isType(CM_PAGEDONE)) {
                userInterface.openPageCompleted(
                        myOnion.getOnionString("owner"),
                        myOnion.getOnionString("name"));
                return false;
            }
            if (myOnion.isType(CM_WRITESTRING)) {
                userInterface.sm(Base64Coder.decodeString(myOnion.getOnionString("data")));
                return false;
            }
            if (myOnion.isType(CM_PARAM)) {
                userInterface.requestParamInput(myOnion);
                return false;
            }
        } catch (org.json.JSONException e) {
            Logger.getLogger(Core.class.getName()).log(Level.SEVERE,
                    "JSON exception..");
            return false;
        }
        return false;
    }

    public void handleMsg() {
        Message thisMsg;
        System.out.println("Looking into UI Handler msg quere");
        while ((thisMsg = this.getMsgPort().getMsg(0)) != null) { // just waiting
            // and handling
            // messages
            System.out.println("UI Handler: msg found");
            if (actionRequest(thisMsg.getContent()) == true) {
                try {
                    thisMsg.setContent(thisMsg.getContent().setValue("replyID",
                            thisMsg.getContent().getInt("msgID")));
                } catch (JSONException ex) {
                    Logger.getLogger(Core.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
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
     * @param owner
     *            who owns the visualizer
     * @param vis
     *            the visualizer
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
     * @param value
     *            Onion containing value and scriptengine
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
