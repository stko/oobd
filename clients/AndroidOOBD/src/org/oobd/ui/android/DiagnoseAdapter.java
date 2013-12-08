package org.oobd.ui.android;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.oobd.base.OOBDConstants;
import org.oobd.base.support.OnionNoEntryException;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.application.OOBDApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author Andreas Budde, Peter Mayer Adapter to show the differen
 *         {@link Visualizer} in a {@link ListView}
 */
public class DiagnoseAdapter extends ArrayAdapter<Visualizer> {

	private final LayoutInflater mlayoutInflater;

	VizTable myVisualizer;
	Context context;
	private Bitmap[] myIcons = new Bitmap[6];

	public BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int updateLevel = intent.getIntExtra(OOBDApp.UPDATE_LEVEL, 0);
			notifyDataSetChanged();
		}
	};

	public DiagnoseAdapter(Context context, ArrayList<Visualizer> items) {
		super(context, 0, items);
		mlayoutInflater = LayoutInflater.from(context);

		myVisualizer = (VizTable) items;

		myIcons[0] = BitmapFactory.decodeResource(MainActivity
				.getMyMainActivity().getResources(), R.drawable.blank);
		myIcons[1] = BitmapFactory.decodeResource(MainActivity
				.getMyMainActivity().getResources(), R.drawable.forward);
		myIcons[2] = BitmapFactory.decodeResource(MainActivity
				.getMyMainActivity().getResources(), R.drawable.update);
		myIcons[3] = BitmapFactory.decodeResource(MainActivity
				.getMyMainActivity().getResources(), R.drawable.timer);
		myIcons[4] = BitmapFactory.decodeResource(MainActivity
				.getMyMainActivity().getResources(), R.drawable.text);
		myIcons[5] = BitmapFactory.decodeResource(MainActivity
				.getMyMainActivity().getResources(), R.drawable.back);
		this.context = context;
		context.registerReceiver(receiver, new IntentFilter(
				OOBDApp.VISUALIZER_UPDATE));
	}

	public void guiPaused() {
		context.unregisterReceiver(receiver);
	}

	public void guiResumed() {
		context.registerReceiver(receiver, new IntentFilter(
				OOBDApp.VISUALIZER_UPDATE));
	}

	/**
	 * Called by the ListView component for each DiagnoseListViewItem and
	 * properly displays each {@link Visualizer}. Returns a View object that
	 * represents a list row.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @returns A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// Visualizer item = null;
		try {
			final Visualizer item = getItem(position);

			if (item != null) {
				if (item.isTypeOf("TextEdit")) {
					// optimization: reuse diagnose items

					if (convertView == null) {
						// if there is no old view to reuse, a new one is
						// created
						// based on
						// layout diagnose_item
						convertView = mlayoutInflater.inflate(
								R.layout.diagnose_textedit_item, parent, false);
					}
					EditText functionValue = (EditText) convertView
							.findViewWithTag("value");
					functionValue.setText(item.toString());
					functionValue
							.setOnEditorActionListener(new OnEditorActionListener() {

								public boolean onEditorAction(
										TextView textView, int actionId,
										KeyEvent event) {
									String value = textView.getText()
											.toString();

									boolean inputOk = (item.getRegex() == null || item
											.getRegex().equalsIgnoreCase(""))
											|| (value != null && value
													.matches(item.getRegex()));
									System.out.println("input ok:"
											+ item.getRegex());
									if (actionId == EditorInfo.IME_NULL
											&& event.getAction() == KeyEvent.ACTION_DOWN) {
										if (inputOk) {
											textView.clearFocus();
											item.inputNewValue(value);
											item.updateRequest(OOBDConstants.UR_USER);
										}
										return true;
									}

									return false;
								}
							});
					// functionValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
					functionValue.addTextChangedListener(new myTextWatcher(
							functionValue) {
						public void beforeTextChanged(CharSequence s,
								int start, int count, int after) {
						}

						public void onTextChanged(CharSequence s, int start,
								int before, int count) {
						}

						public void afterTextChanged(Editable v) {

							String value = myEditView.getText().toString();

							boolean inputOk = (item.getRegex() == null || item
									.getRegex().equalsIgnoreCase(""))
									|| (value != null && value.matches(item
											.getRegex()));
							System.out.println("input ok:" + item.getRegex());
							if (inputOk) {
								myEditView.setTextColor(Color.GREEN);
							} else {
								myEditView.setTextColor(Color.RED);
							}
						}
					});
					TextView functionName = (TextView) convertView
							.findViewWithTag("name");
					functionName.setText(item.getToolTip());
					/*
					 * } else if (item.isTypeOf("CheckBox")) {
					 */
				} else if (item.isTypeOf("Slider")) {
					// optimization: reuse diagnose items

					if (convertView == null) {
						// if there is no old view to reuse, a new one is
						// created
						// based on
						// layout diagnose_item
						convertView = mlayoutInflater.inflate(
								R.layout.diagnose_slider_item, parent, false);
					}
					SeekBar functionValue = (SeekBar) convertView
							.findViewWithTag("value");
					functionValue.setMax(item.getMax());
					functionValue.setProgress(Visualizer.safeInt(item
							.toString()));
					functionValue
							.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

								public void onProgressChanged(SeekBar seekBar,
										int progress, boolean fromUser) {
									item.inputNewValue(progress);
									item.updateRequest(OOBDConstants.UR_USER);
								}

								public void onStartTrackingTouch(SeekBar arg0) {
									// TODO Auto-generated method stub

								}

								public void onStopTrackingTouch(SeekBar arg0) {
									// TODO Auto-generated method stub

								}
							});

					TextView functionName = (TextView) convertView
							.findViewWithTag("name");
					functionName.setText(item.getToolTip());
					/*
					 * } else if (item.isTypeOf("Combo")) {
					 */
				} else if (item.isTypeOf("Checkbox")) {
					// optimization: reuse diagnose items

					if (convertView == null) {
						// if there is no old view to reuse, a new one is
						// created
						// based on
						// layout diagnose_item
						convertView = mlayoutInflater.inflate(
								R.layout.diagnose_checkbox_item, parent, false);
					}
					CheckBox functionValue = (CheckBox) convertView
							.findViewWithTag("value");
					functionValue.setText(item.getToolTip());
					functionValue.setSelected(new Boolean(item.toString()));
/*					functionValue
							.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {

									item.inputNewValue(new Boolean(
											((CheckedTextView) v).isSelected())
											.toString());
									item.updateRequest(OOBDConstants.UR_USER);
									// ((CheckedTextView) v).toggle();
								}

							});*/
					functionValue
							.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(
										CompoundButton buttonView,
										boolean isChecked) {
									item.inputNewValue(new Boolean(buttonView
											.isChecked()).toString());
									item.updateRequest(OOBDConstants.UR_USER);

								}
							});
					TextView functionName = (TextView) convertView
							.findViewWithTag("name");
					functionName.setText(item.getToolTip());
					/*
					 * } else if (item.isTypeOf("Combo")) {
					 */
				} else if (item.isTypeOf("Gauge")) {
					// optimization: reuse diagnose items

					if (convertView == null) {
						// if there is no old view to reuse, a new one is
						// created
						// based on
						// layout diagnose_item
						convertView = mlayoutInflater.inflate(
								R.layout.diagnose_gauge_item, parent, false);
					}
					ProgressBar functionValue = (ProgressBar) convertView
							.findViewWithTag("value");
					functionValue.setMax(item.getMax());
					functionValue.setProgress(Visualizer.safeInt(item
							.toString()));

					TextView functionName = (TextView) convertView
							.findViewWithTag("name");
					functionName.setText(item.getToolTip());
					/*
					 * } else if (item.isTypeOf("Combo")) {
					 */

				} else { // default label
					// optimization: reuse diagnose items

					if (convertView == null) {
						// if there is no old view to reuse, a new one is
						// created
						// based on
						// layout diagnose_item
						convertView = mlayoutInflater.inflate(
								R.layout.diagnose_item, parent, false);
					}
					TextView functionValue = (TextView) convertView
							.findViewWithTag("value");
					functionValue.setText(item.toString());

					TextView functionName = (TextView) convertView
							.findViewWithTag("name");
					functionName.setText(item.getToolTip());
				}
			}
			if (item != null) {

				if (item.getUpdateFlag(4)) {
					((ImageView) convertView.findViewWithTag("back"))
							.setImageBitmap(myIcons[OOBDConstants.VE_BACK + 1]);
				} else {
					((ImageView) convertView.findViewWithTag("back"))
							.setImageBitmap(myIcons[0]);
				}

				if (item.getUpdateFlag(1)) {
					((ImageView) convertView.findViewWithTag("update"))
							.setImageBitmap(myIcons[OOBDConstants.VE_UPDATE + 1]);
				} else {
					((ImageView) convertView.findViewWithTag("update"))
							.setImageBitmap(myIcons[0]);
				}

				if (item.getUpdateFlag(2)) {
					((ImageView) convertView.findViewWithTag("timer"))
							.setImageBitmap(myIcons[OOBDConstants.VE_TIMER + 1]);
				} else {
					((ImageView) convertView.findViewWithTag("timer"))
							.setImageBitmap(myIcons[0]);
				}

				if (item.getUpdateFlag(3)) {
					((ImageView) convertView.findViewWithTag("log"))
							.setImageBitmap(myIcons[OOBDConstants.VE_LOG + 1]);
				} else {
					((ImageView) convertView.findViewWithTag("log"))
							.setImageBitmap(myIcons[0]);
				}

				if (item.getUpdateFlag(0)) {
					((ImageView) convertView.findViewWithTag("forward"))
							.setImageBitmap(myIcons[OOBDConstants.VE_MENU + 1]);
				} else {
					((ImageView) convertView.findViewWithTag("forward"))
							.setImageBitmap(myIcons[0]);
				}

			}
		} catch (Exception ex) {
			Logger.getLogger(DiagnoseAdapter.class.getName()).log(
					Level.WARNING, null, ex);
		}
		return convertView;

	}

}

abstract class myTextWatcher implements TextWatcher {
	EditText myEditView;

	myTextWatcher(EditText myView) {
		super();
		myEditView = myView;

	}
}
