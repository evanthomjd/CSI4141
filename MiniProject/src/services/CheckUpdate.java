package services;

import java.util.TimerTask;

import communication.Buffer;

import speechfactory.Factory;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;
/*
 * Check update class, gets the runners current postion and compares it with the path they should be on.
 * It then places the appropriate message in a messaging queue that.
 * 
 * If it notices you are not on the correct path. It updates the path.
 */
public class CheckUpdate extends TimerTask {
	 
	
   private Buffer buffer;
	
	

	
	public CheckUpdate( Buffer buffer){
		super();
		this.buffer = buffer;		
		
	}

	@Override
	public void run() {				
		
	}

}
