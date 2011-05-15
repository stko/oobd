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
    private Command defaultCmd;
    private final ChoiceGroup choiceGroup;
    private FileBrowser fileBrowser;
    private MobileLogger log;

    //TODO Store prefered BT-Device

    public ConfigForm(Form parent, BTSerial btComm, OOBD_MEv2 mainMidlet) {
        super("OOBD Configuration");
        this.parent = parent;
        this.mainMidlet = mainMidlet;
        this.btComm= btComm;
        log = mainMidlet.getLog();

        display = Display.getDisplay(mainMidlet);

        btConf= new TextField("Configure Bluetooth Device:", mainMidlet.getBTurl(), 32, TextField.UNEDITABLE);
        btCmd=new Command("Select", Command.ITEM, 0);
        btConf.addCommand(btCmd);
        btConf.setItemCommandListener(this);

        confSpacer = new Spacer(10,10);

        scriptConf = new TextField("Select script:", mainMidlet.getActScript(), 32, TextField.UNEDITABLE);
        scriptCmd = new Command("Select", Command.ITEM, 0);
        scriptConf.addCommand(scriptCmd);
        defaultCmd = new Command("Default", Command.ITEM,0);
        scriptConf.addCommand(defaultCmd);
        scriptConf.setItemCommandListener(this);

        confSpacer2 = new Spacer(10,10);

        choiceGroup = new ChoiceGroup("Blind Mode (for testing)", Choice.MULTIPLE);
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
            Form scanning = new Form("Scanning...");
            scanning.append("...for Bluetooth devices");
            display.setCurrent(scanning);
            btComm.getDeviceURL(this, display);
            //TODO Improve Bluetooth choice
        }
        else if(c==scriptCmd){
            if (fileBrowser == null) {                
                try {
                    fileBrowser = new FileBrowser(display);              
                    fileBrowser.setTitle("Select a script");
                    fileBrowser.setCommandListener(this);
                    fileBrowser.addCommand(FileBrowser.SELECT_FILE_COMMAND);
                    fileBrowser.addCommand(backCmd);
                } catch (Exception ex){
                    log.log(ex.getMessage());
                }
            }
            display.setCurrent(fileBrowser);
        }
        else if (c==defaultCmd){
            scriptConf.setString(mainMidlet.getScriptDefault());
            mainMidlet.setScript(mainMidlet.getScriptDefault());
        }
    }
    
    public void commandAction(Command c, Displayable d) {
        if (c==backCmd){
            if (d==this){
                if (choiceGroup.isSelected(0)){
                    mainMidlet.switchBlindMode(true);
                }
                else {
                    mainMidlet.switchBlindMode(false);
                }
                display.setCurrent(parent);
            } else if (d==fileBrowser){
                display.setCurrent(this);
            }

        }
        else if(c==FileBrowser.SELECT_FILE_COMMAND){
            log.log ("Trying to get file from FileBrowser");
            String file = fileBrowser.getSelectedFileURL();
            log.log("Selected file: "+file);
            scriptConf.setString(file);
            log.log("Filename stored to config-menu");
            mainMidlet.setScript(file);
            log.log("Filename stored to main midlet");
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
