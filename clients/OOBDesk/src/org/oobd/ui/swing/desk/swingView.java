/*
 * swingView.java
 */
package org.oobd.ui.swing.desk;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.io.IOException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileOutputStream;

import jssc.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.oobd.base.*;
import org.oobd.base.Core;
import org.oobd.base.visualizer.*;
import org.oobd.base.uihandler.OobdUIHandler;
import org.oobd.base.support.Onion;

import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import static org.oobd.base.OOBDConstants.FT_PROPS;
import org.oobd.base.archive.*;
import org.oobd.base.port.OOBDPort;
import org.oobd.base.port.PortInfo;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.support.OnionWrongTypeException;

/**
 * The application's main frame.
 */
public class swingView extends org.jdesktop.application.FrameView implements org.oobd.base.OOBDConstants, ActionListener {

    final static String MAINPANEL = "card2";
   Core core;
     private Vector<IFvisualizer> pageObjects = null;
    private boolean alreadyRefreshing;
      MouseAdapter popupMenuHandle;
    private final Timer timer;
    private int processBarMax = 100;
    String connectURLDefault = "";
 
    public swingView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

 
        timer = new Timer(1000, this);
        timer.setInitialDelay(500);
        timer.start();

       statusMessageLabel.setText("Selected the Script you want to use and press Start");

