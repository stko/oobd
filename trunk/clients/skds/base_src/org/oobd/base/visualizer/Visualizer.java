/**
 * \defgroup visualisation Visualize values and return User inputs
 *
 * The visualisation is one of the basic elements of OOBD, because in the end the user want's to see what happens and he wants to start some actions.
 *
 * To make all this inpendent from the internal processes, the following principles have been used:
 *
 * \section Setting up the User Interface
 * \subsection pane Each scriptengine has his own drawing pane
 *
 * Whenever a scriptengine is started, the system opens a new drawing pane for this scriptengine. The pane and the scriptengine belong together, and the pane is reserved for his
 * scriptengine to place its visual elements on it.
 *
 * Depenting on the implementation, there can also be one pane, which does not belong to a scriptengine, but to the user hinself. Here he can arrange his own visualistions, as
 * the content of sriptengine panels are only controlled by the scriptengines itself.
 *
 * A scriptengine is started, when the user e.g. selects an available scriptengine out of the list (see \link init initialisation \endlink where that list comes from). At the same time 
 * the GUI sets up the pane for that scriptengine to have something where the scriptengine can draw onto.
 * 
 *  \msc
    User,GUI,Core;
    User->GUI [label="choose a scriptengine"];
    GUI->Core [label="createScriptEngine()", URL="\ref org::oobd::base::Core.createScriptEngine()"];
     --- [label="create pane"];
    GUI->Core [label="startScriptEngine()", URL="\ref org::oobd::base::Core.startScriptEngine()"];
 \endmsc
 * 
 * 
 * \subsection canvas Each pane has one or more named canvas
 * 
 * While the pane itself is more the generic container for all the scriptengine avtivities, a canvas is a real drawing surface where the scriptengine can put visual elements 
 * onto.
 *
 * A scriptengine can have several canvas. They can be identified by their name and they are generated and deleted by addCanvas() and delCanvas().
 *
 * The parameter rowcount and colcount lays a grid over the canvas. The grid coordinates are used later to locate graphical elements on the canvas.
 *
 * It's up to the actual implementation, if these grid coordinates will be used or if the elements are just put into an list, like on an mobile phone with a small display.
 *
 *  \msc
    GUI,Core;
    GUI<-Core [label="addCanvas()", URL="\ref org::oobd::base::IFui.addCanvas()"];
     --- [label="do funny things"];
   GUI<-Core [label="delCanvas()", URL="\ref org::oobd::base::IFui.delCanvas()"];
  \endmsc

 * \subsection visualisator Visual Elements are placed on a canvas
 *
 * After the canvas is set up as descripted above, the scriptengine can now place it's graphical elements onto it.
 *
 * This is done by calling visualize(). Visualize() itself then calls getVisualizerClass() to find the closest match to the requested element.
 *
 * Instead of simply creating a new instance of that element, the element class itself is asked by getInstance() for the instance which should represent the new element.
 *
 * This needs to be understood first:
 *
 * If you have e.g. a button, this button can obviously only represent one element. But in case you have a table, than this can respresent a whole table of values.
 * And as the only one, who knows, if it can represent just one or multiple values, it that class itself. For that reason the class itself decide if for a new value also a
 * new instance is needed or if an exisiting instance can handle the new value too
 *
 * \remark As the getInstance() needs to be a static method of that class, but static methods can not be defined in an Interface, the programmer itself is responcible to implement this
 * method, it can't be covered already in the IFui - interface
 *
 *  \msc
    IFvisualiser,GUI,Core;
    GUI<-Core [label="visualize()", URL="\ref org::oobd::base::IFui.visualize()"];
    GUI<-GUI [label="getVisualizerClass()", URL="\ref org::oobd::base::IFui.getVisualizerClass()"];
    IFvisualiser<-IFvisualiser [label="getVisualizerClass()", URL="\ref org::oobd::base::IFui.getVisualizerClass()"];
     --- [label="place new element"];
  \endmsc

 *
 */

package org.oobd.base.visualizer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.support.Onion;
import org.oobd.base.*;

/**
 *
 * @author steffen
 */
public class Visualizer {

    Onion ownerEngine;
    Onion value;
    String name;
    IFvisualizer myObject;
    boolean updateNeeded = false;

    public Visualizer(Onion onion) {
        ownerEngine = onion.getOnion(OOBDConstants.FN_OWNER);
        name = onion.getOnionString(OOBDConstants.FN_NAME);
//        this.myObject=myObject;
        Core.getSingleInstance().addVisualizer(ownerEngine.getOnionString(OOBDConstants.FN_NAME), this);
    }

    /** sets the corrosponding visual element to this visualizer
     * 
     * @param myObject
     */
    public void setOwner(IFvisualizer myObject) {
        this.myObject = myObject;
    }

    /** sets the Value for this visualizer
     * 
     * @param myObject
     */
    public void setValue(Onion value) {
        if (this.name.matches(value.getOnionString("to/name"))) {
            try {
                this.value = new Onion(value.toString());
                updateNeeded = true;
            } catch (JSONException ex) {
            }
        }
    }

    public String getValue(String name) {
        if (value != null) {
            return value.getOnionString(name);
        } else {
            return null;
        }
    }

    /** returns the name of the owning scriptengine
     *
     * @return
     */
    public String getOwnerEngine() {
        return ownerEngine.getOnionString(OOBDConstants.FN_NAME);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String val = getValue(OOBDConstants.FN_VALUESTRING);
        if (val == null) {
            return "#NA";
        } else {
            return val;
        }

    }

    /** do update
     *  0: start 1: update data 2: finish
     */
    public void doUpdate(int updateLevel) {
        if (myObject != null && updateNeeded) {
            myObject.update(updateLevel);
            updateNeeded = !myObject.update(updateLevel);
        }
    }

    /** Update request from Component
     *  0: start 1: update data 2: finish
     */
    public void updateRequest(int type) {
        System.out.println("Update request" + Integer.toString(type));
        System.out.println("my ownwer is: " + ownerEngine.toString());
        try {
            Core.getSingleInstance().transferMsg(new Message(Core.getSingleInstance(), OOBDConstants.CoreMailboxName, new Onion(""
                    + "{" +
                    "'type':'" + OOBDConstants.CM_UPDATE +
                    "'," +
                    "'vis':'" + this.name +
                    "',"  +
                    "'to':'" + getOwnerEngine() +
                    "',"  +
                     "'optid':'" + getValue("optid") +
                    "',"  +
                    "'actValue':'" + getValue("ValueString") +
                    "',"  +
                    "'updType':" + Integer.toString(type) +
                    "}")));
        } catch (JSONException ex) {
            Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
