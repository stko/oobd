/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.ui.swing.support;

import java.io.FileNotFoundException;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.HashMap;
import org.oobd.base.*;
import org.oobd.base.OOBDConstants;
import java.net.URL;
import java.net.URLClassLoader;
import org.oobd.base.port.ComPort_Win;
import org.oobd.base.scriptengine.OobdScriptengine;
import org.oobd.crypt.AES.EncodeDecodeAES;

//import java.io.FileInputStream;
import java.io.*;
import javax.swing.JFileChooser;
import org.oobd.base.archive.Archive;
import org.oobd.base.archive.Factory;
import org.oobd.base.support.Onion;
import org.oobd.base.port.ComPort_Unix;
import org.oobd.crypt.AES.PassPhraseProvider;

/**
 * This class is the connection between the generic oobd system and the enviroment for e.g. IO operations
 * @author steffen
 */
public class SwingSystem implements IFsystem, OOBDConstants {

    Core core;
    private String userPassPhrase = "";

    public void registerOobdCore(Core thisCore) {
        core = thisCore;
    }

    public HashMap loadOobdClasses(String path, String classPrefix, Class<?> classType) {
        HashMap<String, Class<?>> myInstances = new HashMap<String, Class<?>>();
        // TODO copy this code section to Swing / ME IFSystem implementation

        File directory = new File(path);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            URL sourceURL = null;
            try {
                // read the path of the directory
                sourceURL = directory.toURI().toURL();
            } catch (java.net.MalformedURLException ex) {
                Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "", ex.getMessage());
            }
            // generate URLClassLoader for that directory

