/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.ui.swing.support;

import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import org.oobd.base.archive.Archive;
import org.oobd.base.support.Onion;
import org.oobd.base.port.ComPort_Kadaver;
import org.oobd.base.port.ComPort_Telnet;
import org.oobd.crypt.AES.PassPhraseProvider;

/**
 * This class is the connection between the generic oobd system and the
 * enviroment for e.g. IO operations
 *
 * @author steffen
 */
public class SwingSystem implements IFsystem, OOBDConstants {

    Core core;
    private String userPassPhrase = "";
    String webRootDir = "";
    String webLibraryDir= "";
    Preferences appProbs;

    @Override
    public void registerOobdCore(Core thisCore) {
        core = thisCore;
    }


    @Override
    public String generateUIFilePath(int pathID, String fileName) {
        switch (pathID) {

            case FT_RAW:
            case FT_WEBPAGE:
            case FT_DATABASE:
                return fileName;
            case FT_KEY:
                return System.getProperty("user.home") + "/" + fileName;

            default:
                File myFile = new File(fileName);
                if (myFile.exists()) {
                    return myFile.getAbsolutePath();
                } else {
                    myFile = new File(appProbs.get(OOBDConstants.PropName_ScriptDir, "") + "/" + fileName);
                    if (myFile.exists()) {
                        return myFile.getAbsolutePath();
                    }
                    return fileName;
                }

        }

    }

    /*
     replaces leading directory alias against their physical location
     */
    String mapDirectory(String[] mapDir, String path) {
        int i = 0;
        while (i < mapDir.length) {
            if (path.toLowerCase().startsWith("/" + mapDir[i].toLowerCase()+"/")) {
                path = path.substring(mapDir[i].length() + 2);
                if (mapDir[i].toLowerCase().equalsIgnoreCase("theme") && path.toLowerCase().startsWith("default/")) { //map the theme folder to  the actual theme
                    path = core.readDataPool(DP_WEBUI_ACTUAL_THEME, "default") +"/"+ path.substring("default/".length());
                }
                return mapDir[i + 1] + path;
            }
            i += 2;
        }
        return null;
    }

    @Override
    public InputStream generateResourceStream(int pathID, String resourceName)
            throws java.util.MissingResourceException {
        Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "Try to load: " + resourceName
                + " with path ID : " + pathID);
        InputStream resource = null;
        Archive scriptArchive = (Archive) core.readDataPool(DP_ACTIVE_ARCHIVE, null);
        try {
            switch (pathID) {
                case OOBDConstants.FT_WEBPAGE:
                    appProbs = core.getSystemIF().loadPreferences(FT_PROPS, OOBDConstants.AppPrefsFileName);
                    webRootDir = (String)core.readDataPool(DP_SCRIPTDIR, "")+ "/";
                    webLibraryDir = (String)core.readDataPool(DP_WWW_LIB_DIR, "")+ "/";
                     // in case the resource name points to a "executable" scriptengine, the engine get started 
                    // and the resourcename is corrected to the html start page to be used
                    resourceName = core.startScriptEngineByURL(resourceName);
                    scriptArchive = (Archive) core.readDataPool(DP_ACTIVE_ARCHIVE, null);
                    String mapping = mapDirectory(new String[]{"libs", webLibraryDir + "libs/", "theme", webLibraryDir + "theme/"}, resourceName);
                    if (mapping != null) {
                        return new FileInputStream(generateUIFilePath(pathID, mapping));
                    }
                    if (scriptArchive != null) {
                        return scriptArchive.getInputStream(resourceName);
                    }

                    break;
                case OOBDConstants.FT_PROPS:
                case OOBDConstants.FT_RAW:
                    resource = new FileInputStream(generateUIFilePath(pathID,
                            resourceName));
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_DATABASE:
                    if (scriptArchive != null) {
                        resource = scriptArchive.getInputStream(resourceName);
                    }
                    break;
                case OOBDConstants.FT_SCRIPT:
                    appProbs = core.getSystemIF().loadPreferences(FT_PROPS,
                            OOBDConstants.AppPrefsFileName);
                    // save actual script directory to buffer it for later as webroot directory
                    resource = scriptArchive.getInputStream(scriptArchive.getProperty(OOBDConstants.MANIFEST_SCRIPTNAME, OOBDConstants.MANIFEST_SCRIPTNAME_DEFAULT));
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "File " + resourceName
                            + " loaded");
                    break;

                case OOBDConstants.FT_KEY:
                    resource = new FileInputStream(System.getProperty("user.home") + "/" + resourceName);
                    Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "Key File "
                            + resourceName + " loaded");
                    break;

