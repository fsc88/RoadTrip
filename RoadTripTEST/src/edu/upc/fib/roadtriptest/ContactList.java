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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

public class ContactList extends ListActivity implements OnClickListener {
	private static final int PICK_CONTACT = 0;
	private static final int EDIT_CONTACT = 1;
	protected static final int MODE_ADD_CONTACT = 0;
	protected static final int MODE_EDIT_CONTACT = 1;
	/** Called when the activity is first created. */

	DBAdapter db;
	ListView peopleList;
	Button new_contact_button;
	int mode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.contactlist);
		db = new DBAdapter(this);
		
		Intent data = new Intent();
		data = getIntent();
		mode = data.getExtras().getInt("mode");

		new_contact_button = (Button) findViewById(R.id.new_contact_button);
		new_contact_button.setOnClickListener(this);
		
		ListView listview = (ListView)findViewById(android.R.id.list);
		listview.setOnItemClickListener(mDeviceClickListener);
		
		refresh();

	}
	
	private void refresh(){

		db.open();
		int count = db.countContacts();
		if(count > 0){
			String[] contact_list = new String[count];
			Cursor contCur = db.getAllContacts();
		
			int counter = 0;
			contCur.moveToFirst();
			do{
				contact_list[counter++] = contCur.getString(contCur.getColumnIndex(DBAdapter.KEY_NAME));
			}while(contCur.moveToNext());
			contCur.close();
			db.close();
			
			setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
					R.id.label, contact_list));
		}
		else{
			db.close();
			setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
					R.id.label, new String[0]));
		}
	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);


	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        	Intent intent;
        	LinearLayout linearlayout = (LinearLayout) v;
        	String name = ((TextView) linearlayout.findViewById(R.id.label)).getText().toString();
            switch(mode){
	            case MODE_ADD_CONTACT:
		        	db.open();
					int id = db.getContactId(name);
					db.close();
		            
		            // Create the result Intent and include the MAC address
		            intent = new Intent();
		            intent.putExtra("id", id);
		
		            // Set result and finish this Activity
		            setResult(Activity.RESULT_OK, intent);
		            finish();
		            break;
	            case MODE_EDIT_CONTACT:
	            	intent = new Intent(ContactList.this,PopupEditContact.class);
	            	intent.putExtra("name", name);
	            	startActivityForResult(intent, EDIT_CONTACT);
	            	break;
            }
        }
    };
    
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		ContentResolver content_resolver = getContentResolver();
		switch (reqCode) {
		case (EDIT_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Intent data2 = new Intent();
				data2 = getIntent();
				String oldName = data.getExtras().getString("oldname");
				String contName = data.getExtras().getString("name");
				String contEmail = data.getExtras().getString("email");
				String contPhone = data.getExtras().getString("phone");
				db.open();
				db.updateContact(oldName, contName, contEmail, contPhone);
				db.close();
				refresh();
				// Toast.makeText(this, oldName + " " + contName + " " + contEmail + " " + contPhone,Toast.LENGTH_LONG).show();
			}
			break;
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				String name = "";
				String mobile = "";
				String email = "";
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					// Aqui ens ha retornat el contacte en una taula. 
					name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

					// MOBIL
					Cursor curPhone = content_resolver.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", 
							new String[] { id }, 
							null);
					while (curPhone.moveToNext()) {
						String phonenumber = curPhone.getString(curPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						int esmobil = curPhone.getInt(curPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
						if (esmobil == 2) {
							mobile = phonenumber;
						}
					}

					// EMAIL
					Cursor curEmail = content_resolver.query(
							ContactsContract.CommonDataKinds.Email.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
							new String[] { id }, 
							null);
					while (curEmail.moveToNext()) {
						email = curEmail.getString(curEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
						break;
						//String emailtype = curEmail.getString(curEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
						//Toast.makeText(this, "eMail: " + email + "; " + emailtype,Toast.LENGTH_LONG).show();
					}
				}
				// Ja tenim el contacte
				if(name != ""){
					db.open();
					if(db.getContactId(name) == -1){
						db.insertContact(name, mobile, email);
					}
					db.close();
					refresh();
				}
			}
			break;
		}
	}

}
