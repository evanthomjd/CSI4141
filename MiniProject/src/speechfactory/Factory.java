package speechfactory;

import android.content.Context;
import android.os.Build;

public abstract class Factory {

	private static Factory factory;

	public abstract void speak(String toSay);

	public abstract void init(Context context);

	public abstract void close();

	public static Factory getInstance(){
		if(factory == null){
			int sdkVersion = Build.VERSION.SDK_INT;
			if(sdkVersion < Build.VERSION_CODES.DONUT){
				return null;
			}


			try{
				String name = "SpeechFactory";
				Class<? extends Factory> clazz = Class.forName(Factory.class.getPackage().getName()+"."+name).asSubclass(Factory.class);
				factory = clazz.newInstance();
			}catch (Exception e){
				throw new IllegalStateException(e);
			}
		}


		return factory;
	}


}
