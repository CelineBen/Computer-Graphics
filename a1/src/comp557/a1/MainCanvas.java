package comp557.a1;

import java.text.DecimalFormat;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JButton;
import javax.vecmath.Matrix3d;

import mintools.viewer.EasyViewer;
import mintools.viewer.FancyAxis;
import mintools.viewer.FlatMatrix4d;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import comp557.a1.rotationControllers.RotationController;

/**
 * OpenGL frame containing the target orientation 
 * @author kry
 *
 */
public class MainCanvas implements GLEventListener {

	GLCanvas glCanvas;
	
    Timer timer = new Timer();

    double error;
    
    /** set by the main application when tabs are changed, setting the interaction type */
    RotationController rotationController;
    
    /** set by the main application so we can enable the next button when the error is under 10 degrees */
    JButton next;

    /** set by the main application so we can monitor our progress toward the target rotation */
    Matrix3d targetRotation;
	
	public MainCanvas() {
		GLProfile glp = GLProfile.getDefault();
        GLCapabilities glcap = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glcap );
        glCanvas.setSize( 300, 300 );
        glCanvas.addGLEventListener(this);
        final FPSAnimator animator; 
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
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
    	GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        ///////////////
        //gl.glMatrixMode( GL2.GL_PROJECTION );
        //double a = drawable.getWidth() / (double) drawable.getHeight();
        //gl.glLoadIdentity();
        //gl.glOrtho(-1.5*a, 1.5*a, -1.5, 1.5, -10, 10);
        //gl.glMatrixMode( GL2.GL_MODELVIEW );
        ////////////////
        
        setupProjectionAndModelview( gl, drawable.getWidth(), drawable.getHeight() );
        rotationController.applyTransformation(gl);
        fancyAxis.draw(gl);
        error = evaluate(gl);
        
        //////////////////
        gl.glDisable( GL2.GL_LIGHTING );
        gl.glColor4f( 1,1,1,0.5f );
        final GLUT glut = new GLUT();
        glut.glutWireSphere(1.5, 16, 14);
        gl.glEnable( GL2.GL_LIGHTING );
        ///////////////////
        
        
        EasyViewer.beginOverlay(drawable);

        if ( error < 10 ) {
        	timer.stop();
        	int w = drawable.getWidth();
        	int h = drawable.getHeight();
        	next.setEnabled(true);
        	gl.glColor4f( 0, 1, 0, 0.75f );
        	gl.glLineWidth( 32 );
        	gl.glBegin( GL2.GL_QUAD_STRIP );
        	double r1 = Math.min(w, h)/3;
        	double r2 = Math.min(w, h)/2.1;
        	for ( int i = 0; i <= 40; i++ ) {
        		double theta = i * Math.PI / 20;
        		gl.glVertex2d( w*0.5 + r1 * Math.cos(theta), h*.5 + r1*Math.sin(theta) );
        		gl.glVertex2d( w*0.5 + r2 * Math.cos(theta), h*.5 + r2*Math.sin(theta) );
        	}
        	gl.glEnd();
        	gl.glLineWidth( 2 );
        } else {
        	timer.start();
        }

        final DecimalFormat df = new DecimalFormat("0.0");
        String message = "";
        // Shouldn't show error... can be used as a gradient in tweaking rotations
        //message = "error = " + df.format( error ) + "\n";
        double elapsed = timer.getElapsed();
        message +=  df.format( elapsed ) + "\n";
        if ( elapsed > 60 ) {
        	gl.glColor3f(1,0,0);
        } else {
        	gl.glColor3f(1,1,1);	
        }
        EasyViewer.printTextLines( drawable, message, 10, 14, 12, GLUT.BITMAP_HELVETICA_10 );
        gl.glEnable( GL2.GL_LIGHTING );
        
        EasyViewer.endOverlay(drawable);

    }
    
    /**
     * Computes the error of the current transformation on the modelview matrix stack
     * @param gl
     * @return
     */
    public double evaluate( GL2 gl ) {
    	final FlatMatrix4d M = new FlatMatrix4d();
    	gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, M.asArray(), 0 );
    	M.reconstitute();
    	final Matrix3d R = new Matrix3d();
    	M.getBackingMatrix().getRotationScale( R );
    	final Matrix3d error = new Matrix3d();
    	error.mulTransposeLeft(R, targetRotation);
    	return Math.acos( (error.m00 + error.m11 + error.m22 - 1) / 2 ) * 180 / Math.PI;
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
