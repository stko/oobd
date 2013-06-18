/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skdsswing;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

import java.awt.event.*;
import java.awt.*;


import java.util.HashMap;
import org.oobd.base.*;
import org.oobd.base.visualizer.*;
import org.oobd.base.support.Onion;

/**
 *
 * @author steffen
 */
public class SwingVizTable extends JTable implements IFvisualizer {
    //as a VizTable may contain many columns, we need just one instance per page

    static HashMap<String, SwingVizTable> singleInstance = new HashMap<String, SwingVizTable>();
    static String[] columnNames = {"value", "Description"};
    boolean toBePlaced = true; //indicates, if the actual instance is already been placed on an canvas or not
    boolean awaitingUpdate = false;
    JTable myTable;

    /** this is a tricky but important part: As a set of visualizers can be only one instance of e.g. an table, we've to leave
     * the decision to create an new instance to that visualizer class itself. For that reason a visualizer object has the  method
     * getInstance, which decides on the parameter groupId, if a new instance of that visualizer needs to be created or
     * if the own instance gets the job
     * 
     * unfortunatelly this method is static, so it can be put in the interface, where it would normally belongs to
     * 
     * @param pageID tells the component to which page it belongs to
     * @return a graphic component
     */
    public static IFvisualizer getInstance(String pageID, String vizName) {
        SwingVizTable newInst;
        if (singleInstance.containsKey(pageID)) {
            newInst = singleInstance.get(pageID);
        } else {
            newInst = new SwingVizTable();
            singleInstance.put(pageID, newInst);
            return newInst;
        }
        return newInst;
    }

    public void setRemove(String pageID) {
        singleInstance.remove(pageID);
    }

    public SwingVizTable() {
        super();
        //myTable=new JTable();
        myTable = this;
        myTable.setAutoCreateRowSorter(true);

        myTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Output", "Description"
                }));
        myTable.setFillsViewportHeight(true);
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Point p = e.getPoint();
                    int row = myTable.rowAtPoint(p);
                    int col = myTable.columnAtPoint(p);
                    if (col == 0 && row > -1) {
                        Visualizer value = (Visualizer) myTable.getValueAt(row, col);
                        value.updateRequest(OOBDConstants.UR_USER);
                    }
                }
            }
        });


//      this.setViewportView(myTable);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public boolean isGroup() {
        if (toBePlaced) {
            toBePlaced = false;
            return true;
        } else {
            return false;
        }
    }

    public void setVisualizer(Visualizer viz) {
        myTable.getRowCount();
        /** here the visulasions works quite simple: The visualizer itself is inserted in the table,
         * so that each time the table is redrawn, the visualizer.toString() method is used to give the cell value.
         * in other visualisation elements this might be needed to do more complicated, but for a simple textual representation
         * this method is good enough
         */
        myTable.putClientProperty(myTable.getRowCount(), viz);
    }

    public void initValue(Visualizer viz, Onion onion) {
        ((DefaultTableModel) myTable.getModel()).addRow(
                new Object[]{
                    viz,
                    onion.getOnionString("tooltip")
                });

    }

    public boolean update(int level) {
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
    }

    public Visualizer getVisualizer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
   
}
