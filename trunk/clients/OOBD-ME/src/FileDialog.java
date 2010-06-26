
import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
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

public class FileDialog extends Form implements ActionListener, Runnable {

    private String currDirName;
    private ConfigForm parent; //Where this form was started from
    private String ext;
    private Command backCommand = null;
    Form mySelf = null;
    private List browser = new List();
    private final static String UP_DIRECTORY = "..";
    private final static String MEGA_ROOT = "/";
    private final static String SEP_STR = "/";
    private final static char SEP = '/';

    public FileDialog(ConfigForm parent, String title, String path, String ext) {
        super(title);
        this.parent = parent;
        this.mySelf = this;
        this.addComponent(browser);
        new Thread(this).start();
        if (path == null || "".equals(path)) {
            currDirName = MEGA_ROOT;
        } else {
            currDirName = getPath(path);
        }
        this.ext = ext;
        if (System.getProperty(
                "microedition.io.file.FileConnection.version") != null) {
            showForm();
            mySelf.show();
            showCurrDir();
//
        } else {
            Dialog.show("No File System", "Sorry, but this mobile does not support file access", "Cancel", null);
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

    void showForm() {
        this.addCommand(backCommand = new Command("Back"));
        addCommandListener(this);
        browser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                final String currFile = browser.getSelectedItem().toString();
                //             new Thread(new Runnable() {
                //               public void run() {
                if (currFile != null) {
                    if (currFile.endsWith(SEP_STR) ||
                            currFile.equals(UP_DIRECTORY)) {
                        traverseDirectory(currFile);
                        showCurrDir();
                    } else {
                        parent.setFileDialogResult(currDirName, currFile);
                        parent.showBack();

                    }
                } else {
                    System.out.println("no filename read from list :-(");

                }
                //                 }
                //            });
            }
        });

        System.out.println("Try to show filedialog form");

    }

    public void actionPerformed(ActionEvent ae) {
        Command command = ae.getCommand();
        if (command == backCommand) {
            parent.setFileDialogResult(null, null);
            parent.showBack();
        }
    }

    void showCurrDir() {
        Enumeration e;
        FileConnection currDir = null;
        while (browser.getModel().getSize() > 0) { // umständlicher gehts nicht...
            browser.getModel().removeItem(0);
        }
        System.out.println("Try showCurrDir");
        System.out.println("currDirName: " + currDirName);
        try {
            if (MEGA_ROOT.equals(currDirName)) {
                e = FileSystemRegistry.listRoots();
            } else {
                currDir = (FileConnection) Connector.open(
                        "file://localhost/" + currDirName);
                e = currDir.list();
                browser.addItem(UP_DIRECTORY);
            }
            while (e.hasMoreElements()) {
                String fileName = (String) e.nextElement();
                System.out.println("Try showCurrDir found; " + fileName);
                if (fileName.charAt(fileName.length() - 1) == SEP) {
                    browser.addItem(fileName);
                } else {
                    browser.addItem(fileName);
                }
            }
            if (currDir != null) {
                currDir.close();
            }
            repaint();
        } catch (IOException ioe) {
            System.out.println(ioe.toString());

        }
    }

    void traverseDirectory(String fileName) {
        if (currDirName.equals(MEGA_ROOT)) {
            if (fileName.equals(UP_DIRECTORY)) {
                // can not go up from MEGA_ROOT
                return;
            }
            currDirName = fileName;
        } else if (fileName.equals(UP_DIRECTORY)) {
            // Go up one directory
            // TODO use setFileConnection when implemented
            int i = currDirName.lastIndexOf(SEP, currDirName.length() - 2);
            if (i != -1) {
                currDirName = currDirName.substring(0, i + 1);
            } else {
                currDirName = MEGA_ROOT;
            }
        } else {
            currDirName = currDirName + fileName;
        }
        showCurrDir();
    }

    String getPath(String filename) {
        if (filename == null || "".equals(filename) || filename.equals(MEGA_ROOT)) {
            return MEGA_ROOT;
        } else {
            //int i = filename.lastIndexOf(SEP, filename.length() - 2);
            int i = filename.lastIndexOf(SEP);
            if (i != -1) {
                return filename.substring(0, i + 1);
            } else {
                return MEGA_ROOT;
            }

        }
    }
}

