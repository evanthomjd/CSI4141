package services;

import java.util.List;

import locationfactory.LocationFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.location.Geocoder;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

public class AlarmService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			Location lastLocation = LocationService.getLastLocation();

			String phoneNumber = bundle.getString("contact");
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
				Toast.makeText(context, "Send SMS to: " + phoneNumber, Toast.LENGTH_SHORT).show();
				
				List<Address> addresses = new Geocoder(context).getFromLocation(lastLocation.getLatitude(),lastLocation.getLongitude() , 1);
				Address current = addresses.get(0);
				
				String line1 = current.getAddressLine(0);
				String line2 = current.getAddressLine(1);
				String area = current.getLocality();
				String postal = current.getPostalCode();
				
				if (line1 == null)
					line1 = "";
				if (line2 == null)
					line2 = "";
				if (area == null)
					area = "";
				if (postal == null)
					postal = "";
				
				String message = "I'm late coming back from my run. My last known location was: " + line1 + ", " +
						line2 + ", " + area + ", " + postal;
				
				sendSMS(phoneNumber, message);
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
	
	private void sendSMS(String number, String message) {
	    SmsManager smsManager = SmsManager.getDefault();
	    smsManager.sendTextMessage(number, null, message, null, null);
	}
	
	

}