/*
 * Created on Feb 25, 2005
 * 
 * Implementation of mouseDragged
 * CŽline Bensoussan
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

import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4d;

/**
 * Implementation of a simple XY ball rotation interface
 * 
 * @author kry
 */
public class XYBall implements MouseListener, MouseMotionListener, RotationController {
    
	String name = "XYBall";
	
	@Override
	public String getName() {
		return name;
	}
	    
    /**
     * The mouse motion in pixels will be multiplied by this value before
     * applying a rotation in radians.  
     */
    private DoubleParameter gain = new DoubleParameter("gain", 0.01, 0.005, 0.1);
    
    /**
     * Our current transformation 
     */
    public Matrix4d bakedTransformation = new Matrix4d();
    
    /**
     * A flat matrix for passing to OpenGL, backed by our transformation matrix.
     * (see asArray and reconstitute methods of FlatMatrix)
     */
    public FlatMatrix4d transformation = new FlatMatrix4d(bakedTransformation);
            
    /**
     * Create a new XYBall with the default settings
     */
    public XYBall() {
        bakedTransformation.setIdentity();
    }
    
    /**
     * Attach this XYBall to the given component
     * @param component 
     */
    public void attach(Component component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }
    
    /** previous mouse position */
    Point previousPosition = new Point();
        
    public void mousePressed(MouseEvent e) {
    	previousPosition.setLocation( e.getPoint() );
    }
    
    public void mouseDragged(MouseEvent e) {
    	
    	// X-coordinate previous position
        double x1 = previousPosition.getX();
        // Y-coordinate previous position
        double y1 = previousPosition.getY();
        // X-coordinate new position
        double x2 = e.getPoint().getX();
        // Y-coordinate new position
        double y2 = e.getPoint().getY();
        
        //Rotation matrix
        Matrix4d R = new Matrix4d();
        
        //Calculation of the angle in radians
        double angleY = Math.abs(x2-x1) * gain.getFloatValue();   	
    	if(x1 > x2) // if mouse goes from right to left, change the sign of the angle
    		angleY = angleY * -1;
    	
    	//Set the rotation matrix about the y-axis
    	R.rotY(angleY);
    	
    	//Apply transformation about the y-axis
    	R.mul(bakedTransformation);
    	bakedTransformation.set(R);
        
    	//Calculation of the angle in radians
    	double angleX = Math.abs(y2-y1) * gain.getFloatValue();
    	if(y1 > y2) // change sign of the angle when mouse going in opposite direction
    		angleX = angleX * -1;
    	
    	//Set the rotation matrix about the x-axis
    	R.rotX(angleX);
    	
    	//Apply transformation about the x-axis
    	R.mul(bakedTransformation);
    	bakedTransformation.set(R);
    	               	
    	//Set previous position to current position
    	previousPosition.setLocation( e.getPoint() );
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
     * Gets the controls for the XY ball
     * @return controls
     */
    public JPanel getControls() {
        if ( controlPanel != null ) return controlPanel;
        VerticalFlowPanel panel = new VerticalFlowPanel();
        panel.add( new JLabel("XYBall Settings") );
        panel.add(gain.getSliderControls(true));
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
        gl.glMultMatrixd(transformation.asArray(), 0 );        
    }
 
}