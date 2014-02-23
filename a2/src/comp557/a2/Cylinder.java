/**
 * @author CŽline Bensoussan
 * A2 - Transform Hierarchy for Animated Characters
 * October 4, 2013
 */

package comp557.a2;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class Cylinder extends DAGNode {

	private float[] color;	
	private double radius;
	private double height;

	private double translateX; 
	private double translateY; 
	private double translateZ;
	
	private double rotateX;
	private double rotateY;
	private double rotateZ;

	public Cylinder(String name, double radius, double height, 
			double translateX, double translateY, double translateZ,
			double rotateX, double rotateY, double rotateZ, float[] color){
		this.name = name;
		this.radius = radius;
		this.height = height;
		this.translateX = translateX;
		this.translateY = translateY;
		this.translateZ = translateZ;
		this.rotateX = rotateX;
		this.rotateY = rotateY;
		this.rotateZ = rotateZ;	
		this.color = color;
	}

	public void display( GLAutoDrawable drawable ) {		
		GL2 gl = drawable.getGL().getGL2();
		gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0 );
		gl.glPushMatrix();			
		gl.glTranslated(translateX, translateY, translateZ);
			gl.glPushMatrix();	
				gl.glRotated(rotateX,1,0,0);	
				gl.glRotated(rotateY,0,1,0);	
				gl.glRotated(rotateZ,0,0,1);	
				gl.glTranslated(0, 0, -height/2);
				glut.glutSolidCylinder(radius, height, 32, 2);
			gl.glPopMatrix();
			super.display(drawable);
		gl.glPopMatrix();
	}
}
