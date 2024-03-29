/**
 * \defgroup visualisation Visualize values and return User inputs
 *
 * The visualisation is one of the basic elements of OOBD, because in the end
 * the user want's to see what happens and he wants to start some actions.
 *
 * To make all this inpendent from the internal processes, the following
 * principles have been used:
 *
 * \section StartAction Start the first Activities The whole mechanism is
 * started when the user e.g. selects an available scriptengine out of the list
 * (see \link init initialisation \endlink where that list comes from). At the
 * same time the GUI sets up the pane for that scriptengine to have something
 * where the scriptengine can draw onto.
 *
 * \section setupUI Setting up the User Interface \subsection pane Each
 * scriptengine has his own drawing pane
 *
 * Whenever a scriptengine is started, the system opens a new drawing pane for
 * this scriptengine. The pane and the scriptengine belong together, and the
 * pane is reserved for his scriptengine to place its visual elements on it.
 *
 * Depending on the implementation, there can also be one pane, which does not
 * belong to a scriptengine, but to the user hinself. Here he can arrange his
 * own visualistions, as the content of scriptengine panels are only controlled
 * by the scriptengines itself.
 *
 *
 * \msc User,GUI,Core; User->GUI [label="choose a scriptengine"]; GUI->Core
 * [label="createScriptEngine()", URL="\ref
 * org::oobd::base::Core.createScriptEngine()"]; --- [label="create pane"];
 * GUI->Core [label="startScriptEngine()", URL="\ref
 * org::oobd::base::Core.startScriptEngine()"]; \endmsc
 *
 * It's up to the implementation to allow the user multiple or just one
 * scriptengine at a time.
 *
 * \subsection page Each pane represents a page
 *
 * While the pane itself is more the generic container for all the scriptengine
 * avtivities, a page is a real drawing surface where the scriptengine can put
 * visual elements onto.
 *
 * A scriptengine can has one page at a time. They are generated by openPage().
 * OpenPage() destroys the provious page, if there's one.
 *
 * After opening a page, the page can be filled with graphic elements. When this
 * is done
 *
 * The parameter rowcount and colcount lays a grid over the canvas. The grid
 * coordinates are used later to locate graphical elements on the canvas.
 *
 * It's up to the actual implementation, if these grid coordinates will be used
 * or if the elements are just put into an list, like on an mobile phone with a
 * small display.
 *
 * \msc GUI,Core; GUI<-Core [label="openPage()", URL="\ref
 * org::oobd::base::IFui.openPage()"]; --- [label="add visual elements to
 * page"]; GUI<-Core [label="openPageCompleted()", URL="\ref
 * org::oobd::base::IFui.openPageCompleted()"]; \endmsc
 *
 * \subsection visualisator Visual Elements are placed on a page
 *
 * After the page is opened as descripted above, the scriptengine can now place
 * it's graphical elements onto it.
 *
 * This is done by calling visualize(). Visualize() itself then calls
 * getVisualizerClass() to find the closest match to the requested element.
 *
 * Instead of simply creating a new instance of that element, the element class
 * itself is asked by getInstance() for the instance which should represent the
 * new element.
 *
 * This needs to be understood first:
 *
 * If you have e.g. a button, this button can obviously only represent one
 * element. But in case you have a table, than this can represent a whole table
 * of values. And the only one, who knows, if it can represent just one or
 * multiple values, it that class itself. For that reason the class itself
 * decide if for a new value also a new instance is needed or if an exisiting
 * instance can handle the new value too
 *
 * \remark As the getInstance() needs to be a static method of that class, but
 * static methods can not be defined in an Interface, the programmer itself is
 * responsible to implement this method, it can't be covered already in the IFui
 * - interface
 *
 * After creating the visual element, the element is told who its corrosponding
 * visualizer is by calling its setVisualizer() method
 *
 * \msc IFvisualiser,GUI,Core; GUI<-Core [label="visualize()", URL="\ref
 * org::oobd::base::IFui.visualize()"]; GUI<-GUI [label="getVisualizerClass()",
 * URL="\ref org::oobd::base::IFui.getVisualizerClass()"]; IFvisualiser<-GUI
 * [label="getInstance()", URL="\ref org::oobd::base::IFui.getInstance()"]; ---
 * [label="place new element"]; IFvisualiser<-GUI [label="setVisualizer()",
 * URL="\ref org::oobd::base::IFui.setVisualizer()"];
 *
 * \endmsc
 *
 *
 * After the responsible visualisation object has been identified, it's supplied
 * with all necessary information via the onion object and it's placed on the
 * page with the right coordinates, min & max values and unit.
 *
 * \section updatevalues Updating Values on the screen
 *
 * Whenever there's a new value to visualize, the core calls the update() method
 * of the visualisation element to tell that there a new values to show (with 0
 * as parameter).
 *
 * The update() method is called several times: First with an 0 as level just to
 * tell the element that there's an update to come, and later again with a 2 to
 * do the update now. This two way method is used to avoid that each new value
 * causes an immediate UI refresh, which is time consuming. By this method all
 * new vaules can be updated with just one UI refresh.
 *
 *
 *
 * \msc IFvisualiser,Core;
 *
 * --- [label="inform about updates to come"]; IFvisualiser<-Core
 * [label="Update(0)", URL="\ref org::oobd::base::IFui.Update()"]; ---
 * [label="refresh the UI"];
IFvisualiser<-Core [label="Update(2)", URL="\ref org::oobd::base::IFui.Update()"];
 *
\endmsc
 *
 *
 *
 *\section userinputs User Input Handling
 *
 * When ever the visalisation element founds that the user did some actions, like press a button, doubleclick on a value or a new selection in a combobox, it calls  updateRequest()
 * of its corrosponding visualizer. The visualizer than forwards the message to the core for further handling
 *
 *  \msc
 * IFvisualiser,Core;
 *
 * IFvisualiser->Core [label="updateRequest()", URL="\ref
 * org::oobd::base::IFui.updateRequest()"];
 *
 * \endmsc
 *
 *
 *
 */
