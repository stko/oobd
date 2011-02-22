/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.mobile;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;
import javax.microedition.midlet.*;

/**
 * @author Axel
 */
public class OOBD_MEv2 extends MIDlet implements CommandListener {
    public void startApp() {

        btComm = new BTSerial();
        
        display = Display.getDisplay(this);

        mainwindow = new Form("OOBD-MEv2",null);
        mainwindow.setCommandListener(this);
        
        confCom = new Command("Config", Command.SCREEN,0);
        mainwindow.addCommand(confCom);
        startCom = new Command("Start", Command.OK,0);
        mainwindow.addCommand(startCom);

        display.setCurrent(mainwindow);
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        
    }

    public void commandAction(Command c, Displayable d) {
        if (d == mainwindow) {                                           
            if (c == confCom) {                                         
                if (configwindow == null){
                    configwindow = new ConfigForm(mainwindow, btComm, this);

                }
                                          
                display.setCurrent(configwindow);
            }                                           
        }
        if (d == configwindow) {
            if (c == back2mainCom) {
                display.setCurrent(mainwindow);
            }
        }
        
    }
    
    BTSerial btComm;
    Form mainwindow;
    ConfigForm configwindow;
    Spacer confSpacer;
    TextField btConf;
    TextField scriptConf;
    Command confCom;
    Command back2mainCom;
    Command startCom;
    Display display;

    
}