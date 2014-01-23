package edu.upc.fib.roadtriptest;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PopupCost extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	
	DBAdapter db;
	Button okButton;
	EditText costText;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_cost);
        db = new DBAdapter(this);
        
        okButton = (Button)findViewById(R.id.button_cost);
        okButton.setOnClickListener(this);
        
        costText = (EditText)findViewById(R.id.text_cost);
        try{
        	Intent data = new Intent();
        	data = getIntent();
        	double cost = data.getExtras().getDouble("cost");
        	if(cost > 0){
        		costText.setText(Double.toString(cost));
        	}
        }catch (Exception ex) {}
        
    }
    

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.button_cost: 
				String strCost = costText.getText().toString();
				double dCost;
				if(strCost.length() == 0 || strCost.equals(".")){
					dCost = 0;
				}else{
					dCost = Double.parseDouble(strCost);
					dCost = (Math.round(dCost*100));
					dCost = dCost/100;
				}
				Intent intent = new Intent();
				intent.putExtra("cost",dCost);
				setResult(Activity.RESULT_OK,intent);
				finish();
				break;
		
		}
		
	}
	
	public void onDestroy(){
		super.onDestroy();
		if(costText.getText().toString().length() == 0){
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED,intent);
		}
	}

	
}