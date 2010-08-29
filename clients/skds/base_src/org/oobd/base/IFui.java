
package org.oobd.base;

import org.oobd.base.support.Onion;

/**
 * \brief Interface for the application object who does the graphical User Interface to communicate with the OOBD core
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
     * this method is UI- depending and places a visualizer onto an canvas
     *
     * @param myOnion
     */
                public void visualize(Onion myOnion);

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
