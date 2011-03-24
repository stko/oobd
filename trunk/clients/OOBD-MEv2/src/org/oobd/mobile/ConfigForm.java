package org.oobd.mobile;

import javax.microedition.lcdui.*;
import org.netbeans.microedition.lcdui.pda.FileBrowser;

/**
 * @author axel
 */
public class ConfigForm extends Form implements CommandListener,ItemCommandListener, Runnable {

    private Display display;
    private Form parent; //Where this form was started from
    private BTSerial btComm; //Where the bluetooth routines are
    private OOBD_MEv2 mainMidlet;
    private TextField scriptConf;
    private TextField btConf;
    private Spacer confSpacer;
    private Spacer confSpacer2;
    private Command backCmd;
    private Command btCmd;
    private Command scriptCmd;
    private final ChoiceGroup choiceGroup;
    private FileBrowser fileBrowser;

    public ConfigForm(Form parent, BTSerial btComm, OOBD_MEv2 mainMidlet) {
        super("OOBD Configuration");
        this.parent = parent;
        this.mainMidlet = mainMidlet;
        this.btComm= btComm;

        display = Display.getDisplay(mainMidlet);

        btConf= new TextField("Configure Bluetooth Device:", "...Search", 32, TextField.UNEDITABLE);
        btCmd=new Command("Select", Command.ITEM, 0);
        btConf.addCommand(btCmd);
        btConf.setItemCommandListener(this);

        confSpacer = new Spacer(10,10);

        scriptConf = new TextField("Select script:", mainMidlet.getActScript(), 32, TextField.UNEDITABLE);
        scriptCmd = new Command("Select", Command.ITEM, 0);
        scriptConf.addCommand(scriptCmd);
        scriptConf.setItemCommandListener(this);

        confSpacer2 = new Spacer(10,10);

        choiceGroup = new ChoiceGroup("Blind Mode (for tesing)", Choice.MULTIPLE);
        choiceGroup.append("activated", null);
        choiceGroup.setSelectedFlags(new boolean[] { false });

        this.append(btConf);
        this.append(confSpacer);
        this.append(scriptConf);
        this.append(confSpacer2);
        this.append(choiceGroup);

        backCmd = new Command("Back",Command.BACK,0);
        this.setCommandListener(this);
        this.addCommand(backCmd);

        display.setCurrent(this);
    }

    public void commandAction(Command c, Item item) {
        if (c==btCmd){
            btComm.getDeviceURL(this, display);
            //TODO Improve Bluetooth choice
        }
        else if(c==scriptCmd){
            if (fileBrowser == null) {                
                fileBrowser = new FileBrowser(display);
                fileBrowser.setTitle("Select a script");
                fileBrowser.setCommandListener(this);
                fileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);                
            }
            display.setCurrent(fileBrowser);
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c==backCmd){
//            display.setCurrent(new Alert("test"));
            if (choiceGroup.isSelected(0)){
                mainMidlet.switchBlindMode(true);
            }
            else {
                mainMidlet.switchBlindMode(false);
            }
            display.setCurrent(parent);
        }
        else if(c==FileBrowser.SELECT_FILE_COMMAND){
            scriptConf.setString(fileBrowser.getSelectedFileURL());
            display.setCurrent(this);
        }
    }
    
    public void run() {
        System.out.println("CommandAction not yet supported");
    }

    public void setBTname(String btName){
        btConf.setString(btName);
    }
}
