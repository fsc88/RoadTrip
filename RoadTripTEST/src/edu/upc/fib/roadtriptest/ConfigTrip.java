package edu.upc.fib.roadtriptest;


import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ConfigTrip extends ListActivity implements OnClickListener {
    /** Called when the activity is first created. */
	
	DBAdapter db;
	Button addNewItem,calculate;
	TextView tvTripName;
	String tripName;
	int tripId;
	int[] itemIdList;
	double tripTotal;
	TextView tripTotalText;
	ListView listview;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configtrip);
        db = new DBAdapter(this);
        
        addNewItem = (Button)findViewById(R.id.button_add_new_item);
        addNewItem.setOnClickListener(this);
        
        calculate = (Button)findViewById(R.id.button_calculate);
        calculate.setOnClickListener(this);
        
        listview = (ListView)findViewById(android.R.id.list);
		listview.setOnItemClickListener(mDeviceClickListener);

        Intent data;
        data = getIntent();
        tripName = data.getExtras().getString("name");
        db.open();
        tripId = db.getTripId(tripName);
        db.close();
        tvTripName = (TextView)findViewById(R.id.tripname);
        tvTripName.setText(tripName);
        
        tripTotalText = (TextView)findViewById(R.id.triptotal);
	
		refresh();
    }
        
    private void refresh(){

		db.open();
		int count = db.countTripItems(tripId);
		if(count > 0){
			calculate.setEnabled(true);
			//String[] item_list = new String[count];
			itemIdList = new int[count];
			Cursor itemCur = db.getAllTripItems(tripId);
			ArrayList<HashMap<String, String>> item_list = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map = new HashMap<String, String>();
		
			int counter = 0;
			itemCur.moveToFirst();
			do{
				double total = itemCur.getDouble(itemCur.getColumnIndex(DBAdapter.KEY_TOTAL));
				String totalStr = Double.toString(Math.round(total*100)/100);
				
				map.put("name",itemCur.getString(itemCur.getColumnIndex(DBAdapter.KEY_NAME)));
				map.put("total",totalStr + " €");
				item_list.add(map);
				map = new HashMap<String,String>();
				itemIdList[counter] = itemCur.getInt(itemCur.getColumnIndex(DBAdapter.KEY_ROWID));
				counter++;
			}while(itemCur.moveToNext());
			itemCur.close();
			
			SimpleAdapter saItems = new SimpleAdapter(this, item_list, R.layout.item_list,
		            new String[] {"name", "total"}, new int[] {R.id.label, R.id.total});
			listview.setAdapter(saItems);
			
			double totalTrip = db.getTripTotal(tripId);
			totalTrip=Math.round(totalTrip*100);
			totalTrip=totalTrip/100;
			String totalTripStr = Double.toString(totalTrip);
			
			tripTotalText.setText(totalTripStr + " €");
			db.close();
		}
		else{
			db.close();
			setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
					R.id.label, new String[0]));
			calculate.setEnabled(false);
		}
	}
    
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	
        	Intent intent = new Intent(ConfigTrip.this,ConfigItem.class);
        	intent.putExtra("name", tripName);
        	intent.putExtra("itemId", itemIdList[arg2]);
        	startActivity(intent);
        }
    };


	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()){
			case R.id.button_add_new_item:
				intent = new Intent(this,ConfigItem.class);
				intent.putExtra("name", tripName);
				intent.putExtra("itemId", -1);
				startActivity(intent);
				break;
			case R.id.button_calculate:
				intent = new Intent(this,CalculsStage1.class);
				intent.putExtra("tripId",tripId);
				startActivity(intent);
				break;
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		refresh();
		db.open();
		int count = db.countTripItems(tripId);
		tripTotal = 0;
		for(int i = 0; i < count ; i++){
			double itemTotal = db.updateItemTotal(itemIdList[i]);
			tripTotal += itemTotal;
		}
		db.insertTripTotal(tripId,tripTotal);
		tripTotalText.setText(Double.toString(tripTotal)+ " €");
		db.close();
		refresh();
	}
}
