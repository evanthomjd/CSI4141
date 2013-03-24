package geo;

public class Line {
	
	private double x1, x2, y1, y2;
	
	public Line(double x1, double y1, double x2, double y2){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		
		
	
	}
	
	public double distanceFromPoint(double x3, double y3){
		double result = 0.0;
        double normal = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        result = Math.abs(((x3-x1)*(x3-x2)-(y3-y1)*(y3-y2))/normal);
        return result;
	}

}
