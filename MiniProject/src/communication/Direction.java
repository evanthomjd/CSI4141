package communication;

import android.os.Parcel;
import android.os.Parcelable;

public class Direction implements Parcelable {
	private String direction;
	private double lat;
	private double log;
	
	public Direction(){
		
	}
	public Direction(Parcel source){
		lat = source.readDouble();
		log = source.readDouble();
		direction = source.readString();
	}
	
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLog() {
		return log;
	}
	public void setLog(double log) {
		this.log = log;
	}
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(lat);
		dest.writeDouble(log);
		dest.writeString(direction);
		
	}
	
	
	public static final Parcelable.Creator<Direction> CREATOR
    = new Parcelable.Creator<Direction>() 
   {
         public Direction createFromParcel(Parcel source) 
         {
             return new Direction(source);
         }

         public Direction[] newArray (int size) 
         {
             return new Direction[size];
         }
    };
	
	
	
	
}
