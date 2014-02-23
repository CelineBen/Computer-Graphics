package comp557.a1.rotationControllers;

import java.awt.Component;

import javax.media.opengl.GL2;
import javax.swing.JPanel;

/**
 * Interface for rotation controllers.
 * Controllers must have a name, can optionally attach mouse listeners to the 
 * canvas to use mouse motion to set rotation, must have a control panel, and
 * implement a method that applies a rotation to the openGL modelview matrix stack.
 * @author kry
 *
 */
public interface RotationController {

	public String getName();
    public void attach(Component component);
	public JPanel getControls();
	public void applyTransformation( GL2 gl );
	
}
