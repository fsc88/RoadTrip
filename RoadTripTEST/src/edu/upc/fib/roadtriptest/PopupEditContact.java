package edu.upc.fib.roadtriptest;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PopupEditContact extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	DBAdapter db;
	Button okButton;
	EditText nameText, emailText, phoneText;
	String oldName, contName, contEmail, contPhone; 
	int contId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_edit_contact);
        db = new DBAdapter(this);
        
        Intent data = new Intent();
        data = getIntent();
        contName = data.getExtras().getString("name");
        oldName = contName;
        db.open();
        contId = db.getContactId(contName);
        contEmail = db.getContactEmail(contId);
        contPhone = db.getContactPhone(contId);
        db.close();
        
        okButton = (Button)findViewById(R.id.button_popup_contact_ok);
        okButton.setOnClickListener(this);
        
        nameText = (EditText)findViewById(R.id.text_popup_name);
        emailText = (EditText)findViewById(R.id.text_popup_email);
        phoneText = (EditText)findViewById(R.id.text_popup_phone);
        
        nameText.setText(contName);
        emailText.setText(contEmail);
        phoneText.setText(contPhone);
    }
    

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.button_popup_contact_ok:
				Intent intent = new Intent();
				intent = getIntent();
				if ( !nameText.getText().toString().equals(contName) || 
				!emailText.getText().toString().equals(contEmail) ||
				!phoneText.getText().toString().equals(contPhone) ){
					intent.putExtra("oldname", oldName);
					contName = nameText.getText().toString();
					intent.putExtra("name", contName);
					contEmail = emailText.getText().toString();
					intent.putExtra("email", contEmail);
					contPhone = phoneText.getText().toString();
					intent.putExtra("phone", contPhone);
					setResult(Activity.RESULT_OK, intent);
					finish();
				}
				else{
					setResult(Activity.RESULT_CANCELED,intent);
					finish();
				}
				break;
		}
		
	}
	
	public void onDestroy(){
		super.onDestroy();
		if(nameText.getText().toString().length() == 0){
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED,intent);
		}
	}

	
}