package org.oobd.ui.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;

public class OutputActivity extends Activity {
	public static Handler myRefreshHandler;
	private EditText mytext;
	public static OutputActivity myOutputActivityInstance = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    myOutputActivityInstance = this;
		setContentView(R.layout.output);
		mytext = (EditText) findViewById(R.id.outputText);

		myRefreshHandler= new Handler(){
			@Override
			public void handleMessage(Message msg) {

				mytext.setText(mytext.getText().toString()+msg.obj.toString());
			}
			};

	}
	
	public static OutputActivity getInstance() {
		return myOutputActivityInstance;
	}


	public void addText(String text) {
		myRefreshHandler.sendMessage( Message.obtain(myRefreshHandler, 2, text));
		}

	
}
