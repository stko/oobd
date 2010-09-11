

package org.oobd.base.visualizer;
import org.oobd.base.support.Onion;
/**
 *
 * \brief Interface for the GUI elements (buttons, textfields, tables..)  to support the OOBD core functions
 * \ingroup visualisation
 */
public interface IFvisualizer {
    /**
     * /brief does the visualisation element represents a group of values?
     * 
     * Tells, if that visualisation is a single element, like a butten,  or does it represent a whole group of visualisations, like an JTable, where each column
     * of the table is a single visualisation
     *
     * @return true, if group
     */
    public boolean isGroup();

    /**
     * \brief links the visualizer to the real visualization component
     *
     * When e.g. the user press a button, the button needs to tell this to his corrosponding visualizer to start some action.
     * @param viz
     */
    public void setVisualizer(Visualizer viz);

    /**
     * makes the visualisation component to show the value
     * @param viz
     * @param onion
     */
    public void putValue(Visualizer viz,Onion onion);
    /**
     * initialize the shown data
     * @param viz
     * @param onion
     */
    public void initValue(Visualizer viz,Onion onion);

    /**
     * controls the update of the visualisation of this visualizer
     * @param viz
     * @param onion
     * @return true if update has been done
     */
    public boolean update(int level);

}
