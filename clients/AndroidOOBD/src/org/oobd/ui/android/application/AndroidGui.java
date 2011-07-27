package org.oobd.ui.android.application;



import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.oobd.base.Core;
import org.oobd.base.IFui;
import org.oobd.base.support.Onion;
import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.Diagnose;
import org.oobd.ui.android.MainActivity;
import org.oobd.ui.android.VizTable;

import android.content.Intent;
import android.util.Log;

public class AndroidGui implements IFui {

	public static AndroidGui thisAndroidGui; //singleton
	public Core core;
	public Map<String, String> scriptEngineMap = new HashMap <String, String>() ;
	
	private String scriptEngineID;
	private String scriptEngineVisibleName;
	
	public AndroidGui() {
		thisAndroidGui = this;
	}
	
	public static AndroidGui getInstance() {
		return thisAndroidGui;
	}
	
	public void sm(String msg) {
		// TODO Auto-generated method stub

	}

	public void registerOobdCore(Core core) {
		this.core = core;
		Log.v(this.getClass().getSimpleName(), "Core registered in IFui");

	}


	public void announceScriptengine(String id, String visibleName) {
		Log.v(this.getClass().getSimpleName(), "Interface announcement: Scriptengine-ID: " + id + " visibleName:" + visibleName);
		scriptEngineMap.put(id, visibleName);
		
		scriptEngineID = id;
		scriptEngineVisibleName = visibleName;

	}

	public Class getVisualizerClass(String visualizerType, String theme) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getSimpleName(), "Jetzt sollte ich den Visualizer rausruecken");
		return VizTable.class;
	}

	public void visualize(Onion myOnion) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getSimpleName(), "Und jetzt sollte ich visualisieren..");

		IFvisualizer visualComponent;
		
		Visualizer newVisualizer = new Visualizer(myOnion);
		Class<IFvisualizer> visualizerClass = getVisualizerClass(myOnion.getOnionString("type"), myOnion.getOnionString("theme"));
        Class[] argsClass = new Class[2]; // first we set up an pseudo - args - array for the scriptengine- constructor
        argsClass[0] = String.class; // and fill it with the info, that the argument for the constructor will be first a String
        argsClass[1] = String.class;
        // and fill it with the info, that the argument for the constructor will be first a String
        try {
            Method classMethod = visualizerClass.getMethod("getInstance", argsClass); // and let Java find the correct constructor with one string as parameter
            Object[] args = {newVisualizer.getOwnerEngine(), newVisualizer.getName()}; //we will an args-array with our String parameter
            // now call getInstance() in class VizTable
            visualComponent = (IFvisualizer)classMethod.invoke(null, args); // and finally create the object from the scriptengine class with its unique id as parameter
            // add new Visualizer object to already existing VizTable object 
            newVisualizer.setOwner((IFvisualizer) visualComponent);
            
            // add to internal list
            ((IFvisualizer) visualComponent).initValue(newVisualizer, myOnion);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}

	public void openPage(String seID, String Name, int colcount, int rowcount) {
		// TODO Auto-generated method stub
		Log.v(this.getClass().getSimpleName(), "Und jetzt open page machen..");
		VizTable vizTable = (VizTable)VizTable.getInstance("", "");
		if (vizTable != null && !vizTable.isEmpty())
			vizTable.clear();
	}

	public void openPageCompleted(String seID, String Name) {
		// TODO Auto-generated method stub
		//MainActivity.getMyMainActivity().startDiagnoseActivity();
		
		Log.v(this.getClass().getSimpleName(), "...open page completed");
		
	}
	
	public void startScriptEngine () {
		System.out.println("Attempt to create ScriptEngine ");
		
        String seID = OOBDApp.getInstance().getCore().createScriptEngine(scriptEngineID);
        
        //JTabbedPane newjTabPane = new JTabbedPane(); //create a inner JTabbedPane as container for the later coming scriptengine pages
        //newjTabPane.setName(seID); // set the name of that canvas that it can be found again later
        //mainSeTabbedPane.addTab(seID, newjTabPane); // and put this canvas inside the pane which belongs to that particular scriptengine
        // and now, after initalisation of the UI, let the games begin...
        OOBDApp.getInstance().getCore().setAssign(seID, org.oobd.base.OOBDConstants.CL_PANE, new Object()); //store the related drawing pane, the TabPane for that scriptengine
        OOBDApp.getInstance().getCore().startScriptEngine(seID);
	}

}
