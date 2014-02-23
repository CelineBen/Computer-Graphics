package comp557.a5;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Color4f;

/**
 * A class defining the material properties of a surface, 
 * such as colour and specularity. 
 */
public class Material {
	
	/**
	 * All the materials.
	 */
	public static Map<String,Material> materials = new HashMap<String,Material>();
	
	/** 
	 * Material name. 
	 */
    public String name;
    
    /** 
     * Diffuse colour. 
     */
    public Color4f diffuse;
    
    /** 
     * Specular colour. 
     */
    public Color4f specular;
    
    /** 
     * Specular hardness. 
     */
    public float hardness;
 
    /**
     * Default constructor. Creates a material with:
     *    - white diffuse colour
     *    - no specular colour
     */
    public Material() {
    	this.name = "";
    	this.diffuse = new Color4f(1,1,1,1);
    	this.specular = new Color4f(0,0,0,0);
    	this.hardness = 0;
    }
    
}
