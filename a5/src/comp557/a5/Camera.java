package comp557.a5;

import java.awt.Dimension;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple camera object, which could be extended to handle a variety of 
 * different camera settings (e.g., aperature size, lens, shutter)
 */
public class Camera {
	
	/**
	 * Camera name.
	 */
    public String name;

    /**
     * The eye position.
     */
    public Point3d from;
    
    /**
     * The "look at" position
     */
    public Point3d to;
    
    /**
     * Up direction.
     */
    public Vector3d up;
    
    /**
     * Vertical field of view (in degrees). Default is 45 degrees.
     */
    public double fovy;
    
    /**
     * The rendered image size.
     */
    public Dimension imageSize = new Dimension();

    /**
     * Default constructor. Creates a camera located at (0,0,0) and looking in the direction (0,0,-1).
     */
    public Camera() {
    	this.name = "";
    	this.from = new Point3d(0,0,0);
    	this.to = new Point3d(0,0,1);
    	this.up = new Vector3d(0,1,0);
    	this.fovy = 45.0;
    	this.imageSize = new Dimension(640,480);
    }
}

