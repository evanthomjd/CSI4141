package services;

import locationfactory.LocationFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

public class AlarmService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			Location lastLocation = LocationService.getLastLocation();

			String email = bundle.getString("email");
			double homeLatitude = bundle.getDouble("homeLat");
			double homeLongitude = bundle.getDouble("homeLong");

			LocationFactory locationFactory = LocationFactory.getInstance();

			if (locationFactory == null) {
				throw new Exception("Location Factory not instantiated");
			}

			//Do stuff here
			if (inSafeZone(homeLatitude, homeLongitude, lastLocation)) {
				//Do nothing, we're done here. Maybe send a toast notification?
			}
			else {
				Toast.makeText(context, "Send email to: " + email, Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
			e.printStackTrace();

		}

	}

	private boolean inSafeZone(double homeLat, double homeLong, Location last){
		Location home = new Location("Home");
		home.setLatitude(homeLat);
		home.setLongitude(homeLong);
		
		double distance = home.distanceTo(last);
		
		if (distance > 100)
			return false;
		
		return true;
	}

}