package edu.upc.fib.roadtriptest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TripList extends ListActivity {
	/** Called when the activity is first created. */

	DBAdapter db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.triplist);
		db = new DBAdapter(this);
		
		ListView listview = (ListView)findViewById(android.R.id.list);
		listview.setOnItemClickListener(mDeviceClickListener);

		refresh();
	}
	
	private void refresh(){

		db.open();
		int count = db.countTrips();
		if(count > 0){
			String[] trip_list = new String[count];
			Cursor tripCur = db.getAllTrips();
		
			int counter = 0;
			tripCur.moveToFirst();
			do{
				trip_list[counter++] = tripCur.getString(tripCur.getColumnIndex(DBAdapter.KEY_NAME));
			}while(tripCur.moveToNext());
			tripCur.close();
			db.close();
			
			setListAdapter(new ArrayAdapter<String>(this, R.layout.triplist_row,
					R.id.label, trip_list));
		}
		else{
			db.close();
			setListAdapter(new ArrayAdapter<String>(this, R.layout.triplist_row,
					R.id.label, new String[0]));
		}
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	
        	LinearLayout linearlayout = (LinearLayout) v;
        	String name = ((TextView) linearlayout.findViewById(R.id.label)).getText().toString();
        	
			Intent test = new Intent(TripList.this,ConfigTrip.class);
        	test.putExtra("name", name);
        	startActivity(test);
        	
        }
    };
}