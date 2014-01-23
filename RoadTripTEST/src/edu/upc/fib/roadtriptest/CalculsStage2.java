package edu.upc.fib.roadtriptest;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class CalculsStage2  extends ListActivity implements OnGestureListener{

	GestureDetector gestureScanner;
	DBAdapter db;
	ListView listview;
	double[][] matchContacts;
	int[] contactIdList;
	int numContacts;
	int selected, selectedId;
	TextView textview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.calculs_stage2);
		db = new DBAdapter(this);
		textview=(TextView)findViewById(R.id.contact_name);
		
		listview = (ListView)findViewById(android.R.id.list);
		gestureScanner = new GestureDetector(this);
		
		Intent data = new Intent();
		data = getIntent();
		selected=data.getIntExtra("selected", -1);
		contactIdList=data.getIntArrayExtra("contactIdList");
		numContacts=data.getIntExtra("numContacts", 0);
		matchContacts = new double[numContacts][numContacts];
		if(numContacts>0){
			for(int i=0; i<numContacts; i++){
				matchContacts[i]=data.getDoubleArrayExtra("match"+Integer.toString(i));
			}
		}
		
		db.open();
		String contName=db.getContactName(contactIdList[selected]);
		db.close();
		textview.setText(contName);
		
		refresh();
	}
	
	private void refresh(){

		if(numContacts > 0){
			db.open();
			ArrayList<HashMap<String, String>> contact_list = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map = new HashMap<String, String>();
			for(int i=0; i<numContacts; i++){
				if(i!=selected){
					
					double compte = matchContacts[i][selected]-matchContacts[selected][i];
					compte=Math.round(compte*100);
					compte=compte/100;
					String totalStr = Double.toString(compte);
					
					map.put("name",db.getContactName(contactIdList[i]));
					map.put("total",totalStr);
					contact_list.add(map);
					map = new HashMap<String,String>();
				}
			}
			db.close();
			
			SimpleAdapter saItems = new SimpleAdapter(this, contact_list, R.layout.item_list,
		            new String[] {"name", "total"}, new int[] {R.id.label, R.id.total});
			listview.setAdapter(saItems);
		}
		else{
			setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
					R.id.label, new String[0]));
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return gestureScanner.onTouchEvent(e);
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		finish();
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
