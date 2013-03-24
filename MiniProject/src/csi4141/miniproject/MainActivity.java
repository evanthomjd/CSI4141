package csi4141.miniproject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import communication.Direction;

import locationfactory.LocationFactory;
import services.AlarmService;
import services.LocationService;
import services.PathFetchService;
import services.PathService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {


	public final static String TEST_TUPLE = "test.tupple";
	private LocationFactory locationFactory;
	private int range, angle;
	private double currentLat, currentLong, destLat, destLong;
	private String queryString;
	private ArrayList<Direction> directionList = new ArrayList<Direction>();

	private Geocoder geocoder;
	private EditText emergencyContact, distance, maxTime;
	private Intent alarmIntent;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		locationFactory = LocationFactory.getInstance();

	}

	public void startRun(View view) throws ParserConfigurationException, SAXException, IOException, InterruptedException, ExecutionException{
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

				//House coordinate 45.381256,-75.637736
				currentLat = location.getLatitude();
				currentLong = location.getLongitude();
				angle = 1 + (int)(Math.random()*361);
				destLat = (Math.cos(angle)*range)+currentLat;
				destLong = (Math.sin(angle)*range)+currentLong;

				//Store the home location for use by the emergency alarm
				alarmIntent.putExtra("homeLat", currentLat);
				alarmIntent.putExtra("homeLong", currentLong);

				List<Address> addresses;
				geocoder = new Geocoder(this, Locale.getDefault());
				addresses = geocoder.getFromLocation(currentLat, currentLong, 1);
				String address = addresses.get(0).getAddressLine(0);
				String city = addresses.get(0).getAddressLine(1);
				String country = addresses.get(0).getAddressLine(2);
				
			

				
				String currentAddress = address+"  "+" "+city+" "+country;
				//using my own destinations, starting point will be:45.381256,-75.637736
				queryString = "http://maps.googleapis.com/maps/api/directions/xml?origin="+addresses.get(0).getLatitude()+","+addresses.get(0).getLongitude()+"&destination=45.380412,-75.642618&sensor=true&mode=walking";
				showSettingsAlert(currentAddress);

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
 
 
        // on pressing cancel button
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
        
     // on pressing cancel button
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    private void start() throws InterruptedException, ExecutionException{
    	Intent intent = new Intent(this, PathService.class);
    	
    	NodeList lat, lng, steps;
    	Document document = new PathFetchService().execute(queryString).get();


		NodeList latNodeList = document.getElementsByTagName("lat");       
		NodeList lngNodeList = document.getElementsByTagName("lng");
		NodeList instructions = document.getElementsByTagName("html_instructions");

		Direction direction = new Direction();
		lat = latNodeList.item(0).getChildNodes(); 
		lng = lngNodeList.item(0).getChildNodes();
		direction.setLat(Double.parseDouble(lat.item(0).getNodeValue()));
		direction.setLog(Double.parseDouble(lng.item(0).getNodeValue()));
		directionList.add(direction);
		//Create direction objects and give them their latitude and longitude values
		for(int i=1; i<latNodeList.getLength()-3; i++){
			if(i%2!=0){
				direction = new Direction();
				lat = latNodeList.item(i).getChildNodes(); 
				lng = lngNodeList.item(i).getChildNodes();
				direction.setLat(Double.parseDouble(lat.item(0).getNodeValue()));
				direction.setLog(Double.parseDouble(lng.item(0).getNodeValue()));
				directionList.add(direction);
			}              
		}



		String message, stripped;
		List<String> messageContent = new ArrayList<String>();
		Pattern pattern = Pattern.compile("<b>(.+?)</b>");
		for (int j = 0; j < instructions.getLength(); j++) {
			message ="";
			steps = instructions.item(j).getChildNodes();
			String step = steps.item(0).getNodeValue();


			Matcher matcher = pattern.matcher(step);
			while (matcher.find()) {
				stripped = matcher.group(1);
				if (stripped.contains("/")) {
					stripped = stripped.substring(0, stripped.indexOf('/'));
				}
				messageContent.add(stripped);
			}

			if(messageContent.size()==2){
				if(messageContent.get(0).toLowerCase().equals("left")||messageContent.get(0).toLowerCase().equals("right")){
					directionList.get(j).setDirection(messageContent.get(0));
					directionList.get(j).setStreet(messageContent.get(1));
				}else{
					directionList.get(j).setStreet(messageContent.get(1));
				}

			}else if(messageContent.size() == 1){
				directionList.get(j).setStreet(messageContent.get(0));

			}

			messageContent.clear();

		}

		Toast.makeText(this, "Instruction Size: "+directionList.size(), Toast.LENGTH_SHORT).show();
		//Set up the rest of the information for the emergency alarm		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, Integer.parseInt(maxTime.getText().toString()));	
		alarmIntent.putExtra("contact", emergencyContact.getText().toString());

		PendingIntent sender = PendingIntent.getBroadcast(this, 192837, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);


		emergencyContact.setVisibility(View.GONE);
		distance.setVisibility(View.GONE);
		maxTime.setVisibility(View.GONE);
		findViewById(R.id.button1).setVisibility(View.GONE);
		findViewById(R.id.button2).setVisibility(View.VISIBLE);


		Toast.makeText(this, instructions.item(0).getChildNodes().item(0).getNodeValue().replaceAll("\\<.*?>",""), Toast.LENGTH_SHORT).show();
		intent.putParcelableArrayListExtra("directionsList", directionList);

		startService(intent);
    	
    }
	
}
