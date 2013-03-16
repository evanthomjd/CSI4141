package locationfactory;

import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.os.Build;

public abstract class LocationFactory extends Service {

	private static LocationFactory locationFactory;

	public abstract void init(Context context);

	public abstract Location getLocation();

	public abstract Location getPos();

	public abstract void stop();

	public abstract boolean canGetLocation();

	public abstract void showSettingsAlert();

	public static LocationFactory getInstance(){
		if(locationFactory == null){
			int sdkVersion = Build.VERSION.SDK_INT;
			if(sdkVersion < Build.VERSION_CODES.DONUT){
				return null;
			}	

			try{
				String name = "LocationFactoryImpl";
				Class<? extends LocationFactory> clazz = Class.forName(LocationFactory.class.getPackage().getName()+"."+name).asSubclass(LocationFactory.class);
				locationFactory = clazz.newInstance();
			}catch(Exception e){
				throw new IllegalStateException(e);
			}
		}

		return locationFactory;
	}




}