            URLClassLoader loader = new URLClassLoader(new java.net.URL[]{sourceURL}, Thread.currentThread().getContextClassLoader());
            // For each file in dir...
            for (int i = 0; i
                    < files.length; i++) {
                // split file name into name and extension
                Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "File to be load as class: " + files[i].getName());
                String name[] = files[i].getName().split("\\.");
                // only class names without $ are taken
                if (name.length > 1 && name[1].equals("class") && name[1].indexOf("$") == -1) {
                    try {
                        // load the class itself
                        Class<?> source = loader.loadClass(classPrefix + name[0]);
                        // Prüfen, ob die geladene Klasse das Interface implementiert
                        // bzw. ob sie das Interface beerbt
                        // Das Interface darf dabei natürlich nicht im selben Verzeichnis liegen
                        // oder man muss prüfen, ob es sich um ein Interface handelt Class.isInterface()
                        if (classType.isAssignableFrom(source)) {
                            // save unitialized class object in hashmap
                            myInstances.put(name[0], source);
                        } else {
                            Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, classPrefix + name[0] + "can not be inherited from " + classType.getName());
                        }

                    } catch (ClassNotFoundException ex) {
                        // Wird geworfen, wenn die Klasse nicht gefunden wurde
                        Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "couldn't load class", ex);
                        ex.printStackTrace();
                    }

                }
            }
        }// if
        else {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "Directory " + directory.getName() + " does not exist. Class " + classPrefix + " could not be loaded.");
        }
        // returns Hashmap filled with classes found

        return myInstances;

    }

    public String generateUIFilePath(int pathID, String fileName) {
        switch (pathID) {

            case FT_RAW:
            case FT_DATABASE:
                return fileName;
            case FT_KEY:
                System.out.println("key path generated:" + System.getProperty("user.home") + java.io.File.separator + fileName);
                return System.getProperty("user.home") + java.io.File.separator + fileName;

            default:
                return fileName;

        }


    }

    public InputStream generateResourceStream(int pathID, String resourceName)
            throws java.util.MissingResourceException {
        Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "Try to load: " + resourceName
                + " with path ID : " + pathID);
        InputStream resource = null;
        try {
            switch (pathID) {
                case OOBDConstants.FT_PROPS:
                    resource = new FileInputStream(generateUIFilePath(pathID,
                            resourceName));
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_RAW:
                    resource = new FileInputStream(generateUIFilePath(pathID,
                            resourceName));
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_DATABASE:
                case OOBDConstants.FT_SCRIPT:
                    String filePath = generateUIFilePath(pathID, resourceName);
                    Archive achive = Factory.getArchive(filePath);
                    achive.bind(filePath);
                    resource = achive.getInputStream("");
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_KEY:
                    resource = new FileInputStream(System.getProperty("user.home") + java.io.File.separator + resourceName);
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "Key File "
                            + resourceName + " loaded");
                    break;


                default:
                    throw new java.util.MissingResourceException("Resource not known",
                            "OOBDApp", resourceName);

            }

        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "generateResourceStream: File {0} not loaded", resourceName);
        }
        return resource;
    }

    public Object supplyHardwareHandle(Onion typ) {
        String osname = System.getProperty("os.name", "").toLowerCase();
        Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "OS detected: " + osname);
        try {
            if (osname.startsWith("windows")) {
                return new ComPort_Win();
            } else {
                return new ComPort_Unix();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public Preferences loadPreferences(int pathID, String filename) {
        Preferences myPrefs = null;

        try {
            Preferences prefsRoot;
            prefsRoot = Preferences.userRoot();
            prefsRoot.sync();
            myPrefs = prefsRoot.node("com.oobd.preference." + filename);
            if (myPrefs.keys().length == 0 && OOBDConstants.CorePrefsFileName.equalsIgnoreCase(filename)) { //no entries yet
                //generate system specific settings
                myPrefs.put("EngineClassPath", "scriptengine");
                myPrefs.put("ProtocolClassPath", "protocol");
                myPrefs.put("BusClassPath", "bus");
                myPrefs.put("DatabaseClassPath", "db");
                myPrefs.put("UIHandlerClassPath", "uihandler");
                myPrefs.flush();
            }
            return myPrefs;
        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "could not load property id " + filename, e);
        }
        return myPrefs;
    }

    public boolean savePreferences(int pathID, String filename, Preferences properties) {
        try {
            properties.flush();
            return true;
        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "could not load property id " + filename, e);
            return false;
        }
    }

    public char[] getAppPassPhrase() {
        return PassPhraseProvider.getPassPhrase();
    }

    public String getUserPassPhrase() {
        if (userPassPhrase.equals("")) {
            return "";
        } else {
            try {
                return new String(EncodeDecodeAES.decrypt(new String(
                        getAppPassPhrase()), userPassPhrase));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "";
            }
        }
    }

    public void setUserPassPhrase(String upp) {
        try {
            userPassPhrase = EncodeDecodeAES.encrypt(new String(
                    getAppPassPhrase()), upp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            userPassPhrase = "";
        }
    }

    public void createEngineTempInputFile(OobdScriptengine eng) {
        File f = null;

        try {
            //do we have to delete a previous first?

            eng.removeTempInputFile();
            // creates temporary file
            f = File.createTempFile("oobd", null, null);

            // deletes file when the virtual machine terminate
            f.deleteOnExit();

            eng.setTempInputFile(f);

        } catch (Exception e) {
            // if any error occurs
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "could not create temp file! ", e);
        }

    }

    public String doFileSelector(String path, final String extension, String message, Boolean save) {
        JFileChooser chooser = new JFileChooser();
        File oldDir = null;
        String oldDirName = path;
        if (oldDirName != null) {
            oldDir = new File(oldDirName);
        }
        chooser.setCurrentDirectory(oldDir);
        chooser.setSelectedFile(oldDir);
        chooser.setMultiSelectionEnabled(false);
        if (save){
        chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        }else{
            chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        }
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                if (extension != null) {
                    return f.getName().toLowerCase().endsWith(extension);
                } else {
                    return true;
                }
            }

            public String getDescription() {
                return extension + " Ext";
            }
        });
        if ((save && chooser.showSaveDialog(null)== JFileChooser.APPROVE_OPTION) || (!save &&chooser.showOpenDialog(null)== JFileChooser.APPROVE_OPTION)
                ) {
            try {
                return chooser.getSelectedFile().getAbsolutePath().toString();

            } catch (Exception ex) {
                Logger.getLogger(SwingSystem.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return null;
        }
    }
}
