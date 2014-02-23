/*
 * Created on Feb 25, 2005
 * 
 * Implementation of setTrackballVector and mouseDragged
 * Céline Bensoussan
 * A1 - Rotations
 * September 20, 2013
 */

package comp557.a1.rotationControllers;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GL2;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4d;

/**
 * Implementation of a simple track ball
 * 
 * @author kry
 */
public class TrackBall implements MouseListener, MouseMotionListener, RotationController {
    
	String name = "TrackBall";
	
	@Override
	public String getName() {
		return name;
	}
	    
    /**
     * The computed angle of rotation will be multiplied by this value before
     * applying the rotation.  Values larger than one are useful for getting
     * useful amounts of rotation without requiring lots of mouse movement.
     */
    private DoubleParameter trackballGain = new DoubleParameter("gain", 1.2, 0.1, 5);
    
    /**
     * The fit parameter describes how big the ball is relative to the smallest 
     * screen dimension.  With a square window and fit of 2, the ball will fit
     * just touch the edges of the screen.  Values less than 2 will give a ball
     * larger than the window while smaller values will give a ball contained 
     * entirely inside. 
     */
    private DoubleParameter trackballFit = new DoubleParameter("fit", 2.0, 0.1, 5);
      
    /** The component (canvas) on which we are receiving mouse motion events */
    private Component trackingSource;
    
    /** 
     * previous track ball vector 
     */
    private Vector3f tbv0 = new Vector3f();

    /** 
     * current track ball vector 
     */
    private Vector3f tbv1 = new Vector3f();
            
    /**
     * Our current transformation 
     */
    public Matrix4d bakedTransformation = new Matrix4d();
    
    /**
     * A flat matrix for passing to opengl, backed by our transformation matrix
     */
    public FlatMatrix4d transformation = new FlatMatrix4d(bakedTransformation);
            
    /**
     * Create a new track ball with the default settings
     */
    public TrackBall() {
        bakedTransformation.setIdentity();
    }
    
    /**
     * Attach this track ball to the given component.
     * @param component 
     */
    public void attach(Component component) {
    	trackingSource = component;
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }
    
    /**
     * Set track ball vector v, given the mouse position and the window size.
     * @param point
     * @param v
     * @param vnp
     */
    private void setTrackballVector( Point point, Vector3f v ) { 
        int width = trackingSource.getWidth();
        int height = trackingSource.getHeight();
        
        /* 
         * To find the XYZ components of the track ball vector, I used the following
         * algorithm, found in the paper ARCBALL by Ken Shoemake:
         * pt.x ← (screen.x – center.x)/radius;
         * pt.y ← (screen.y – center.y)/radius;
         * r ← pt.x*pt.x + pt.y*pt.y;
         * IF r > 1.0
         * 		THEN s ← 1.0/Sqrt[r];
         * 		pt.x ← s*pt.x;
         *		pt.y ← s*pt.y;
         *		pt.z ← 0.0;
         * ELSE pt.z ← Sqrt[1.0 - r];
         */
        
        double centerX = width / 2;
        double centerY = height / 2; 	
        double radius = 0;
        
        // Radius = minimum screen dimension / fit
        if (width > height)
        	radius = height / trackballFit.getFloatValue();
        else
        	radius = width / trackballFit.getFloatValue();

        double compX = (point.getX() - centerX) / radius;
        double compY = (point.getY() - centerY) / radius * -1; // We change the sign because the y-axis with openGL points downwards
        double compZ = 0.0; // if the mouse point is not "on" the screen space ball, the z component is 0.
        
        double r = (compX*compX) + (compY*compY);
        
        if (r>1.0){
        	double s = 1 / Math.sqrt(r);
        	compX = s*compX;
        	compY = s*compY;	
        }
        else{
        	compZ = Math.sqrt(1.0 - r);
        }
        
        v.set((float)compX, (float)compY, (float)compZ);
        }    

    public void mousePressed(MouseEvent e) {
        setTrackballVector( e.getPoint(), tbv1 );
        tbv0.set( tbv1 );
    }
    
    public void mouseDragged(MouseEvent e) {
        setTrackballVector( e.getPoint(), tbv1 );
             
        //The axis is the direction of the cross product
        Vector3f axis = new Vector3f(); 
        axis.cross(tbv0, tbv1);
        
        //The angle is the angle between the two vectors
        //and scale the angle computed by the trackballGain value
        double angle = tbv0.angle(tbv1) * trackballGain.getFloatValue();
        
        //Use an AxisAngle4d to specify the rotation
        float[] t = new float[3];
        axis.get(t);       
        Vector3d v = new Vector3d((double)t[0], (double)t[1], (double)t[2]);           
        AxisAngle4d rotation = new AxisAngle4d(v, angle);
        
        //Set the rotation matrix
        Matrix4d matrixRotation = new Matrix4d();
        matrixRotation.set(rotation);      
        
        //Apply transformation
        matrixRotation.mul(bakedTransformation);
        bakedTransformation.set(matrixRotation);
        
        //Copy current vector to previous 
        tbv0.set( tbv1 );
    }

    public void mouseReleased(MouseEvent e) {
        // re normalize and orthgonalize
        Matrix3d m = new Matrix3d();        
        bakedTransformation.getRotationScale( m );
        m.normalizeCP();
        bakedTransformation.setRotationScale( m );
    }

    protected JPanel controlPanel;

    /**
     * Gets the controls for the track ball 
     * @return controls
     */
    public JPanel getControls() {
        if ( controlPanel != null ) return controlPanel;
        VerticalFlowPanel panel = new VerticalFlowPanel();
        panel.add( new JLabel("TrackBall Settings") );
        panel.add(trackballFit.getSliderControls(false));
        panel.add(trackballGain.getSliderControls(true));
        controlPanel = panel.getPanel();
        return controlPanel;        
    }

    public void mouseClicked(MouseEvent e) { /* do nothing */ }
    public void mouseEntered(MouseEvent e) { /* do nothing */ }
    public void mouseExited(MouseEvent e) { /* do nothing */ }
    public void mouseMoved(MouseEvent e) { /* do nothing */ }
            
    /**
     * Applies the transformation to the current matrix stack
     * @param gl
     */
    public void applyTransformation(GL2 gl) {
        gl.glMultMatrixd( transformation.asArray(), 0 );        
    }
 
}