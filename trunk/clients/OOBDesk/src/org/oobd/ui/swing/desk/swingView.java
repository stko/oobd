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
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import purejavacomm.CommPortIdentifier;
import gnu.io.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.oobd.base.*;
import org.oobd.base.Core;
import org.oobd.base.IFui;
import org.oobd.base.visualizer.*;
import org.oobd.base.support.Onion;


import java.util.Vector;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import org.json.JSONException;
import org.oobd.base.archive.*;

/**
 * The application's main frame.
 */
public class swingView extends org.jdesktop.application.FrameView implements IFui, org.oobd.base.OOBDConstants, ActionListener {

    final static String MAINPANEL = "card2";
    final static String DIAGNOSEPANEL = "card3";
    final static String SETTINGSPANEL = "card4";
    Core oobdCore;
    Properties appProbs;
    private String scriptEngineID = null;
    private String scriptEngineVisibleName;
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

        popupMenuHandle =
                new MouseAdapter() {

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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        main = new javax.swing.JPanel();
        scriptSelectComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
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

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setIcon(resourceMap.getIcon("jLabel3.icon")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setToolTipText(resourceMap.getString("jLabel3.toolTipText")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        main.add(jLabel3, java.awt.BorderLayout.CENTER);

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
        settings.add(backButtonLabel, new java.awt.GridBagConstraints());

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        settings.add(jLabel1, gridBagConstraints);

        comportComboBox.setEditable(true);
        comportComboBox.setName("comportComboBox"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        settings.add(chooseScriptDirectoryButton, gridBagConstraints);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        settings.add(jLabel4, gridBagConstraints);

        scriptDir.setText(resourceMap.getString("scriptDir.text")); // NOI18N
        scriptDir.setName("scriptDir"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        settings.add(scriptDir, gridBagConstraints);

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

        appProbs.setProperty(OOBDConstants.PropName_ScriptDir, scriptDir.getText());
        appProbs.setProperty(OOBDConstants.PropName_SerialPort, comportComboBox.getEditor().getItem().toString());
        oobdCore.getSystemIF().saveProperty(FT_PROPS,
                OOBDConstants.AppPrefsFileName, appProbs);
        String script = appProbs.getProperty(OOBDConstants.PropName_ScriptName, null);
        scriptSelectComboBox.removeAllItems();
        int i = -1;
        ArrayList<Archive> files = Factory.getDirContent(appProbs.getProperty(OOBDConstants.PropName_ScriptDir, null));
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

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        String scriptName = scriptSelectComboBox.getSelectedItem().toString();
        appProbs.setProperty(OOBDConstants.PropName_ScriptName, scriptName);
        oobdCore.getSystemIF().saveProperty(FT_PROPS,
                OOBDConstants.AppPrefsFileName, appProbs);
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, DIAGNOSEPANEL);
        try {
            startScriptEngine(
                    new Onion("{" + "'scriptpath':'" + ((Archive) scriptSelectComboBox.getSelectedItem()).getFilePath().replace("\\", "/")
                    + "'" + "}"));
        } catch (JSONException ex) {
            // TODO Auto-generated catch block
            Logger.getLogger(swingView.class.getName()).log(Level.WARNING, "JSON creation error with file name:" + ((Archive) scriptSelectComboBox.getSelectedItem()).getFilePath(), ex.getMessage());
        }
    }//GEN-LAST:event_jLabel3MouseClicked

    private void settingsComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_settingsComponentShown

        String osname = System.getProperty("os.name", "").toLowerCase();
        Enumeration pList = null;
        Logger.getLogger(swingView.class.getName()).log(Level.CONFIG, "OS detected: {0}", osname);
        try {
            if (osname.startsWith("windows")) {
                pList = purejavacomm.CommPortIdentifier.getPortIdentifiers();
            } else {
                pList = gnu.io.CommPortIdentifier.getPortIdentifiers();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String port = appProbs.getProperty(OOBDConstants.PropName_SerialPort, null);
        int portListIndex = -1;
        // Process the list.
        while (pList.hasMoreElements()) {
            CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
            if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                comportComboBox.addItem(cpi.getName());
                if (cpi.getName().equalsIgnoreCase(port)) {
                    portListIndex = comportComboBox.getItemCount() - 1;
                }
            }
        }
        if (portListIndex > -1) {
            comportComboBox.setSelectedIndex(portListIndex);
        } else {
            comportComboBox.setSelectedItem(port);
        }
        scriptDir.setText(appProbs.getProperty(OOBDConstants.PropName_ScriptDir, ""));

    }//GEN-LAST:event_settingsComponentShown

    private void chooseScriptDirectoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseScriptDirectoryButtonActionPerformed
        JFileChooser chooser = new JFileChooser();
        File oldDir = null;
        String oldDirName = appProbs.getProperty(OOBDConstants.PropName_ScriptDir, null);
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
            appProbs.setProperty(OOBDConstants.PropName_ScriptDir, chooser.getSelectedFile().toString());
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
        JFileChooser chooser = new JFileChooser();
        File oldDir = null;
        String oldDirName = appProbs.getProperty(OOBDConstants.PropName_OutputDir, null);
        if (oldDirName != null) {
            oldDir = new File(oldDirName);
        }
        chooser.setCurrentDirectory(oldDir);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                //return f.getName().toLowerCase().endsWith(".lbc");
                return true;
            }

            public String getDescription() {
                return "All Files";
            }
        });
        if (chooser.showSaveDialog(this.getFrame())
                == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter os = new FileWriter(chooser.getSelectedFile().toString());
                os.write(jTextAreaOutput.getText());
                os.close();
                appProbs.setProperty(OOBDConstants.PropName_OutputDir, chooser.getCurrentDirectory().toString());
                oobdCore.getSystemIF().saveProperty(FT_PROPS,
                        OOBDConstants.AppPrefsFileName, appProbs);

            } catch (IOException ex) {
                Logger.getLogger(swingView.class.getName()).log(Level.SEVERE, null, ex);
            }
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JToggleButton logButton;
    private javax.swing.JPanel main;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel oobdImage;
    private javax.swing.JButton outputBackButton;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JToolBar outputToolbar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField scriptDir;
    private javax.swing.JComboBox scriptSelectComboBox;
    private javax.swing.JPanel settings;
    private javax.swing.JMenuItem settingsMenuItem;
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

