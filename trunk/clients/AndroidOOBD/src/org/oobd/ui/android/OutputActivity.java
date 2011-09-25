package org.oobd.ui.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONException;
import org.oobd.base.support.Onion;
import org.oobd.ui.android.application.AndroidGui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.lamerman.*;

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
											Intent intent = new Intent(
													OutputActivity
															.getInstance()
															.getBaseContext(),
													FileDialog.class);
											intent.putExtra(
													FileDialog.START_PATH,
													"/sdcard/OOBD");
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
												saveOutput(file);

												if (!file.exists()
														|| !file.canRead()) {
													Toast
															.makeText(
																	myOutputActivityInstance,
																	"Attachment Error",
																	Toast.LENGTH_SHORT)
															.show();
													return;
												}
												intent
														.putExtra(
																Intent.EXTRA_TEXT,
																Html.fromHtml("<html><body>With best regards from <a href='http://oobd.org'>OOBD</a></body></html>"));

												Uri uri = Uri.parse("file://"
														+ file);
												intent.putExtra(
														Intent.EXTRA_STREAM,
														uri);

											}
											startActivity(Intent.createChooser(
													intent, "Forward Output Text..."));
										}
									}
								});
						AlertDialog alert = builder.create();
						alert.show();
					}
				});

	}

	public synchronized void onActivityResult(final int requestCode,
			int resultCode, final Intent data) {

		if (resultCode == Activity.RESULT_OK) {

			if (requestCode == 1) {
				System.out.println("Saving...");
			} else if (requestCode == 2) {
				System.out.println("Loading...");
			}

			String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
			final File file = new File(filePath);
			if (file.exists()) {
				AlertDialog alertDialog = new AlertDialog.Builder(
						myOutputActivityInstance).create();
				alertDialog.setTitle("File already exist!");
				alertDialog.setMessage("OK to overwrite?");
				alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								saveOutput(file);
							}
						});
				alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", (DialogInterface.OnClickListener)null);
				alertDialog.show();
			} else {
				saveOutput(file);
			}

		} else if (resultCode == Activity.RESULT_CANCELED) {

		}

	}

	private void saveOutput(File file) {
		try {
			OutputStream outStream = new FileOutputStream(file);
			outStream.write(mytext.getText().toString().getBytes());

			outStream.close();
		} catch (IOException e) {
			// Unable to create file,
			// likely because external
			// storage is
			// not currently mounted.
			Log.w("ExternalStorage", "Error writing " + file, e);
		}

	}

	public static OutputActivity getInstance() {
		return myOutputActivityInstance;
	}

	public void addText(String text) {
		myRefreshHandler.sendMessage(Message.obtain(myRefreshHandler, 2, text));
	}

}
