/**
 * @author CŽline Bensoussan
 * Assignment 5 - Ray Tracing
 * November 29, 2013
 * 
 * some code found here 
 * http://www.scratchapixel.com/lessons/3d-basic-lessons/lesson-7-intersecting-simple-shapes/ray-box-intersection/
 */
package comp557.a5;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;

	/**
	 * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
	 */
	public Box() {
		super();
		this.max = new Point3d(1, 1, 1);
		this.min = new Point3d(-1, -1, -1);
	}	

	@Override
	public void intersect(Ray ray, IntersectResult result)
	{
		double txmin, txmax, tymin, tymax, tzmin, tzmax;
		if(ray.viewDirection.x >= 0){
			txmin = (min.x - ray.eyePoint.x) / ray.viewDirection.x;
			txmax = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
		}else{
			txmin = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
			txmax = (min.x - ray.eyePoint.x) / ray.viewDirection.x;
		}
		if(ray.viewDirection.y >= 0){
			tymin = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
			tymax = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
		}else{
			tymin = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
			tymax = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
		}
		if((txmin > tymax) || (tymin > txmax)){
			return;
		}
		if (tymin > txmin){txmin = tymin;}
		if (tymax < txmax){txmax = tymax;}

		if(ray.viewDirection.z >= 0){
			tzmin = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
			tzmax = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
		}else{
			tzmin = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
			tzmax = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
		}
		if ((txmin > tzmax) || (tzmin > txmax)){
			return;
		}    
		if (tzmin > txmin){txmin = tzmin;}
		if (tzmax < txmax){txmax = tzmax;}
		
		if(txmin <= 0){
			return;
		}

		//Set intersect result
		Point3d intersection = new Point3d();
		ray.getPoint(txmin, intersection); 		
		result.p = intersection;
		result.material = this.material;
		result.t = txmin;
		
		//NORMAL
		float EPS = (float)0.00001;
		Vector3d normal = new Vector3d(intersection);

		if(Math.abs(normal.x - min.x) < EPS) {
			normal.set(-1,0,0);
		}else if(Math.abs(normal.x - max.x) < EPS) {
			normal.set(1,0,0);
		}else if(Math.abs(normal.y - min.y) < EPS) {
			normal.set(0,-1,0);
		}else if(Math.abs(normal.y - max.y) < EPS) {
			normal.set(0,1,0);
		}else if(Math.abs(normal.z - min.z) < EPS) {
			normal.set(0,0,-1);
		}else if(Math.abs(normal.z - max.z) < EPS) {
			normal.set(0,0,1);
		}
		result.n = normal;
	}	

}
