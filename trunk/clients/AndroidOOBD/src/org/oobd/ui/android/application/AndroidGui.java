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
import org.oobd.ui.android.DiagnoseAdapter;
import org.oobd.ui.android.DiagnoseTab;
import org.oobd.ui.android.MainActivity;
import org.oobd.ui.android.OutputActivity;
import org.oobd.ui.android.VizTable;

//import android.app.ProgressDialog;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class AndroidGui implements IFui {

	public static AndroidGui thisAndroidGui; // singleton
	public Core core;
	public Map<String, String> scriptEngineMap = new HashMap<String, String>();

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
		OutputActivity.getInstance().addText(msg + "\n");
		// TODO outputtofront crashes
		// DiagnoseTab.getInstance().outputToFront();
	}

	public void registerOobdCore(Core core) {
		this.core = core;
		Log.v(this.getClass().getSimpleName(), "Core registered in IFui");

	}

	public void announceScriptengine(String id, String visibleName) {
		Log.v(this.getClass().getSimpleName(),
				"Interface announcement: Scriptengine-ID: " + id
						+ " visibleName:" + visibleName);
		scriptEngineMap.put(id, visibleName);

		scriptEngineID = id;
		scriptEngineVisibleName = visibleName;

	}

	public Class getVisualizerClass(String visualizerType, String theme) {
		return VizTable.class;
	}

	public void visualize(Onion myOnion) {
		IFvisualizer visualComponent;

		Visualizer newVisualizer = new Visualizer(myOnion);
		Class<IFvisualizer> visualizerClass = getVisualizerClass(
				myOnion.getOnionString("type"), myOnion.getOnionString("theme"));
		String debugText = myOnion.toString();
		Class[] argsClass = new Class[2]; // first we set up an pseudo - args -
											// array for the scriptengine-
											// constructor
		argsClass[0] = String.class; // and fill it with the info, that the
										// argument for the constructor will be
										// first a String
		argsClass[1] = String.class;
		// and fill it with the info, that the argument for the constructor will
		// be first a String
		try {
			Method classMethod = visualizerClass.getMethod("getInstance",
					argsClass); // and let Java find the correct constructor
								// with one string as parameter
			Object[] args = { newVisualizer.getOwnerEngine(),
					newVisualizer.getName() }; // we will an args-array with our
												// String parameter
			// now call getInstance() in class VizTable
			visualComponent = (IFvisualizer) classMethod.invoke(null, args); // and
																				// finally
																				// create
																				// the
																				// object
																				// from
																				// the
																				// scriptengine
																				// class
																				// with
																				// its
																				// unique
																				// id
																				// as
																				// parameter
			// add new Visualizer object to already existing VizTable object
			newVisualizer.setOwner(visualComponent);

			// add to internal list
			(visualComponent).initValue(newVisualizer, myOnion);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void openPage(String seID, String Name, int colcount, int rowcount) {
		Diagnose.getInstance().stopProgressDialog();
		DiagnoseTab.getInstance().setMenuTitle(Name);
		Diagnose.getInstance().startProgressDialog("Build Page...");
		Log.v(this.getClass().getSimpleName(), "open page ..");
		Diagnose.getInstance().stopVisibility();
		MainActivity.getMyMainActivity().runOnUiThread(new Runnable() {

			public void run() {

				Diagnose.getInstance()
						.getListView()
						.setAdapter(
								new DiagnoseAdapter(Diagnose.getInstance(),
										null));
			}
		});

		VizTable vizTable = VizTable.getInstance("", "");
		if (vizTable != null) {
			if (!vizTable.isEmpty()) {
				vizTable.setRemove("");
				vizTable.clear();
			}
			Intent broadcast = new Intent(OOBDApp.VISUALIZER_UPDATE);
			broadcast.putExtra(OOBDApp.UPDATE_LEVEL, 1);
			Diagnose.myDiagnoseInstance.getApplicationContext().sendBroadcast(
					broadcast);
		}

	}

	public void openPageCompleted(String seID, String Name) {
		MainActivity.getMyMainActivity().runOnUiThread(new Runnable() {

			public void run() {
				Diagnose.getInstance().setItems(VizTable.getInstance("", ""));
				Diagnose.getInstance().stopProgressDialog();
			}
		});


		Log.v(this.getClass().getSimpleName(), "...open page completed");
		Intent broadcast = new Intent(OOBDApp.VISUALIZER_UPDATE);
		broadcast.putExtra(OOBDApp.UPDATE_LEVEL, 1);
		Diagnose.myDiagnoseInstance.getApplicationContext().sendBroadcast(
				broadcast);

	}

	public void startScriptEngine(Onion onion) {

		String seID = OOBDApp.getInstance().getCore()
				.createScriptEngine(scriptEngineID, onion);

		// JTabbedPane newjTabPane = new JTabbedPane(); //create a inner
		// JTabbedPane as container for the later coming scriptengine pages
		// newjTabPane.setName(seID); // set the name of that canvas that it can
		// be found again later
		// mainSeTabbedPane.addTab(seID, newjTabPane); // and put this canvas
		// inside the pane which belongs to that particular scriptengine
		// and now, after initialisation of the UI, let the games begin...
		OOBDApp.getInstance()
				.getCore()
				.setAssign(seID, org.oobd.base.OOBDConstants.CL_PANE,
						new Object()); // store the related drawing pane, the
										// TabPane for that scriptengine
		// stop the Progress Dialog BEFORE the script starts
		// Diagnose.getInstance().stopProgressDialog();
		OOBDApp.getInstance().getCore().startScriptEngine(seID, onion);

	}

}
