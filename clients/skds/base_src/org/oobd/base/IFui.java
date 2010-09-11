
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
     * \brief register the core instance to the UserInterface
     * 
     * \ingroup init
     * 
     * @param Core the core object
     */
    public void registerOobdCore(Core core);

    /**
     * \brief tells the UserInterface about the existence of a scriptengine
     * \ingroup init
     * 
     * During startup, the core looks for all scriptengines the user might want to use and these
     * through this function to the user interface, e.g. to add this to a selection menu
     * @param id the key of the scriptengines hash array where the loaded instances are been stored
     * @param visibleName
     */
    public void announceScriptengine(String id, String visibleName);

    /**
     * \brief search for a specific vizualizer type
     * \ingroup visualization
     * this method is UI- depending and returns the class which comes closest to the requested visualizer type.
     * 
     * 
     * @param visualizerType
     * @param theme
     * @return Visulizerclass, if found
     */
    public Class getVisualizerClass(String visualizerType, String theme);


    /**
     * \brief places a visualizer onto an canvas
     * \ingroup visualization
     *
     * puts the visualizer defined in the onion data on one of the canvas on the pane, which were previously defined with addCanvas
     *
     * @param myOnion
     */
                public void visualize(Onion myOnion);

    /**
     * \brief Adds a new canvas to the pane of the given ScriptEngine
     * \ingroup visualization
     * @param seID ID of the ScriptEngine
     * @param Name of the new canvas
     */
    public void addCanvas(String seID, String Name);


    /**
     * \brief Deletes a canvas from the pane of the given ScriptEngine
     * \ingroup visualization
     * @param seID ID of the ScriptEngine
     * @param Name of the new canvas
     */
    public void delCanvas(String seID, String Name);
}
