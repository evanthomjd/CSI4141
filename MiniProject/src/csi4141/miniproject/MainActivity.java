package csi4141.miniproject;


import services.PathService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	 
	 public final static String TEST_TUPLE = "test.tupple";
	 
	 
	  public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_main);
	    }
	    
	    public void startRun(View view){
	        Intent intent = new Intent(this, PathService.class);
	       
	        
	        EditText emergencyContact = (EditText) findViewById(R.id.emergency_email);
	        EditText distance = (EditText) findViewById(R.id.distance);
	        EditText maxTime = (EditText) findViewById(R.id.max_time);
	       
	        
	        intent.putExtra(TEST_TUPLE, emergencyContact.getText().toString() + " , "+distance.getText().toString()+","+maxTime.getText().toString());
	          
	        startService(intent);
	    }
    
}
