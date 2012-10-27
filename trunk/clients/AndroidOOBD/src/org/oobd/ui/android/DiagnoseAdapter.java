package org.oobd.ui.android;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.oobd.base.OOBDConstants;
import org.oobd.base.visualizer.Visualizer;
import org.oobd.ui.android.application.OOBDApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;

/**
 * @author Andreas Budde, Peter Mayer
 * Adapter to show the differen {@link Visualizer} in a {@link ListView}
 */
public class DiagnoseAdapter extends ArrayAdapter<Visualizer> {
	
	private final LayoutInflater mlayoutInflater;
	
	VizTable myVisualizer;
	Context context;
	private Bitmap[] myIcons = new  Bitmap[6];
	
	public BroadcastReceiver receiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
                int updateLevel = intent.getIntExtra(OOBDApp.UPDATE_LEVEL,0);
                notifyDataSetChanged();
        }
	};
	
	public DiagnoseAdapter(Context context, ArrayList<Visualizer> items) {
		super(context, 0, items);
		mlayoutInflater = LayoutInflater.from(context);
		
		myVisualizer = (VizTable)items;

		myIcons[0]=BitmapFactory.decodeResource(MainActivity.getMyMainActivity().getResources(),R.drawable.blank);
		myIcons[1]=BitmapFactory.decodeResource(MainActivity.getMyMainActivity().getResources(),R.drawable.forward);
		myIcons[2]=BitmapFactory.decodeResource(MainActivity.getMyMainActivity().getResources(),R.drawable.update);
		myIcons[3]=BitmapFactory.decodeResource(MainActivity.getMyMainActivity().getResources(),R.drawable.timer);
		myIcons[4]=BitmapFactory.decodeResource(MainActivity.getMyMainActivity().getResources(),R.drawable.text);
		myIcons[5]=BitmapFactory.decodeResource(MainActivity.getMyMainActivity().getResources(),R.drawable.back);
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
	 * and properly displays each {@link Visualizer}. Returns a View object 
	 * that represents a list row.
	 * @param position 	The position of the item within the adapter's data set of the item whose view we want.
	 * @param convertView 	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view.
	 * @param parent 	The parent that this view will eventually be attached to
	 * @returns A View corresponding to the data at the specified position. 
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// optimization: reuse diagnose items
		if (convertView == null) {
			// if there is no old view to reuse, a new one is created based on layout diagnose_item
			convertView = mlayoutInflater.inflate(R.layout.diagnose_item,
					parent, false);
		}

		Visualizer item = null;
		try {
			item = getItem(position);

		} catch (Exception ex) {
			Logger.getLogger(DiagnoseAdapter.class.getName()).log(Level.WARNING,
					null, ex);
		}
		
		if (item != null) {
			
			TextView functionValue = (TextView) convertView
				.findViewById(R.id.diagnose_function_value);
			functionValue.setText(item.toString());
			
			TextView functionName = (TextView) convertView
				.findViewById(R.id.diagnose_function_name);
			functionName.setText(item.getToolTip());
			
/*			
			backImageView
			updateImageView
			timerImageView
			LogImageView
			forwardImageView
*/			
			if (item.getUpdateFlag(4)){
				((ImageView) convertView.findViewById(R.id.backImageView)).setImageBitmap(myIcons[OOBDConstants.VE_BACK+1]);
			}else{
				((ImageView) convertView.findViewById(R.id.backImageView)).setImageBitmap(myIcons[0]);			
			}
			
			if (item.getUpdateFlag(1)){
				((ImageView) convertView.findViewById(R.id.updateImageView)).setImageBitmap(myIcons[OOBDConstants.VE_UPDATE+1]);
			}else{
				((ImageView) convertView.findViewById(R.id.updateImageView)).setImageBitmap(myIcons[0]);
			}
			
			if (item.getUpdateFlag(2)){
				((ImageView) convertView.findViewById(R.id.timerImageView)).setImageBitmap(myIcons[OOBDConstants.VE_TIMER+1]);
			}else{
				((ImageView) convertView.findViewById(R.id.timerImageView)).setImageBitmap(myIcons[0]);
			}
			
			if (item.getUpdateFlag(3)){
				((ImageView) convertView.findViewById(R.id.LogImageView)).setImageBitmap(myIcons[OOBDConstants.VE_LOG+1]);
			}else{
				((ImageView) convertView.findViewById(R.id.LogImageView)).setImageBitmap(myIcons[0]);
			}
			
			if (item.getUpdateFlag(0)){
				((ImageView) convertView.findViewById(R.id.forwardImageView)).setImageBitmap(myIcons[OOBDConstants.VE_MENU+1]);
			}else{
				((ImageView) convertView.findViewById(R.id.forwardImageView)).setImageBitmap(myIcons[0]);
			}
			

			
		}
		return convertView;
	}
	
}
