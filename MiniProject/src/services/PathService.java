package services;

import java.util.Timer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

public class PathService extends Service {

	private CheckUpdate checkUpdate;
	private Timer timer;
	
	private boolean running = false;
	
	@Override
	public void onCreate() {
	    // TODO Auto-generated method stub
	    super.onCreate();
	    timer = new Timer();
	    timer.scheduleAtFixedRate(new CheckUpdate((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), getApplicationContext()), 1000, 2000);
	    running = true;

	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(!running){
			 timer = new Timer();
			 timer.scheduleAtFixedRate(new CheckUpdate((LocationManager) this.getSystemService(Context.LOCATION_SERVICE), getApplicationContext()), 1000, 2000);
			 running = true;
		}
		Toast.makeText(this, "service started3", Toast.LENGTH_SHORT).show();
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
