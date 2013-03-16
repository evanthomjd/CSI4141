package csi4141.miniproject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import locationfactory.LocationFactory;
import services.LocationService;
import services.PathService;
import android.app.Activity;
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


	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		locationFactory = LocationFactory.getInstance();

	}

	public void startRun(View view) throws ParserConfigurationException, SAXException, IOException{
		//Create intent to start PathService
		Intent intent = new Intent(this, PathService.class);


		//Retrieve the input from the user 
		EditText emergencyContact = (EditText) findViewById(R.id.emergency_email);
		EditText distance = (EditText) findViewById(R.id.distance);
		EditText maxTime = (EditText) findViewById(R.id.max_time);


		//initialize the location factory 
		if(locationFactory!=null){
			locationFactory.init(MainActivity.this);
		}

		if(locationFactory.canGetLocation()){
			//Get first location 
			Location location = locationFactory.getPos();



			if(location!=null){
				List<String> tuples = new ArrayList<String>();
				NodeList lat, lng;
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

				//House coordinate 45.381256,-75.637736
				currentLat = location.getLatitude();
				currentLong = location.getLongitude();
				angle = 1 + (int)(Math.random()*361);
				destLat = (Math.cos(angle)*range)+currentLat;
				destLong = (Math.sin(angle)*range)+currentLong;
				
				
				//http://maps.googleapis.com/maps/api/directions/xml?origin=43.6533100,-79.3827700&destination=45.5104800,-73.5533200&sensor=false&mode=walking
				queryString = "http://maps.googleapis.com/maps/api/directions/xml?origin="+currentLat+","+currentLong+"&destination="+destLat+","+destLong+"&sensor=false&mode=walking";
				Document document = documentBuilder.parse(queryString);

				String currentLocation= currentLat + " , " + currentLong;
				Toast.makeText(this, currentLocation, Toast.LENGTH_SHORT).show();
			}
		}else{
			locationFactory.showSettingsAlert();
		}


		//intent.putExtra(TEST_TUPLE, emergencyContact.getText().toString() + " , "+distance.getText().toString()+","+maxTime.getText().toString());

		//startService(intent);
	}

}
