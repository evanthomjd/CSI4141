package csi4141.miniproject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import services.PathService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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


	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		locationFactory = LocationFactory.getInstance();

	}

	public void startRun(View view) throws ParserConfigurationException, SAXException, IOException{
		//Create intent to start PathService
		Intent intent = new Intent(this, PathService.class);

		Toast.makeText(this, "One", Toast.LENGTH_SHORT).show();
		//Retrieve the input from the user 
		EditText emergencyContact = (EditText) findViewById(R.id.emergency_email);
		EditText distance = (EditText) findViewById(R.id.distance);
		EditText maxTime = (EditText) findViewById(R.id.max_time);


		Toast.makeText(this, "Two", Toast.LENGTH_SHORT).show();
	

		if(locationFactory!=null){
			locationFactory.init(MainActivity.this);
		}

		
		//Create the Intent for the alarmService
		Intent alarmIntent = new Intent(MainActivity.this, AlarmService.class);


		if(locationFactory.canGetLocation()){
			//Get first location 
			Location location = locationFactory.getPos();

			if(location!=null){
				List<String> tuples = new ArrayList<String>();
				NodeList lat, lng, steps;
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

				//House coordinate 45.381256,-75.637736
				currentLat = location.getLatitude();
				currentLong = location.getLongitude();
				angle = 1 + (int)(Math.random()*361);
				destLat = (Math.cos(angle)*range)+currentLat;
				destLong = (Math.sin(angle)*range)+currentLong;
				
				//Store the home location for use by the emergency alarm
				alarmIntent.putExtra("homeLat", currentLat);
				alarmIntent.putExtra("homeLong", currentLong);
				
				
			
				queryString = "http://maps.googleapis.com/maps/api/directions/xml?origin="+currentLat+","+currentLong+"&destination="+destLat+","+destLong+"&sensor=false&mode=walking";
				Document document = documentBuilder.parse(queryString);

				NodeList latNodeList = document.getElementsByTagName("lat");       
		        NodeList lngNodeList = document.getElementsByTagName("lng");
		        NodeList instructions = document.getElementsByTagName("html_instructions");
				
		        //Create direction objects and give them their latitude and longitude values
		        for(int i=1; i<latNodeList.getLength()-3; i++){
		            if(i%2!=0){
		            	Direction direction = new Direction();
		                lat = latNodeList.item(i).getChildNodes(); 
		                lng = lngNodeList.item(i).getChildNodes();
		                direction.setLat(Double.parseDouble(lat.item(0).getNodeValue()));
		                direction.setLog(Double.parseDouble(lng.item(0).getNodeValue()));
		                directionList.add(direction);
		            }              
		        }
		        
		        for(int i=0; i<instructions.getLength(); i++){
		        	steps = instructions.item(i).getChildNodes();
		        	directionList.get(i).setDirection(steps.item(0).getNodeValue());
		        }
				String currentLocation= currentLat + " , " + currentLong;
				Toast.makeText(this, currentLocation, Toast.LENGTH_SHORT).show();
			}
		}else{
			locationFactory.showSettingsAlert();
		}
		
		//Set up the rest of the information for the emergency alarm		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, Integer.parseInt(maxTime.getText().toString()));	
		alarmIntent.putExtra("email", emergencyContact.getText().toString());

		PendingIntent sender = PendingIntent.getBroadcast(this, 192837, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);



		intent.putParcelableArrayListExtra("directionsList", directionList);

		startService(intent);
	}

}
