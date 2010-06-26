
import com.sun.lwuit.Form;
import com.sun.lwuit.Image;
import com.sun.lwuit.Form;
import com.sun.lwuit.Display;
import com.sun.lwuit.Label;
import com.sun.lwuit.Component;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Button;
import com.sun.lwuit.events.*;
import com.sun.lwuit.layouts.*;
import com.sun.lwuit.List;
import com.sun.lwuit.Command;
import com.sun.lwuit.Component;
import com.sun.lwuit.Label;
import com.sun.lwuit.Button;
import com.sun.lwuit.events.*;
import com.sun.lwuit.list.*;
import com.sun.lwuit.*;
import javax.microedition.io.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.Vector;

/**
 *
 * @author steffen
 */
public class ScriptForm extends Form implements ActionListener, Runnable {

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
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        cellList.setListCellRenderer(new ScriptCellRenderer());
        cellList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                System.out.println(cellList.getSelectedItem().toString());
                ScriptCell cell = (ScriptCell) cellList.getSelectedItem();
                if (cell != null) {
                    //Dialog.show("Content", cell.toString(), "ok", null);
                    if (cell.execute(1, myEngine)) {
                        repaint();
                        mainMidget.outputDisplayIfAny();
                    }
                }
            }
        });
        //cellList.addItem(new ScriptCell("Voltage", "battery", "12.5V",true, false));
        this.addComponent(cellList);
        this.addCommand(backCommand = new Command("Back"));
        this.addCommand(detailCommand = new Command("Detail"));
        addCommandListener(this);
        show();
        this.setFocused(cellList);
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

    public void actionPerformed(ActionEvent ae) {

        Command command = ae.getCommand();
        if (command == backCommand) {
            parent.show();
        }
        if (command == detailCommand) {
            ScriptCell cell = (ScriptCell) cellList.getSelectedItem();
            if (cell != null) {
                Dialog.show("Content", cell.toString(), "ok", null);
            }
        }

    }

//    class MyRenderer extends Label implements ListCellRenderer {
//
//        private Label focus = new Label("");
//
//        MyRenderer(String text) {
//            setText(text);
//            focus.getStyle().setBgTransparency(100);
//        }
//
//        public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {
//            setText(value.toString());
//            if (isSelected) {
//                setFocus(true);
//                getStyle().setBgTransparency(100);
//                getStyle().setBgColor(255, true);
//            } else {
//                setFocus(false);
//                getStyle().setBgTransparency(0);
//            }
//            return this;
//        }
//
//        public String toString() {
//            return getText();
//        }
//
//        public Component getListFocusComponent(List list) {
//            return focus;
//        }
//    }

    class ScriptCellRenderer extends Container implements ListCellRenderer {

        private Label title = new Label("");
        private Label value = new Label("");
        private Label update = new Label("");
        private Label timer = new Label("");
        private Label focus = new Label("");

        public ScriptCellRenderer() {
            if (true) {
                setLayout(new BorderLayout());
                Container cntLeft = new Container(new BoxLayout(BoxLayout.Y_AXIS));
                Container cntRight = new Container(new BoxLayout(BoxLayout.Y_AXIS));
                value.getStyle().setBgTransparency(0);
                value.getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                title.getStyle().setBgTransparency(0);
                update.getStyle().setBgTransparency(0);
                timer.getStyle().setBgTransparency(0);
                cntLeft.addComponent(value);
                cntLeft.addComponent(title);
                cntRight.addComponent(update);
                cntRight.addComponent(timer);
                addComponent(BorderLayout.CENTER, cntLeft);
                addComponent(BorderLayout.EAST, cntRight);
            } else {


                setLayout(new GridLayout(2, 2));
                value.getStyle().setBgTransparency(0);
                value.getStyle().setFont(Font.createSystemFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
                title.getStyle().setBgTransparency(0);
                update.getStyle().setBgTransparency(0);
                timer.getStyle().setBgTransparency(0);
                addComponent(value);
                addComponent(update);
                addComponent(title);
                addComponent(timer);
            }
            focus.getStyle().setBgTransparency(100);
        }

        public Component getListCellRendererComponent(List list, Object value, int index, boolean isSelected) {

            ScriptCell cell = (ScriptCell) value;
            this.title.setText(cell.getTitle());
            this.value.setText(cell.getValue());
            //this.update.setIcon(person.getPic());
            if (cell.getUpdate()) {
                this.update.setText("u");
            } else {
                this.update.setText("-");
            }
            if (cell.getTimer()) {
                this.timer.setText("t");
            } else {
                this.timer.setText("-");
            }
            if (isSelected) {
                this.getStyle().setBgColor(5);
            }
            if (isSelected) {
                setFocus(true);
                getStyle().setBgTransparency(100);
            } else {
                setFocus(false);
                getStyle().setBgTransparency(0);
            }



            return this;
        }

        public Component getListFocusComponent(List list) {
            return focus;
        }
    }
//Read more: http://lwuit.blogspot.com/2008/07/lwuit-list-renderer-by-chen-fishbein.html#ixzz0q3CKIUfz
}
