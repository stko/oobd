/*
 * SKDSSwingView.java
 */
package skdsswing;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import org.oobd.base.Core;
import org.oobd.base.IFui;

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
        JOptionPane.showMessageDialog(null, msg);
    }

    public void registerOobdCore(Core core) {
        oobdCore = core;
    }

    public void announceScriptengine(String id, String visibleName) {
        System.out.println("Interface announcement: Scriptengine-ID: " + id + " visibleName:" + visibleName);
        javax.swing.JMenuItem menuItem;
        menuItem = new javax.swing.JMenuItem(visibleName);
        menuItem.getAccessibleContext().setAccessibleDescription(
                "This doesn't really do anything");
        menuItem.addActionListener(this);
        menuItem.setActionCommand(id);
        openMenu.add(menuItem);


    }

    /**
     * Returns the class (not the instance!) to visulize the requested visualizer type
     * @param visualizerType
     * @param theme optional to request a design different from standard
     * @return
     */
    public Class getVisualizerClass(String visualizerType, String theme) {
        return SwingVizTable.class;

    }

    public void addCanvas(String seID, String name) {
        delCanvas(seID, name);
        JTabbedPane basejTabPane = (JTabbedPane) oobdCore.getAssign(seID, org.oobd.base.OOBDConstants.CL_PANE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setName(name);
        //JTabbedPane pane = new JTabbedPane();
        //pane.add(panel);
        basejTabPane.add(panel);
        GridBagConstraints c = new GridBagConstraints();

        /**
         * For each component to be added to this container:
         * ...Create the component...
         * ...Set instance variables in the GridBagConstraints instance...
         */
        JButton button = new JButton("Button 1");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(button, c);

        button = new JButton("Button 2");

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        panel.add(button, c);
    }

    public void delCanvas(String seID, String Name) {
        if (oobdCore.getAssign(seID, org.oobd.base.OOBDConstants.CL_PANE + ":" + Name) != null) {
            // delete the canvas..
            //delCanvas(seID, Name)
        }

    }

    /**
     * create ScriptEngine, when the user select the "Start.." menu item
     * @todo the event needs to checked if it really comes from a "Start.." menu entry
     * @todo tabs needs a "close" button as described on http://java.sun.com/docs/books/tutorial/uiswing/examples/components/index.html#TabComponentsDemo
     *
     */
    @Action
    public void actionPerformed(ActionEvent e) {
        //...Get information from the action event...
        //...Display it in the text area...
        System.out.println("Attempt to create ScriptEngine " + e.getActionCommand());
        String seID = oobdCore.createScriptEngine(e.getActionCommand()); //first get the unique name for the new scriptEngine Canvas
        JTabbedPane newjTabPane = new JTabbedPane(); //create a inner JTabbedPane as container for the later coming scriptengine pages
        newjTabPane.setName(seID); // set the name of that canvas that it can be found again later
        mainSeTabbedPane.addTab(seID, newjTabPane); // and put this canvas inside the pane which belongs to that particular scriptengine
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
        mainSeTabbedPane = new javax.swing.JTabbedPane();
        VisualizerPanel = new javax.swing.JPanel();
        visualizerToolBar = new javax.swing.JToolBar();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        openMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
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

        mainSeTabbedPane.setName("mainSeTabbedPane"); // NOI18N

        javax.swing.GroupLayout scriptEnginePanelLayout = new javax.swing.GroupLayout(scriptEnginePanel);
        scriptEnginePanel.setLayout(scriptEnginePanelLayout);
        scriptEnginePanelLayout.setHorizontalGroup(
            scriptEnginePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scriptEngineToolbar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
            .addComponent(mainSeTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 787, Short.MAX_VALUE)
        );
        scriptEnginePanelLayout.setVerticalGroup(
            scriptEnginePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scriptEnginePanelLayout.createSequentialGroup()
                .addComponent(scriptEngineToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainSeTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE))
        );

        mainSplitPanel.setRightComponent(scriptEnginePanel);

        VisualizerPanel.setName("VisualizerPanel"); // NOI18N

        visualizerToolBar.setRollover(true);
        visualizerToolBar.setName("visualizerToolBar"); // NOI18N

        javax.swing.GroupLayout VisualizerPanelLayout = new javax.swing.GroupLayout(VisualizerPanel);
        VisualizerPanel.setLayout(VisualizerPanelLayout);
        VisualizerPanelLayout.setHorizontalGroup(
            VisualizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(visualizerToolBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE)
        );
        VisualizerPanelLayout.setVerticalGroup(
            VisualizerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VisualizerPanelLayout.createSequentialGroup()
                .addComponent(visualizerToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(452, Short.MAX_VALUE))
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
                .addComponent(mainSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(skdsswing.SKDSSwingApp.class).getContext().getResourceMap(SKDSSwingView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        openMenu.setText(resourceMap.getString("openMenu.text")); // NOI18N
        openMenu.setName("openMenu"); // NOI18N
        fileMenu.add(openMenu);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(skdsswing.SKDSSwingApp.class).getContext().getActionMap(SKDSSwingView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 690, Short.MAX_VALUE)
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
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTabbedPane mainSeTabbedPane;
    private javax.swing.JSplitPane mainSplitPanel;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu openMenu;
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
}
