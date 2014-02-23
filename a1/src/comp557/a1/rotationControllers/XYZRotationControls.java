/*
 * Implementation of applyTransformation
 * CŽline Bensoussan
 * A1 - Rotations
 * September 20, 2013package comp557.a1.rotationControllers;
 */

package comp557.a1.rotationControllers;

import java.awt.Component;

import javax.media.opengl.GL2;
import javax.swing.JPanel;


import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;

public class XYZRotationControls implements RotationController {
    
	String name = "XYZ";
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void attach(Component component) {
		// do nothing, mouse interaction unused!		
	}
	
	DoubleParameter X = new DoubleParameter( "X", 0, -180, 180 );
	DoubleParameter Y = new DoubleParameter( "Y", 0, -180, 180 );
	DoubleParameter Z = new DoubleParameter( "Z", 0, -180, 180 );

	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
    	DoubleParameter.DEFAULT_SLIDER_LABEL_WIDTH = 30;
    	DoubleParameter.DEFAULT_SLIDER_TEXT_WIDTH = 80;
    	vfp.add( X.getSliderControls(false) );
    	vfp.add( Y.getSliderControls(false) );
    	vfp.add( Z.getSliderControls(false) );
    	return vfp.getPanel();
	}
	
	public void applyTransformation( GL2 gl ) {		
		//Points should be first transformed by the Z rotation, then the Y rotation, and finally the X rotation
		gl.glRotated(X.getFloatValue(), 1, 0, 0);	
		gl.glRotated(Y.getFloatValue(), 0, 1, 0);
		gl.glRotated(Z.getFloatValue(), 0, 0, 1);
	}

}
