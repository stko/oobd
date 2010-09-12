

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
     * Tells, if that visualisation is a single element, like a button,  or does it represent a whole group of visualisations, like an JTable, where each column
     * of the table is a single visualisation
     * \ingroup visualisation
     *
     * @return true, if group
     */
    public boolean isGroup();

    /**
     * \brief links the visualizer to the real visualization component
     * \ingroup visualisation
     *
     * When e.g. the user press a button, the button needs to tell this to his corrosponding visualizer to start some action.
     * @param viz
     */
    public void setVisualizer(Visualizer viz);

    /**
     * \brief makes the visualisation component to show the value
     * \ingroup visualisation
     *
     * This method of the visual object is called when the OOBD core wants to display a new value. It's then up to the visual object to make it visible (just print the value, or e.g.
     * display a bar graph)
     * @param viz the corrosponding visualizer
     * @param onion an onion data structure containing the details \see http://sourceforge.net/apps/mediawiki/oobd/index.php?title=SKDS:onion for details
     */
    public void putValue(Visualizer viz,Onion onion);
    /**
     * \brief initialize the visualisation object
     * \ingroup visualisation
     *
     * in opposite to just show a value, the initialisation of a visualisation object needs some more information about min & max values, the unit etc. This initial setup is done here
     *
     * @param viz the corrosponding visualizer
     * @param onion an onion data structure containing the details \see http://sourceforge.net/apps/mediawiki/oobd/index.php?title=SKDS:onion for details
     */
    public void initValue(Visualizer viz,Onion onion);

    /**
     * \requests the update of the visualisation
     * \ingroup visualisation
     *
     * @param viz the corrosponding visualizer
     * @param onion an onion data structure containing the details \see http://sourceforge.net/apps/mediawiki/oobd/index.php?title=SKDS:onion for details
     * @return true if update has been done
     * @todo the usage of the different udpate levels in unclear yet-
     */
    public boolean update(int level);

}
