/**
 * @author CŽline Bensoussan
 * Assignment 5 - Ray Tracing
 * November 29, 2013
 */

package comp557.a5;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {

	/**
	 * Static map storing all meshes by name.
	 */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();

	/**
	 * Name for this mesh. We can reference this to re-use a polygon soup across meshes.
	 */
	public String name;

	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.name = "";
		this.soup = null;
	}			

	@Override
	public void intersect(Ray ray, IntersectResult result) {	

		double a,b,c,d,e,f,g,h,i,j,k,l,M,beta, gamma, t;
		
		for(int[] faceVertex: soup.faceList){		
			Point3d A = soup.vertexList.get( faceVertex[0] ).p;
			Point3d B = soup.vertexList.get( faceVertex[1] ).p;
			Point3d C = soup.vertexList.get( faceVertex[2] ).p;		

			a = A.x - B.x;
			b = A.y - B.y;
			c = A.z - B.z;
			d = A.x - C.x;
			e = A.y - C.y;
			f = A.z - C.z;
			g = ray.viewDirection.x;
			h = ray.viewDirection.y;
			i = ray.viewDirection.z;
			j = A.x - ray.eyePoint.x;
			k = A.y - ray.eyePoint.y;
			l = A.z - ray.eyePoint.z;

			M = a*((e*i)-(h*f)) + b*((g*f)-(d*i)) + c*((d*h)-(e*g));
			
			t = - (f*(a*k-j*b) + e*(j*c-a*l) + d*(b*l-k*c)) / M;

			if(t > -0.0001 && t < result.t){
				gamma = (i*(a*k-j*b) + h*(j*c-a*l) + g*(b*l-k*c)) / M;
				
				if(gamma > 0 & gamma < 1){
					beta = (j*(e*i-h*f) + k*(g*f-d*i) + l*(d*h-e*g)) / M;
					
					if(beta > 0 && beta < 1-gamma){
						//Intersection point is: p = e + td
						Point3d intersection = new Point3d(ray.viewDirection);
						intersection.scale(t);
						intersection.add(ray.eyePoint); 		
						result.p = intersection;
						result.material = this.material;
						result.t = t;

						//Normal
						Vector3d ba = new Vector3d();
						Vector3d ca = new Vector3d();
						Vector3d n = new Vector3d();
						ba.sub(B,A);
						ca.sub(C,A);
						n.cross(ba, ca);
						n.normalize();
						result.n = n;
					}
				}
			}
		}
	}	
}
