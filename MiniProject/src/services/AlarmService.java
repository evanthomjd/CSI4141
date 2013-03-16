package services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
		     Bundle bundle = intent.getExtras();
		     String message = bundle.getString("alarm_message");
		     //Do stuff here
		     	//Check if we are within the 100m "safe zone"
		     
		     	//If so, do nothing, we're done
		     
		     	//If not, send out the message to the emergency contact
		     
		     
		     
		     
		     Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		    } catch (Exception e) {
		     Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
		     e.printStackTrace();
		 
		    }
		
	}

}