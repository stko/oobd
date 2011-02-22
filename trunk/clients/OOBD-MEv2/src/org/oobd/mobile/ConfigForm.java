package org.oobd.mobile;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author steffen
 */
public class ConfigForm extends Form implements CommandListener, Runnable {

    private Form parent; //Where this form was started from
    private BTSerial btComm; //Where the bluetooth routines are
    private Command backCommand = null;
 //   Button scriptSelect = null;
    ConfigForm mySelf = null;
    final int SCRIPTREQUEST = 1;
    final int THEMEREQUEST = 2;
    private OOBD_MEv2 mainMidlet;
    private TextField scriptConf;
    private TextField btConf;
    private Spacer confSpacer;


    public ConfigForm(Form parent, BTSerial btComm, OOBD_MEv2 mainMidlet) {
        super("OOBD Configuration");
        this.parent = parent;
        this.mainMidlet = mainMidlet;
        this.btComm= btComm;

        new Thread(this).start();

        btConf= new TextField("Configure Bluetooth Device:", "...Search", 32, TextField.UNEDITABLE);
        confSpacer = new Spacer(10,10);
        scriptConf = new TextField("Select script:", "/LUA.lbc", 32, TextField.UNEDITABLE);

        this.append(btConf);
        this.append(confSpacer);
        this.append(scriptConf);

        this.setCommandListener(this);
        this.addCommand(new Command("Back",Command.BACK,0));

        Display.getDisplay(mainMidlet).setCurrent(this);
    }



    public void commandAction(Command c, Displayable d) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    
 //   Button btDevice;
/**
    public ConfigForm(Form parent, BTSerial btComm, OOBD_MEv2 mainMidlet) {
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

   

    public void showNotify() {
        if (btDevice != null) {
            if (btComm.URL != null) {
                btDevice.setText(btComm.URL);
            } else {
                btDevice.setText("Search..");
            }
        }

    }

**/
}
