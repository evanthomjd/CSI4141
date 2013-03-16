package services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.audiofx.BassBoost.Settings;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service implements LocationListener  {
	
	private final Context context;
	private static final long MIN_DISTANCE = 5;
	private static final long MIN_TIME = 1000;
	
	boolean gpsEnabled = false;	
	boolean netWorkEnabled = false;
	boolean canGetLocation = false;
	
	Location location;
	double lat, lag;
	
	//This is used by the alarm service.
	private static Location currentLocation;
	
	protected LocationManager locationManager;
	
	public LocationService(Context context){
		this.context = context;
		getLocation();
	}
	
	public Location getLocation(){

		try{
			locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);			
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			netWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if(!gpsEnabled || !netWorkEnabled){
				//do nothing
			}else{
				canGetLocation = true;
				
				if(netWorkEnabled){
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
					if(locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(location != null){
							lat = location.getLatitude();
							lag = location.getLongitude();
						}
					}
				}
				
				if(gpsEnabled){
					 if (location == null) {
	                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
	                        if (locationManager != null) {
	                            location = locationManager
	                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                            if (location != null) {
	                                lat = location.getLatitude();
	                                lag = location.getLongitude();
	                            }
	                        }
	                    }	
				}
			}
			
			currentLocation = location;
		}
		catch(Exception e){};
		
		return location;
	}
	
	
	
	public Location getPos(){
		return location;
	}
	
	
	
	
	public void stopLocationService(){
		if(locationManager != null){
			locationManager.removeUpdates(LocationService.this);
		}
	}
	
	public boolean canGetLocation(){
		return canGetLocation;
	}
	

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
 
        alertDialog.setTitle("GPS and Network settings");
 
        // Setting Dialog Message
        alertDialog.setMessage("GPS or Data is not enabled. Please enable your Both before using the application!");
 
 
        // on pressing cancel button
        alertDialog.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
 
        // Showing Alert Message
        alertDialog.show();
    }
    
    public static Location getLastLocation() {
    	return currentLocation;
    }
	
	//Following methods are not used so are left unimplemented
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

}
