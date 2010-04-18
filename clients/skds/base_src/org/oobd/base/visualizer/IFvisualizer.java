/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base.visualizer;
import org.oobd.base.support.Onion;
/**
 *
 * @author steffen
 */
public interface IFvisualizer {
    /**
     * tells, if that visualisation is a single element or represents a whole group of visualisations, like an JTable
     * @return
     */
    public boolean isGroup();

    /**
     * links the visualizer to the real visualization component
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
     */
    public void update(int level);

}