package org.oobd.core.visualizer;

import org.oobd.core.OOBDConstants;
import org.oobd.core.Message;
import org.oobd.core.Base64Coder;
import org.oobd.core.Core;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.core.scriptengine.ScriptengineLua;
import org.oobd.core.support.Onion;
import org.oobd.core.support.OnionNoEntryException;
import org.oobd.core.support.OnionWrongTypeException;

/**
 *
 * @author steffen
 */
public class Visualizer {

    Onion ownerEngine;
    Onion value;
    String name;
    String optId;
    String optType;
    String regex;
    int min;
    int max;
    int step;
    String unit;
    String toolTip;
    String lastValue = "";
    int updateEvents;
    int overflowProtectionCounter;
    int averageOverflowProtection = 1;
    int thisOverflowProtection = 1;
    int nrOfTrials = 0;
    IFvisualizer myObject;
    boolean updateNeeded = false;
    boolean obsulete = false;
    Object relatedObject = null;

    public Visualizer(Onion onion) {
        ownerEngine = onion.getOnion(OOBDConstants.FN_OWNER);
        name = onion.getOnionString(OOBDConstants.FN_NAME, null);
        optId = onion.getOnionString(OOBDConstants.FN_OPTID, null);
        optType = onion.getOnionString(OOBDConstants.FN_OPTTYPE, null);
        regex = onion.getOnionString(OOBDConstants.FN_OPTREGEX, null);
        try {
            min = safeInt(onion.getOnionObject(OOBDConstants.FN_OPTMIN));
        } catch (OnionNoEntryException e1) {
            min = 0;
        }
        try {
            max = safeInt(onion.getOnionObject(OOBDConstants.FN_OPTMAX));
        } catch (OnionNoEntryException e1) {
            max = 0;
        }
        try {
            step = safeInt(onion.getOnionObject(OOBDConstants.FN_OPTSTEP));
        } catch (OnionNoEntryException e1) {
            step = 0;
        }
        unit = onion.getOnionString(OOBDConstants.FN_OPTUNIT, "");

        toolTip = onion.getOnionBase64String(OOBDConstants.FN_TOOLTIP, "");
        try {
            updateEvents = onion.getInt(OOBDConstants.FN_UPDATEOPS);
        } catch (JSONException e) {
            updateEvents = 0;
        }
        value = onion;
        // this.myObject=myObject;
        Core.getSingleInstance().getUiHandler().addVisualizer(
                ownerEngine.getOnionString(OOBDConstants.FN_NAME, ""), this);
    }

    /**
     * sets the corrosponding visual element to this visualizer
     *
     * @param myObject
     */
    public void setOwner(IFvisualizer myObject) {
        this.myObject = myObject;
    }

    /**
     * sets a generic object reference to this visualizer
     *
     * @param myObject
     */
    public void setReleatedObject(Object myObject) {
        this.relatedObject = myObject;
    }

    /**
     * gets a generic object reference to this visualizer
     *
     * @param myObject
     */
    public Object getReleatedObject() {
        return this.relatedObject;
    }

