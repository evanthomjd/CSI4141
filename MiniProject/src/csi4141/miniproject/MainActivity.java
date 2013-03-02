package csi4141.miniproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	 public final static String DISTANCE = "run.DISTANCE";
	 public final static String EMERGENCY_CONTACT = "emergency.CONTACT";
	 public final static String MAX_TIME = "max.TIME";
	 
	  public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	    }
	    
	    public void startRun(View view){
	        Intent intent = new Intent(this, TestDisplayActivity.class);
	        
	        EditText emergencyContact = (EditText) findViewById(R.id.emergency_email);
	        EditText distance = (EditText) findViewById(R.id.distance);
	        EditText maxTime = (EditText) findViewById(R.id.max_time);
	       
	        intent.putExtra(EMERGENCY_CONTACT, emergencyContact.getText().toString());
	        intent.putExtra(DISTANCE, distance.getText().toString());
	        intent.putExtra(EMERGENCY_CONTACT, maxTime.getText().toString());
	             
	        startActivity(intent);
	    }
    
}
