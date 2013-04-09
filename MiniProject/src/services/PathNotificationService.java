package services;

import speechfactory.Factory;
import communication.Buffer;

import android.content.Context;
import android.content.Intent;

/**
 * This class runs a thread that continually tries to read from a buffer. When it successful reads,
 * It then "speaks"/informs the user of the message that has just been read.
 * @author Evan
 *
 */
public class PathNotificationService extends Thread {
	
	private Context context;
	private Buffer buffer;
	private Factory speechFactory;
	private boolean run;
	private String lastDirection = "";
	
	
	public PathNotificationService(Context context, Buffer buffer){
		this.context = context;
		this.buffer = buffer;
		this.run = true;
		speechFactory = Factory.getInstance();
		if(speechFactory != null){
			speechFactory.init(this.context);
		}
	}
	
	@Override
	public void run(){
		String message;
		try{
			while(run){
				message = buffer.read();
				speechFactory.speak(message);
				Intent intent = new Intent();
				intent.setAction("UPDATE_DIRECTION");
				if(!message.equals(lastDirection)){
					intent.putExtra("update", message);
					lastDirection= message;
					
				}else{
					intent.putExtra("update", "NA");
				}
				
				
				context.sendBroadcast(intent);
			}
			
		}catch(InterruptedException e){
			run = false;
		}
	}
	

}
