package org.oobd.base;

import org.oobd.base.support.Onion;

/**
 * \brief Interface for the application object who does the graphical User
 * Interface to communicate with the OOBD core
 */
public interface IFui {

    /**
     * \brief append and shows a string in the output area
     *
     * The string is added to the content of the output area and the output area
     * is made visible to the user
     *
     * @param msg
     *
     * the modifier extends the meaning of the string and can be used optional
     * to e.g. clear the output or save the content
     */
    public void sm(String msg, String modifier);

    /**
     * \brief Builds an input window which allows the user to input some
     * parameters
     *
     * According to http://www.oobd.org/doku.php?id=dev:onionspec
     *
     * @param msg a param onion (see
     * http://www.oobd.org/doku.php?id=dev:onionspec&#param)
     */
    public Onion requestParamInput(Onion msg);

    /**
     * \brief reads the stred preferences into the actual system settings
     *
     * @todo when there is no UI anymore, this belongs to core
     * @param connectTypeName the connectionType to be loaded from prefs
     */
    public void transferPreferences2System(String connectTypeName);

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
     * The userinterface must collect these information and present this to the
     * user, as the first step of the user interaction would be, that the user
     * selects the scriptengine he wants to work with and start their
     * functionality by calling startScriptEngine()
     *
     *
     * During startup, the core looks for all scriptengines the user might want
     * to use and these through this function to the user interface, e.g. to add
     * this to a selection menu
     *
     * @param id the key of the scriptengines hash array where the loaded
     * instances are been stored
     * @param visibleName
     */
    public void announceScriptengine(String id, String visibleName);

    /**
     * \brief tells the UserInterface about the existence of a UIHandler
     * \ingroup init
     *
     * The userinterface must collect these information and present this to the
     * user, as the first step of the user interaction would be, that the user
     * selects the UIHandler he wants to use and start their functionality by
     * calling startUIHandler()
     *
     *
     * During startup, the core looks for all available UIHandler and announce
     * these through this function to the user interface, e.g. to add this to a
     * selection menu
     *
     * @param id the key of the UIHandler hash array where the loaded instances
     * are been stored
     * @param visibleName
     */
    public void announceUIHandler(String id, String visibleName);

    /**
     * \brief search for a specific vizualizer type \ingroup visualisation this
     * method is UI- depending and returns the class which comes closest to the
     * requested visualizer type.
     *
     *
     * @param visualizerType
     * @param theme
     * @return Visulizerclass, if found
     */
    public Class getVisualizerClass(Onion myOnion);

    /**
     * \brief places a visualizer onto an canvas \ingroup visualisation
     *
     * puts the visualizer defined in the onion data on one of the canvas on the
     * pane, which were previously defined with addCanvas
     *
     * @param myOnion
     */
    public void visualize(Onion myOnion);

    /**
     * \brief update the UI \ingroup visualisation
     *
     * on most java implementations the UI thread need to do all updates of the
     * UI elements, otherways the program likes to crash call this function
     * regulary to transfer changes between UI and oobd core
     *
     */
    public void updateOobdUI();

    /**
     * \brief Adds a new canvas to the pane of the given ScriptEngine \ingroup
     * visualisation
     *
     * @param seID ID of the ScriptEngine
     * @param Name of the new canvas
     */
    public void openPage(String seID, String Name, int colcount, int rowcount);

    /**
     * \brief Deletes a canvas from the pane of the given ScriptEngine \ingroup
     * visualisation
     *
     * @param seID ID of the ScriptEngine
     * @param Name of the new canvas
     */
    public void openPageCompleted(String seID, String Name);

    /**
     * \brief sends a JSON data set to the openXC interface \ingroup
     * visualisation
     *
     * @param onion
     */
    public void openXCVehicleData(Onion onion);
}
