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
import org.oobd.base.IFui;
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
public class swingView extends org.jdesktop.application.FrameView implements IFui, org.oobd.base.OOBDConstants, ActionListener {

    final static String MAINPANEL = "card2";
    final static String DIAGNOSEPANEL = "card3";
    final static String SETTINGSPANEL = "card4";
    Core core;
     private Vector<IFvisualizer> pageObjects = null;
    private boolean alreadyRefreshing;
    private final String popupText_update = "Toggle Update Flag";
    private final String popupText_timer = "Toggle Timer Flag";
    private final String popupText_log = "Toggle Log Flag";
    MouseAdapter popupMenuHandle;
    private final Timer timer;
    private int processBarMax = 100;
    private int elementCount;
    private int defaultGridWidth = 200;
    private String pageTitle;
    String connectURLDefault = "";
    private String connectDeviceName;
    private String connectTypeName;
    private String oldConnectTypeName = null;
    private Hashtable<String, Class> supplyHardwareConnects;

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
        ActionListener popupActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String whichItemString = e.getActionCommand();
                int index = 0;
                if (popupText_update.equals(whichItemString)) {
                    index = 1;
                } else if (popupText_timer.equals(whichItemString)) {
                    index = 2;
                } else if (popupText_log.equals(whichItemString)) {
                    index = 3;
                }
                JMenuItem menuItem = ((JMenuItem) e.getSource());
                JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
                IFvisualizer vi = (IFvisualizer) popupMenu.getInvoker();
                vi.getVisualizer().setUpdateFlag(index, !vi.getVisualizer().getUpdateFlag(index));
                ((Component) vi).invalidate();
                ((Component) vi).validate();
                ((Component) vi).repaint();
            }
        };
        final JPopupMenu Pmenu = new JPopupMenu("Toggle Flags");
        JMenuItem menuItem = new JMenuItem(popupText_update, new ImageIcon(swingView.class.getResource("/org/oobd/base/images/update_16.png")));
        menuItem.addActionListener(popupActionListener);
        Pmenu.add(menuItem);
        menuItem = new JMenuItem(popupText_timer, new ImageIcon(swingView.class.getResource("/org/oobd/base/images/timer_16.png")));
        menuItem.addActionListener(popupActionListener);
        Pmenu.add(menuItem);
        menuItem = new JMenuItem(popupText_log, new ImageIcon(swingView.class.getResource("/org/oobd/base/images/text_16.png")));
        menuItem.addActionListener(popupActionListener);
        Pmenu.add(menuItem);
        Pmenu.setBorder(BorderFactory.createTitledBorder("Toggle Flags"));
        Pmenu.setBorderPainted(true);

        popupMenuHandle = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                //((JPanel) e.getComponent()).setBackground(Color.white);
                ((JPanel) e.getComponent()).setBorder(BorderFactory.createLineBorder(Color.black));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                //((JPanel) e.getComponent()).setBackground(Color.lightGray);
                ((JPanel) e.getComponent()).setBorder(BorderFactory.createEmptyBorder());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {

                    ((IFvisualizer) e.getComponent()).getVisualizer().updateRequest(OOBDConstants.UR_USER);
                }
            }

            public void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Pmenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        timer = new Timer(1000, this);
        timer.setInitialDelay(500);
        timer.start();

        diagnoseTabPanel.setSelectedComponent(diagnosePanel);
        diagnoseTitle.setText("");
        connectSymbol.setText("");
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
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        main = new javax.swing.JPanel();
        scriptSelectComboBox = new javax.swing.JComboBox();
        startButtonLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        oobdImage = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        settings = new javax.swing.JPanel();
        backButtonLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        comportComboBox = new javax.swing.JComboBox();
        chooseScriptDirectoryButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        scriptDir = new javax.swing.JTextField();
        pgpEnabled = new javax.swing.JCheckBox();
        pgpImportKeys = new javax.swing.JButton();
        pgpStatus = new javax.swing.JLabel();
        jLabelRemoteServer = new javax.swing.JLabel();
        jTextFieldRemoteServer = new javax.swing.JTextField();
        jTextFieldProxyHost = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSpinnerProxyPort = new javax.swing.JSpinner();
        httpEnabled = new javax.swing.JCheckBox();
        protocolComboBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        libraryDir = new javax.swing.JTextField();
        chooseLibsDirectoryButton = new javax.swing.JButton();
        diagnose = new javax.swing.JPanel();
        toolPanelDiagnose = new javax.swing.JPanel();
        diagnoseTitle = new javax.swing.JLabel();
        connectSymbol = new javax.swing.JLabel();
        diagnoseTabPanel = new javax.swing.JTabbedPane();
        diagnosePanel = new javax.swing.JPanel();
        diagnoseToolBar = new javax.swing.JToolBar();
        backButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        timerButton = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        gridSmallerButton = new javax.swing.JButton();
        gridBiggerButton = new javax.swing.JButton();
        diagnoseScrollPanel = new javax.swing.JScrollPane();
        diagnoseButtonPanel = new javax.swing.JPanel();
        outputPanel = new javax.swing.JPanel();
        outputToolbar = new javax.swing.JToolBar();
        outputBackButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        logButton = new javax.swing.JToggleButton();
        saveButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaOutput = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        mainPanel.add(main,MAINPANEL);
        mainPanel.add(settings,SETTINGSPANEL);
        mainPanel.add(diagnose,DIAGNOSEPANEL);
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

        scriptSelectComboBox.setName("scriptSelectComboBox"); // NOI18N
        main.add(scriptSelectComboBox, java.awt.BorderLayout.PAGE_END);

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 503, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        main.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        mainPanel.add(main, "card2");

        settings.setName("settings"); // NOI18N
        settings.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                settingsComponentHidden(evt);
            }
            public void componentShown(java.awt.event.ComponentEvent evt) {
                settingsComponentShown(evt);
            }
        });
        settings.setLayout(new java.awt.GridBagLayout());

        backButtonLabel.setIcon(resourceMap.getIcon("backButtonLabel.icon")); // NOI18N
        backButtonLabel.setText(resourceMap.getString("backButtonLabel.text")); // NOI18N
        backButtonLabel.setToolTipText(resourceMap.getString("backButtonLabel.toolTipText")); // NOI18N
        backButtonLabel.setName("backButtonLabel"); // NOI18N
        backButtonLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backButtonLabelMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(backButtonLabel, gridBagConstraints);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabel1, gridBagConstraints);

        comportComboBox.setEditable(true);
        comportComboBox.setName("comportComboBox"); // NOI18N
        comportComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comportComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(comportComboBox, gridBagConstraints);

        chooseScriptDirectoryButton.setIcon(resourceMap.getIcon("chooseScriptDirectoryButton.icon")); // NOI18N
        chooseScriptDirectoryButton.setText(resourceMap.getString("chooseScriptDirectoryButton.text")); // NOI18N
        chooseScriptDirectoryButton.setToolTipText(resourceMap.getString("chooseScriptDirectoryButton.toolTipText")); // NOI18N
        chooseScriptDirectoryButton.setName("chooseScriptDirectoryButton"); // NOI18N
        chooseScriptDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseScriptDirectoryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        settings.add(chooseScriptDirectoryButton, gridBagConstraints);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabel4, gridBagConstraints);

        scriptDir.setText(resourceMap.getString("scriptDir.text")); // NOI18N
        scriptDir.setName("scriptDir"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(scriptDir, gridBagConstraints);

        pgpEnabled.setText(resourceMap.getString("pgpEnabled.text")); // NOI18N
        pgpEnabled.setName("pgpEnabled"); // NOI18N
        pgpEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pgpEnabledActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(pgpEnabled, gridBagConstraints);

        pgpImportKeys.setText(resourceMap.getString("pgpImportKeys.text")); // NOI18N
        pgpImportKeys.setName("pgpImportKeys"); // NOI18N
        pgpImportKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pgpImportKeysActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(pgpImportKeys, gridBagConstraints);

        pgpStatus.setText(resourceMap.getString("pgpStatus.text")); // NOI18N
        pgpStatus.setName("pgpStatus"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(pgpStatus, gridBagConstraints);

        jLabelRemoteServer.setText(resourceMap.getString("jLabelRemoteServer.text")); // NOI18N
        jLabelRemoteServer.setName("jLabelRemoteServer"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabelRemoteServer, gridBagConstraints);

        jTextFieldRemoteServer.setText(resourceMap.getString("jTextFieldRemoteServer.text")); // NOI18N
        jTextFieldRemoteServer.setName("jTextFieldRemoteServer"); // NOI18N
        jTextFieldRemoteServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldRemoteServerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(jTextFieldRemoteServer, gridBagConstraints);

        jTextFieldProxyHost.setText(resourceMap.getString("jTextFieldProxyHost.text")); // NOI18N
        jTextFieldProxyHost.setName("jTextFieldProxyHost"); // NOI18N
        jTextFieldProxyHost.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldProxyHostActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(jTextFieldProxyHost, gridBagConstraints);

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabel2, gridBagConstraints);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabel5, gridBagConstraints);

        jSpinnerProxyPort.setName("jSpinnerProxyPort"); // NOI18N
        jSpinnerProxyPort.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                jSpinnerProxyPortAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(jSpinnerProxyPort, gridBagConstraints);

        httpEnabled.setText(resourceMap.getString("HTTPCheckBox.text")); // NOI18N
        httpEnabled.setToolTipText(resourceMap.getString("HTTPCheckBox.toolTipText")); // NOI18N
        httpEnabled.setName("HTTPCheckBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(httpEnabled, gridBagConstraints);

        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Discovery", "Kadaver", "Bluetooth", "Telnet" }));
        protocolComboBox.setSelectedIndex(2);
        protocolComboBox.setToolTipText(resourceMap.getString("connectionTypeSpinner.toolTipText")); // NOI18N
        protocolComboBox.setName("connectionTypeSpinner"); // NOI18N
        protocolComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protocolComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(protocolComboBox, gridBagConstraints);

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabel6, gridBagConstraints);

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        settings.add(jLabel7, gridBagConstraints);

        libraryDir.setText(resourceMap.getString("libraryDir.text")); // NOI18N
        libraryDir.setName("libraryDir"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(libraryDir, gridBagConstraints);

        chooseLibsDirectoryButton.setIcon(resourceMap.getIcon("chooseLibsDirectoryButton.icon")); // NOI18N
        chooseLibsDirectoryButton.setText(resourceMap.getString("chooseLibsDirectoryButton.text")); // NOI18N
        chooseLibsDirectoryButton.setName("chooseLibsDirectoryButton"); // NOI18N
        chooseLibsDirectoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseLibsDirectoryButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        settings.add(chooseLibsDirectoryButton, gridBagConstraints);

        mainPanel.add(settings, "card4");

        diagnose.setName("diagnose"); // NOI18N
        diagnose.setLayout(new javax.swing.BoxLayout(diagnose, javax.swing.BoxLayout.PAGE_AXIS));

        toolPanelDiagnose.setName("toolPanelDiagnose"); // NOI18N
        toolPanelDiagnose.setLayout(new javax.swing.BoxLayout(toolPanelDiagnose, javax.swing.BoxLayout.LINE_AXIS));

        diagnoseTitle.setText(resourceMap.getString("diagnoseTitle.text")); // NOI18N
        diagnoseTitle.setName("diagnoseTitle"); // NOI18N
        toolPanelDiagnose.add(diagnoseTitle);

        connectSymbol.setText(resourceMap.getString("connectSymbol.text")); // NOI18N
        connectSymbol.setName("connectSymbol"); // NOI18N
        toolPanelDiagnose.add(connectSymbol);

        diagnose.add(toolPanelDiagnose);

        diagnoseTabPanel.setName("diagnoseTabPanel"); // NOI18N

        diagnosePanel.setToolTipText(resourceMap.getString("diagnosePanel.toolTipText")); // NOI18N
        diagnosePanel.setName("diagnosePanel"); // NOI18N
        diagnosePanel.setLayout(new javax.swing.BoxLayout(diagnosePanel, javax.swing.BoxLayout.PAGE_AXIS));

        diagnoseToolBar.setRollover(true);
        diagnoseToolBar.setMaximumSize(new java.awt.Dimension(32767, 46));
        diagnoseToolBar.setName("diagnoseToolBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.oobd.ui.swing.desk.swing.class).getContext().getActionMap(swingView.class, this);
        backButton.setAction(actionMap.get("onClickButton_Back")); // NOI18N
        backButton.setIcon(resourceMap.getIcon("backButton.icon")); // NOI18N
        backButton.setText(resourceMap.getString("backButton.text")); // NOI18N
        backButton.setToolTipText(resourceMap.getString("backButton.toolTipText")); // NOI18N
        backButton.setFocusable(false);
        backButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        backButton.setName("backButton"); // NOI18N
        backButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        diagnoseToolBar.add(backButton);

        updateButton.setAction(actionMap.get("onClickButton_Update")); // NOI18N
        updateButton.setIcon(resourceMap.getIcon("updateButton.icon")); // NOI18N
        updateButton.setText(resourceMap.getString("updateButton.text")); // NOI18N
        updateButton.setToolTipText(resourceMap.getString("updateButton.toolTipText")); // NOI18N
        updateButton.setFocusable(false);
        updateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateButton.setName("updateButton"); // NOI18N
        updateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        diagnoseToolBar.add(updateButton);

        timerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/oobd/ui/swing/desk/resources/timer.png"))); // NOI18N
        timerButton.setText(resourceMap.getString("timerButton.text")); // NOI18N
        timerButton.setToolTipText(resourceMap.getString("timerButton.toolTipText")); // NOI18N
        timerButton.setFocusable(false);
        timerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        timerButton.setName("timerButton"); // NOI18N
        timerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        diagnoseToolBar.add(timerButton);

        jSeparator1.setName("jSeparator1"); // NOI18N
        diagnoseToolBar.add(jSeparator1);

        gridSmallerButton.setIcon(resourceMap.getIcon("gridSmallerButton.icon")); // NOI18N
        gridSmallerButton.setText(resourceMap.getString("gridSmallerButton.text")); // NOI18N
        gridSmallerButton.setToolTipText(resourceMap.getString("gridSmallerButton.toolTipText")); // NOI18N
        gridSmallerButton.setFocusable(false);
        gridSmallerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridSmallerButton.setName("gridSmallerButton"); // NOI18N
        gridSmallerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridSmallerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridSmallerButtonActionPerformed(evt);
            }
        });
        diagnoseToolBar.add(gridSmallerButton);

        gridBiggerButton.setIcon(resourceMap.getIcon("gridBiggerButton.icon")); // NOI18N
        gridBiggerButton.setText(resourceMap.getString("gridBiggerButton.text")); // NOI18N
        gridBiggerButton.setToolTipText(resourceMap.getString("gridBiggerButton.toolTipText")); // NOI18N
        gridBiggerButton.setFocusable(false);
        gridBiggerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        gridBiggerButton.setName("gridBiggerButton"); // NOI18N
        gridBiggerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBiggerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridBiggerButtonActionPerformed(evt);
            }
        });
        diagnoseToolBar.add(gridBiggerButton);

        diagnosePanel.add(diagnoseToolBar);

        diagnoseScrollPanel.setName("diagnoseScrollPanel"); // NOI18N

        diagnoseButtonPanel.setName("diagnoseButtonPanel"); // NOI18N
        diagnoseButtonPanel.setLayout(new java.awt.GridLayout(1, 0));
        diagnoseScrollPanel.setViewportView(diagnoseButtonPanel);

        diagnosePanel.add(diagnoseScrollPanel);

        diagnoseTabPanel.addTab(resourceMap.getString("diagnosePanel.TabConstraints.tabTitle"), resourceMap.getIcon("diagnosePanel.TabConstraints.tabIcon"), diagnosePanel, resourceMap.getString("diagnosePanel.TabConstraints.tabToolTip")); // NOI18N

        outputPanel.setName("outputPanel"); // NOI18N
        outputPanel.setLayout(new javax.swing.BoxLayout(outputPanel, javax.swing.BoxLayout.PAGE_AXIS));

        outputToolbar.setRollover(true);
        outputToolbar.setMaximumSize(new java.awt.Dimension(32767, 46));
        outputToolbar.setName("outputToolbar"); // NOI18N

        outputBackButton.setIcon(resourceMap.getIcon("outputBackButton.icon")); // NOI18N
        outputBackButton.setText(resourceMap.getString("outputBackButton.text")); // NOI18N
        outputBackButton.setFocusable(false);
        outputBackButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        outputBackButton.setName("outputBackButton"); // NOI18N
        outputBackButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        outputBackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputBackButtonActionPerformed(evt);
            }
        });
        outputToolbar.add(outputBackButton);

        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/oobd/ui/swing/desk/resources/cancel.png"))); // NOI18N
        cancelButton.setToolTipText(resourceMap.getString("cancelButton.toolTipText")); // NOI18N
        cancelButton.setFocusable(false);
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        outputToolbar.add(cancelButton);

        logButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/oobd/ui/swing/desk/resources/text.png"))); // NOI18N
        logButton.setSelected(true);
        logButton.setToolTipText(resourceMap.getString("logButton.toolTipText")); // NOI18N
        logButton.setFocusable(false);
        logButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logButton.setName("logButton"); // NOI18N
        logButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        outputToolbar.add(logButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/oobd/ui/swing/desk/resources/disk.png"))); // NOI18N
        saveButton.setToolTipText(resourceMap.getString("saveButton.toolTipText")); // NOI18N
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setName("saveButton"); // NOI18N
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        outputToolbar.add(saveButton);

        outputPanel.add(outputToolbar);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTextAreaOutput.setColumns(20);
        jTextAreaOutput.setRows(5);
        jTextAreaOutput.setName("jTextAreaOutput"); // NOI18N
        jScrollPane1.setViewportView(jTextAreaOutput);

        outputPanel.add(jScrollPane1);

        diagnoseTabPanel.addTab(resourceMap.getString("outputPanel.TabConstraints.tabTitle"), resourceMap.getIcon("outputPanel.TabConstraints.tabIcon"), outputPanel, resourceMap.getString("outputPanel.TabConstraints.tabToolTip")); // NOI18N

        diagnose.add(diagnoseTabPanel);

        mainPanel.add(diagnose, "card3");

        menuBar.setBackground(resourceMap.getColor("menuBar.background")); // NOI18N
        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        settingsMenuItem.setAction(actionMap.get("onClickMenu_Settings")); // NOI18N
        settingsMenuItem.setText(resourceMap.getString("settingsMenuItem.text")); // NOI18N
        settingsMenuItem.setName("settingsMenuItem"); // NOI18N
        fileMenu.add(settingsMenuItem);

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


    private void backButtonLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backButtonLabelMouseClicked

        if (httpEnabled.isSelected()) {
            Settings.setString(OOBDConstants.PropName_UIHander, UIHANDLER_WS_NAME);
        } else {
            Settings.setString(OOBDConstants.PropName_UIHander, UIHANDLER_LOCAL_NAME);

        }

        Settings.setString(OOBDConstants.PropName_ScriptDir, scriptDir.getText());

        connectTypeName = protocolComboBox.getSelectedItem().toString();
        if (connectTypeName != null && !connectTypeName.equalsIgnoreCase("")) {
            core.writeDataPool(OOBDConstants.DP_ACTUAL_CONNECTION_TYPE, connectTypeName);

// !! The value of the connection device is not stored here, as this already controlled in the comportComboBox change() event
            Settings.setString(connectTypeName + "_" + OOBDConstants.PropName_ConnectServerURL, jTextFieldRemoteServer.getText());
            core.writeDataPool(DP_ACTUAL_REMOTECONNECT_SERVER, jTextFieldRemoteServer.getText());
            Settings.setString(connectTypeName + "_" + OOBDConstants.PropName_ProxyHost, jTextFieldProxyHost.getText());
            try {
                jSpinnerProxyPort.commitEdit();
            } catch (ParseException ex) {
                Logger.getLogger(swingView.class.getName()).log(Level.SEVERE, null, ex);
            }
            Settings.setInt(connectTypeName + "_" + OOBDConstants.PropName_ProxyPort, ((Integer) jSpinnerProxyPort.getValue()));

        }

        Settings.savePreferences();
        String script = Settings.getString(OOBDConstants.PropName_ScriptName, null);
        scriptSelectComboBox.removeAllItems();
        int i = -1;
        String actualScriptDir = Settings.getString(OOBDConstants.PropName_ScriptDir, null);
        core.writeDataPool(DP_SCRIPTDIR, actualScriptDir);
        core.writeDataPool(DP_WWW_LIB_DIR, Settings.getString(OOBDConstants.PropName_LibraryDir, null));
        ArrayList<Archive> files = Factory.getDirContent(actualScriptDir);
        core.writeDataPool(DP_LIST_OF_SCRIPTS, files);
        for (Archive file : files) {
            scriptSelectComboBox.addItem(file);
            if (file.toString().equalsIgnoreCase(script)) {
                i = scriptSelectComboBox.getItemCount() - 1;
            }
        }
        if (i > -1) {
            scriptSelectComboBox.setSelectedIndex(i);
        }
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, MAINPANEL);
    }//GEN-LAST:event_backButtonLabelMouseClicked

    private void startButtonLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startButtonLabelMouseClicked
        if (UIHANDLER_WS_NAME.equalsIgnoreCase((String) core.readDataPool(DP_ACTUAL_UIHANDLER, Settings.getString(OOBDConstants.PropName_UIHander, UIHANDLER_WS_NAME)))) {

            //startButtonLabel.setIcon(resourceMap.getIcon("startButtonLabel.icon"));
            core.getSystemIF().openBrowser();
        } else {
            Archive ActiveArchive = (Archive) scriptSelectComboBox.getSelectedItem();
            if (ActiveArchive == null) {
                return;
            }
            Settings.setString(OOBDConstants.PropName_ScriptName, ActiveArchive.toString());
            CardLayout cl = (CardLayout) (mainPanel.getLayout());
            cl.show(mainPanel, DIAGNOSEPANEL);
            core.writeDataPool(DP_ACTIVE_ARCHIVE, ActiveArchive);
            core.startScriptArchive(ActiveArchive);
        }
    }//GEN-LAST:event_startButtonLabelMouseClicked

    private void settingsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_settingsComponentShown

        updateUI(false);
    }

    private void updateUI(boolean calledFromEvent) {

        PortInfo[] portList = new PortInfo[0];
        connectTypeName = Settings.getString(OOBDConstants.PropName_ConnectType, OOBDConstants.PropName_ConnectTypeBT);
        core.writeDataPool(OOBDConstants.DP_ACTUAL_CONNECTION_TYPE, connectTypeName);
        connectDeviceName = Settings.getString(connectTypeName + "_" + OOBDConstants.PropName_SerialPort, "");
        pgpEnabled.setSelected("true".equalsIgnoreCase(Settings.getString(OOBDConstants.PropName_PGPEnabled, "")));
        httpEnabled.setSelected(UIHANDLER_WS_NAME.equalsIgnoreCase(Settings.getString(OOBDConstants.PropName_UIHander, UIHANDLER_WS_NAME)));
        jTextFieldRemoteServer.setText(Settings.getString(connectTypeName + "_" + OOBDConstants.PropName_ConnectServerURL, OOBDConstants.PropName_KadaverServerDefault));
        jTextFieldProxyHost.setText(Settings.getString(connectTypeName + "_" + OOBDConstants.PropName_ProxyHost, ""));
        jSpinnerProxyPort.setValue(Settings.getInt(connectTypeName + "_" + OOBDConstants.PropName_ProxyPort, 0));
        if (!calledFromEvent) { //t
            for (int i = 0; i < protocolComboBox.getItemCount(); i++) {
                if (protocolComboBox.getItemAt(i).toString().equals(connectTypeName)) {
                    protocolComboBox.setSelectedIndex(i);
                }
            }
        }

        Class<OOBDPort> value = supplyHardwareConnects.get(connectTypeName);
        try { // tricky: try to call a static method of an interface, where a
            // interface don't have static values by definition..
            // Class[] parameterTypes = new Class[]{};
            java.lang.reflect.Method method = value.getMethod("getPorts", new Class[]{}); // no parameters
            Object instance = null;
            portList = (PortInfo[]) method.invoke(instance, new Object[]{}); // no parameters

        } catch (Exception ex) {
            Logger.getLogger(Core.class.getName())
                    .log(Level.WARNING,
                            "can't call static methods  of "
                            + value.getName());
            ex.printStackTrace();
            return;
        }

        int portListIndex = -1;

        PortInfo[] portCopyPlusOne = new PortInfo[portList.length + 1]; // needed maybe later, in case the port is not part of the port list, which was delivered by the port-getPorts() function
        for (int i = 0; i < portList.length; i++) {
            portCopyPlusOne[i + 1] = portList[i];
            if (portList[i].getDevice().equals(connectDeviceName)) {
                portListIndex = i;
            }
        }
        if (portListIndex == -1) { // now we use the List, which has space on item[0] to add the port which was not found in the device list
            portCopyPlusOne[0] = new PortInfo(connectDeviceName, connectDeviceName);
            comportComboBox.setModel(new javax.swing.DefaultComboBoxModel(portCopyPlusOne));
            comportComboBox.setSelectedIndex(0);
        } else {
            comportComboBox.setModel(new javax.swing.DefaultComboBoxModel(portList));
            comportComboBox.setSelectedIndex(portListIndex);
        }
        libraryDir.setText(Settings.getString(OOBDConstants.PropName_LibraryDir, ""));
        scriptDir.setText(Settings.getString(OOBDConstants.PropName_ScriptDir, ""));
        int pgp = checkKeyFiles();
        String pgpStatusText = "No PGP keys available";

        if (pgp == 0) { // all ok
            pgpStatusText = "All Keys in place";
        }
        if ((pgp & 0x04) > 0) { //no group key
            pgpStatusText = "Missing Group Key File !!";
        }
        if ((pgp & 0x01) > 0) {//new group key
            pgpStatusText = "New Group Key File is waiting for import";
        }
        if ((pgp & 0x08) > 0) { //no user key
            pgpStatusText = "Missing User Key File !!";
        }
        if ((pgp & 0x02) > 0) {//new user key
            pgpStatusText = "New User Key File is waiting for import";
        }
        if ((pgp == (0x04 + 0x04))) {//no keys
            pgpStatusText = "No PGP keys available";
        }

        pgpStatus.setText("PGP Key Status: " + pgpStatusText);
        if (pgp != 0) {
            Settings.setBoolean(OOBDConstants.PropName_PGPEnabled, false);
            pgpEnabled.setSelected(false);
            pgpEnabled.setEnabled(false);
            if ((pgp & (0x02 + 0x01)) > 0) { // any new keys there
                pgpImportKeys.setText("Import PGP keys now");
            } else {
                pgpImportKeys.setText("No PGP Keys available");
            }
        } else {
            pgpEnabled.setEnabled(true);
            pgpImportKeys.setText("DELETE PGP keys now");
        }


    }//GEN-LAST:event_settingsComponentShown

    private void chooseScriptDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseScriptDirectoryButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        File oldDir = null;
        String oldDirName = Settings.getString(OOBDConstants.PropName_ScriptDir, null);
        if (oldDirName != null) {
            oldDir = new File(oldDirName);
        }
        chooser.setCurrentDirectory(oldDir);
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().toLowerCase().endsWith(".lbc");
            }

            public String getDescription() {
                return "OOBD Scripts";
            }
        });
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this.getFrame())
                == JFileChooser.APPROVE_OPTION) {
            Settings.setString(OOBDConstants.PropName_ScriptDir, chooser.getSelectedFile().toString());
            scriptDir.setText(chooser.getSelectedFile().toString());
        }
    }//GEN-LAST:event_chooseScriptDirectoryButtonActionPerformed

    private void settingsComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_settingsComponentHidden
        // TODO add your handling code here:
    }//GEN-LAST:event_settingsComponentHidden

    private void mainComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainComponentShown
        statusMessageLabel.setText("Selected the Script you want to use and press Start");
    }//GEN-LAST:event_mainComponentShown

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        String oldDirName = Settings.getString(OOBDConstants.PropName_OutputFile, null);
        oldDirName = saveBufferAsFileRequest(oldDirName, jTextAreaOutput.getText(), false);
        if (oldDirName != null) {
            Settings.setString(OOBDConstants.PropName_OutputFile, oldDirName);
            Settings.savePreferences();
        }
    }//GEN-LAST:event_saveButtonActionPerformed

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

    private void outputBackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputBackButtonActionPerformed

        diagnoseTabPanel.setSelectedComponent(diagnosePanel);
    }//GEN-LAST:event_outputBackButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        jTextAreaOutput.setText("");
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void gridSmallerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridSmallerButtonActionPerformed
        int actColCount = ((GridLayout) diagnoseButtonPanel.getLayout()).getColumns();
        defaultGridWidth = diagnoseButtonPanel.getWidth() / (actColCount + 1);
        refreshGrid();
    }//GEN-LAST:event_gridSmallerButtonActionPerformed

    private void gridBiggerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridBiggerButtonActionPerformed
        int actColCount = ((GridLayout) diagnoseButtonPanel.getLayout()).getColumns();
        if (actColCount > 1) {
            defaultGridWidth = diagnoseButtonPanel.getWidth() / (actColCount - 1);
            refreshGrid();
        }
    }//GEN-LAST:event_gridBiggerButtonActionPerformed

    private void pgpEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pgpEnabledActionPerformed
        Settings.setBoolean(OOBDConstants.PropName_PGPEnabled, pgpEnabled.isSelected());

    }//GEN-LAST:event_pgpEnabledActionPerformed

    private void pgpImportKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pgpImportKeysActionPerformed
        if (checkKeyFiles() != 0) {
            importKeyFiles();
            updateUI(false);
        } else {
            int answer = JOptionPane.showConfirmDialog(settings, "Do you REALLY want to delete your PGP keys??");
            if (answer == JOptionPane.YES_OPTION) {
                try {
                    deleteKeyFiles();
                    updateUI(false);
                } catch (Exception e) {
                }
            }
        }
    }//GEN-LAST:event_pgpImportKeysActionPerformed

    private void chooseLibsDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseLibsDirectoryButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        File oldDir = null;
        String oldDirName = Settings.getString(OOBDConstants.PropName_LibraryDir, null);
        if (oldDirName != null) {
            oldDir = new File(oldDirName);
        }
        chooser.setCurrentDirectory(oldDir);
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().toLowerCase().endsWith(".lbc");
            }

            public String getDescription() {
                return "OOBD Library Folder";
            }
        });
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this.getFrame())
                == JFileChooser.APPROVE_OPTION) {
            Settings.setString(OOBDConstants.PropName_LibraryDir, chooser.getSelectedFile().toString());
            libraryDir.setText(chooser.getSelectedFile().toString());
        }
    }//GEN-LAST:event_chooseLibsDirectoryButtonActionPerformed

    private void protocolComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protocolComboBoxActionPerformed
        JComboBox cb = (JComboBox) evt.getSource();
        connectTypeName = cb.getSelectedItem().toString();
        if (connectTypeName != null && !connectTypeName.equalsIgnoreCase("")) {
            core.writeDataPool(OOBDConstants.DP_ACTUAL_CONNECTION_TYPE, connectTypeName);
            if (oldConnectTypeName != null) {
                Settings.setString(OOBDConstants.PropName_ConnectType, connectTypeName);
                // !! The value of the connection device is not stored here, as this already controlled in the comportComboBox change() event
                Settings.setString(oldConnectTypeName + "_" + OOBDConstants.PropName_ConnectServerURL, jTextFieldRemoteServer.getText());
                Settings.setString(oldConnectTypeName + "_" + OOBDConstants.PropName_ProxyHost, jTextFieldProxyHost.getText());
                try {
                    jSpinnerProxyPort.commitEdit();
                } catch (ParseException ex) {
                    Logger.getLogger(swingView.class.getName()).log(Level.SEVERE, null, ex);
                }
                Settings.setInt(oldConnectTypeName + "_" + OOBDConstants.PropName_ProxyPort, ((Integer) jSpinnerProxyPort.getValue()));
            }
            oldConnectTypeName = connectTypeName;
            updateUI(true);
        }

    }//GEN-LAST:event_protocolComboBoxActionPerformed

    private void jTextFieldRemoteServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldRemoteServerActionPerformed
        //
    }//GEN-LAST:event_jTextFieldRemoteServerActionPerformed

    private void jTextFieldProxyHostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldProxyHostActionPerformed
        //
    }//GEN-LAST:event_jTextFieldProxyHostActionPerformed

    private void jSpinnerProxyPortAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_jSpinnerProxyPortAncestorAdded
        //
    }//GEN-LAST:event_jSpinnerProxyPortAncestorAdded

    private void comportComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comportComboBoxActionPerformed
        JComboBox cb = (JComboBox) evt.getSource();
        Object item = cb.getSelectedItem();
        if (item != null) {
            if (item instanceof PortInfo) {
                connectDeviceName = ((PortInfo) item).getDevice();
            } else {
                connectDeviceName = item.toString();
                connectDeviceName = connectDeviceName.replaceAll("\\(.*\\)", "").trim();
                cb.getEditor().setItem(connectDeviceName);
            }
            if (connectDeviceName != null && !connectDeviceName.equalsIgnoreCase("")) {
                Settings.setString(connectTypeName + "_" + OOBDConstants.PropName_SerialPort, connectDeviceName);
                core.writeDataPool(OOBDConstants.DP_ACTUAL_CONNECT_ID, connectDeviceName);
            }
        }
    }//GEN-LAST:event_comportComboBoxActionPerformed

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

    @Action
    public void onClickButton_Start() {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        //cl.next(mainPanel);
        cl.show(mainPanel, DIAGNOSEPANEL);
    }

    @Action
    public void onClickButton_BackSettings() {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        //cl.next(mainPanel);
        cl.show(mainPanel, MAINPANEL);
    }

    @Action
    public void onClickMenu_Settings() {
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        //cl.next(mainPanel);
        cl.show(mainPanel, SETTINGSPANEL);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backButton;
    private javax.swing.JLabel backButtonLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton chooseLibsDirectoryButton;
    private javax.swing.JButton chooseScriptDirectoryButton;
    private javax.swing.JComboBox comportComboBox;
    private javax.swing.JLabel connectSymbol;
    private javax.swing.JPanel diagnose;
    private javax.swing.JPanel diagnoseButtonPanel;
    private javax.swing.JPanel diagnosePanel;
    private javax.swing.JScrollPane diagnoseScrollPanel;
    private javax.swing.JTabbedPane diagnoseTabPanel;
    private javax.swing.JLabel diagnoseTitle;
    private javax.swing.JToolBar diagnoseToolBar;
    private javax.swing.JButton gridBiggerButton;
    private javax.swing.JButton gridSmallerButton;
    private javax.swing.JCheckBox httpEnabled;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelRemoteServer;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JSpinner jSpinnerProxyPort;
    private javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JTextField jTextFieldProxyHost;
    private javax.swing.JTextField jTextFieldRemoteServer;
    private javax.swing.JTextField libraryDir;
    private javax.swing.JToggleButton logButton;
    private javax.swing.JPanel main;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel oobdImage;
    private javax.swing.JButton outputBackButton;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JToolBar outputToolbar;
    private javax.swing.JCheckBox pgpEnabled;
    private javax.swing.JButton pgpImportKeys;
    private javax.swing.JLabel pgpStatus;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JComboBox protocolComboBox;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField scriptDir;
    private javax.swing.JComboBox scriptSelectComboBox;
    private javax.swing.JPanel settings;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JLabel startButtonLabel;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToggleButton timerButton;
    private javax.swing.JPanel toolPanelDiagnose;
    private javax.swing.JButton updateButton;
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

    public void sm(String msg, String modifier) {

        String actBuffer;
        if (!"".equalsIgnoreCase(modifier)) {
            if (modifier.equalsIgnoreCase(OB_CMD_SETBUFFER)) {
                actualBufferName = msg.toLowerCase().trim();
                if (!actualBufferName.equals(OB_DEFAULT_NAME)) {
                    if (!outputBuffers.containsKey(actualBufferName)) {
                        outputBuffers.put(actualBufferName, "");
                        actBuffer = "";
                    }
                }
            }
            if (modifier.equalsIgnoreCase(OB_CMD_CLEAR)) {
                if (actualBufferName.equals(OB_DEFAULT_NAME)) { // do the special handling of the UI textbox here
                    jTextAreaOutput.setText("");
                } else {
                    outputBuffers.put(actualBufferName, "");
                    actBuffer = "";
                }
            } else if (modifier.equalsIgnoreCase(OB_CMD_CLEARALL)) {
                jTextAreaOutput.setText("");
                outputBuffers = new Hashtable<String, String>();
                actBuffer = "";
            } else {
                // here we need the buffer content, so we need to do the time consuming conversion here
                if (actualBufferName.equals(OB_DEFAULT_NAME)) {
                    actBuffer = jTextAreaOutput.getText();
                } else {
                    if (outputBuffers.containsKey(actualBufferName)) {
                        actBuffer = outputBuffers.get(actualBufferName);
                    } else {
                        outputBuffers.put(actualBufferName, "");
                        actBuffer = "";
                    }
                }
                if (modifier.equalsIgnoreCase(OB_CMD_SAVEAS)) {
                    saveBufferAsFileRequest(msg, actBuffer, false);
                }
                if (modifier.equalsIgnoreCase(OB_CMD_SAVE)) {
                    saveBufferToFile(msg, actBuffer, false);
                }
                if (modifier.equalsIgnoreCase(OB_CMD_APPENDAS)) {
                    saveBufferAsFileRequest(msg, actBuffer, true);
                }
                if (modifier.equalsIgnoreCase(OB_CMD_APPEND)) {
                    saveBufferToFile(msg, actBuffer, true);
                }
            }
        } else {
            if (actualBufferName.equals(OB_DEFAULT_NAME)) {
                if (logButton.isSelected()) {
                    jTextAreaOutput.append(msg + "\n");
                }
            } else {
                String actBufferArrayList = outputBuffers.get(actualBufferName) + msg;
                outputBuffers.put(actualBufferName, actBufferArrayList);
            }
        }
    }

    public void registerOobdCore(Core core) {
        this.core = core;
        connectTypeName = Settings.getString(OOBDConstants.PropName_ConnectType, OOBDConstants.PropName_ConnectTypeBT);
        transferPreferences2System(connectTypeName);
        String script = Settings.getString(OOBDConstants.PropName_ScriptName, null);
        int i = -1;
        String actualScriptDir = Settings.getString(OOBDConstants.PropName_ScriptDir, null);
        ArrayList<Archive> files = Factory.getDirContent(actualScriptDir);
        for (Archive file : files) {
            scriptSelectComboBox.addItem(file);
            if (file.toString().equalsIgnoreCase(script)) {
                i = scriptSelectComboBox.getItemCount() - 1;
            }
        }
        if (i > -1) {
            scriptSelectComboBox.setSelectedIndex(i);
        }
        if (!Settings.getBoolean(OOBDConstants.PropName_PGPEnabled, false)) {
            this.core.getSystemIF().setUserPassPhrase("");

        } else {

            PWDialog pwDialog = new PWDialog(null);
            String str = pwDialog.showDialog();
            // System.err.println("passwort="+str);
            //       String str = JOptionPane.showInputDialog(null, "Enter your PGP PassPhrase : ",                "OOBD PGP Script Encryption", 1);
            if (str != null) {
                try {
                    this.core.getSystemIF().setUserPassPhrase(
                            str);
                    str = "";
                } catch (Exception e) {
                    // e.printStackTrace();
                    this.core.getSystemIF().setUserPassPhrase("");
                }
            }
        }
        List<String> list = new ArrayList<>();
        supplyHardwareConnects = core.getConnectorList();

        Enumeration<String> e = supplyHardwareConnects.keys();

        // iterate through Hashtable keys Enumeration
        while (e.hasMoreElements()) {
            list.add(e.nextElement());
        }
        protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(list.toArray())); //al.toArray(new String[al.size()])
        updateUI(false);
    }

    @Override
    public void announceScriptengine(String id, String visibleName) {
        Logger.getLogger(swingView.class.getName()).log(Level.CONFIG, "Interface announcement: Scriptengine-ID: {0} visibleName:{1}", new Object[]{id, visibleName
        });
    }

    @Override
    public void announceUIHandler(String id, String visibleName) {
        Logger.getLogger(swingView.class.getName()).log(Level.CONFIG, "Interface announcement: UIHandler-ID: {0} visibleName:{1}", new Object[]{id, visibleName
        });
        if (Settings.getString(OOBDConstants.PropName_UIHander, UIHANDLER_WS_NAME).equalsIgnoreCase(visibleName)) {
            Onion onion = new Onion();
            String seID = core.createUIHandler(id, onion);

            core.startUIHandler(seID, onion);
        }

    }

    @Override
    public void updateOobdUI() {
        OobdUIHandler uiHandler = core.getUiHandler();
        if (uiHandler != null) {
            uiHandler.handleMsg();
        }
    }

    @Override
    public Class getVisualizerClass(Onion myOnion) {
        return VisualizerJPanel.class;
    }

    public void visualize(Onion myOnion) {
        Visualizer newVisualizer = new Visualizer(myOnion);
        JComponent newJComponent;
        // to be able to delete the created  objects on a a page later when closing the page, we need to log the creation here
        pageObjects = (Vector<IFvisualizer>) core.getAssign(newVisualizer.getOwnerEngine(), org.oobd.base.OOBDConstants.CL_OBJECTS);
        if (pageObjects == null) {
            pageObjects = new Vector<IFvisualizer>();
            core.setAssign(newVisualizer.getOwnerEngine(), org.oobd.base.OOBDConstants.CL_OBJECTS, pageObjects);
        }
        Class<IFvisualizer> visualizerClass = getVisualizerClass(myOnion);
        Class[] argsClass = new Class[1]; // first we set up an pseudo - args - array for the scriptengine- constructor
        argsClass[0] = Onion.class; // and fill it with the info, that the argument for the constructor will be an Onion
        try {
            Method classMethod = visualizerClass.getMethod("getInstance", argsClass); // and let Java find the correct constructor with one string as parameter
            Object[] args = {myOnion}; //we will an args-array with our String parameter
            newJComponent = (JComponent) classMethod.invoke(null, args); // and finally create the object from the scriptengine class with its unique id as parameter
            newVisualizer.setOwner((IFvisualizer) newJComponent);
            ((IFvisualizer) newJComponent).setVisualizer(newVisualizer);
            // add to internal list
            pageObjects.add((IFvisualizer) newJComponent);
            setStatusLine("progress", 100 - 100 / elementCount);
            elementCount++;
            if (((IFvisualizer) newJComponent).isGroup()) {
                /*               // if the component is not already placed
                 //JScrollPane scrollpane = new JScrollPane(newJComponent);
                 JScrollPane scrollpane = new JScrollPane();
                 scrollpane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                 scrollpane.setViewportView(newJComponent);
                 // scrollpane.setPreferredSize(new Dimension(300, 300));
                 GridBagConstraints c = new GridBagConstraints();
                 JPanel panel = (JPanel) core.getAssign(
                 newVisualizer.getOwnerEngine(),
                 org.oobd.base.OOBDConstants.CL_PANE + ":page");
                 c.fill = GridBagConstraints.REMAINDER;
                
                 c.gridx = 0;
                 c.gridy = 0;
                 c.weightx = 1;
                 c.weighty = 1;
                 //panel.add(newJComponent, c);
                 panel.add(scrollpane, java.awt.BorderLayout.CENTER);//, c);
                 //panel.add(scrollpane, c);
                 panel.validate();
                 */            }
            ((IFvisualizer) newJComponent).initValue(newVisualizer, myOnion);
            newJComponent.addMouseListener(popupMenuHandle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openPage(String seID, String name, int colcount, int rowcount) {
        //cleans the page
        setStatusLine("started", null);
        setStatusLine("message", "Load Page");
        pageTitle = name;
        if (pageObjects != null) {
            diagnoseButtonPanel.removeAll();
            for (IFvisualizer vis : pageObjects) {
                vis.setRemove(seID);
            }
            pageObjects.removeAllElements();
            //diagnoseButtonPanel.validate();
        }
        elementCount = 1;
        setStatusLine("message", "Load Page..");

    }

    public void openPageCompleted(String seID, String Name) {

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    refreshGrid();
                }
            });
        } else {
            refreshGrid();
        }
        setStatusLine("done", null);
        statusMessageLabel.setText(pageTitle);

    }

    void refreshGrid() {
        //build the components out of the previously collected list of vsiualizers
        // it seems that sometimes the resize form event is called during rezize Event... which generates an endless
        // recursive loop. To avoid this, the variable alreadyRefreshing started should indicate that there's already a refresh ongoing..
        alreadyRefreshing = false;
        if (pageObjects != null && !alreadyRefreshing) {
            alreadyRefreshing = true;
            Dimension panel = diagnoseButtonPanel.getSize();
            Dimension s = diagnoseScrollPanel.getSize();
            panel.setSize(s.getWidth(), panel.getHeight());
            diagnoseButtonPanel.setSize(panel);
            GridLayout thisGrid = (GridLayout) diagnoseButtonPanel.getLayout();

            int cols = s.width / defaultGridWidth;
            if (cols < 1) {
                cols = 1;
            }
            thisGrid = new GridLayout(0, cols);
            diagnoseButtonPanel.removeAll();
            diagnoseButtonPanel.setLayout(thisGrid);
            for (IFvisualizer vis : pageObjects) {
                JComponent newJComponent = (JComponent) vis;
                diagnoseButtonPanel.add(newJComponent);
            }
            thisGrid.layoutContainer(diagnoseButtonPanel);
            diagnoseButtonPanel.repaint();
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
        if (timerButton.isSelected() && pageObjects != null) {
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
                updateOobdUI();
            }
        });
        timer.restart();
    }

    @Override
    public Onion requestParamInput(Onion msg) {
        Onion answer = null;
        boolean valid = false;
        JSONArray params;
        try {
            params = msg.getJSONArray(CM_PARAM);
            if (params != null) {
                Onion p0Onion = new Onion(params.get(0).toString());
                    String type = p0Onion.getOnionString("type");
                    String message = Base64Coder.decodeString(p0Onion.getOnionString("message"));
                    String title = Base64Coder.decodeString(p0Onion.getOnionString("title"));
                    String defaultValue = p0Onion.getOnionString("default");
                    if (defaultValue != null) {
                        defaultValue = Base64Coder.decodeString(defaultValue);
                    }
                    if ("alert".equalsIgnoreCase(type)) {
                        JOptionPane.showMessageDialog(null, message);
                        valid = true;
                    }
                    if ("string".equalsIgnoreCase(type)) {
                        String answerString = (String) JOptionPane.showInputDialog(null, message,
                                title,
                                JOptionPane.PLAIN_MESSAGE, null, null, defaultValue);
                        if (answerString != null) {
                            answer = new Onion().setValue("answer", Base64Coder.encodeString(answerString));
                        }
                        valid = true;
                    }

            }
        } catch (JSONException | OnionWrongTypeException | OnionNoEntryException ex) {
            Logger.getLogger(swingView.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!valid) {
            JOptionPane.showMessageDialog(null, "internal error: Invalid cmd parameters");
        }
        return answer;
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

    @Override
    public void openXCVehicleData(Onion onion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void transferPreferences2System(String localConnectTypeName) {

        if (localConnectTypeName != null && !localConnectTypeName.equalsIgnoreCase("")) {
            core.writeDataPool(DP_ACTUAL_REMOTECONNECT_SERVER, Settings.getString(localConnectTypeName + "_" + OOBDConstants.PropName_ConnectServerURL, ""));
            core.writeDataPool(DP_ACTUAL_PROXY_HOST, Settings.getString(localConnectTypeName + "_" + OOBDConstants.PropName_ProxyHost, ""));
            core.writeDataPool(DP_ACTUAL_PROXY_PORT, Settings.getInt(localConnectTypeName + "_" + OOBDConstants.PropName_ProxyPort, 0));

        }
        core.writeDataPool(DP_ACTUAL_UIHANDLER, Settings.getString(OOBDConstants.PropName_UIHander, UIHANDLER_WS_NAME));
        String actualScriptDir = Settings.getString(OOBDConstants.PropName_ScriptDir, null);
        core.writeDataPool(DP_SCRIPTDIR, actualScriptDir);
        core.writeDataPool(DP_WWW_LIB_DIR, Settings.getString(OOBDConstants.PropName_LibraryDir, null));
        ArrayList<Archive> files = Factory.getDirContent(actualScriptDir);
        core.writeDataPool(DP_LIST_OF_SCRIPTS, files);
        core.writeDataPool(DP_HTTP_HOST, core.getSystemIF().getSystemIP());
        core.writeDataPool(DP_HTTP_PORT, 8080);
        core.writeDataPool(DP_WSOCKET_PORT, 8443);

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
