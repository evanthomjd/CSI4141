package csi4141.miniproject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


import javax.xml.parsers.ParserConfigurationException;


import org.xml.sax.SAXException;

import communication.Direction;

import locationfactory.LocationFactory;
import services.AlarmService;
import services.PathFetchService;
import services.PathService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {


	public final static String TEST_TUPLE = "test.tupple";
	private LocationFactory locationFactory;
	private int angle;
	private double currentLat, currentLong, destLat, destLong, range;
	private String queryString;
	private ArrayList<Direction> directionList = new ArrayList<Direction>();

	private Geocoder geocoder;
	private EditText emergencyContact, distance, maxTime;
	private Intent alarmIntent;
	private BroadcastReceiver reciever;
	
	private PendingIntent sender;
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		locationFactory = LocationFactory.getInstance();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("UPDATE_DIRECTION");
		
		reciever = new BroadcastReceiver(){
			@Override 
			public void onReceive(Context context, Intent intent){
				//update UI FROM HEAR
				TextView directions = (TextView) findViewById(R.id.directions);
				String direction = intent.getExtras().getString("update");
				if(!direction.equals("NA")){
					directions.setText(direction);
				}
				
			}
		};
		
		registerReceiver(reciever, filter);

	}

	public void startRun(View view) throws ParserConfigurationException, SAXException, InterruptedException, ExecutionException{
		//Create intent to start PathService
		

		//Retrieve the input from the user 
		emergencyContact = (EditText) findViewById(R.id.emergency_email);
		distance = (EditText) findViewById(R.id.distance);
		maxTime = (EditText) findViewById(R.id.max_time);


		if(locationFactory!=null){
			locationFactory.init(MainActivity.this);
		}		
		//Create the Intent for the alarmService
		alarmIntent = new Intent(MainActivity.this, AlarmService.class);

		if(locationFactory.canGetLocation()){
			//Get first location 
			Location location = locationFactory.getPos();

			if(location!=null){
				range = Double.parseDouble(distance.getText().toString());
				
				/**
				 * In the equation used to generate a random point at a given distance from the start, a range of 0.01 comes out to about
				 * 1 kms in distance. So we divided the input by 200 to get a point that is about half the given distance from the users
				 * present location.
				 */
				range = range / 200.0;

				currentLat = location.getLatitude();
				currentLong = location.getLongitude();
				angle = 1 + (int)(Math.random()*361);
				destLat = (Math.cos(angle)*range)+currentLat;
				destLong = (Math.sin(angle)*range)+currentLong;

				//Store the home location for use by the emergency alarm
				alarmIntent.putExtra("homeLat", currentLat);
				alarmIntent.putExtra("homeLong", currentLong);
				
				try{
					List<Address> addresses;
					geocoder = new Geocoder(this, Locale.getDefault());
					addresses = geocoder.getFromLocation(currentLat, currentLong, 1);
					String address = addresses.get(0).getAddressLine(0);
					String city = addresses.get(0).getAddressLine(1);
					String country = addresses.get(0).getAddressLine(2);
						
					String currentAddress = address+"  "+" "+city+" "+country;

					queryString = "http://maps.googleapis.com/maps/api/directions/xml?origin="+addresses.get(0).getLatitude()+","+addresses.get(0).getLongitude()+"&destination="+destLat+","+destLong+"&sensor=true&mode=walking";
					showSettingsAlert(currentAddress);

				}catch (IOException e) {	
					cannotGeoCode();
				}
				
			}else{
				Log.e("CHECKUPDATE", "NULL LOACTION");
			}
		}else{
			locationFactory.showSettingsAlert();
		}

	}
	
    public void showSettingsAlert(String location){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
 
        alertDialog.setTitle("GPS Reading");
 
        // Setting Dialog Message
        alertDialog.setMessage("We see your address is " + location + " is this correct?");
 
 
        // on pressing YES button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            try {
				start();
				dialog.cancel();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
            }
        });
        
     // on pressing NO button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void cannotGeoCode(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
 
        alertDialog.setTitle("GeoCoding Error");
 
        // Setting Dialog Message
        alertDialog.setMessage("An error occured while trying to establish your current address. Please ensure you have a DATA connection. If this problem persists try restarting your phone");
       
     // on pressing OKAY button
        alertDialog.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    private void start() throws InterruptedException, ExecutionException{
    	Intent intent = new Intent(this, PathService.class);
    	
    	directionList = new PathFetchService().execute(queryString).get();

		Toast.makeText(this, "Instruction Size: "+directionList.size(), Toast.LENGTH_SHORT).show();
		//Set up the rest of the information for the emergency alarm		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, Integer.parseInt(maxTime.getText().toString()));	
		alarmIntent.putExtra("contact", emergencyContact.getText().toString());

		sender = PendingIntent.getBroadcast(this, 192837, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);


		emergencyContact.setVisibility(View.GONE);
		distance.setVisibility(View.GONE);
		maxTime.setVisibility(View.GONE);
		findViewById(R.id.button1).setVisibility(View.GONE);
		findViewById(R.id.button2).setVisibility(View.VISIBLE);
		TextView directions = (TextView) findViewById(R.id.directions);
		directions.setVisibility(View.VISIBLE);
		
		Toast.makeText(this, "head towards " +directionList.get(0).getDirection(), Toast.LENGTH_LONG).show();
		intent.putParcelableArrayListExtra("directionsList", directionList);

		startService(intent);
    	
    }
    
    public void stopRun(View view){
    	Intent intent = new Intent(this, PathService.class);
    	stopService(intent);
    	locationFactory.stop();
    	AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    	am.cancel(sender);
    	emergencyContact.setVisibility(View.VISIBLE);
		distance.setVisibility(View.VISIBLE);
		maxTime.setVisibility(View.VISIBLE);
		findViewById(R.id.button1).setVisibility(View.VISIBLE);
		findViewById(R.id.button2).setVisibility(View.GONE);
		findViewById(R.id.directions).setVisibility(View.GONE);
    }
    
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	unregisterReceiver(reciever);
    }
	
}
