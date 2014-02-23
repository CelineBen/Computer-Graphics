/**
 * @author CŽline Bensoussan
 * Assignment 5 - Ray Tracing
 * November 29, 2013
 */
package comp557.a5;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials. If both are defined, a 1x1 tile checker board pattern should be
 *  generated on the plane using the two materials.
 */
public class Plane extends Intersectable {

	/**
	 * The second material. If non-null, it is used to produce a checker board pattern.
	 */
	Material material2;

	/**
	 * The plane normal.
	 */
	public static final Vector3d n = new Vector3d(0, 1, 0);

	/**
	 * Default constructor. Creates a unit sphere centered at (0,0,0)
	 */
	public Plane() {
		super();
		this.material2 = null;
	}

	@Override
	public void intersect( Ray ray, IntersectResult result ) {
		Vector3d position = new Vector3d(ray.eyePoint);
		double t = - position.dot(n) / ray.viewDirection.dot(n);
		
		if(t<0.0001){
			return;
		}

		//Set intersect result
		Point3d intersection = new Point3d();
		ray.getPoint(t, intersection); 		
		result.p = intersection;		
		result.n = n;
		result.t = t;

		//If only one material, do not bother getting the checkerboard
		if(this.material2 == null){
			result.material = this.material;
		}else{
			int x, z;
			if(intersection.x < 0){
				x = (int)intersection.x - 1;
			}else{
				x = (int)intersection.x;
			}
			if(intersection.z < 0){
				z = (int)intersection.z - 1;
			}else{
				z = (int)intersection.z;
			}
			if((x%2 == 0 && z%2 == 0) || (Math.abs(x%2) == 1 && Math.abs(z%2) == 1)){
				result.material = this.material;
			}else{
				result.material = this.material2;
			}
		}			
	}

}
