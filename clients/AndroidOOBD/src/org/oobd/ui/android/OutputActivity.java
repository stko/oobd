package org.oobd.ui.android;

import org.json.JSONException;
import org.oobd.base.support.Onion;
import org.oobd.ui.android.application.AndroidGui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ToggleButton;


//http://code.google.com/p/android-file-dialog/


public class OutputActivity extends Activity {
	public static Handler myRefreshHandler;
	private EditText mytext;
	private ToggleButton myLogActiveButton;
	public static OutputActivity myOutputActivityInstance = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myOutputActivityInstance = this;
		setContentView(R.layout.output);
		mytext = (EditText) findViewById(R.id.outputText);
		myLogActiveButton = (ToggleButton) findViewById(R.id.loggingToggleButton);
		myRefreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				if (myLogActiveButton.isChecked()) {
					mytext.setText(mytext.getText().toString()
							+ msg.obj.toString());

				}
			}
		};
		((ImageButton) findViewById(R.id.clearButton))
		.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				mytext.setText("");
			}
		});
		((ImageButton) findViewById(R.id.mailButton))
		.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
//				intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
				intent.putExtra(Intent.EXTRA_SUBJECT, "OOBD Mail");
				intent.putExtra(Intent.EXTRA_TEXT, mytext.getText().toString());
/*				File root = Environment.getExternalStorageDirectory();
				File file = new File(root, xmlFilename);
				if (!file.exists() || !file.canRead()) {
				    Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
				    finish();
				    return;
				}
				Uri uri = Uri.parse("file://" + file);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
*/				startActivity(Intent.createChooser(intent, "Send Message..."));

			}
		});

	}

	public static OutputActivity getInstance() {
		return myOutputActivityInstance;
	}

	public void addText(String text) {
		myRefreshHandler.sendMessage(Message.obtain(myRefreshHandler, 2, text));
	}

}
