package services;

import java.util.ArrayList;
import java.util.Timer;

import communication.Buffer;
import communication.Direction;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

/*
 * Wrapper class that runs the checkupdate class every second. 
 */
public class PathService extends Service {

	private Timer timer;
	private boolean running = false;
	private Buffer buffer;
	private PathNotificationService pathNotificationService;
	private ArrayList<Direction> directionList;
	
	@Override
	public void onCreate() {
	    super.onCreate();
	    timer = new Timer();
	    this.buffer = new Buffer();	

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		directionList = intent.getExtras().getParcelableArrayList("directionsList");		
		if(!running){
			 timer = new Timer();
			 
			 if(pathNotificationService == null){
				    this.pathNotificationService = new PathNotificationService(getApplicationContext(),buffer);	
				    pathNotificationService.start();
				}
			 
			 timer.scheduleAtFixedRate(new CheckUpdate(buffer, directionList, getApplicationContext()), 1000, 2000);
			 running = true;
		}		
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	@Override 
	public void onDestroy(){
		super.onDestroy();
		if(timer!=null){
			timer.cancel();
		}
		running = false;
		
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		//Do nothing as this service will not bind
		return null;
	}

}
