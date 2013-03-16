package services;

import speechfactory.Factory;
import communication.Buffer;

import android.content.Context;
/**
 * This class runs a thread that continuly tries to read from a buffer. When it succesffuly reads,
 * It then "speaks"/informs the user of the message that has just been read.
 * @author evan
 *
 */
public class PathNotificationService extends Thread {
	
	private Context context;
	private Buffer buffer;
	private Factory speechFactory;
	private boolean run;
	
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
			}
			
		}catch(InterruptedException e){
			run = false;
		}
	}
	

}
