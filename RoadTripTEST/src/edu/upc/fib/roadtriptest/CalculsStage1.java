package edu.upc.fib.roadtriptest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CalculsStage1 extends ListActivity implements OnClickListener {
		/** Called when the activity is first created. */

		DBAdapter db;
		int tripId;
		int numContacts;
		static int[] contactIdList;
		int[] itemList;
		public static double[][] matchContacts; //matchContacts[y][x]
		ListView listview;
		boolean emailSent;
		Button buttonSendMail;
		
		

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.calculs_stage1);
			db = new DBAdapter(this);
	        emailSent = false;

			
			listview = (ListView)findViewById(android.R.id.list);
			listview.setOnItemClickListener(mDeviceClickListener);
			
			buttonSendMail = (Button)findViewById(R.id.button_send_email);
			buttonSendMail.setOnClickListener(this);
			
			try{
				Intent data = new Intent();
				data = getIntent();
				tripId= data.getIntExtra("tripId", -1);
			} catch (Exception ex){
				Toast.makeText(this, "No hi ha tripId",Toast.LENGTH_LONG).show();
				finish();
			}
						
			db.open();
			
			numContacts = db.countTripContacts(tripId);
			contactIdList = new int[numContacts];
			Cursor contCur = db.getAllTripContacts(tripId);
	
			int counter = 0;
			contCur.moveToFirst();
			do{
				contactIdList[counter] = contCur.getInt(contCur.getColumnIndex(DBAdapter.KEY_ROWID));
				counter++;
			}while(contCur.moveToNext());
			contCur.close();
			
			// Agafem el número de items del trip
			int numItems = db.countTripItems(tripId);
			itemList = new int[numItems];
			
			// Omplim una array amb la llista de IDs dels items
			Cursor itemCur = db.getAllTripItems(tripId);
			itemCur.moveToFirst();
			int i = 0;
			do{
				int itemId = itemCur.getInt(itemCur.getColumnIndex(DBAdapter.KEY_ROWID)); 
				itemList[i++] = itemId;
			}while(itemCur.moveToNext());
			itemCur.close();
			
			matchContacts = new double[numContacts][numContacts];
			for (int ii = 0; ii < matchContacts.length; ii++)
			{
			      Arrays.fill(matchContacts[ii], 0);
			}
			
			// Agafem quant ha pagat cadascú per cada item
			double[] workingColumn = new double[numContacts];
			for(int itemi = 0 ; itemi<numItems; itemi++){
				double totalItem=0;
				int participants=0;
				
				for(int conti = 0; conti<numContacts ; conti++){
					if(db.contactInItem(contactIdList[conti], itemList[itemi])){
						workingColumn[conti] = db.getItemContactCost(contactIdList[conti], itemList[itemi]);
						totalItem+=workingColumn[conti];
						participants++;
					}else{
						workingColumn[conti] = -1;
					}
				}
				
				double pricePerContact = totalItem/participants;
				double diff;
				double alreadyPaid;
				double pay;
				double reallyPay;
				
				//Aqui tenim una columna amb lo que ha pagat cadascú per un item en particular
				for(int y=0; y<numContacts; y++){
					if(workingColumn[y]<pricePerContact && workingColumn[y]!=-1){
						alreadyPaid=0;
						for(int x=0; x<numContacts; x++){
							if(workingColumn[x]>pricePerContact){
								pay=workingColumn[x]-pricePerContact;
								if(pay>pricePerContact){
									diff=pricePerContact-alreadyPaid;
									reallyPay=diff;
								}else{
									reallyPay=pay;
								}
								alreadyPaid+=reallyPay;
								matchContacts[y][x]+=reallyPay;
								workingColumn[x]-=reallyPay;
								if(alreadyPaid==pricePerContact){
									break;
								}
							}
						}						
					}
				}				
			}
			//Ajustament dels diners inter-contactes
			for(int y=0; y<numContacts; y++){
				for (int x=y; x<numContacts; x++){
					if(matchContacts[y][x]>matchContacts[x][y]){
						matchContacts[y][x]-=matchContacts[x][y];
						matchContacts[x][y]=0;
					}else if(matchContacts[y][x]<matchContacts[x][y]){
						matchContacts[x][y]-=matchContacts[y][x];
						matchContacts[y][x]=0;
					}
				}
			}
			db.close();
			
			refresh();
			
		}
		
		private void refresh(){

			db.open();
			numContacts = db.countTripContacts(tripId);
			if(numContacts > 0){
				//String[] item_list = new String[count];
				contactIdList = new int[numContacts];
				Cursor contCur = db.getAllTripContacts(tripId);
				ArrayList<HashMap<String, String>> contact_list = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> map = new HashMap<String, String>();
			
				int counter = 0;
				contCur.moveToFirst();
				do{
					contactIdList[counter] = contCur.getInt(contCur.getColumnIndex(DBAdapter.KEY_ROWID));
					double totalContact = 0;
					for(int y=0; y<numContacts; y++){
						totalContact-=matchContacts[y][counter];
					}
					for(int y=0; y<numContacts; y++){
						totalContact+=matchContacts[counter][y];
					}
					
					if(totalContact<0){
						totalContact=0; //No volem veure els que no deuen diners
					}
					counter++;
					
					totalContact=Math.round(totalContact*100);
					totalContact=totalContact/100;
					String totalContStr = Double.toString(totalContact);
					
					map.put("name",contCur.getString(contCur.getColumnIndex(DBAdapter.KEY_NAME)));
					map.put("total",totalContStr + " €");
					contact_list.add(map);
					map = new HashMap<String,String>();
				}while(contCur.moveToNext());
				contCur.close();
				
				SimpleAdapter saItems = new SimpleAdapter(this, contact_list, R.layout.item_list,
			            new String[] {"name", "total"}, new int[] {R.id.label, R.id.total});
				listview.setAdapter(saItems);
				
				db.close();
			}
			else{
				db.close();
				setListAdapter(new ArrayAdapter<String>(this, R.layout.add_people_list,
						R.id.label, new String[0]));
			}
		}

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.button_send_email:
				buttonSendMail.setEnabled(false);
				buttonSendMail.setText("Email Sent");
				if(!emailSent){
					emailSent = true;
					try {  
						Toast.makeText(this, "Sending email..." ,Toast.LENGTH_LONG).show();
						ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						if(cm.getActiveNetworkInfo().isConnectedOrConnecting()){
			                GMailSender sender = new GMailSender("roadtripmailing@gmail.com", "tpaagptpaagp");
			                db.open();
			                for(int i=0; i<numContacts; i++){
			                	String receiver=db.getContactEmail(contactIdList[i]);
			                	String tripName=db.getTripName(tripId);
			                	String subject="[RoadTrip] Trip: "+tripName;
			                	String cos="";
			                	cos+="Trip: "+tripName+"\n\tYou owe:\n";
			                	String cos1="";
			                	String cos2="";
			                	for(int j=0;j<numContacts; j++){
			                		if(i!=j){
			                			double compte = matchContacts[j][i]-matchContacts[i][j];
			        					compte=Math.round(compte*100);
			        					compte=compte/100;
			        					String totalStr = Double.toString(Math.abs(compte));
			        					if(compte<0){
			        						cos1+="\t\t"+db.getContactName(contactIdList[j])+": "+totalStr+" €\n";
			        					}else{
			        						cos2+="\t\t"+db.getContactName(contactIdList[j])+": "+totalStr+" €\n";
			        					}
			        						
			                		}
			                	}
			                	cos+=cos1;
			                	cos+="\n\tOthers owe you:\n";
			                	cos+=cos2;
			                	cos+="\n--\nRoadTrip Team";			            
				                sender.sendMail(subject,   
				                        cos,
				                        "roadtripmailing@gmail.com",   
				                        receiver);
			                }
			                db.close();
						}
		            } catch (Exception e) {
		            	Toast.makeText(this, "Email cannot be sent" ,Toast.LENGTH_LONG).show();
		                Log.e("SendMail", e.getMessage(), e);   
		            }
				}
				break;
			}
			
		}
		private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
	        	Intent intent = new Intent(CalculsStage1.this,CalculsStage2.class);
	        	intent.putExtra("selected",arg2);
	        	intent.putExtra("contactIdList",CalculsStage1.contactIdList);
	        	intent.putExtra("numContacts", numContacts);
	        	for(int i=0; i<numContacts; i++){
	        		intent.putExtra("match"+Integer.toString(i),CalculsStage1.matchContacts[i]);
				}
	        	startActivity(intent);
	        }
	    };
}
