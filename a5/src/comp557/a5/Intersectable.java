package comp557.a5;

/**
 * Abstract class for an intersect-able surface 
 */
public abstract class Intersectable {
	
	/**
	 * Material for this surface.
	 */
	public Material material;
	
	/** 
	 * Default constructor. Creates a surface with default material.
	 */
	public Intersectable() {
		this.material = new Material();
	}
	
	/**
	 * Test for intersection between a ray and this surface. This is an abstract
	 *   method and must be overridden for each surface type.
	 * @param ray
	 * @param result
	 */
    public abstract void intersect(Ray ray, IntersectResult result);
    
}
