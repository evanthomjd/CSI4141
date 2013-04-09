package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;




import locationfactory.LocationFactory;

import communication.Buffer;
import communication.Direction;

import android.content.Context;
import android.content.Intent;
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
	private Direction lastCheckPoint;	
	private int count = 0;
	private Context context;
	private boolean recalculating, headedHome;
	private int recalculated;

	private String offPathType;
	private int timesOffPath;


	public CheckUpdate( Buffer buffer, ArrayList<Direction> directionList, Context context){
		super();
		this.buffer = buffer;	
		this.directionList = directionList;
		this.home = new Stack<Direction>();
		this.headedHome = false;
		this.locationFactory = LocationFactory.getInstance();
		if(locationFactory!=null){
			locationFactory.init(context);
		}		
		this.lastCheckPoint = directionList.remove(0);
		home.push(lastCheckPoint);
		this.context = context;
		this.recalculating = false;
		this.recalculated = 0;
		timesOffPath = 0;
		offPathType = "NA";

	}

	@Override
	public void run() {	

		if(!directionList.isEmpty() && !recalculating){
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
								if(!headedHome){
									headedHome = true;
									buffer.write("Turn around, running in the direction you came");
									lastCheckPoint = directionList.remove(0);
									
									while(!home.isEmpty()){
										directionList.add(home.pop());
									}
								}else{
									buffer.write("Congradulations, Run complete");								
								}
								
							} catch (InterruptedException e) {

							}
						}
					}
					//Check too see if this checkpoint involoves a turn
					else if(between(lastCheckPoint, direction, currentLocation)==1){	
						int locationReading = between(direction, directionList.get(1), currentLocation);
						if(locationReading == 0){
							lastCheckPoint = directionList.remove(0);		
							Direction copy = new Direction();
							copy.setLat(lastCheckPoint.getLat());
							copy.setLog(lastCheckPoint.getLog());
							if(lastCheckPoint.getDirection()!=null && lastCheckPoint.getDirection().equals("right")){
								copy.setDirection("left");
							}else if (lastCheckPoint.getDirection()!=null && lastCheckPoint.getDirection().equals("left")){
								copy.setDirection("right");
							}
							copy.setStreet(home.peek().getStreet());					
							home.push(copy);													
						}

						else if (locationReading == 1 && currentLocation.hasAccuracy()){		
							checkPoint.setLatitude(directionList.get(0).getLat());
							checkPoint.setLongitude(directionList.get(0).getLog());
							if(currentLocation.distanceTo(checkPoint) > currentLocation.getAccuracy()){
								if(offPathType.equals("AHEAD")){
									if(timesOffPath == 10){
										try {
											if(recalculated > 4){
												buffer.write("You have ignored to many directions, the application can no longer guide you.");
											}else{
												recalculating = true;
												buffer.write("RECALCULATING , 1 then 1");
												recalculatePath(currentLocation);
												recalculated++;	
											}
										
										} catch (InterruptedException e) {
											//Place warning telling user update too path has failed, display output.
										} catch (ExecutionException e) {
											Intent intent = new Intent();
											intent.setAction("UPDATE_DIRECTION");
											intent.putExtra("update", "There was an error when trying to update your route, we are no longer able to track your position");
											context.sendBroadcast(intent);
										}
										recalculating = false;	
										timesOffPath = 0;
									}else{
										timesOffPath++;
									}

								}else{
									timesOffPath = 0;
									offPathType = "AHEAD";
								}
							}
						}else if(locationReading == -1 && currentLocation.hasAccuracy()){
							if(currentLocation.distanceTo(checkPoint) > currentLocation.getAccuracy()){
								if(offPathType.equals("BEHINDNEXT")){
									if(timesOffPath == 10){
										try {
											if(recalculated > 4){
												buffer.write("You have ignored to many directions, the application can no longer guide you.");
											}
											else{
												recalculating = true;
												buffer.write("RECALCULATING , 1 then -1");
												recalculatePath(currentLocation);
												recalculated++;	
											}
											
										} catch (InterruptedException e) {
											
										} catch (ExecutionException e) {
											Intent intent = new Intent();
											intent.setAction("UPDATE_DIRECTION");
											intent.putExtra("update", "There was an error when trying to update your route, we are no longer able to track your position");
											context.sendBroadcast(intent);					
										}
										recalculating = false;	
										timesOffPath = 0;	
									}else{
										timesOffPath++;
									}
								}
								else{
									timesOffPath = 0;
									offPathType = "BEHINDNEXT";
								}
							}
						}

					}else if (between(lastCheckPoint, direction, currentLocation)==-1 && currentLocation.hasAccuracy()){
						checkPoint.setLatitude(lastCheckPoint.getLat());
						checkPoint.setLongitude(lastCheckPoint.getLog());
						if(currentLocation.distanceTo(checkPoint) > currentLocation.getAccuracy()){
							if(offPathType.equals("BEHIND")){
								if(timesOffPath == 10){
									try {
										if(recalculated > 4){
											buffer.write("You have ignored to many directions, the application can no longer guide you.");
										}
										else{
											recalculating = true;
											buffer.write("RECALCULATING , -1");
											recalculatePath(currentLocation);
											recalculated++;
										}							
									} catch (InterruptedException e) {
										//Place warning telling user update too path has failed, display output.
									} catch (ExecutionException e) {
										Intent intent = new Intent();
										intent.setAction("UPDATE_DIRECTION");
										intent.putExtra("update", "There was an error when trying to update your route, we are no longer able to track your position");
										context.sendBroadcast(intent);					
									}
									recalculating = false;	
									timesOffPath = 0;	
								}else{
									timesOffPath++;
								}
							}
							else{
								timesOffPath = 0;
								offPathType = "BEHIND";
							}
						}

					}

					try {
						if(direction.getDirection() != null && count% 10==0){
							buffer.write("At " + direction.getStreet() + " turn " + direction.getDirection());
						}
						else if(direction.getStreet() !=null && count%10 == 0){
							buffer.write("Head towards " + direction.getStreet());
						}
						else if(direction.getDirection() == null && direction.getStreet() == null && count%10==0){
							buffer.write("Continue forward");
						}

					} catch (InterruptedException e) {

					}

				}else{
					if(recalculated < 5){
						recalculating = true;
						try {
							buffer.write("recalculating, off path");
							recalculatePath(currentLocation);
							recalculated++;
						} catch (InterruptedException e) {
							//Place warning telling user update too path has failed, display output.
						} catch (ExecutionException e) {
							Intent intent = new Intent();
							intent.setAction("UPDATE_DIRECTION");
							intent.putExtra("update", "There was an error when trying to update your route, we are no longer able to track your position");
							context.sendBroadcast(intent);
						}
						recalculating = false;		
					}
					else{
						try{
							buffer.write("You have ignored to many directions, the application can no longer guide you.");
						}catch(InterruptedException e){
							
						}
						
					}

				}
			}else{
				if(!locationFactory.canGetLocation()){
					try{
						buffer.write("STOP");
						Intent intent = new Intent();
						intent.setAction("UPDATE_DIRECTION");
						intent.putExtra("update", "Your GPS and DATA connection are no longer active, the application can no longer guide you");
						context.sendBroadcast(intent);
					}catch(InterruptedException e){
						
					}
				}
			}
		}
		count++;
	}

	/**
	 * Checks to see if a users current position is close enough to a straight line between the start and end 
	 * calculated using the projection of a point onto a line
	 * @param start
	 * @param end
	 * @param current
	 * @return
	 */
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

		//6371 - approximent radius of the earth in KM		
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
		}
		else{
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
				//errror establishing connection with geocoder so do not initialize the direction coordinates 
			}
		}

		return direction;

	}


	public void recalculatePath(Location currentLocation) throws InterruptedException, ExecutionException{
		Direction runningTo = directionList.get(directionList.size()-1);
		String queryString ="http://maps.googleapis.com/maps/api/directions/xml?origin="+currentLocation.getLatitude()+","+currentLocation.getLongitude()+"&destination="+runningTo.getLat()+","+runningTo.getLog()+"&sensor=true&mode=walking";
		ArrayList<Direction> newDirection = new PathFetchService().execute(queryString).get();
		directionList.clear();

		directionList = newDirection;

		recalculated++;
	}

}
