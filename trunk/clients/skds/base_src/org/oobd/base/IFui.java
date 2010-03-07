/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

/**
 * Interface to allow the oobd core to talk to the graphical interface
 * @author steffen
 */
public interface IFui  {

    /**
     * just a test
     * @todo remove test
     * @param msg
     */
    public void sm(String msg);

    /**
     * register the core object to the UserInterface for calls of core methods
     * @param Core the core object
     */
    public void registerOobdCore(Core core);

    /**
     * tells the UserInterface about the existence of a scriptengine, e.g. to add this to a selection menu
     * @param id the key of the scriptengines hash array where the loaded instances are been stored
     * @param visibleName
     */
    public void announceScriptengine(String id, String visibleName);

    /**
     * this method is UI- depending and returns the class which comes closes to the requested visualizer type
     *
     * @param visualizerType
     * @param theme
     * @return
     */
    public Class getVisualizerClass(String visualizerType, String theme);


    /**
     * Adds a new canvas to the pane of the given ScriptEngine
     * @param seID ID of the ScriptEngine
     * @param Name of the new canvas
     */
    public void addCanvas(String seID, String Name);


    /**
     * Deletes a canvas from the pane of the given ScriptEngine
     * @param seID ID of the ScriptEngine
     * @param Name of the new canvas
     */
    public void delCanvas(String seID, String Name);
}
