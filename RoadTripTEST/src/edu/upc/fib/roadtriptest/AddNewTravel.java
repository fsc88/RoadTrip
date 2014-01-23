package edu.upc.fib.roadtriptest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class AddNewTravel extends ListActivity implements OnClickListener {
	private static final int PICK_CONTACT = 0;
	/** Called when the activity is first created. */

	DBAdapter db;
	Button new_contact_button;
	String contactList;
	String tripName;
	EditText titleEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addnewtravel);
		contactList = "";
		tripName = "";
		db = new DBAdapter(this);

		refresh();
		
		titleEditText = (EditText)findViewById(R.id.newtravelname);
		
		Button okTrip = (Button)findViewById(R.id.ok_trip);
		okTrip.setOnClickListener(this);
		
		
		ListView listview = (ListView)findViewById(android.R.id.list);
		listview.setOnItemClickListener(mDeviceClickListener);
		
		new_contact_button = (Button) findViewById(R.id.new_contact_button);
		new_contact_button.setOnClickListener(this);

	}
	
	private void refresh(){
		if(contactList.length() > 0){
			db.open();
			int count = contactList.split(",").length;
			String[] strContactList = new String[count];
			Cursor contCur = db.getContactList(contactList.substring(0,contactList.length()-1));
		
			int counter = 0;
			contCur.moveToFirst();
			do{
				strContactList[counter++] = contCur.getString(contCur.getColumnIndex(DBAdapter.KEY_NAME));
			}while(contCur.moveToNext());
			contCur.close();
			db.close();
			
			setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
					R.id.label, strContactList));
		}
		else{
			setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
					R.id.label, new String[0]));
		}
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	
        	LinearLayout linearlayout = (LinearLayout) v;
        	String name = ((TextView) linearlayout.findViewById(R.id.label)).getText().toString();
        	db.open();
			int id = db.getContactId(name);
			db.close();
			
			contactList = contactList.replace(id+",", "");
			
			refresh();
        }
    };

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.new_contact_button: 
			Intent intent = new Intent(this,ContactList.class);
			intent.putExtra("mode", ContactList.MODE_ADD_CONTACT);
			startActivityForResult(intent, PICK_CONTACT);
			break;
		case R.id.ok_trip:
			String tripName = titleEditText.getText().toString();
			if(contactList != "" && tripName.length() > 0){
				db.open();
				int tripId = (int)db.insertTrip(tripName);
				Log.d("newtravel",Integer.toString(tripId));
				String[] exploded = contactList.split(",");
				for (String eachexploded : exploded){
					db.insertRelConTrip(Integer.parseInt(eachexploded), tripId);
				}
				db.close();
				intent = new Intent(AddNewTravel.this,ConfigTrip.class);
				intent.putExtra("name", tripName);
				startActivity(intent);
				finish();
			}
			break;
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		//ContentResolver content_resolver = getContentResolver();
		switch (reqCode) {
			case (PICK_CONTACT):
				//Intent intent = getIntent();
				if(resultCode == Activity.RESULT_OK){
					int newId= data.getExtras().getInt("id");
					boolean repetit = false;
					String[] exploded = contactList.split(",");
					if(contactList != ""){
						for(int i = 0; i<exploded.length; i++){
							int provaId = Integer.parseInt(exploded[i]);
							if( newId == provaId ){
								repetit = true;
								break;
							}
						}
					}
					if(newId > 0 && !repetit ){
						// Afegir userid a trip
						contactList += newId+",";
						// Actualitzar llista
						refresh();
					}
				} //catch(Exception ex) {}
				break;
		}
	}

}
