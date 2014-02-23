package comp557.a1;

import java.util.Random;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.viewer.EasyViewer;
import mintools.viewer.FancyAxis;
import mintools.viewer.FlatMatrix4d;

/**
 * OpenGL frame containing the target orientation 
 * @author kry
 *
 */
public class TargetCanvas implements GLEventListener {

	GLCanvas glCanvas;
	
    Matrix3d targetRotation = new Matrix3d();
	
	public TargetCanvas() {
		GLProfile glp = GLProfile.getDefault();
        GLCapabilities glcap = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glcap );
        glCanvas.setSize( 300, 300 );
        glCanvas.addGLEventListener(this);
        final FPSAnimator animator; 
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
        setNewTargetRotation();        
	}
	
	public void setNewTargetRotation() {
    	// quick and dirty rejection sampling based uniform orientation
    	final Random rand = new Random();
    	Quat4d q = new Quat4d();
    	do {
    		q.x = rand.nextDouble()*2-1;
    		q.y = rand.nextDouble()*2-1;
    		q.z = rand.nextDouble()*2-1;
    		q.w = rand.nextDouble()*2-1;
    	} while ( q.x*q.x + q.y*q.y + q.z*q.z + q.w*q.w > 1 );
    	q.normalize();
    	targetRotation.set(q);
    }

    public void setupProjectionAndModelview( GL2 gl, int w, int h ) {
        gl.glMatrixMode( GL2.GL_PROJECTION );
        double a = w / (double) h;
        double n = 1;
        double f = 100;
        double fovy = 45;
        double s = n * Math.tan( fovy/2 ); // scale is for fovy
        gl.glLoadIdentity();
        gl.glFrustum(-a*s, a*s, -1*s, 1*s, n, f );
        gl.glMatrixMode( GL2.GL_MODELVIEW );
        gl.glLoadIdentity();
        gl.glTranslated( 0, 0, -3 ) ;
    }
    
    /** 
     * initializes the class for display 
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0f); // Black Background
        gl.glClearDepth(1.0f); // Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Testing To Do
        gl.glEnable( GL2.GL_NORMALIZE ); // normals stay normal length under scale
        
        // setup lights and default material
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();        
        gl.glEnable( GL2.GL_LIGHTING );
        gl.glEnable( GL2.GL_LIGHT0 );
        // WATCH OUT: need to provide homogeneous coordinates to many calls!! 
        gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_POSITION, new float[] {10,10,10, 1}, 0 );
        gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[] {0,0,0,1}, 0);
        gl.glLightModelfv( GL2.GL_LIGHT_MODEL_AMBIENT, new float[] {0.1f,0.1f,0.1f,1}, 0);
        // default material properties
        gl.glMaterialfv( GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1,1,1,1}, 0 );
        gl.glMaterialfv( GL.GL_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1,1,1,1}, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {1,1,1,1}, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50 );
        // default blending properties for some simple anti-aliasing
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL2.GL_POINT_SMOOTH );
        gl.glEnable( GL2.GL_MULTISAMPLE );
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
    	final FancyAxis fancyAxis = new FancyAxis();
    	final FlatMatrix4d M = new FlatMatrix4d();
    	GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        setupProjectionAndModelview( gl, drawable.getWidth(), drawable.getHeight() );
    	M.getBackingMatrix().setIdentity();
    	M.getBackingMatrix().setRotation(targetRotation);
    	gl.glPushMatrix();
    	gl.glMultMatrixd( M.asArray(), 0 );
    	fancyAxis.draw(gl);
    	gl.glPopMatrix();
    	
    	EasyViewer.beginOverlay(drawable);
    	gl.glColor3f(1,1,1);
    	EasyViewer.printTextLines(drawable, "TARGET", 10, 14, 12, GLUT.BITMAP_HELVETICA_10 ); 
    	EasyViewer.endOverlay(drawable);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// do nothing
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    	// do nothing
    }
	
}