    /**
     * sets the Value for this visualizer
     *
     * @param myObject
     */
    public void setValue(Onion value) {
        try {
            if (!obsulete && this.name.matches(value.getOnionString("to/name"))) {
                Logger.getLogger(Visualizer.class.getName()).log(Level.INFO,
                        "Visualizer.setValue(): update needed.");
                this.value = new Onion(value.toString());
                if (getUpdateFlag(OOBDConstants.VE_LOG)
                        && !lastValue.equalsIgnoreCase(this.toString())) {
                    Date date = new Date();
                    Core.getSingleInstance().outputText(
                            date.toGMTString() + "\t" + this.toString() + "\t"
                            + this.toolTip);
                    lastValue = this.toString();

                }
                updateNeeded = true;
                // as this is an actual value, we can reset the overflow
                // protection for now and switch the protection delay one step
                // downwards
                averageOverflowProtection = (averageOverflowProtection
                        * nrOfTrials + thisOverflowProtection) / 2;
                nrOfTrials = 0;
                if (averageOverflowProtection < 1) {
                    averageOverflowProtection = 1; // otherways the average calc
                }													// won't work
                thisOverflowProtection = 0;
                System.err.println("set overflow delay to"
                        + Integer.toString(averageOverflowProtection));
                overflowProtectionCounter = 0;
            }

        } catch (JSONException | OnionWrongTypeException | OnionNoEntryException ex) {
            Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getValue(String name) {
        if (value != null) {
            return value.getOnionString(name,null);
        } else {
            return null;
        }
    }

    public Object getValueOnion(String name) {
        if (value != null) {
            try {
                return value.getOnionObject(name);
            } catch (OnionNoEntryException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean getUpdateFlag(int bitNr) {
        return (updateEvents & (1 << bitNr)) != 0;
    }

    public void setUpdateFlag(int bitNr, boolean bit) {
        if (bit) {
            updateEvents |= (1 << bitNr);
        } else {
            updateEvents &= (~(1 << bitNr));
        }
    }

    /**
     * returns the name of the owning scriptengine
     *
     * @return
     */
    public String getOwnerEngine() {
        return ownerEngine.getOnionString(OOBDConstants.FN_NAME,null);
    }

    /**
     * returns if the visualizer is if requested, where "" and "Label" equals
     * the default type "Label"
     *
     * @return
     */
    public boolean isTypeOf(String requestedType) {
        if (requestedType != null) {
            if ((requestedType.equals("") || requestedType.equalsIgnoreCase("Label")) && (optType == null || optType.equals("") || optType.equalsIgnoreCase("Label"))) {
                return true;
            } else {
                return requestedType.equalsIgnoreCase(optType);
            }
        } else {
            return false;
        }
    }

    public String getRegex() {
        return regex;
    }

    public int getMin() {
        return safeInt(min);
    }

    public int getMax() {
        return safeInt(max);
    }

    public int getStep() {
        return safeInt(step);
    }

    public String getUnit() {
        return unit;
    }

    public String getName() {
        return name;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void inputNewValue(String newValue) {
        if (value != null) {
            value.setValue("value", Base64Coder.encodeString(newValue));
        }
    }

    @Override
    public String toString() {
        String val = getValue("value");
        if (val == null) {
            return "#NA";
        } else {
            return Base64Coder.decodeString(val);
        }

    }

    /**
     * do update 0: start 1: update data 2: finish
     */
    public void doUpdate(int updateLevel) {
        if (obsulete) {
            myObject = null; // set object reference to null, so that the
            // garbage collection can remove it
        } else {
            if (myObject != null && updateNeeded) {
                updateNeeded = !myObject.update(updateLevel);
            }
        }
    }

    /**
     * Update request from Component \todo the information, if the user has
     * changed any selection, needs to be addded \ingroup visualisation
     */
    public void updateRequest(int type) {
        if (!obsulete) {
            if (type != OOBDConstants.UR_TIMER
                    || overflowProtectionCounter == 0) {
                Logger.getLogger(Visualizer.class.getName()).log(Level.INFO,
                        "Update request" + Integer.toString(type)
                        + " my ownwer is: "
                        + ownerEngine.toString()
                        + "actual visualizer data: "
                        + value.toString());
                try {
                    Core.getSingleInstance().transferMsg(
                            new Message(Core.getSingleInstance(),
                                    OOBDConstants.UIHandlerMailboxName, new Onion(""
                                            + "{" + "'type':'"
                                            + OOBDConstants.CM_UPDATE + "',"
                                            + "'vis':'" + this.name + "',"
                                            + "'to':'" + getOwnerEngine()
                                            + "'," + "'optid':'" + this.optId
                                            + "'," + "'actValue':'"
                                            + getValue("value") + "',"
                                            + "'updType':"
                                            + Integer.toString(type) + "}")));
                } catch (JSONException ex) {
                   Logger.getLogger(Visualizer.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (type == OOBDConstants.UR_TIMER) {
                    // reduce the actual delay time, which is used to reduce the
                    // overall number of
                    // sended msgs in case no update message is answered at all
                    overflowProtectionCounter = averageOverflowProtection;
                    nrOfTrials++;
                }
            } else {
                // overflow delay seems not sufficent, so increase it
                thisOverflowProtection++;
                if (overflowProtectionCounter > 0) {
                    overflowProtectionCounter--;
                }
                System.err.println("increase actual overflow delay to "
                        + Integer.toString(thisOverflowProtection)
                        + " overflowProtectionCounter:"
                        + Integer.toString(overflowProtectionCounter));
            }
        }
    }

    /**
     * declares the visualizer as invalid, to that the core can remove it
     */
    public void setRemove() {
        obsulete = true;
    }

    /**
     * tells the core, if that visualizer is valid or should be removed
     *
     * @return
     */
    public boolean getRemoved() {
        return obsulete;
    }

    public void inputNewValue(int progress) {
        inputNewValue(new Integer(progress).toString());

    }

    public static int safeInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