        getFrame().setIconImage(Toolkit.getDefaultToolkit().createImage(swingView.class.getResource("/org/oobd/base/images/obd2_icon.png")));

    }

    void setStatusLine(String propertyName, Object content) {

        if ("started".equals(propertyName)) {
            if (!busyIconTimer.isRunning()) {
                statusAnimationLabel.setIcon(busyIcons[0]);
                busyIconIndex = 0;
                busyIconTimer.start();
            }
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
        } else if ("done".equals(propertyName)) {
            busyIconTimer.stop();
            statusAnimationLabel.setIcon(idleIcon);
            progressBar.setVisible(false);
            progressBar.setValue(0);
        } else if ("message".equals(propertyName)) {
            String text = (String) content;
            statusMessageLabel.setText((text == null) ? "" : text);
        } else if ("progress".equals(propertyName)) {
            int value = (Integer) content;
            progressBar.setMaximum(processBarMax);
            progressBar.setVisible(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(value);
        } else if ("maximum".equals(propertyName)) {
            processBarMax = (Integer) content;
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = swing.getApplication().getMainFrame();
            aboutBox = new swingAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        swing.getApplication().show(aboutBox);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        main = new javax.swing.JPanel();
        startButtonLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        oobdImage = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        adminTextField = new javax.swing.JTextField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        mainPanel.add(main,MAINPANEL);
        mainPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainPanelComponentResized(evt);
            }
        });
        mainPanel.setLayout(new java.awt.CardLayout());

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.oobd.ui.swing.desk.swing.class).getContext().getResourceMap(swingView.class);
        main.setBackground(resourceMap.getColor("main.background")); // NOI18N
        main.setForeground(resourceMap.getColor("main.foreground")); // NOI18N
        main.setName("main"); // NOI18N
        main.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                mainComponentShown(evt);
            }
        });
        main.setLayout(new java.awt.BorderLayout());

        startButtonLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        startButtonLabel.setIcon(resourceMap.getIcon("startButtonLabel.icon")); // NOI18N
        startButtonLabel.setText(resourceMap.getString("startButtonLabel.text")); // NOI18N
        startButtonLabel.setToolTipText(resourceMap.getString("startButtonLabel.toolTipText")); // NOI18N
        startButtonLabel.setName("startButtonLabel"); // NOI18N
        startButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                startButtonLabelMouseClicked(evt);
            }
        });
        main.add(startButtonLabel, java.awt.BorderLayout.CENTER);

        jPanel2.setBackground(resourceMap.getColor("jPanel2.background")); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        oobdImage.setBackground(resourceMap.getColor("oobdImage.background")); // NOI18N
        oobdImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        oobdImage.setIcon(resourceMap.getIcon("oobdImage.icon")); // NOI18N
        oobdImage.setText(resourceMap.getString("oobdImage.text")); // NOI18N
        oobdImage.setName("oobdImage"); // NOI18N
        jPanel2.add(oobdImage, java.awt.BorderLayout.CENTER);

        jPanel3.setBackground(resourceMap.getColor("jPanel3.background")); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        adminTextField.setBackground(resourceMap.getColor("adminTextField.background")); // NOI18N
        adminTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        adminTextField.setText(resourceMap.getString("adminTextField.text")); // NOI18N
        adminTextField.setName("adminTextField"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(176, 176, 176)
                .addComponent(adminTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addGap(187, 187, 187))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(adminTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        main.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        mainPanel.add(main, "card2");

        menuBar.setBackground(resourceMap.getColor("menuBar.background")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.oobd.ui.swing.desk.swing.class).getContext().getActionMap(swingView.class, this);
        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        fileMenu.add(aboutMenuItem);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 319, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents


    private void startButtonLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startButtonLabelMouseClicked
        if (true || UIHANDLER_WS_NAME.equalsIgnoreCase((String) Settings.readDataPool(DP_ACTUAL_UIHANDLER, Settings.getString(PropName_UIHander, UIHANDLER_WS_NAME)))) {

            //startButtonLabel.setIcon(resourceMap.getIcon("startButtonLabel.icon"));
            Core.getSingleInstance().getSystemIF().openBrowser();
        } 
    }//GEN-LAST:event_startButtonLabelMouseClicked

    private void mainComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainComponentShown
        statusMessageLabel.setText("Selected the Script you want to use and press Start");
    }//GEN-LAST:event_mainComponentShown

    private void mainPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainPanelComponentResized
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    refreshGrid();
                }
            });
        } else {
            refreshGrid();
        }
    }//GEN-LAST:event_mainPanelComponentResized

    @Action
    public void onClickButton_Back() {
        IFvisualizer back = null;
        if (pageObjects != null) {
            for (IFvisualizer vis : pageObjects) {
                if (back == null && vis.getVisualizer().getUpdateFlag(4)) {
                    back = vis;
                }
            }
        }
        if (back != null) {
            back.getVisualizer().updateRequest(OOBDConstants.UR_USER);
        } else {
            core.stopScriptEngine();
            CardLayout cl = (CardLayout) (mainPanel.getLayout());
            cl.show(mainPanel, MAINPANEL);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField adminTextField;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel main;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel oobdImage;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel startButtonLabel;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private Hashtable<String, String> outputBuffers = new Hashtable<String, String>();
    private String actualBufferName = OB_DEFAULT_NAME; // name of the actual writestring output, default is "display" for screen output

    /* as char[] and Arraylist<Character> are not compatible, we need to handle dislpay output and normal
     buffer handling independet from each other and only convert, when really needed.
     */
    char[] arrayListToCharArray(ArrayList<Character> input) {
        char[] actBuffer = new char[input.size()];
        int position = 0;
        for (char i : input) {
            actBuffer[position] = i;
            position++;
        }
        return actBuffer;
    }




 

   

  

  

    void refreshGrid() {
        //build the components out of the previously collected list of vsiualizers
        // it seems that sometimes the resize form event is called during rezize Event... which generates an endless
        // recursive loop. To avoid this, the variable alreadyRefreshing started should indicate that there's already a refresh ongoing..
        alreadyRefreshing = false;
        if (pageObjects != null && !alreadyRefreshing) {
            alreadyRefreshing = true;
           for (IFvisualizer vis : pageObjects) {
                JComponent newJComponent = (JComponent) vis;
             }
            alreadyRefreshing = false;
        }
    }

    @Action
    public void onClickButton_Update() {
        int i = 2;
        setStatusLine("started", null);
        if (pageObjects != null) {
            for (IFvisualizer vis : pageObjects) {
                if (vis.getVisualizer().getUpdateFlag(1)) {
                    vis.getVisualizer().updateRequest(OOBDConstants.UR_UPDATE);
                    setStatusLine("progress", 100 - 100 / i);
                    i++;
                }
            }
        }
        setStatusLine("done", 100 - 100 / i);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ( pageObjects != null) {
            synchronized (pageObjects) {
                for (IFvisualizer vis : pageObjects) {
                    if (vis.getVisualizer().getUpdateFlag(2)) {
                        vis.getVisualizer().updateRequest(OOBDConstants.UR_TIMER);
                    }
                }

            }
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                // Here, we can safely update the GUI
                // because we'll be called from the
                // event dispatch thread
                //updateOobdUI();
            }
        });
        timer.restart();
    }



    private int checkKeyFiles() {
        Boolean userKeyExist;
        Boolean groupKeyExist;
        Boolean newUserKeyExist;
        Boolean newGroupKeyExist;
        try {
            InputStream keyfile = core.getSystemIF().generateResourceStream(
                    OOBDConstants.FT_KEY, OOBDConstants.PGP_USER_KEYFILE_NAME);
            userKeyExist = keyfile != null;
            keyfile.close();
        } catch (Exception e) {
            userKeyExist = false;
        }
        try {
            InputStream keyfile = core.getSystemIF().generateResourceStream(
                    OOBDConstants.FT_KEY, OOBDConstants.PGP_GROUP_KEYFILE_NAME);
            groupKeyExist = keyfile != null;
            keyfile.close();
        } catch (Exception e) {
            groupKeyExist = false;
        }
        try {
            InputStream keyfile = core.getSystemIF().generateResourceStream(
                    OOBDConstants.FT_RAW,
                    Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + OOBDConstants.PGP_USER_KEYFILE_NAME);
            newUserKeyExist = keyfile != null;
            keyfile.close();
        } catch (Exception e) {
            newUserKeyExist = false;
        }
        try {
            InputStream keyfile = core.getSystemIF().generateResourceStream(
                    OOBDConstants.FT_RAW,
                    Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + OOBDConstants.PGP_GROUP_KEYFILE_NAME);
            newGroupKeyExist = keyfile != null;
            keyfile.close();
        } catch (Exception e) {
            newGroupKeyExist = false;
        }
        return (userKeyExist ? 0 : 8) + (groupKeyExist ? 0 : 4)
                + (newUserKeyExist ? 2 : 0) + (newGroupKeyExist ? 1 : 0);
    }

    private void deleteKeyFiles() {

        File f = new File(core.getSystemIF().generateUIFilePath(OOBDConstants.FT_KEY, OOBDConstants.PGP_USER_KEYFILE_NAME));
        f.delete();
        f = new File(core.getSystemIF().generateUIFilePath(OOBDConstants.FT_KEY, OOBDConstants.PGP_GROUP_KEYFILE_NAME));
        f.delete();
    }

    private void importKeyFiles() {
        if (importsingleKeyFile(Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + OOBDConstants.PGP_USER_KEYFILE_NAME,
                OOBDConstants.PGP_USER_KEYFILE_NAME)) {
            File f = new File(core.getSystemIF().generateUIFilePath(
                    OOBDConstants.FT_SCRIPT,
                    Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + OOBDConstants.PGP_USER_KEYFILE_NAME));
            f.delete();
        }
        if (importsingleKeyFile(Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + OOBDConstants.PGP_GROUP_KEYFILE_NAME,
                OOBDConstants.PGP_GROUP_KEYFILE_NAME)) {
            File f = new File(core.getSystemIF().generateUIFilePath(
                    OOBDConstants.FT_SCRIPT,
                    Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + OOBDConstants.PGP_GROUP_KEYFILE_NAME));
            f.delete();
        }
    }

    private boolean importsingleKeyFile(String from, String to) {
        FileOutputStream fos;
        InputStream inFile = core.getSystemIF().generateResourceStream(
                OOBDConstants.FT_RAW, from);
        if (inFile != null) {
            try {
                fos = new FileOutputStream(core.getSystemIF().generateUIFilePath(
                        OOBDConstants.FT_KEY, to));
                org.apache.commons.io.IOUtils.copy(inFile, fos);
                inFile.close();
                fos.close();
                return true;
            } catch (IOException e) {
                // e.printStackTrace(); no stacktrace needed
            }
        }
        return false;

    }

    boolean saveBufferToFile(String fileName, String content, boolean append) {
        try {
            Writer os = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fileName, append), "UTF-8"));
            os.write(content);
            os.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(swingView.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private String saveBufferAsFileRequest(String FileName, String content, boolean append) {
        String oldDirName = Settings.getString(OOBDConstants.PropName_OutputFile, null);
        JFileChooser chooser = new JFileChooser(oldDirName);
        File oldDir = null;
        if (FileName != null) {
            oldDir = new File(FileName);
            chooser.setSelectedFile(oldDir);
        }
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.resetChoosableFileFilters();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }
                if (ext != null) {
                    if (ext.equals("txt")
                            || ext.equals("csv")) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "Text Files";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }
                if (ext != null) {
                    if (ext.equals("xml")) {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }

            public String getDescription() {
                return "XML Files";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {

                return true;
            }

            public String getDescription() {
                return "All Files";
            }
        });
        if (chooser.showSaveDialog(this.getFrame())
                == JFileChooser.APPROVE_OPTION && saveBufferToFile(chooser.getSelectedFile().toString(), content, append)) {
            Settings.setString(OOBDConstants.PropName_OutputFile, chooser.getCurrentDirectory().toString());
            return chooser.getSelectedFile().toString();
        } else {
            return null;
        }
    }


}

