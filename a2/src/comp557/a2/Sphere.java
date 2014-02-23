/**
 * @author CŽline Bensoussan
 * A2 - Transform Hierarchy for Animated Characters
 * October 4, 2013
 */

package comp557.a2;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class Sphere extends DAGNode {

	private float[] color; 	
	private double size;
	
	private double scaleX;
	private double scaleY;
	private double scaleZ;

	private double translateX; 
	private double translateY; 
	private double translateZ;
	
	public Sphere(String name, double size,  
			double scaleX, double scaleY, double scaleZ,
			double translateX, double translateY, double translateZ, float[] color){
		this.name = name;
		this.size = size;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
		this.translateX = translateX;
		this.translateY = translateY;
		this.translateZ = translateZ;
		this.color = color;
	}

	public void display( GLAutoDrawable drawable ) {		
		GL2 gl = drawable.getGL().getGL2();
		gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0 );
		gl.glPushMatrix();			
		gl.glTranslated(translateX, translateY, translateZ);
			gl.glPushMatrix();		
				gl.glScaled(scaleX, scaleY, scaleZ);				
				glut.glutSolidSphere( size, 32, 32 );
			gl.glPopMatrix();
			super.display(drawable);
		gl.glPopMatrix();
	}
}