package csi4141.miniproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;;

public class TestDisplayActivity extends Activity {
    
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String distance, maxTime, emergencyContact;
        
        Intent intent = getIntent();
        distance = intent.getStringExtra(MainActivity.DISTANCE);
        maxTime = intent.getStringExtra(MainActivity.MAX_TIME);
        emergencyContact = intent.getStringExtra(MainActivity.EMERGENCY_CONTACT);
        
        //Creates new text view
        TextView distanceView, timeView, contactView;
        
        distanceView = new TextView(this);
        distanceView.setTextSize(40);
        distanceView.setText(distance);
        
        timeView = new TextView(this);
        timeView.setTextSize(40);
        timeView.setText(maxTime);
        
        contactView = new TextView(this);
        contactView.setTextSize(40);
        contactView.setText(emergencyContact);
        
        //set text view as activity layout
        setContentView(distanceView);
        setContentView(timeView);
        setContentView(contactView);
        
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
       
        return super.onOptionsItemSelected(item);
    }
    
}
