package services;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import locationfactory.LocationFactory;

import communication.Buffer;
import communication.Direction;

import speechfactory.Factory;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
/*
 * Check update class, gets the runners current postion and compares it with the path they should be on.
 * It then places the appropriate message in a messaging queue that.
 * 
 * If it notices you are not on the correct path. It updates the path.
 */
public class CheckUpdate extends TimerTask {


	private Buffer buffer;
	private ArrayList<Direction> directionList;
	private ArrayList<String> test;
	private LocationFactory locationFactory;


	public CheckUpdate( Buffer buffer, ArrayList<Direction> directionList, Context context){
		super();
		this.buffer = buffer;	
		this.directionList = directionList;
		this.locationFactory = LocationFactory.getInstance();
		if(locationFactory!=null){
			locationFactory.init(context);
		}


	}

	@Override
	public void run() {	
		Pattern pattern = Pattern.compile("<b>(.+?)</b>");
		Matcher matcher;
		if(!directionList.isEmpty()){
			Location currentLocation = locationFactory.getPos();
			Direction direction = directionList.get(0);
			List<String> messageContent = new ArrayList<String>();
			
			if(currentLocation != null ){
				Location checkPoint = new Location(currentLocation);
				checkPoint.setLatitude(direction.getLat());
				checkPoint.setLongitude(direction.getLog());
				
				//Check too see if this checkpoint involoves a turn
				if(direction.getDirection().toLowerCase().contains("left")||direction.getDirection().toLowerCase().contains("right")){
					String message, stripped;
					if(currentLocation.distanceTo(checkPoint)<=20){
						matcher = pattern.matcher(direction.getDirection());
						while(matcher.find()){
							stripped = matcher.group(1);
							if(stripped.contains("/")){
								stripped = stripped.substring(0, stripped.indexOf('/'));
							}
							messageContent.add(stripped);
						}
						
						message = messageContent.get(0) + " onto " + messageContent.get(1);
						try {
							buffer.write(message);
							directionList.remove(0);
						} catch (InterruptedException e) {
							
						}
					}
				}


			}
		}



	}

}
