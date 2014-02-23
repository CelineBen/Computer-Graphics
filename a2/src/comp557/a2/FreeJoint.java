/**
 * @author CŽline Bensoussan
 * A2 - Transform Hierarchy for Animated Characters
 * October 4, 2013
 */

package comp557.a2;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;

public class FreeJoint extends DAGNode {
	private float[] color;
	private double radius;
	private double height;

	private DoubleParameter translateX; 
	private DoubleParameter translateY; 
	private DoubleParameter translateZ;

	private DoubleParameter rotateX;
	private DoubleParameter rotateY;
	private DoubleParameter rotateZ;

	public FreeJoint(String name, double radius, double height, float[] color){
		this.name = name;
		this.radius = radius;
		this.height = height;
		this.color = color;

		translateX = new DoubleParameter("translateX", 0, -30, 30);
		dofs.add(translateX);
		translateY = new DoubleParameter("translateY", 0, -5, 5);
		dofs.add(translateY);
		translateZ = new DoubleParameter("translateZ", -10, -20, 20);
		dofs.add(translateZ);

		rotateX = new DoubleParameter("rotateX", 0, -100, 100);
		dofs.add(rotateX);
		rotateY = new DoubleParameter("rotateY", 0, -50, 50);
		dofs.add(rotateY);
		rotateZ = new DoubleParameter("rotateZ", 0, -50, 50);
		dofs.add(rotateZ);
	}

	public void display( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0 );
		gl.glRotated(rotateX.getValue(), 1,0,0);
		gl.glRotated(rotateY.getValue(), 0,1,0);
		gl.glRotated(rotateZ.getValue(), 0,0,1);
		gl.glPushMatrix();
			gl.glTranslated(translateX.getValue(), translateY.getValue(), translateZ.getValue());
			gl.glPushMatrix();	
				gl.glRotated(90,1,0,0);	
				gl.glTranslated(0, 0, -height/2);
				glut.glutSolidCylinder(radius, height, 32, 2);
			gl.glPopMatrix();
			super.display(drawable);
		gl.glPopMatrix();
	}
}
