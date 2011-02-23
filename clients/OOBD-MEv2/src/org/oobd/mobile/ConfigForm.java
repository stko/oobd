package org.oobd.mobile;


import javax.microedition.lcdui.*;

/**
 *
 * @author steffen
 */
public class ConfigForm extends Form implements CommandListener,ItemCommandListener, Runnable {


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
    Command backCmd;
    Command btCmd;
    Command scriptCmd;
    private final ChoiceGroup choiceGroup;



    public ConfigForm(Form parent, BTSerial btComm, OOBD_MEv2 mainMidlet) {
        super("OOBD Configuration");
        this.parent = parent;
        this.mainMidlet = mainMidlet;
        this.btComm= btComm;

        new Thread(this).start();

        btConf= new TextField("Configure Bluetooth Device:", "...Search", 32, TextField.UNEDITABLE);
        btCmd=new Command("Configure Bluetooth", Command.ITEM, 0);
        btConf.addCommand(btCmd);
        btConf.setItemCommandListener(this);

        confSpacer = new Spacer(10,10);

        scriptConf = new TextField("Select script:", "/LUA.lbc", 32, TextField.UNEDITABLE);
        scriptCmd = new Command("Select Script", Command.ITEM, 0);
        scriptConf.addCommand(scriptCmd);
        scriptConf.setItemCommandListener(this);

        choiceGroup = new ChoiceGroup("Blind Mode (for tesing)", Choice.MULTIPLE);
        choiceGroup.append("activated", null);
        choiceGroup.setSelectedFlags(new boolean[] { false });
        
        this.append(btConf);
        this.append(confSpacer);
        this.append(scriptConf);
        this.append(choiceGroup);

        
        this.setCommandListener(this);
        Command back=new Command("Back",Command.BACK,0);
        this.addCommand(back);

        Display.getDisplay(mainMidlet).setCurrent(this);
    }

    public void commandAction(Command c, Item item) {
        if (c==btCmd){
            btComm.getDeviceURL(this, mainMidlet.getDisplay());
        }
        else if(c==scriptCmd){

        }
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void commandAction(Command c, Displayable d) {
        if (c==backCmd){
            mainMidlet.getDisplay().setCurrent(parent);
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
