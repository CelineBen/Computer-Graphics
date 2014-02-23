package comp557.a2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import mintools.swing.ControlFrame;
import mintools.viewer.TrackBallCamera;

import com.jogamp.opengl.util.FPSAnimator;

/**
 * Class for Assignment 2, provides a viewing interface and keyframing interface for
 * a scene graph constructed with DAGNodes.
 * @author kry
 */
public class A2App implements GLEventListener {

    /**
     * Creates a Basic GL Window and links it to a GLEventListener
     * @param args
     */
    public static void main(String[] args) {
    	new A2App();
    }

    /** Class from mintools for setting the camera and viewing parameters with the mouse. */
    private TrackBallCamera tbc = new TrackBallCamera();
 
    private KeyFramedScene keyFramedScene = new KeyFramedScene();

    /** Helper for recording images to the directory "stills" */
    private CanvasRecorder canvasRecorder = new CanvasRecorder();
          
    public A2App() {
        String windowName = "Assignment 2 - " + CharacterCreator.name;
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities glcap = new GLCapabilities(glp);
        // comment the next three lines if your computer is slow
        glcap.setHardwareAccelerated(true);
        glcap.setNumSamples(2);
        glcap.setSampleBuffers(true);
        GLCanvas glCanvas = new GLCanvas( glcap );
        final FPSAnimator animator; 
        animator = new FPSAnimator(glCanvas, 30);
        animator.start();
        ControlFrame controls = new ControlFrame("Controls", new Dimension( 600,600 ), new Point(680,0) );
        controls.add("Key Frame Controls", keyFramedScene.getControls() );
        controls.add("Canvas Recorder Controls", canvasRecorder.getControls() );
        controls.setVisible(true);    
        JFrame frame = new JFrame(windowName);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(glCanvas, BorderLayout.CENTER);
        glCanvas.setSize(640, 360); // half 720p resolution
        glCanvas.addGLEventListener( this);
        tbc.attach( glCanvas );
        try {
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.pack(); // want our frame to come out the right size!
            frame.setVisible(true);
            glCanvas.requestFocus(); // activates the Event Listeners
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        tbc.prepareForDisplay(drawable);
        keyFramedScene.display( drawable );
        canvasRecorder.snapshot(drawable);
    }
 
    /** 
     * initializes the canvas with some reasonable default settings
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1f); // Black Background
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

        gl.glMaterialfv( GL.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1,1,1,1}, 0 );
        gl.glMaterialfv( GL.GL_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[] {1,1,1,1}, 0 );
        gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, new float[] {1,1,1,1}, 0 );
        gl.glMaterialf( GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 50 );
        
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL2.GL_POINT_SMOOTH );
        gl.glEnable( GL2.GL_MULTISAMPLE );
    }
        
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
    	// do nothing
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// do nothing
    }
}