    public void sm(String msg) {
        if (logButton.isSelected()) {
            jTextAreaOutput.append(msg + "\n");
        }
    }

    public void registerOobdCore(Core core) {
        oobdCore = core;
        appProbs = oobdCore.getSystemIF().loadProperty(FT_PROPS,
                OOBDConstants.AppPrefsFileName);
        String script = appProbs.getProperty(OOBDConstants.PropName_ScriptName, null);
        int i = -1;
        ArrayList<Archive> files = Factory.getDirContent(appProbs.getProperty(OOBDConstants.PropName_ScriptDir, null));
        for (Archive file : files) {
            scriptSelectComboBox.addItem(file);
            if (file.toString().equalsIgnoreCase(script)) {
                i = scriptSelectComboBox.getItemCount() - 1;
            }
        }
        if (i > -1) {
            scriptSelectComboBox.setSelectedIndex(i);
        }
    }

    public void announceScriptengine(String id, String visibleName) {
        Logger.getLogger(swingView.class.getName()).log(Level.CONFIG, "Interface announcement: Scriptengine-ID: {0} visibleName:{1}", new Object[]{id, visibleName});
        // more as one scriptengine is not used in this app
        //scriptEngineMap.put(id, visibleName);
        if ("ScriptengineLua".equalsIgnoreCase(id)) {
            scriptEngineID = id;
            scriptEngineVisibleName = visibleName;
        }
    }

    public Class getVisualizerClass(String visualizerType, String theme) {
        return TextVisualizerJPanel.class;
    }

    public void visualize(Onion myOnion) {
        Visualizer newVisualizer = new Visualizer(myOnion);
        JComponent newJComponent;
        // to be able to delete the created  objects on a a page later when closing the page, we need to log the creation here
        pageObjects = (Vector<IFvisualizer>) oobdCore.getAssign(newVisualizer.getOwnerEngine(), org.oobd.base.OOBDConstants.CL_OBJECTS);
        if (pageObjects == null) {
            pageObjects = new Vector<IFvisualizer>();
            oobdCore.setAssign(newVisualizer.getOwnerEngine(), org.oobd.base.OOBDConstants.CL_OBJECTS, pageObjects);
        }
        Class<IFvisualizer> visualizerClass = getVisualizerClass(myOnion.getOnionString("type"), myOnion.getOnionString("theme"));
        Class[] argsClass = new Class[2]; // first we set up an pseudo - args - array for the scriptengine- constructor
        argsClass[0] = String.class; // and fill it with the info, that the argument for the constructor will be first a String
        argsClass[1] = String.class;
        // and fill it with the info, that the argument for the constructor will be first a String
        try {
            Method classMethod = visualizerClass.getMethod("getInstance", argsClass); // and let Java find the correct constructor with one string as parameter
            Object[] args = {newVisualizer.getOwnerEngine(), newVisualizer.getName()}; //we will an args-array with our String parameter
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
                JPanel panel = (JPanel) oobdCore.getAssign(
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
            diagnoseButtonPanel.getParent().invalidate();
            diagnoseButtonPanel.invalidate();
            GridLayout thisGrid = (GridLayout) diagnoseButtonPanel.getLayout();
            Dimension s = diagnose.getSize();
            int cols = s.width / defaultGridWidth;
            if (cols < 1) {
                cols = 1;
            }
            thisGrid = new GridLayout(0, cols);
            diagnoseButtonPanel.setLayout(thisGrid);

            diagnoseButtonPanel.removeAll();
            for (IFvisualizer vis : pageObjects) {
                JComponent newJComponent = (JComponent) vis;
                diagnoseButtonPanel.add(newJComponent);
            }
            diagnoseButtonPanel.validate();
            diagnoseButtonPanel.repaint();
            diagnoseButtonPanel.getParent().validate();
            diagnoseButtonPanel.getParent().repaint();
            alreadyRefreshing = false;
        }
    }

    public void startScriptEngine(Onion onion) {

        if (scriptEngineID != null) {
            String seID = oobdCore.createScriptEngine(scriptEngineID, onion);

            //JTabbedPane newjTabPane = new JTabbedPane(); //create a inner JTabbedPane as container for the later coming scriptengine pages
            //newjTabPane.setName(seID); // set the name of that canvas that it can be found again later
            //mainSeTabbedPane.addTab(seID, newjTabPane); // and put this canvas inside the pane which belongs to that particular scriptengine
            // and now, after initialisation of the UI, let the games begin...
            oobdCore.setAssign(seID, org.oobd.base.OOBDConstants.CL_PANE, new Object()); //store the related drawing pane, the TabPane for that scriptengine
            //stop the Progress Dialog BEFORE the script starts
            //Diagnose.getInstance().stopProgressDialog();
            oobdCore.startScriptEngine(seID, onion);
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
        timer.restart();
    }
}