                default:
                    throw new java.util.MissingResourceException("Resource not known",
                            "OOBDApp", resourceName);

            }

        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.INFO, "generateResourceStream: File " + resourceName + " not loaded");
        }
        return resource;
    }

    @Override
    public Object supplyHardwareHandle(Onion typ) {
        appProbs = core.getSystemIF().loadPreferences(FT_PROPS,
                OOBDConstants.AppPrefsFileName);

        String connectURL = typ.getOnionBase64String("connecturl");
        String proxyHost = appProbs.get(OOBDConstants.PropName_ProxyHost, null);
        int proxyPort = appProbs.getInt(OOBDConstants.PropName_ProxyPort, 0);
        if (connectURL.toLowerCase().startsWith("ws")) {
            try {
                Proxy thisProxy = Proxy.NO_PROXY;
                if (proxyHost != null && proxyPort != 0) {
                    thisProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                    System.setProperty("https.proxyHost", proxyHost);
                    System.setProperty("https.proxyPort", Integer.toString(proxyPort));

                }
                return new ComPort_Kadaver(new URI(connectURL), thisProxy, proxyHost, proxyPort);

            } catch (URISyntaxException ex) {
                Logger.getLogger(SwingSystem.class.getName()).log(Level.SEVERE, null, ex);
                return null;

            }
        } else if (connectURL.equalsIgnoreCase(
                "serial")) {
            String osname = System.getProperty("os.name", "").toLowerCase();
            Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "OS detected: " + osname);

            try {
                return new ComPort_Win();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Preferences loadPreferences(int pathID, String filename) {
        Preferences myPrefs = null;

        try {
            Preferences prefsRoot;
            prefsRoot = Preferences.userRoot();
            prefsRoot.sync();
            myPrefs = prefsRoot.node("com.oobd.preference." + filename);
            return myPrefs;

        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.CONFIG, "could not load property id " + filename, e);
        }
        return myPrefs;
    }

    @Override
    public boolean savePreferences(int pathID, String filename, Preferences properties) {
        try {
            properties.flush();
            return true;

        } catch (Exception e) {
            Logger.getLogger(SwingSystem.class.getName()).log(Level.WARNING, "could not load property id " + filename, e);

            return false;
        }
    }

    @Override
    public char[] getAppPassPhrase() {
        return PassPhraseProvider.getPassPhrase();
    }

    @Override
    public String getUserPassPhrase() {
        if (userPassPhrase.equals("")) {
            return "";
        } else {
            try {
                return EncodeDecodeAES.decrypt(new String(
                        getAppPassPhrase()), userPassPhrase);
            } catch (Exception e) {
                 e.printStackTrace();
                return "";
            }
        }
    }

    @Override
    public void setUserPassPhrase(String upp) {
        try {
            userPassPhrase = EncodeDecodeAES.encrypt(new String(
                    getAppPassPhrase()), upp);
        } catch (Exception e) {
            e.printStackTrace();
            userPassPhrase = "";
        }
    }

    @Override
    public void createEngineTempInputFile(OobdScriptengine eng) {
        File f;

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

    @Override
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
        if (save) {
            chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        } else {
            chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        }
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {

            @Override
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

            @Override
            public String getDescription() {
                return extension + " Ext";
            }
        });
        if ((save && chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) || (!save && chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)) {
            try {
                return chooser.getSelectedFile().getAbsolutePath();

            } catch (Exception ex) {
                Logger.getLogger(SwingSystem.class.getName()).log(Level.SEVERE, null, ex);

                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Hashtable<String, Class> getConnectorList() {
        Hashtable<String, Class> connectClasses = new Hashtable<>();
        connectClasses.put(OOBDConstants.PropName_ConnectTypeBT, ComPort_Win.class);
        connectClasses.put(OOBDConstants.PropName_ConnectTypeRemoteConnect,
                ComPort_Kadaver.class);
        connectClasses.put(OOBDConstants.PropName_ConnectTypeRemoteDiscovery,
                ComPort_Telnet.class);
        connectClasses.put(OOBDConstants.PropName_ConnectTypeTelnet,
                ComPort_Telnet.class);

        return connectClasses;
    }

    @Override
    public DatagramSocket getUDPBroadcastSocket() {
        try {
            DatagramSocket socket = null;
            if (socket == null) {
                socket = new DatagramSocket(UDP_PORT);
            }
            socket.setBroadcast(true);
            return socket;
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        }
    }

}
