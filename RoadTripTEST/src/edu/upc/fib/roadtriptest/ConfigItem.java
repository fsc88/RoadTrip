package edu.upc.fib.roadtriptest;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ConfigItem extends ListActivity implements OnClickListener {
	protected static final int GET_COST = 0;
	/** Called when the activity is first created. */

	DBAdapter db;
	Button new_contact_button;
	Button buttonSaveReturn;
	String contactList;
	String tripName;
	int tripId;
	int itemId;
	int selectedRow;
	EditText titleEditText;
	EditText descEditText;
	ListView listview;
	Button okItem;
	RelativeLayout layoutList;
	
	private ChkListAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.configitem);
		db = new DBAdapter(this);
		
		contactList = "";
		Intent data;
		data = getIntent();
		itemId = data.getExtras().getInt("itemId");
		tripName = data.getExtras().getString("name");
		db.open();
		tripId = db.getTripId(tripName);
		db.close();
		
		titleEditText = (EditText)findViewById(R.id.itemname);
		descEditText = (EditText)findViewById(R.id.description);
		
		okItem = (Button)findViewById(R.id.ok_item);
		okItem.setOnClickListener(this);
		
		buttonSaveReturn = (Button)findViewById(R.id.button_save_return);
		buttonSaveReturn.setOnClickListener(this);
		
		listview = (ListView)findViewById(android.R.id.list);
		listview.setOnItemClickListener(mDeviceClickListener);
		
		layoutList = (RelativeLayout)findViewById(R.id.layout_list);
				
		if(itemId > 0){
			db.open();
			titleEditText.setText(db.getItemName(itemId));
			descEditText.setText(db.getItemDesc(itemId));
			db.close();
			layoutList.setVisibility(ListView.VISIBLE);
			okItem.setVisibility(Button.INVISIBLE);
			
		}
		
		refresh();
	}
	
	private void refresh(){
		db.open();
		int count = db.countTripContacts(tripId);
		if(count > 0){ // Només popular quan hi hagi items
			
			adapter = new ChkListAdapter(count);
			
			Cursor contCur = db.getAllTripContacts(tripId);
			contCur.moveToFirst();
			do{
				int contId = contCur.getInt(contCur.getColumnIndex(DBAdapter.KEY_ROWID)); 
				if(db.contactInItem(contId, itemId)){
					double cost = db.getItemContactCost(contId, itemId);
					adapter.addItem(new ContactItemRow(
						contId,
						contCur.getString(contCur.getColumnIndex(DBAdapter.KEY_NAME)),
						cost,
						true));
				}else{
					adapter.addItem(new ContactItemRow(
						contId,
						contCur.getString(contCur.getColumnIndex(DBAdapter.KEY_NAME)), 
						0,
						itemId<0));
				}
			}while(contCur.moveToNext());
			contCur.close();
			db.close();
			listview.setAdapter(adapter);
		}
		else{
			setListAdapter(new ArrayAdapter<String>(this, R.layout.check_contact_list,
					R.id.label, new String[0]));
		}
	}
	
	private void commitDb(){
		String itemName = titleEditText.getText().toString();
		String description = descEditText.getText().toString();
		if( itemName.length() > 0 ){
			ArrayList<ContactItemRow> dades = adapter.getArrayList();
			int count = dades.size();
			db.open();
			db.deleteItemContacts(itemId);
			db.updateItemNoCost(itemId, itemName, description);
			for(int i = 0; i<count; i++){
				if( dades.get(i).getCheck() ){
					db.insertRelCostItems(
							dades.get(i).getId(), 
							itemId, 
							dades.get(i).getCost());
				}
			}
			db.close();
		}
	}
	
	private void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
		imm.hideSoftInputFromWindow(descEditText.getWindowToken(), 0);
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	selectedRow = arg2;
        	Intent intent = new Intent(ConfigItem.this,PopupCost.class);
        	double cost = adapter.getCost(selectedRow);
        	intent.putExtra("cost", cost);
        	startActivityForResult(intent, GET_COST);
        }
    };
    
    @Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		
		if( resultCode == Activity.RESULT_OK ){
			switch (reqCode) {
				case (GET_COST):
					double cost = data.getExtras().getDouble("cost",0);
					adapter.setCost(selectedRow, cost);
					if(cost > 0){
						adapter.setCheck(selectedRow, true);
					}
					commitDb();
					
					refresh();
					break;
			}
		}
		hideKeyboard();
	}

	@Override
	public void onClick(View v) {
		String itemName = "";
		String description = "";
		switch(v.getId()){
		case R.id.ok_item:
			itemName = titleEditText.getText().toString();
			description = descEditText.getText().toString();
			if( itemName.length() > 0 ){
				db.open();
				itemId = (int)db.insertItem(itemName, description);
				db.insertRelTripItems(tripId, itemId);
				db.close();
				layoutList.setVisibility(ListView.VISIBLE);
				okItem.setVisibility(Button.INVISIBLE);
				hideKeyboard();
			}
			commitDb();
			break;
		case R.id.button_save_return:
			commitDb();
			finish();
			break;
		}
	}
	
	private class ChkListAdapter extends BaseAdapter {
		private ArrayList<ContactItemRow> ciRows = new ArrayList<ContactItemRow>();
		private LayoutInflater inflater;

		public ChkListAdapter(int size) {
			inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(final ContactItemRow contactitemrow) {
			ciRows.add(contactitemrow);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return ciRows.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}
		
		public String getName(int position) {
			return ciRows.get(position).getName();
		}
		
		public double getCost(int position){
			return ciRows.get(position).getCost();
		}
		
		public void setCost(int position, double cost){
			ciRows.get(position).setCost(cost);
		}
		
		public void setCheck(int position, boolean checked){
			ciRows.get(position).setCheck(checked);
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.check_contact_list, null);
			final ViewHolder holder = new ViewHolder();
			holder.chkItem = (CheckBox)convertView.findViewById(R.id.checkbox);
			holder.chkItem.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							ciRows.get(position).setCheck(holder.chkItem.isChecked());
							if(holder.chkItem.isChecked()){
								ciRows.get(position).setCost(0);
							}
							//Toast.makeText(ConfigItem.this, Boolean.toString(holder.chkItem.isChecked()), Toast.LENGTH_SHORT).show();
						}
					});
			holder.labelItem = (TextView)convertView.findViewById(R.id.label);
			holder.costItem = (TextView)convertView.findViewById(R.id.cost);

			holder.chkItem.setChecked(ciRows.get(position).getCheck());
			holder.labelItem.setText(getName(position));
			holder.costItem.setText(Double.toString(getCost(position))+" €");
			convertView.setTag(holder);
			return convertView;
		}

		public ArrayList<ContactItemRow> getArrayList(){
			return ciRows;
		}

	}
	
	public static class ViewHolder {
		public CheckBox chkItem;
		public TextView labelItem;
		public TextView costItem;
		}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		//commitDb();
	}
	
}
