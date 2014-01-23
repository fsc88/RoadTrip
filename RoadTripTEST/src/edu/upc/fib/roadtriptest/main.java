package edu.upc.fib.roadtriptest;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class main extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	EditText edittext;
	TextView textview;
	DBAdapter db;
	Button tripList, addNewTrip, contactList;
	int countTrip;
	boolean emailSent;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        db = new DBAdapter(this);
        
        db.open();
        countTrip = db.countTrips();
        db.close();
        
        tripList = (Button)findViewById(R.id.button_trip_list);
        tripList.setOnClickListener(this);
        addNewTrip = (Button)findViewById(R.id.button_add_new_trip);
        addNewTrip.setOnClickListener(this);
        contactList = (Button)findViewById(R.id.button_contact_list);
        contactList.setOnClickListener(this);
        
        emailSent = false;
        
        
        
        if(countTrip==0){
        	tripList.setEnabled(false);
        }
        
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	db.open();
        countTrip = db.countTrips();
        db.close();
    	if(countTrip > 0){
    		tripList.setEnabled(true);
        }
    	else{
    		tripList.setEnabled(false);
    	}
    }

	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()){
			case R.id.button_add_new_trip:
				intent = new Intent(this,AddNewTravel.class);
				startActivity(intent);
				break;
			case R.id.button_trip_list:
				intent = new Intent(this,TripList.class);
				startActivity(intent);
				break;
			case R.id.button_contact_list:
				intent = new Intent(this,ContactList.class);
    			intent.putExtra("mode", ContactList.MODE_EDIT_CONTACT);
    			startActivity(intent);
				break;
				/*
			case default:
				test.setEnabled(false);
				if(!emailSent){
					emailSent = true;
					try {  
						Toast.makeText(this, "Sending email..." ,Toast.LENGTH_LONG).show();
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						if(cm.getActiveNetworkInfo().isConnectedOrConnecting()){
			                GMailSender sender = new GMailSender("roadtripmailing@gmail.com", "tpaagptpaagp");
			                sender.sendMail("Correu de prova",   
			                        "Putooo\n\n><)))'>",   
			                        "roadtripmailing@gmail.com",   
			                        "fsc666@gmail.com");   
			                Toast.makeText(this, "Email Sent" ,Toast.LENGTH_LONG).show();
						}
		            } catch (Exception e) {
		            	Toast.makeText(this, "Email cannot be sent" ,Toast.LENGTH_LONG).show();
		                Log.e("SendMail", e.getMessage(), e);   
		            }
				}
				break;*/
		}
		
	}

	
}