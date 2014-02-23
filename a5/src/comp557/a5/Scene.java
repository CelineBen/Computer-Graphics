/**
 * @author CŽline Bensoussan
 * Assignment 5 - Ray Tracing
 * November 29, 2013
 */
package comp557.a5;

import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {

	/**
	 * Flat array of surfaces
	 */
	public SceneNode root;

	/**
	 * All scene lights.
	 */
	public Map<String,Light> lights;

	/** 
	 * Contains information about how to render the scene.
	 */
	public Render render;

	/** 
	 * The ambient colour.
	 */
	public Color3f ambient;

	/** 
	 * Default constructor.
	 */
	public Scene() {
		this.root = null;
		this.render = new Render();
		this.ambient = new Color3f();
		this.lights = new HashMap<String,Light>();
	}

	/**
	 * renders the scene
	 */
	public void render(boolean showPanel) {

		Camera cam = render.camera; 
		int width = cam.imageSize.width;
		int height = cam.imageSize.height;

		render.init(width, height, showPanel);


		for ( int i = 0; i < height && !render.isDone(); i++ ) {
			for ( int j = 0; j < width && !render.isDone(); j++ ) {
			
				//offset[0] = (k + Math.random()) / render.samples;
				//offset[1] = (l + Math.random()) / render.samples;
				// TODO: Objective 1: generate a ray (use the generateRay method)
				Ray ray = new Ray();
				double[] offset = {0.5, 0.5};           	
				generateRay(j, i, offset, cam, ray);
				IntersectResult result = new IntersectResult();  

				root.intersect(ray, result);

				Color3f shading = new Color3f();

				if(result.t == Double.POSITIVE_INFINITY){				
					shading.set(render.bgcolor);
				}else{	
					getPixelColor(root, ray, result, lights, ambient, shading);
				}

				int r = (int)(255*shading.x);
				int g = (int)(255*shading.y);
				int b = (int)(255*shading.z);

				if(r > 255){
					r = 255;
				}
				if(g > 255){
					g = 255;
				}
				if(b > 255){
					b = 255;
				}

				int a = 255;
				int argb = (a<<24 | r<<16 | g<<8 | b);    

				// update the render image
				//height-i-1
				render.setPixel(j, i, argb);
			}
		}

		// save the final render image
		render.save();

		// wait for render viewer to close
		render.waitDone();
	}

	/**
	 * Get the color of pixel.
	 * 
	 * @param i The pixel row.
	 * @param j The pixel column.
	 * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
	 * @param cam The camera.
	 * @param ray Contains the generated ray.
	 */
	public static void getPixelColor(final SceneNode root, final Ray ray, final IntersectResult result, final Map<String,Light> lights, final Color3f ambient, Color3f shading) {
		
		//Ambient Shading
		shading.x += ambient.x * result.material.diffuse.x;
		shading.y += ambient.y * result.material.diffuse.y;
		shading.z += ambient.z * result.material.diffuse.z;
		
		for(Light myLight: lights.values()){

			Ray shadowRay = new Ray();
			IntersectResult shadowResult = new IntersectResult();

			if(!inShadow(result, myLight, root, shadowResult, shadowRay)){
				//Vector from point to light source
				Vector3d l = new Vector3d(myLight.from);
				l.sub(result.p);
				l.normalize();

				//Lambertian Shading
				shading.x += (float)(myLight.color.x*myLight.power*result.material.diffuse.x*Math.max(0, result.n.dot(l)));
				shading.y += (float)(myLight.color.y*myLight.power*result.material.diffuse.y*Math.max(0, result.n.dot(l)));
				shading.z += (float)(myLight.color.z*myLight.power*result.material.diffuse.z*Math.max(0, result.n.dot(l)));

				//Bisector vector
				Vector3d h = new Vector3d(ray.viewDirection);
				h.negate();
				h.add(l);
				h.normalize();
				
				//Blinn-Phong Shading
				shading.x += (float)(myLight.power * result.material.specular.x * Math.pow(Math.max(0, h.dot(result.n)), result.material.hardness));
				shading.y += (float)(myLight.power * result.material.specular.y * Math.pow(Math.max(0, h.dot(result.n)), result.material.hardness));
				shading.z += (float)(myLight.power * result.material.specular.z * Math.pow(Math.max(0, h.dot(result.n)), result.material.hardness));
			}
		}
	}

	/**
	 * Generate a ray through pixel (i,j).
	 * 
	 * @param i The pixel row.
	 * @param j The pixel column.
	 * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
	 * @param cam The camera.
	 * @param ray Contains the generated ray.
	 */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray) {

		double width = cam.imageSize.width;
		double height = cam.imageSize.height;

		//Ray origin is e ( camera eyepoint)
		ray.eyePoint.set(cam.from);

		double d  = cam.from.distance(cam.to);

		double a = (width / height); //aspect ratio	
		double temp = Math.tan(Math.toRadians(cam.fovy)/2.0) * d;

		double left = -a*temp;
		double right = a*temp;
		double top = temp;
		double bottom = -temp;
		double u = left + (right - left) * (i+offset[0]) / width; 
		double v = bottom + (top - bottom) * (j+offset[1]) / height; 

		// Get camera coordinates/frame		
		Vector3d wCam = new Vector3d(cam.from);
		wCam.sub(cam.to);
		wCam.normalize();	

		Vector3d uCam = new Vector3d();
		uCam.cross(cam.up, wCam);
		uCam.normalize();

		Vector3d vCam = new Vector3d();
		vCam.cross(uCam, wCam);
		vCam.normalize();				

		uCam.scale(u);
		vCam.scale(v);
		wCam.scale(d);

		Vector3d s = new Vector3d(cam.from);
		s.add(uCam);
		s.add(vCam);
		s.sub(wCam);

		//d = s-e
		s.sub(cam.from);
		s.normalize();
		ray.viewDirection.set(s);
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(final IntersectResult result, final Light light, final SceneNode root, IntersectResult shadowResult, Ray shadowRay) {
		shadowRay.eyePoint = result.p;
		shadowRay.viewDirection.x = light.from.x - result.p.x;
		shadowRay.viewDirection.y = light.from.y - result.p.y;
		shadowRay.viewDirection.z = light.from.z - result.p.z;
		shadowRay.viewDirection.normalize();
		root.intersect(shadowRay, shadowResult);

		if(shadowResult.t != Double.POSITIVE_INFINITY ){
			if(shadowResult.p.distance(result.p) > 0.0001){
				return true;
			}
		}
		return false;
	}    
}