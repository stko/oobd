package org.oobd.ui.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;

public class ModalDialog extends DialogFragment {
	
	String message="";
	String title="";
	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	NoticeDialogListener mListener;

	ModalDialog(String title, String thisMessage){
		message=thisMessage;
		this.title=title;
	}
	
	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (NoticeDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title)
				.setMessage(
						Html.fromHtml(message))

				.setPositiveButton("Read and Agreed",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Send the positive button event back to the
								// host activity
								mListener
										.onDialogPositiveClick(ModalDialog.this);
							}
						})
				.setNegativeButton("Do NOT Agree",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Send the negative button event back to the
								// host activity
								mListener
										.onDialogNegativeClick(ModalDialog.this);
							}
						});
		return builder.create();
	}

}
