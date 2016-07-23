package org.oobd.ui.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.oobd.base.OOBDConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

//http://code.google.com/p/android-file-dialog/

public class OutputActivity extends Activity implements
		org.oobd.base.OOBDConstants {
	public static Handler myRefreshHandler;
	private EditText mytext;
	private ToggleButton myLogActiveButton;
	public static OutputActivity myOutputActivityInstance = null;
	private Hashtable<String, ArrayList<Character>> outputBuffers = new Hashtable<String, ArrayList<Character>>();
	private String actualBufferName = OB_DEFAULT_NAME; // name of the actual
														// writestring output,
														// default is "display"
														// for screen output
	private char[] actBuffer; //acts as temporary buffer for the output content, while we waiting for the result of the file save dialog
	private boolean actFileAppend; //acts as temporary flag for the file Dialog intent, if the ouput should be appended to the file or not 
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
			public void handleMessage(Message msgObject) {
				OutputText myOutputText = (OutputText) msgObject.obj;
				String msg = myOutputText.text;
				String modifier = myOutputText.modifier;
				if (!"".equalsIgnoreCase(modifier)) {
					if (modifier.equalsIgnoreCase(OB_CMD_SETBUFFER)) {
						actualBufferName = msg.toLowerCase().trim();
						if (!actualBufferName.equals(OB_DEFAULT_NAME)) {
							if (!outputBuffers.containsKey(actualBufferName)) {
								outputBuffers.put(actualBufferName,
										new ArrayList<Character>());
								actBuffer = new char[0];
							}
						}
					}
					if (modifier.equalsIgnoreCase(OB_CMD_CLEAR)) {
						if (actualBufferName.equals(OB_DEFAULT_NAME)) { // do
																		// the
																		// special
																		// handling
																		// of
																		// the
																		// UI
																		// textbox
																		// here
							mytext.setText("");
						} else {
							outputBuffers.put(actualBufferName,
									new ArrayList<Character>());
							actBuffer = new char[0];
						}
					} else if (modifier.equalsIgnoreCase(OB_CMD_CLEARALL)) {
						mytext.setText("");
						outputBuffers = new Hashtable<String, ArrayList<Character>>();
						actBuffer = new char[0];
					} else {
						// here we need the buffer content, so we need to do the
						// time consuming conversion here
						if (actualBufferName.equals(OB_DEFAULT_NAME)) {
							actBuffer = mytext.getText().toString()
									.toCharArray();
						} else {
							if (outputBuffers.containsKey(actualBufferName)) {
								actBuffer = arrayListToCharArray(outputBuffers
										.get(actualBufferName));
							} else {
								outputBuffers.put(actualBufferName,
										new ArrayList<Character>());
								actBuffer = new char[0];
							}
						}
						if (modifier.equalsIgnoreCase(OB_CMD_SAVEAS)) {
							saveBufferAsFileRequest(msg, false);
						}
						if (modifier.equalsIgnoreCase(OB_CMD_SAVE)) {
							saveBufferToFile(msg, false);
						}
						if (modifier.equalsIgnoreCase(OB_CMD_APPENDAS)) {
							saveBufferAsFileRequest(msg, true);
						}
						if (modifier.equalsIgnoreCase(OB_CMD_APPEND)) {
							saveBufferToFile(msg, true);
						}
					}
				} else {
					if (actualBufferName.equals(OB_DEFAULT_NAME)) {
						if (myLogActiveButton.isChecked()) {
							mytext.setText(mytext.getText().toString() + msg
									+ "\n");
						}
					} else {
						ArrayList<Character> actBufferArrayList = outputBuffers
								.get(actualBufferName);
						for (char c : msg.toCharArray()) {
							actBufferArrayList.add(c);
						}
					}
				}

			}

		};
		((ImageButton) findViewById(R.id.clearButton))
				.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {
						mytext.setText("");
					}
				});

		((ImageButton) findViewById(R.id.diskButton))
				.setOnClickListener(new View.OnClickListener() {

					public void onClick(View v) {

						final CharSequence[] items = { "Save as File..",
								"Send as Text..", "Attach as .txt ..",
								"Attach as .xml .." };

						AlertDialog.Builder builder = new AlertDialog.Builder(
								myOutputActivityInstance);
						builder.setTitle("Handle Output..");
						builder.setItems(items,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										if (item == 0) { // save locally
											/*
											 * Intent intent = new Intent(
											 * OutputActivity .getInstance()
											 * .getBaseContext(),
											 * FileDialog.class);
											 * intent.putExtra(
											 * FileDialog.START_PATH,
											 * "/sdcard/OOBD");
											 * startActivityForResult(intent,
											 * 1);
											 */
											Intent intent = new Intent(
													"org.openintents.action.PICK_FILE");
											intent.putExtra(Intent.EXTRA_TITLE,
													"Save as text");
											startActivityForResult(intent, 1);
										} else { // send it somehow
											Intent intent = new Intent(
													Intent.ACTION_SEND);
											intent.putExtra(
													Intent.EXTRA_SUBJECT,
													"OOBD Mail");
											if (item == 1) {// the body contains
												// the text
												// intent.putExtra(Intent.EXTRA_EMAIL,
												// new String[]
												// {"email@example.com"});
												intent.setType("text/plain");
												intent.putExtra(
														Intent.EXTRA_TEXT,
														mytext.getText()
																.toString());
											} else { // create a attachment
												intent.setType("text/html");
												String fileName = "";
												if (item == 2) {
													fileName = "OOBD-textfile.txt";
												}
												if (item == 3) {
													fileName = "OOBD-xmlfile.xml";
												}
												// first
												String extStorageDirectory = Environment
														.getExternalStorageDirectory()
														.toString()
														+ "/OOBD/";
												File file = new File(
														extStorageDirectory,
														fileName);
												actBuffer=mytext.getText().toString().toCharArray(); //.toString().getBytes();
												saveBufferToFile(file,false);

												if (!file.exists()
														|| !file.canRead()) {
													Toast.makeText(
															myOutputActivityInstance,
															"Attachment Error",
															Toast.LENGTH_SHORT)
															.show();
													return;
												}
												intent.putExtra(
														Intent.EXTRA_TEXT,
														Html.fromHtml("<html><body>With best regards from <a href='http://oobd.org'>OOBD</a></body></html>"));

												Uri uri = Uri.parse("file://"
														+ file);
												intent.putExtra(
														Intent.EXTRA_STREAM,
														uri);

											}
											startActivity(Intent.createChooser(
													intent,
													"Forward Output Text..."));
										}
									}
								});
						AlertDialog alert = builder.create();
						alert.show();
					}
				});

	}

	/*
	 * as char[] and Arraylist<Character> are not compatible, we need to handle
	 * Display output and normal buffer handling independent from each other and
	 * only convert, when really needed.
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

	boolean saveBufferToFile(String fileName,  boolean append) {
			File file = new File(fileName);
			return saveBufferToFile(file, append);
	}

	boolean saveBufferToFile(File fileHandle , boolean append) {
		try {
			FileWriter os = new FileWriter(fileHandle, append);
			os.write(actBuffer);
			os.close();
			return true;
		} catch (IOException ex) {
			// Unable to create file,
			// likely because external
			// storage is
			// not currently mounted.
			Log.w("ExternalStorage", "Error writing " + fileHandle.getName(), ex);
			return false;
		}
	}

	private void saveBufferAsFileRequest(String FileName,
			boolean append) {
		actFileAppend=append;
		Intent intent = new Intent(
				"org.openintents.action.PICK_FILE");
		intent.putExtra(Intent.EXTRA_TITLE,
				"Save to file");
		startActivityForResult(intent, 1);


	}

	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		if (resultCode == Activity.RESULT_OK) {

			URI filePath = null;
			try {
				filePath = new URI(data.getDataString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			final File file = new File(filePath);
			if (file.exists()) {
				AlertDialog alertDialog = new AlertDialog.Builder(
						myOutputActivityInstance).create();
				alertDialog.setTitle("File already exist!");
				alertDialog.setMessage("OK to overwrite?");
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								saveBufferToFile(file,false);
							}
						});
				alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
						"Cancel", (DialogInterface.OnClickListener) null);
				alertDialog.show();
			} else {
				saveBufferToFile(file,false);
			}

		} else if (resultCode == Activity.RESULT_CANCELED) {

		}

	}


	public static OutputActivity getInstance() {
		return myOutputActivityInstance;
	}

	public void addText(String text, String modifier) {
		myRefreshHandler.sendMessage(Message.obtain(myRefreshHandler, 2,
				new OutputText(text, modifier)));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if back- button is pressed
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			DiagnoseTab.getInstance().getTabHost().setCurrentTab(0);
			return true; // stop further handling of the
			// back-button
		}
		return super.onKeyDown(keyCode, event);
	}

}

class OutputText {
	public String text;
	public String modifier;

	public OutputText(String text, String modifier) {
		this.text = text;
		this.modifier = modifier;
	}
}
