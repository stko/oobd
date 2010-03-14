/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package skdsswing;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.util.HashMap;
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
    static Object[][] data = {
        {"Mary", "Campione"},
        {"Alison", "Huml"},
        {"Kathy", "Walrath"},
        {"Sharon", "Zakhour"},
        {"Philip", "Milne"}
    };
    boolean toBePlaced = true; //indicates, if the actual instance is already been placed on an canvas or not
    JTable myTable;

    /** this is a tricky but important part: As a set of visualizers can be only one instance of e.g. an table, we've to leave
     * the decision to create an new instance to that visualizer class itself. For that reason a visualizer object has the  method
     * getInstance, which decides on the parameter ggrouId, if a new instance of that visualizer needs to be created or
     * if the own instance gets the job
     * 
     * 
     * 
     * @param groupID to define, if in case of an multiple value visualizer (like a table) a new instance is needed (for another canvas) or not
     * @param owner tells the component to which interfacing visualizer class it belongs to
     * @return a graphic component
     */
    public static IFvisualizer getInstance(String groupID) {
        SwingVizTable newInst;
        if (singleInstance.containsKey(groupID)) {
            newInst = singleInstance.get(groupID);
        } else {
            newInst = new SwingVizTable();
            singleInstance.put(groupID, newInst);
            return newInst;
        }
        return newInst;
    }

    public SwingVizTable() {
        super();
        //myTable=new JTable();
        myTable=this;
        myTable.setAutoCreateRowSorter(true);
myTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Output", "Description"
            }
        ));
 myTable.setFillsViewportHeight(true);
//      this.setViewportView(myTable);
    }

    public boolean isGroup() {
        if (toBePlaced) {
            toBePlaced = false;
            return true;
        } else {
            return false;
        }
    }

        public void setVisualizer(Visualizer viz){
            myTable.getRowCount();
            myTable.putClientProperty(myTable.getRowCount(), viz);
        }

        public void putValue(Visualizer viz,Onion onion){
            myTable.setValueAt("test", (Integer)this.getClientProperty(viz), 0);
        }

        public void initValue(Visualizer viz,Onion onion){
            ((DefaultTableModel) myTable.getModel()).addRow(
                    new Object[]{
                        viz,
                        viz
                    });

        }

}
