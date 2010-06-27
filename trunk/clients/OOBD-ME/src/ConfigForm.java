
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
    final int SCRIPTREQUEST = 1;
    final int THEMEREQUEST = 2;
    private MainMidlet mainMidlet;
    Button btDevice;

    public ConfigForm(Form parent, BTSerial btComm, MainMidlet mainMidlet) {
        super("OOBD ME Config");
        this.parent = parent;
        this.btComm = btComm;
        this.mySelf = this;
        this.mainMidlet = mainMidlet;
        new Thread(this).start();
        showForm();
    }

    public String showForm() {
        setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        Label btDeviceLabel = new Label(" Bluetooth Device:");
        btDeviceLabel.setTextPosition(Component.BOTTOM);
        this.addComponent(btDeviceLabel);

        btDevice = new Button("Search...");
        btDevice.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                btDevice.setText("Wait..");
                btComm.getDeviceURL(mySelf);
                if (btComm.URL != null) {
                    btDevice.setText(btComm.URL);
                } else {
                    btDevice.setText("Search..");
                }

            }
        });
        if (btComm.URL != null) {
            btDevice.setText(btComm.URL);
        } else {
            btDevice.setText("Search..");
        }
        this.addComponent(btDevice);
        Label scriptLabel = new Label("OOBD LUA Script:");
        scriptLabel.setTextPosition(Component.BOTTOM);
        this.addComponent(scriptLabel);

        scriptSelect = new Button(mainMidlet.getScript());
        scriptSelect.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                new FileDialog(mySelf, "Select Script", null, ".lbc", SCRIPTREQUEST); //open the FileDialog Form
            }
        });
        this.addComponent(scriptSelect);

        final CheckBox blindMode = new CheckBox("Blind mode (for testing w/o BT connection)");
        blindMode.setSelected(mainMidlet.getBlindMode());
        blindMode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ev) {
                mainMidlet.setBlindMode(blindMode.isSelected());
            }
        });
        this.addComponent(blindMode);

        this.addCommand(backCommand = new Command("Back"));
        addCommandListener(this);
        show();
        return "";

    }

    public void setFileDialogResult(String directory, String filename, int requestID) {
        if (directory != null && filename != null) {
            scriptSelect.setText(directory + filename);
            mainMidlet.setScript(directory + filename);

        } else {
            mainMidlet.setScript(null);
            scriptSelect.setText(mainMidlet.getScript());
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

    public void showNotify() {
        if (btDevice != null) {
            if (btComm.URL != null) {
                btDevice.setText(btComm.URL);
            } else {
                btDevice.setText("Search..");
            }
        }

    }

    public void actionPerformed(ActionEvent ae) {

        Command command = ae.getCommand();
        if (command == backCommand) {
            parent.show();
        }
    }
}
