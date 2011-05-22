/*
 * SKDSSwingView.java
 */
package skdsswing;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.lang.reflect.*;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JComponent;
import org.oobd.base.Core;
import org.oobd.base.IFui;
import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The application's main frame.
 */
public class SKDSSwingView extends FrameView implements ActionListener, IFui, org.oobd.base.OOBDConstants {

    Core oobdCore;

    public SKDSSwingView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
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

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
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
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    public void sm(String msg) {
        //JOptionPane.showMessageDialog(null, msg);
        //http://stackoverflow.com/questions/1235644/bringing-tab-to-front-in-jtabbedpane
        jTextAreaOutput.append(msg + "\n");
        mainSeTabbedPane.setSelectedComponent(outputPanel);
    }

    public void registerOobdCore(Core core) {
        oobdCore = core;
    }

    public void announceScriptengine(String id, String visibleName) {
        Logger.getLogger(SKDSSwingView.class.getName()).log(Level.CONFIG, "Interface announcement: Scriptengine-ID: " + id + " visibleName:" + visibleName);
        javax.swing.JMenuItem menuItem;
        menuItem = new javax.swing.JMenuItem(visibleName);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "This doesn't really do anything");
        menuItem.addActionListener(this);
        menuItem.setActionCommand(id);
        openMenu.add(menuItem);


    }

    public Class getVisualizerClass(String visualizerType, String theme) {
        /**
         * @todo implement different types of visualisation
         * Obviously this implementation returns always the same class to visualize
         */
        return SwingVizTable.class;

    }

    public void visualize(Onion myOnion) {
        Visualizer newVisualizer = new Visualizer(myOnion);
        JComponent newJComponent;
        // to be able to delete the created  objects on a a page later when closing the page, we need to log the creation here
        Vector<IFvisualizer> pageObjects = (Vector<IFvisualizer>) oobdCore.getAssign(newVisualizer.getOwnerEngine(), org.oobd.base.OOBDConstants.CL_OBJECTS);
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
            // add to internal list
            pageObjects.add((IFvisualizer) newJComponent);
            if (((IFvisualizer) newJComponent).isGroup()) {
                // if the component is not already placed
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
                c.weightx=1;
                c.weighty=1;
                //panel.add(newJComponent, c);
                panel.add(scrollpane, java.awt.BorderLayout.CENTER);//, c);
                //panel.add(scrollpane, c);
                panel.validate();
            }
            ((IFvisualizer) newJComponent).initValue(newVisualizer, myOnion);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @todo row and colum sizing needs to be implemented
     * @param seID
     * @param name
     * @param colcount
     * @param rowcount
     */
    public void openPage(String seID, String name, int colcount, int rowcount) {
        JTabbedPane basejTabPane = (JTabbedPane) oobdCore.getAssign(seID, org.oobd.base.OOBDConstants.CL_PANE);
        if (basejTabPane != null) {
            java.awt.Component oldPage = (java.awt.Component) oobdCore.getAssign(seID, org.oobd.base.OOBDConstants.CL_PANE + ":page");
            if (oldPage != null) {
                Vector<IFvisualizer> pageObjects = (Vector<IFvisualizer>) oobdCore.getAssign(seID, org.oobd.base.OOBDConstants.CL_OBJECTS);
                if (pageObjects != null) {

                    Iterator<IFvisualizer> itr = pageObjects.iterator();
                    while (itr.hasNext()) {
                        itr.next().remove(seID);
                    }
                }
                basejTabPane.remove(oldPage);
            }
        //    JPanel panel = new JPanel(new GridBagLayout());
           JPanel panel = new JPanel(new java.awt.BorderLayout());
            panel.setName(name);
            //JTabbedPane pane = new JTabbedPane();
            //pane.add(panel);
            basejTabPane.add(panel);
            basejTabPane.setSelectedIndex(basejTabPane.getTabCount() - 1);
            oobdCore.setAssign(seID, org.oobd.base.OOBDConstants.CL_PANE + ":page", panel);
            GridBagConstraints c = new GridBagConstraints();
            basejTabPane.validate();
            /**
             * For each component to be added to this container:
             * ...Create the component...
             * ...Set instance variables in the GridBagConstraints instance...
             */
//        JButton button = new JButton("Button 1");
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 0;
//        c.gridy = 0;
//        panel.add(button, c);
//        button = new JButton("Button 2");
//
//        c.fill = GridBagConstraints.HORIZONTAL;
//        c.gridx = 0;
//        c.gridy = 1;
//        panel.add(button, c);
        }
    }

    public void openPageCompleted(String seID, String name) {
        java.awt.Component oldPage = (java.awt.Component) oobdCore.getAssign(seID, org.oobd.base.OOBDConstants.CL_PANE + ":page");
        if (oldPage != null) {
            oldPage.invalidate();
            oldPage.validate();
            oldPage.repaint();
        }
    }

    /**
     * create ScriptEngine, when the user select the "Start.." menu item
     * @todo the event needs to checked if it really comes from a "Start.." menu entry
     * @todo tabs needs a "close" button as described on http://java.sun.com/docs/books/tutorial/uiswing/examples/components/index.html#TabComponentsDemo
     * @todo actual this function creates straight the pane for the scriptengine  - would it not be better to put this into a seperate oobd interface function to make it better structured?
     *
     *
     */
    @Action
    public void actionPerformed(ActionEvent e) {
        //...Get information from the action event...
        //...Display it in the text area...
         Logger.getLogger(SKDSSwingView.class.getName()).log(Level.CONFIG,"Attempt to create ScriptEngine " + e.getActionCommand());
        String seID = oobdCore.createScriptEngine(e.getActionCommand()); //first get the unique name for the new scriptEngine Canvas
        JTabbedPane newjTabPane = new JTabbedPane(); //create a inner JTabbedPane as container for the later coming scriptengine pages
        newjTabPane.setName(seID); // set the name of that canvas that it can be found again later
        mainSeTabbedPane.addTab(seID, newjTabPane); // and put this canvas inside the pane which belongs to that particular scriptengine
        mainSeTabbedPane.setSelectedComponent(newjTabPane); // bring the new pane to front
        // and now, after initalisation of the UI, let the games begin...
        oobdCore.setAssign(seID, org.oobd.base.OOBDConstants.CL_PANE, newjTabPane); //store the related drawing pane, the TabPane for that scriptengine
        oobdCore.startScriptEngine(seID);


    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = SKDSSwingApp.getApplication().getMainFrame();
            aboutBox = new SKDSSwingAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);


        }
        SKDSSwingApp.getApplication().show(aboutBox);
    }

    @Action
    public void clearOutputWindow() {
        jTextAreaOutput.setText("");

    }

    @Action
    public void showFileExplorer() {
        if (SDKSSwingFileExplorer.getOpenSelected()) {
            SDKSSwingFileExplorer.setOpenSelected(false);
            fileExplorer = null;
        }

        if (fileExplorer == null) {
            JFrame mainFrame = SKDSSwingApp.getApplication().getMainFrame();
            fileExplorer = new SDKSSwingFileExplorer(mainFrame, true);
            fileExplorer.setLocationRelativeTo(mainFrame);

        }

        // SKDSSwingApp.getApplication().show(fileExplorer);

    }

    @Action
    public void showFrameUpload() {
        if (SKDSSwingUpload.getOpenSelected()) {
            SKDSSwingUpload.setOpenSelected(false);
            frameUpload = null;
        }
        if (frameUpload == null) {
            JFrame frame = SKDSSwingApp.getApplication().getMainFrame();
            frameUpload = new SKDSSwingUpload(frame);
            frameUpload.setLocationRelativeTo(frame);
        }
        SKDSSwingApp.getApplication().show(frameUpload);
    }
   @Action
   public void showFramePortConfiguration(){
       if (SKDSSwingUpload.getOpenSelected()) {
             SKDSSwingPortConfiguration.setPortOpenSelected(false);
             framePortconf=null;
       }
       if(framePortconf == null){
            JFrame frame = SKDSSwingApp.getApplication().getMainFrame();
            framePortconf = new SKDSSwingPortConfiguration(frame);
            framePortconf.setLocationRelativeTo(frame);
       }
        SKDSSwingApp.getApplication().show(framePortconf);
   }



   
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        mainToolBar = new javax.swing.JToolBar();
        mainSplitPanel = new javax.swing.JSplitPane();
        scriptEnginePanel = new javax.swing.JPanel();
        scriptEngineToolbar = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        mainSeTabbedPane = new javax.swing.JTabbedPane();
        outputPanel = new javax.swing.JPanel();
        outputToolBar = new javax.swing.JToolBar();
        outputClearButton = new javax.swing.JButton();
        outputSaveButton = new javax.swing.JButton();
        outputEnableButton = new javax.swing.JToggleButton();
        jScrollPaneOutput = new javax.swing.JScrollPane();
        jTextAreaOutput = new javax.swing.JTextArea();
        VisualizerPanel = new javax.swing.JPanel();
        visualizerToolBar = new javax.swing.JToolBar();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        openMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        mainToolBar.setRollover(true);
        mainToolBar.setName("mainToolBar"); // NOI18N

        mainSplitPanel.setName("mainSplitPanel"); // NOI18N

        scriptEnginePanel.setName("scriptEnginePanel"); // NOI18N

        scriptEngineToolbar.setRollover(true);
        scriptEngineToolbar.setName("scriptEngineToolBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(skdsswing.SKDSSwingApp.class).getContext().getResourceMap(SKDSSwingView.class);
        jButton1.setIcon(resourceMap.getIcon("backButton.icon")); // NOI18N
        jButton1.setText(resourceMap.getString("backButton.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("backButton.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("backButton"); // NOI18N
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scriptEngineToolbar.add(jButton1);

        jButton2.setIcon(resourceMap.getIcon("jButton2.icon")); // NOI18N
        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2"); // NOI18N
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scriptEngineToolbar.add(jButton2);

        jToggleButton1.setIcon(resourceMap.getIcon("jToggleButton1.icon")); // NOI18N
        jToggleButton1.setText(resourceMap.getString("jToggleButton1.text")); // NOI18N
        jToggleButton1.setToolTipText(resourceMap.getString("jToggleButton1.toolTipText")); // NOI18N
        jToggleButton1.setFocusable(false);
        jToggleButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jToggleButton1.setName("jToggleButton1"); // NOI18N
        jToggleButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        scriptEngineToolbar.add(jToggleButton1);

        mainSeTabbedPane.setName("mainSeTabbedPane"); // NOI18N

        outputPanel.setName("outputPanel"); // NOI18N

        outputToolBar.setRollover(true);
        outputToolBar.setName("outputToolBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(skdsswing.SKDSSwingApp.class).getContext().getActionMap(SKDSSwingView.class, this);
        outputClearButton.setAction(actionMap.get("clearOutputWindow")); // NOI18N
        outputClearButton.setIcon(resourceMap.getIcon("outputClearButton.icon")); // NOI18N
        outputClearButton.setText(resourceMap.getString("outputClearButton.text")); // NOI18N
        outputClearButton.setToolTipText(resourceMap.getString("outputClearButton.toolTipText")); // NOI18N
        outputClearButton.setFocusable(false);
        outputClearButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        outputClearButton.setName("outputClearButton"); // NOI18N
        outputClearButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        outputToolBar.add(outputClearButton);

        outputSaveButton.setIcon(resourceMap.getIcon("outputSaveButton.icon")); // NOI18N
        outputSaveButton.setText(resourceMap.getString("outputSaveButton.text")); // NOI18N
        outputSaveButton.setToolTipText(resourceMap.getString("outputSaveButton.toolTipText")); // NOI18N
        outputSaveButton.setFocusable(false);
        outputSaveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        outputSaveButton.setName("outputSaveButton"); // NOI18N
        outputSaveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        outputToolBar.add(outputSaveButton);

        outputEnableButton.setIcon(resourceMap.getIcon("outputEnableButton.icon")); // NOI18N
        outputEnableButton.setSelected(true);
        outputEnableButton.setText(resourceMap.getString("outputEnableButton.text")); // NOI18N
        outputEnableButton.setToolTipText(resourceMap.getString("outputEnableButton.toolTipText")); // NOI18N
        outputEnableButton.setFocusable(false);
        outputEnableButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        outputEnableButton.setName("outputEnableButton"); // NOI18N
        outputEnableButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        outputToolBar.add(outputEnableButton);

        jScrollPaneOutput.setName("jScrollPaneOutput"); // NOI18N

        jTextAreaOutput.setColumns(20);
        jTextAreaOutput.setRows(5);
        jTextAreaOutput.setName("jTextAreaOutput"); // NOI18N
        jScrollPaneOutput.setViewportView(jTextAreaOutput);

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputPanelLayout.createSequentialGroup()
                .addComponent(outputToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(439, Short.MAX_VALUE))
            .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPaneOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 849, Short.MAX_VALUE))
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addComponent(outputToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(363, Short.MAX_VALUE))
            .addGroup(outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, outputPanelLayout.createSequentialGroup()
                    .addGap(54, 54, 54)
                    .addComponent(jScrollPaneOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        mainSeTabbedPane.addTab(resourceMap.getString("outputPanel.TabConstraints.tabTitle"), resourceMap.getIcon("outputPanel.TabConstraints.tabIcon"), outputPanel); // NOI18N

        javax.swing.GroupLayout scriptEnginePanelLayout = new javax.swing.GroupLayout(scriptEnginePanel);
        scriptEnginePanel.setLayout(scriptEnginePanelLayout);
        scriptEnginePanelLayout.setHorizontalGroup(
            scriptEnginePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scriptEngineToolbar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
            .addComponent(mainSeTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
        );
        scriptEnginePanelLayout.setVerticalGroup(
            scriptEnginePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptEnginePanelLayout.createSequentialGroup()
                .addComponent(scriptEngineToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSeTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
        );

        mainSplitPanel.setRightComponent(scriptEnginePanel);

        VisualizerPanel.setName("VisualizerPanel"); // NOI18N

        visualizerToolBar.setRollover(true);
        visualizerToolBar.setName("visualizerToolBar"); // NOI18N

        javax.swing.GroupLayout VisualizerPanelLayout = new javax.swing.GroupLayout(VisualizerPanel);
        VisualizerPanel.setLayout(VisualizerPanelLayout);
        VisualizerPanelLayout.setHorizontalGroup(
            VisualizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(visualizerToolBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        VisualizerPanelLayout.setVerticalGroup(
            VisualizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VisualizerPanelLayout.createSequentialGroup()
                .addComponent(visualizerToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(456, Short.MAX_VALUE))
        );

        mainSplitPanel.setLeftComponent(VisualizerPanel);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 874, Short.MAX_VALUE)
            .addComponent(mainSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 874, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(mainToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        openMenu.setText(resourceMap.getString("openMenu.text")); // NOI18N
        openMenu.setName("openMenu"); // NOI18N
        fileMenu.add(openMenu);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setAction(actionMap.get("showFrameUpload")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem3.setAction(actionMap.get("showFramePortConfiguration")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenu1.add(jMenuItem3);

        jMenuItem4.setAction(actionMap.get("showFrameUpload")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setActionCommand(resourceMap.getString("jMenuItem4.actionCommand")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenu1.add(jMenuItem4);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 874, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 704, Short.MAX_VALUE)
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel VisualizerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPaneOutput;
    private javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTabbedPane mainSeTabbedPane;
    private javax.swing.JSplitPane mainSplitPanel;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu openMenu;
    private javax.swing.JButton outputClearButton;
    private javax.swing.JToggleButton outputEnableButton;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JButton outputSaveButton;
    private javax.swing.JToolBar outputToolBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel scriptEnginePanel;
    private javax.swing.JToolBar scriptEngineToolbar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar visualizerToolBar;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private SDKSSwingFileExplorer fileExplorer;
    private SKDSSwingUpload frameUpload;
    private SKDSSwingPortConfiguration framePortconf;
}
