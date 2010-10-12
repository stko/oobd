/**
 * \defgroup visualisation Visualize values and return User inputs
 *
 * The visualisation is one of the basic elements of OOBD, because in the end the user want's to see what happens and he wants to start some actions.
 *
 * To make all this inpendent from the internal processes, the following principles have been used:
 *
 * \section StartAction Start the first Activities
 * The whole mechanism is started when the user e.g. selects an available scriptengine out of the list (see \link init initialisation \endlink where that list comes from). At the same time 
 * the GUI sets up the pane for that scriptengine to have something where the scriptengine can draw onto.
 *
 * \section setupUI Setting up the User Interface
 * \subsection pane Each scriptengine has his own drawing pane
 *
 * Whenever a scriptengine is started, the system opens a new drawing pane for this scriptengine. The pane and the scriptengine belong together, and the pane is reserved for his
 * scriptengine to place its visual elements on it.
 *
 * Depending on the implementation, there can also be one pane, which does not belong to a scriptengine, but to the user hinself. Here he can arrange his own visualistions, as
 * the content of scriptengine panels are only controlled by the scriptengines itself.
 *
 * 
 *  \msc
    User,GUI,Core;
    User->GUI [label="choose a scriptengine"];
    GUI->Core [label="createScriptEngine()", URL="\ref org::oobd::base::Core.createScriptEngine()"];
     --- [label="create pane"];
    GUI->Core [label="startScriptEngine()", URL="\ref org::oobd::base::Core.startScriptEngine()"];
 \endmsc
 * 
 * It's up to the implementation to allow the user multiple or just one scriptengine at a time.
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
 * If you have e.g. a button, this button can obviously only represent one element. But in case you have a table, than this can represent a whole table of values.
 * And the only one, who knows, if it can represent just one or multiple values, it that class itself. For that reason the class itself decide if for a new value also a
 * new instance is needed or if an exisiting instance can handle the new value too
 *
 * \remark As the getInstance() needs to be a static method of that class, but static methods can not be defined in an Interface, the programmer itself is responcible to implement this
 * method, it can't be covered already in the IFui - interface
 *
 * After creating the visual element, the element is told who its corrosponding visualizer is by calling its setVisualizer() method
 * 
 *  \msc
    IFvisualiser,GUI,Core;
    GUI<-Core [label="visualize()", URL="\ref org::oobd::base::IFui.visualize()"];
    GUI<-GUI [label="getVisualizerClass()", URL="\ref org::oobd::base::IFui.getVisualizerClass()"];
    IFvisualiser<-GUI [label="getInstance()", URL="\ref org::oobd::base::IFui.getInstance()"];
     --- [label="place new element"];
 * IFvisualiser<-GUI [label="setVisualizer()", URL="\ref org::oobd::base::IFui.setVisualizer()"];
 *
  \endmsc

 *
 * After the responsible visualisation object has been identified, it's supplied with all necessary information via the onion object and it's placed on the canvas with the
 * right coordinates, min & max values and unit.
 *
 * \section updatevalues Updating Values on the screen
 *
 * Whenever there's a new value to visualize, the core calls the update() method of the visualisation element to tell that there a new values to show (with 0 as parameter).
 *
 * The update() method is called several times: First with an 0 as level just to tell the element that there's an update to come, and later again with a 2 to do the update now.
 * This two way method is used to avoid that each new value causes an immediate UI refresh, which is time consuming. By this method all new vaules can be updated with just one UI refresh.
 *
 *
 *
 *  \msc
    IFvisualiser,Core;

    --- [label="inform about updates to come"];
    IFvisualiser<-Core [label="Update(0)", URL="\ref org::oobd::base::IFui.Update()"];
   --- [label="refresh the UI"];
    IFvisualiser<-Core [label="Update(2)", URL="\ref org::oobd::base::IFui.Update()"];

  \endmsc


 *
 *\section userinputs User Input Handling
 *
 * When ever the visalisation element founds that the user did some actions, like press a button, doubleclick on a value or a new selection in a combobox, it calls  updateRequest()
 * of its corrosponding visualizer. The visualizer than forwards the message to the core for further handling
 *
 *  \msc
    IFvisualiser,Core;

    IFvisualiser->Core [label="updateRequest()", URL="\ref org::oobd::base::IFui.updateRequest()"];

  \endmsc

 *
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
        value=onion;
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
        String val = getValue("value");
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
     *  \todo the information, if the user has changed any selection, needs to be addded
     * \ingroup visualisation
     */
    public void updateRequest(int type) {
        System.out.println("Update request" + Integer.toString(type));
        System.out.println("my ownwer is: " + ownerEngine.toString());
        System.out.println("actual visualizer data: " + value.toString());
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
                    "'actValue':'" + getValue("value") +
                    "',"  +
                    "'updType':" + Integer.toString(type) +
                    "}")));
        } catch (JSONException ex) {
            Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
