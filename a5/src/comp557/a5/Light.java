package comp557.a5;

import javax.vecmath.Color4f;
import javax.vecmath.Point3d;

public class Light {
	
	/** 
	 * Light name.
	 */
    public String name;
    
    /**
     * Light colour.
     */
    public Color4f color;
    
    /**
     * Light position.
     */
    public Point3d from;
    
    /**
     * Light intensity. 
     */
    public double power;
    
    /**
     * Type of light. Default is a point light.
     */
    public String type;

    /**
     * Default constructor.  Creates a point light located at (0,0,0). 
     */
    public Light() {
    	this.name = "";
    	this.color = new Color4f(1,1,1,1);
    	this.from = new Point3d(0,0,0);
    	this.power = 1.0;
    	this.type = "point";
    }
}
