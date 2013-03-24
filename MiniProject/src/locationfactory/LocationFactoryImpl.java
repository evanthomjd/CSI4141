package locationfactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class LocationFactoryImpl extends LocationFactory implements LocationListener{

	private  Context context;
	private static final long MIN_DISTANCE = 10;
	private static final long MIN_TIME = 1000*10;
	
	
	boolean gpsEnabled = false;	
	boolean netWorkEnabled = false;
	boolean canGetLocation = false;
	
	Location location;
	double lat, lag;
	
	protected LocationManager locationManager;
	
	public void init(Context context){
		if(this.context == null){
			this.context = context;
			getLocation();
		}
		
	}
	
	public Location getLocation(){
		Location gpsLocation, netWorkLocation;
		netWorkLocation = null;

		try{
			locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);			
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			netWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			if(!gpsEnabled){
				//do nothing
			}else{
				canGetLocation = true;
				
				if(netWorkEnabled){
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
					if(locationManager != null){
						netWorkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
						if(location != null){
							lat = location.getLatitude();
							lag = location.getLongitude();
						}

					}
				}else{
					Log.e("CHECK UPDATE" ,"netWROKNOTENABLED");
				}
				
				if(gpsEnabled){
					
	                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
	                        if (locationManager != null) {
	                            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	                            if (gpsLocation != null) {
	                                if(netWorkLocation!=null && netWorkLocation.hasAccuracy()){
	    	                        	if(gpsLocation.hasAccuracy() && gpsLocation.getAccuracy() <= netWorkLocation.getAccuracy()){
	    	                        		location = gpsLocation;
	    	                        	}else{
	    	                        		location = netWorkLocation;
	    	                        	}
	    	                        }
	                                lat = location.getLatitude();
	                                lag = location.getLongitude();
	                            }
	                        }
	                    
	                    }
			}
		}
		catch(Exception e){Log.e("CHECKUPDATE", "error in get location");};
		
		return location;
	}
	
	
	
	public Location getPos(){
		return location;
	}
	
	
	public void stop(){
		if(locationManager != null){
			locationManager.removeUpdates(LocationFactoryImpl.this);
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
	
	//Following methods are not used so are left unimplemented
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onLocationChanged(Location newLocation) {
		Log.e("CHECKUPDATE", "LOCATION UPDATED");
		if(newLocation.getAccuracy()<=60.0){
			this.location = newLocation;
		}else{
			Log.e("CHECKUPDATE", "Not Accurate Enough at " + newLocation.getAccuracy());
		}
		
		
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
