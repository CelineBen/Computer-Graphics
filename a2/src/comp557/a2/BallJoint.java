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

public class BallJoint extends DAGNode{
		private float[] color;
		private double size;

		private double scaleX;
		private double scaleY;
		private double scaleZ;

		private double translateX; 
		private double translateY; 
		private double translateZ;
		
		private DoubleParameter rotateX;
		private DoubleParameter rotateY;
		private DoubleParameter rotateZ;
		
		public BallJoint(String name, double size, 
				double scaleX, double scaleY, double scaleZ, 
				double translateX, double translateY, double translateZ, float[] color,
				double minX, double maxX, double minY, double maxY, double minZ, double maxZ){
			this.name = name;
			this.size = size;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.scaleZ = scaleZ;
			this.translateX = translateX;
			this.translateY = translateY;
			this.translateZ = translateZ;
			this.color = color;
			
			rotateX = new DoubleParameter("rotateX", 0, minX, maxX);
			dofs.add(rotateX);
			rotateY = new DoubleParameter("rotateY", 0, minY, maxY);
			dofs.add(rotateY);
			rotateZ = new DoubleParameter("rotateZ", 0, minZ, maxZ);
			dofs.add(rotateZ);
		}

		public void display( GLAutoDrawable drawable ) {
			
			GL2 gl = drawable.getGL().getGL2();
			gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, color, 0 );
			gl.glPushMatrix();
				gl.glTranslated(translateX, translateY, translateZ);
				gl.glRotated(rotateX.getValue(), 1,0,0);
				gl.glRotated(rotateY.getValue(), 0,1,0);
				gl.glRotated(rotateZ.getValue(), 0,0,1);				
				gl.glPushMatrix();	
					gl.glScaled(scaleX, scaleY, scaleZ);				
					glut.glutSolidSphere( size, 32, 32 );
				gl.glPopMatrix();
				super.display(drawable);			
			gl.glPopMatrix();
		}
}
