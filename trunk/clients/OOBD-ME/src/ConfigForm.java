
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
public class ConfigForm extends Form implements ActionListener, Runnable {

    private Form parent; //Where this form was started from
    private BTSerial btComm; //Where the bluetooth routines are
    private Command backCommand = null;
    Button scriptSelect = null;
    ConfigForm mySelf = null;

    public ConfigForm(Form parent, BTSerial btComm) {
        super("Config");
        this.parent = parent;
        this.btComm = btComm;
        this.mySelf = this;
        new Thread(this).start();
        showForm();
    }

    public String showForm() {
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Label btDeviceLabel = new Label(" Bluetooth Device");
        btDeviceLabel.setTextPosition(Component.BOTTOM);
        this.addComponent(btDeviceLabel);

        final Button btDevice = new Button("Search...");
        btDevice.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                btComm.getDeviceURL(mySelf);
                if (btComm.URL != null) {
                    btDevice.setText(btComm.URL);
                } else {
                    btDevice.setText("?");
                }

            }
        });
        this.addComponent(btDevice);
        Label scriptLabel = new Label(" LUA Script:");
        scriptLabel.setTextPosition(Component.BOTTOM);
        this.addComponent(scriptLabel);

        scriptSelect = new Button("Search...");
        scriptSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                FileDialog openFile = new FileDialog(mySelf, "Select Script", null, ".lbc");
                String myFile = null; //
                System.out.println("Zurück im Hauptprogramm");

            }
        });
        this.addComponent(scriptSelect);
        this.addCommand(backCommand = new Command("Back"));
        addCommandListener(this);
        show();
        return "";

    }

    public void setFileDialogResult(String directory, String filename) {
        if (directory !=null && filename != null) {
            scriptSelect.setText(directory+filename);

        } else {
            scriptSelect.setText("????");
        }

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
    }
}
