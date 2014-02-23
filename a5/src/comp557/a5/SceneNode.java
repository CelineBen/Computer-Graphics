/**
 * @author CŽline Bensoussan
 * Assignment 5 - Ray Tracing
 * November 29, 2013
 */
package comp557.a5;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;

/**
 * The scene is constructed from a hierarchy of nodes, where each node
 *  contains a transform, a material definition, some amount of geometry, 
 *  and some number of children nodes.  Each node has a unique name so that
 *  it can be instanced elsewhere in the hierarchy (provided it does not 
 *  make loops. 
 * 
 * Note that if the material for a scene node is non-null, it should override
 *  the material of any child.
 * 
 */
public class SceneNode extends Intersectable {

	/**
	 * Static map for accessing scene nodes by name, to perform instancing.
	 */
	public static Map<String,SceneNode> nodeMap = new HashMap<String,SceneNode>();

	public String name;

	/** Matrix transform for this node. */
	public Matrix4d M;

	/** Inverse matrix transform for this node. */
	public Matrix4d Minv;

	/** Child nodes. */
	public List<Intersectable> children;

	/**
	 * Default constructor.
	 * Note that all nodes must have a unique name, so that they can used as an instance later on.
	 */
	public SceneNode() {
		super();
		this.name = "";
		this.M = new Matrix4d();
		this.Minv = new Matrix4d();
		this.children = new LinkedList<Intersectable>();

	}

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		Minv.transform(ray.eyePoint);
		Minv.transform(ray.viewDirection);
		
		for(Intersectable child: children){	
			IntersectResult closer = new IntersectResult();
			
			child.intersect(ray, closer);
			
			Matrix4d T = new Matrix4d();			
			T.transpose(Minv);
			T.transform(closer.n);
			
			//We only care about this intersection point if it is the closest from light source
			if(closer.t < result.t){
				result.t = closer.t;
				result.material = closer.material;
				M.transform(closer.p);
				result.p = closer.p;
				result.n = closer.n;	
			}		
		}
		if(this.material != null){
			result.material = this.material;
		}
		result.n.normalize();
		
		M.transform(ray.eyePoint);
		M.transform(ray.viewDirection);	
	}
	

}