class PWDialog extends JDialog implements ActionListener {

    private JPanel myPanel = null;
    private JButton yesButton = null;
    private JButton noButton = null;
    final JPasswordField pgpPassword = new JPasswordField("");

    public PWDialog(JFrame frame) {
        super(frame, true);
        myPanel = new JPanel();
        myPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        getContentPane().add(myPanel);

        JLabel text = new JLabel("Enter PGP passphrase");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;

        myPanel.add(text, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;

        myPanel.add(pgpPassword, c);
        JCheckBox pgpShowPW = new JCheckBox("show Passphrase");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;

        myPanel.add(pgpShowPW, c);
        pgpShowPW.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    pgpPassword.setEchoChar((char) 0);
                } else {
                    pgpPassword.setEchoChar('*');
                }
            }
        });

        yesButton = new JButton("Ok");
        yesButton.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 3;

        myPanel.add(yesButton, c);
        noButton = new JButton("Not Today");
        noButton.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.0;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 3;
        myPanel.add(noButton, c);
        pack();
        setLocationRelativeTo(frame);

    }

    public String showDialog() {
        setVisible(true);
        return new String(pgpPassword.getPassword());
    }

    public void actionPerformed(ActionEvent e) {
        if (yesButton == e.getSource()) {
            setVisible(false);
        } else if (noButton == e.getSource()) {
            pgpPassword.setText("");
            setVisible(false);
        }
    }

}
