package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.TimerTask;



import locationfactory.LocationFactory;

import communication.Buffer;
import communication.Direction;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;


import geo.Line;


/*
 * Check update class, gets the runners current postion and compares it with the path they should be on.
 * It then places the appropriate message in a messaging queue that.
 * 
 * If it notices you are not on the correct path. It updates the path.
 */
public class CheckUpdate extends TimerTask {


	private Buffer buffer;
	private ArrayList<Direction> directionList;
	private Stack<Direction> home;
	private LocationFactory locationFactory;
	private Direction lastCheckPoint, currentCheckPoint;
	private Location endPoint;
	
	private int count = 0;
	private Context context;



	public CheckUpdate( Buffer buffer, ArrayList<Direction> directionList, Context context){
		super();
		this.buffer = buffer;	
		this.directionList = directionList;
		this.home = new Stack<Direction>();
		this.locationFactory = LocationFactory.getInstance();
		if(locationFactory!=null){
			locationFactory.init(context);
		}
		
		Log.e("CHECKUPDATE", "List Size: "+directionList.size());
		lastCheckPoint = directionList.remove(0);
		home.push(lastCheckPoint);
		this.context = context;


	}

	@Override
	public void run() {	

		if(!directionList.isEmpty()){
			Location currentLocation = locationFactory.getPos();
			Direction direction = directionList.get(0);
			if(currentLocation != null ){
				Location checkPoint = new Location(currentLocation);
				checkPoint.setLatitude(direction.getLat());
				checkPoint.setLongitude(direction.getLog());
				Direction updated = getClosestLocation(currentLocation, lastCheckPoint, direction);
				
				if(updated.getLat() != 0.0 && updated.getLog() != 0.0){
					currentLocation.setLatitude(updated.getLat());
					currentLocation.setLongitude(updated.getLog());
				}
				
				if(onPath(lastCheckPoint, direction, currentLocation)){				
					if(directionList.size()==1){
						
					
						//Last direction this is the end so we wait until they are close and then inform them to turn around
						if(currentLocation.distanceTo(checkPoint)<=50){
							try {
								buffer.write("Turn around, running in the direction you came");
								lastCheckPoint = directionList.remove(0);
								while(!home.isEmpty()){
									directionList.add(home.pop());
								}
							} catch (InterruptedException e) {
								
							}
						}
					}
					//Check too see if this checkpoint involoves a turn
					else if(between(lastCheckPoint, direction, currentLocation)==1){				
						if(between(direction, directionList.get(1), currentLocation)==0){
							lastCheckPoint = directionList.remove(0);		
							Direction copy = new Direction();
							copy.setLat(lastCheckPoint.getLat());
							copy.setLog(lastCheckPoint.getLog());
							if(lastCheckPoint.getDirection()!=null && lastCheckPoint.getDirection().equals("right")){
								copy.setDirection("left");
							}else if (lastCheckPoint.getDirection()!=null && lastCheckPoint.getDirection().equals("left")){
								copy.setDirection("right");
							}
							copy.setDirection(home.peek().getStreet());					
							home.push(copy);
							
							
							
						}else{
							//off path section
							Log.e("CHECKUPDATE", ""+between(lastCheckPoint, direction, currentLocation));
							
						}					
					}else if (between(lastCheckPoint, direction, currentLocation)==-1){
							//off path section		
					}
					
					try {
						if(direction.getDirection() != null && count% 10==0){
							buffer.write("At " + direction.getStreet() + " turn " + direction.getDirection());
						}
						else if(direction.getDirection() ==null && count%10==0){
							buffer.write("Continue forward");
						}
					
					} catch (InterruptedException e) {
						
					}
					
				}else{
					//recalculate path
					
				}
			}else{
				Log.e("CHECKUPDATE", "null location");
			}
		}else{
			//This means you have read through all directions, you are at end, turn around and run home
		}
		count++;
	}
	
	
	public boolean onPath(Direction start, Direction end, Location current){
		boolean onPath = true;
		double offBy;
		Line line = new Line(start.getLat(), start.getLog(), end.getLat(), end.getLog());
		offBy = line.distanceFromPoint(current.getLatitude(), current.getLongitude());
		
		if(offBy > 2){
			onPath = false; 
		}
		
		return onPath;
	}
	
	//return of 0 means current is in between, 1 means ahead and -1 means behind
	public int between(Direction start, Direction end, Location current){
		int between = 0;
		double startX,  endX, currentX;
		
		//6371
		
		startX = 6371.0 * Math.cos(start.getLat())*Math.cos(start.getLog());
	
		endX = 6371.0 * Math.cos(end.getLat())*Math.cos(end.getLog());
		
		currentX = 6371.0 * Math.cos(current.getLatitude())*Math.cos(current.getLongitude());

		
		
		if(startX < endX){
			if(currentX <= endX && currentX >= startX){
				between = 0;
			}else if(currentX>endX){
				between = 1;
			}else if (startX - currentX > 0.6){
				between = -1;
			}
		}else{
			if(currentX >= endX && currentX <=startX){
				between = 0;
			}else if(currentX<endX){
				between = 1;
			}
			else if (currentX - startX > 0.6){
				between = -1;
			
			}
			
		}
		return between;
	}
	
	
	public Direction getClosestLocation(Location location, Direction start, Direction end){
		Direction direction = new Direction();
		double min = 10000.0;
		Line line = new Line(start.getLat(), start.getLog(), end.getLat(), end.getLog());
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		
		if(location.hasAccuracy() && location.getAccuracy()>10){
			try {
				List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
				for(Address address: addresses){
					if(line.distanceFromPoint(address.getLatitude(), address.getLongitude()) < min){
						min = line.distanceFromPoint(address.getLatitude(), address.getLongitude());
						direction.setLat(address.getLatitude());
						direction.setLog(address.getLongitude());
					}
				}
						
		
			} catch (IOException e) {	
				Log.e("GEOCODER", "GEOCODER HAD I/O Exception");	
			}
		}
	
		return direction;
		

	}

}
