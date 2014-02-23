package comp557.a5;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Ray {
	
	/**
	 * Originating point for the ray.
	 */
	public Point3d eyePoint;
	
	/**
	 * The direction of the ray.
	 */
	public Vector3d viewDirection;

	/** 
	 * Default constructor.
	 */
	public Ray() {
		this.eyePoint = new Point3d(0,0,0);
		this.viewDirection = new Vector3d(0,0,-1);
	}
	
	/** 
	 * Creates a new ray with the given eye point and view direction 
	 * @param eyePoint
	 * @param viewDirection
	 */
	public Ray( Point3d eyePoint, Vector3d viewDirection ) {
		this();
		this.eyePoint.set(eyePoint);
		this.viewDirection.set(viewDirection);
	}

	/**
	 * Setup the ray.
	 * @param eyePoint
	 * @param viewDirection
	 */
	public void set( Point3d eyePoint, Vector3d viewDirection ) {
		this.eyePoint.set(eyePoint);
		this.viewDirection.set(viewDirection);
	}
	
	/**
	 * Computes the location of a point along the ray using parameter t.
	 * @param t
	 * @param p
	 */
	public void getPoint( double t, Point3d p ) {
		p.scale( t, viewDirection );
		p.add( eyePoint );
	}
}
