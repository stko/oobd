package org.oobd.ui.android;

import java.util.ArrayList;

import org.oobd.base.visualizer.IFvisualizer;
import org.oobd.ui.android.application.OOBDApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Andreas Budde, Peter Mayer
 * Adapter to show the differen {@link DiagnoseItem} in a {@link ListView}
 */
public class DiagnoseAdapter extends ArrayAdapter<DiagnoseItem> {
	
	private final LayoutInflater mlayoutInflater;
	
	VizTable myVisualizer;
	Context context;
	
	
	public BroadcastReceiver receiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
                String updateLevel = intent.getStringExtra(OOBDApp.UPDATE_LEVEL);
                System.out.println ("DiagnoseAdapter: Received Broadcast Event Update level: " + updateLevel);
                notifyDataSetChanged();
        }
	};
	
	public DiagnoseAdapter(Context context, ArrayList<DiagnoseItem> items) {
		super(context, 0, items);
		mlayoutInflater = LayoutInflater.from(context);
		
		myVisualizer = (VizTable)items;
		
		this.context = context;
		context.registerReceiver(receiver, new IntentFilter(OOBDApp.VISUALIZER_UPDATE));
	}
	
	public void guiPaused() {
		context.unregisterReceiver(receiver);
	}
	
	public void guiResumed() {
		context.registerReceiver(receiver, new IntentFilter(OOBDApp.VISUALIZER_UPDATE));
	}


	/**
	 * Called by the ListView component for each DiagnoseListViewItem 
	 * and properly displays each {@link DiagnoseItem}. Returns a View object 
	 * that represents a list row.
	 * @param position 	The position of the item within the adapter's data set of the item whose view we want.
	 * @param convertView 	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view.
	 * @param parent 	The parent that this view will eventually be attached to
	 * @returns A View corresponding to the data at the specified position. 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		//System.out.println ("getView(): try to find list position: " + position);
		// optimization: reuse diagnose items
		if (convertView == null) {
			// if there is no old view to reuse, a new one is created based on layout diagnose_item
			convertView = mlayoutInflater.inflate(R.layout.diagnose_item,
					parent, false);
		}

		DiagnoseItem item = getItem(position);
		
		if (item != null) {
			
			TextView functionValue = (TextView) convertView
				.findViewById(R.id.diagnose_function_value);
			functionValue.setText(item.getFunctionValue());
			
			TextView functionName = (TextView) convertView
				.findViewById(R.id.diagnose_function_name);
			functionName.setText(item.getFunctionName());
			
			TextView functionFlag1 = (TextView) convertView
				.findViewById(R.id.diagnose_function_flag1);
			functionFlag1.setText(item.getFunctionFlag1());
			
			TextView functionFlag2 = (TextView) convertView
				.findViewById(R.id.diagnose_function_flag2);
			functionFlag2.setText(item.getFunctionFlag2());
			
		}
		return convertView;
	}
	
}
