package com.mensaunibe.util.gui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.mensaunibe.R;
import com.mensaunibe.app.controller.Controller;
import com.mensaunibe.app.model.Mensa;
import com.mensaunibe.app.model.MensaList;
import com.mensaunibe.app.model.Menu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class AdapterCustomMenulist extends BaseAdapter {
	
	// for logging and debugging purposes
	private static final String TAG = AdapterCustomMenulist.class.getSimpleName();
	
	private Controller mController;

	private MensaList mModel;
	private List<Menu> mMenus;
	private int mResource;

	public AdapterCustomMenulist(Controller controller, MensaList mensalist, List<Menu> menulist, int resource) {
		super();
		this.mController = controller;
		this.mModel = mensalist;
		this.mMenus = menulist;
		this.mResource = resource; // the xml layout file, like this it gets dynamic
	}
	
	public AdapterCustomMenulist(Controller controller, List<Menu> menulist, int resource) {
		super();
		this.mController = controller;
		this.mMenus = menulist;
		this.mResource = resource; // the xml layout file, like this it gets dynamic
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		
		// get a reference to the parent view for the rating dialog
//		this.parent = parent;

		LayoutInflater mInflater = (LayoutInflater) mController.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = mInflater.inflate(mResource, parent, false);
        
        LinearLayout grid = (LinearLayout) rowView.findViewById(R.id.list_grid);
        
        // the actual fields that contain text
        final Menu menu = mMenus.get(position);
        
        // handle the full menu list displaying of the mensa
        if ( (TextView) rowView.findViewById(R.id.mensa) != null ) {
        	TextView mensa = (TextView) rowView.findViewById(R.id.mensa);
        	// get the mensa name for the menu overview (FragmentMenuList)
			Mensa mensaObj = mModel.getMensaById(menu.getMensaID());
			if (mensaObj != null) {
				mensa.setText(mensaObj.getName());
			} else {
				Log.e(TAG, "mensaObj was null!");
			}
        }
        
        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView desc = (TextView) rowView.findViewById(R.id.desc);
        TextView price = (TextView) rowView.findViewById(R.id.price);
        TextView date = (TextView) rowView.findViewById(R.id.date);
        TextView rating = (TextView) rowView.findViewById(R.id.rating);
        TextView count = (TextView) rowView.findViewById(R.id.count);
        
        title.setText(menu.getTitle());
        desc.setText(menu.getDesc());
        price.setText(menu.getPrice());
        date.setText(menu.getDate());
        rating.setText(String.valueOf(menu.getRating()));
        count.setText(String.valueOf(menu.getVotes()));

        // set the click listener for the menu item
        final OnClickListener rowListener = new OnClickListener() {
            @Override
            public void onClick(View rowView) {
            	// TODO: remove dev toast
            	//Toast.makeText(mController, "Menu clicked, show rating...", Toast.LENGTH_SHORT).show();
            	
            	showRating(menu.getMenuID());
            }
        };
        
        // prevent menus that are not served today from getting rated
        if (getCurrentDayName().equals(menu.getDay())) {
        	grid.setOnClickListener(rowListener);
        }
            
		return rowView;
	}
	
	@Override
	public int getCount() {
		return mMenus.size();
	}
	
	@Override
	public Menu getItem(int position) {
		return mMenus.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	/**
	 * this method is used to determine TODAYS name in english
	 * to instruct the class to set the onClickListener for rating or not
	 * @return the english name of today, can be compared with API data!
	 */
	public String getCurrentDayName() {
		Calendar calendar = new GregorianCalendar();
		Date now = new Date();   
		calendar.setTime(now);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		switch(day) {
			// sunday
			case 1: 
				return "Friday";
			// monday
			case 2:
				return "Monday";
			// tuesday
			case 3:
				return "Tuesday";
			// wednesday
			case 4:
				return "Wednesday";
			// thursday
			case 5:
				return "Thursday";
			// friday
			case 6:
				return "Friday";
			// saturday
			case 7:
				return "Friday";
			default:
				return "Monday";
		}
	}
	
	public void showRating(final int menuid) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mController);
		
//		View dialogLayout = inflater.inflate(R.layout.dialog_rating, parent);
		final RatingBar rating = new RatingBar(mController);
//		final RatingBar rating = (RatingBar) dialogLayout.findViewById(R.id.rating_bar);
		rating.setMax(5);
		rating.setStepSize(1.0f);
		rating.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		LinearLayout parentLayout = new LinearLayout(mController);
        parentLayout.setGravity(Gravity.CENTER);
        parentLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        parentLayout.addView(rating);

		dialogBuilder.setIcon(R.drawable.ic_star);
		dialogBuilder.setTitle("Menu bewerten");
		dialogBuilder.setView(parentLayout);

		// Buttons OK
		dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//txtView.setText(String.valueOf(rating.getProgress()));
				// send rating to server
				Controller.getDataHandler().APIRegisterRating(menuid, rating.getProgress());
				Toast.makeText(mController, "Send Vote for menu with ID = " + menuid + " to server..." + String.valueOf(rating.getProgress()), Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}
		});
		
		// Button cancel
		dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		dialogBuilder.create();
		dialogBuilder.show();
	}
}