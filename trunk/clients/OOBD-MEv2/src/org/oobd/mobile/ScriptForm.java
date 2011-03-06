package org.oobd.mobile;



import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.Vector;
import javax.microedition.lcdui.*;


/**
 *
 * @author steffen
 */
public class ScriptForm extends Form implements CommandListener, Runnable {

    private Form parent; //Where this form was started from
    private OutputDisplay mainMidget; //Where the output routines are
    private Command backCommand = null;
    private Command detailCommand = null;
    List cellList = null;
    Script myEngine = null;

    public ScriptForm(Form parent, List cellList, String title, Script scriptEngine, OutputDisplay mainMidget) {
        super(title);
        this.parent = parent;
        this.cellList = cellList;
        this.myEngine = scriptEngine;
        this.mainMidget = mainMidget;
        new Thread(this).start();
        showForm();
    }

    public String showForm() {
//        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
//        cellList.setListCellRenderer(new ScriptCellRenderer());
//        cellList.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent evt) {
//                System.out.println(cellList.getSelectedItem().toString());
//                ScriptCell cell = (ScriptCell) cellList.getSelectedItem();
//                if (cell != null) {
//                    //Dialog.show("Content", cell.toString(), "ok", null);
//                    if (cell.execute(1, myEngine)) {
//                        repaint();
//                        mainMidget.outputDisplayIfAny();
//                    }
//                }
//            }
//        });
//        //cellList.addItem(new ScriptCell("Voltage", "battery", "12.5V",true, false));
//        this.addComponent(cellList);
//        this.addCommand(backCommand = new Command("Back"));
//        this.addCommand(detailCommand = new Command("Detail"));
//        addCommandListener(this);
//        show();
//        this.setFocused(cellList);
        return "";

    }

    public void run() {
        try {
            while (true) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commandAction(Command c, Displayable d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    }
//    class ScriptCellRenderer extends Container implements ListCellRenderer {

//        private Label title = new Label("");
//        private Label value = new Label("");
//        private Label update = new Label("");
//        private Label timer = new Label("");
//        private Label focus = new Label("");
//
//        public ScriptCellRenderer() {
//
//            setLayout(new BorderLayout());
//            Container cntLeft = new Container(new BoxLayout(BoxLayout.Y_AXIS));
//            Container cntRight = new Container(new BoxLayout(BoxLayout.Y_AXIS));
//            value.getStyle().setBgTransparency(0);
//            value.getStyle().setPadding(3, 0, 3, 3);
//            value.getStyle().setMargin(0, 0, 0, 0);
//            value.getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
//            title.getStyle().setBgTransparency(0);
//            title.getStyle().setPadding(0, 3, 3, 3);
//            title.getStyle().setMargin(0, 0, 0, 0);
//            update.getStyle().setBgTransparency(0);
//            update.getStyle().setPadding(3, 0, 3, 3);
//            update.getStyle().setMargin(0, 0, 0, 0);
//            timer.getStyle().setBgTransparency(0);
//            timer.getStyle().setPadding(0, 3, 3, 3);
//            timer.getStyle().setMargin(0, 0, 0, 0);
//            cntLeft.addComponent(value);
//            cntLeft.addComponent(title);
//            cntRight.addComponent(update);
//            cntRight.addComponent(timer);
//            addComponent(BorderLayout.CENTER, cntLeft);
//            addComponent(BorderLayout.EAST, cntRight);
//            focus.getStyle().setBgTransparency(100);
//        }
//
//        public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
//
//            ScriptCell cell = (ScriptCell) value;
//            this.title.setText(cell.getTitle());
//            this.value.setText(cell.getValue());
//            //this.update.setIcon(person.getPic());
//            if (cell.getUpdate()) {
//                this.update.setText("u");
//            } else {
//                this.update.setText("-");
//            }
//            if (cell.getTimer()) {
//                this.timer.setText("t");
//            } else {
//                this.timer.setText("-");
//            }
//            if (isSelected) {
//                this.getStyle().setBgColor(5);
//            }
//            if (isSelected) {
//                setFocus(true);
//                getStyle().setBgTransparency(100);
//            } else {
//                setFocus(false);
//                getStyle().setBgTransparency(0);
//            }
//
//
//
//            return this;
//        }
//
//        public Component getListFocusComponent(List list) {
//            return focus;
//        }
//    }
//Read more: http://lwuit.blogspot.com/2008/07/lwuit-list-renderer-by-chen-fishbein.html#ixzz0q3CKIUfz
//}
