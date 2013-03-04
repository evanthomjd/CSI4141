package services;

import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

public class CheckUpdate extends TimerTask {
	
	private String locationProvider = LocationManager.GPS_PROVIDER;
	private LocationManager locationManager;
	private Location location;
	private Context context;
	
	public CheckUpdate(LocationManager locationManager, Context context){
		super();
		this.locationManager = locationManager;
		this.context = context;
	}

	@Override
	public void run() {		
		location = locationManager.getLastKnownLocation(locationProvider);
		String tuple = location.getLatitude() + " , " + location.getLongitude();
		
		Toast.makeText(context, "test", Toast.LENGTH_SHORT).show();
		
	}

}
