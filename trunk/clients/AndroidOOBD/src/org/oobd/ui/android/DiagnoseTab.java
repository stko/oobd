package org.oobd.ui.android;

import android.app.TabActivity;
import android.widget.TabHost;
import android.os.Bundle;
import 	android.content.Intent;
import android.content.res.Resources;
import org.oobd.ui.android.R;

public class DiagnoseTab extends TabActivity {
	private static DiagnoseTab myDiagnoseTabInstance=null;
	private TabHost myTabHost;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.diagnosetab);
	    myDiagnoseTabInstance=this;
	    Resources res = getResources(); // Resource object to get Drawables
	    myTabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Diagnose.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = myTabHost.newTabSpec("Diagnose").setIndicator("Diagnose")
	                  .setContent(intent);
	    myTabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, OutputActivity.class);
	    spec = myTabHost.newTabSpec("Output").setIndicator("Output")
	                  .setContent(intent);
	    myTabHost.addTab(spec);


	    myTabHost.setCurrentTab(0);
	}
	
	public void outputToFront(){
		myTabHost.setCurrentTab(1);
	}
	
	public static DiagnoseTab getInstance() {
		return myDiagnoseTabInstance;
	}
	
}
