/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TextVisualizerJPanel.java
 *
 * Created on 22.09.2012, 17:28:38
 */
package org.oobd.ui.swing.desk;

import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.oobd.base.*;
import org.oobd.base.visualizer.*;
import org.oobd.base.support.Onion;

/**
 *
 * @author steffen
 */
public class VisualizerJPanel extends javax.swing.JPanel implements IFvisualizer {

    boolean toBePlaced = true; //indicates, if the actual instance is already been placed on an canvas or not
    boolean awaitingUpdate = false;
    boolean removalState = false;
    Visualizer value;
    static protected final Icon[] myIcons = new Icon[6];

    /** Creates new form TextVisualizerJPanel */
    public VisualizerJPanel() {
        super();
        //initComponents();
        if (myIcons[0] == null) { //initial setup
            myIcons[0] = new ImageIcon(swingView.class.getResource("/org/oobd/base/images/blank_16.png"));
            myIcons[1] = new ImageIcon(swingView.class.getResource("/org/oobd/base/images/forward_16.png"));
            myIcons[2] = new ImageIcon(swingView.class.getResource("/org/oobd/base/images/update_16.png"));
            myIcons[3] = new ImageIcon(swingView.class.getResource("/org/oobd/base/images/timer_16.png"));
            myIcons[4] = new ImageIcon(swingView.class.getResource("/org/oobd/base/images/text_16.png"));
            myIcons[5] = new ImageIcon(swingView.class.getResource("/org/oobd/base/images/back_16.png"));
        }
    }

    @Override
    public void paintComponent(Graphics g) {



        super.paintComponent(g);
    }

    public static IFvisualizer getInstance(Onion myOnion) {
        String thisType = myOnion.getOnionString("opts/type");
        System.out.println("Onion=" + myOnion.toString());
        System.out.println("Visualizer Type=" + thisType);

        if ("TextEdit".equalsIgnoreCase(thisType)) {
            return new TextEditVisualizerJPanel();
        } else if ("CheckBox".equalsIgnoreCase(thisType)) {
            return new CheckBoxVisualizerJPanel();
        } else if ("Slider".equalsIgnoreCase(thisType)) {
            return new SliderVisualizerJPanel();
        } else if ("Gauge".equalsIgnoreCase(thisType)) {
            return new ProcessBarVisualizerJPanel();
        } else {
            return new TextVisualizerJPanel();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));

        setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        setName("Form"); // NOI18N
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.PAGE_AXIS));

        filler3.setName("filler3"); // NOI18N
        add(filler3);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler3;
    // End of variables declaration//GEN-END:variables

    public boolean isGroup() {
        return false;
    }

    public void setVisualizer(Visualizer viz) {
        this.value = viz;
    }

    public Visualizer getVisualizer() {
        return this.value;
    }

    public void initValue(Visualizer viz, Onion onion) {
        //functionName.setText(onion.getOnionString("tooltip"));
        this.value = viz;
    }

    public boolean update(int level) {
        if (!removalState) {
            switch (level) {
                case 0: {
                    awaitingUpdate = true;
                    return false;
                }
                case 2: {
                    if (awaitingUpdate == true) {
                        this.invalidate();
                        this.validate();
                        this.repaint();
                        awaitingUpdate = false;
                        return true;
                    }
                }
                default:
                    return false;
            }
        } else {
            return true;
        }
    }

    public void setRemove(String pageID) {
        removalState = true;
        value.setRemove();
    }
}
