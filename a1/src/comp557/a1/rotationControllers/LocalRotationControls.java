package comp557.a1.rotationControllers;

import java.awt.Component;

import javax.media.opengl.GL2;
import javax.swing.JPanel;
import javax.vecmath.Matrix3d;


import mintools.parameters.DoubleParameter;
import mintools.parameters.Parameter;
import mintools.parameters.ParameterListener;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4d;

public class LocalRotationControls implements RotationController {

	String name = "Local/Right";
	
	public LocalRotationControls() {
		M.getBackingMatrix().setIdentity();
		R.setIdentity();
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void attach(Component component) {
		// do nothing, mouse interaction unused!		
	}
	
	/** The accumulated rotation */
	Matrix3d R = new Matrix3d();
	
	/** a helper class for sending the R matrix to OpenGL */
	FlatMatrix4d M = new FlatMatrix4d();

	DoubleParameter X = new DoubleParameter( "X", 0, -180, 180 );
	DoubleParameter Y = new DoubleParameter( "Y", 0, -180, 180 );
	DoubleParameter Z = new DoubleParameter( "Z", 0, -180, 180 );
	
	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();

		vfp.add( X.getSliderControls(false) );
		vfp.add( Y.getSliderControls(false) );
		vfp.add( Z.getSliderControls(false) );
		
		X.addParameterListener(new ParameterListener<Double>() {
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				bake(0);
			}
		});
		Y.addParameterListener(new ParameterListener<Double>() {
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				bake(1);
			}
		});
		Z.addParameterListener(new ParameterListener<Double>() {
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				bake(2);
			}
		});

		return vfp.getPanel();
	}

	/**
	 * Bakes the previously set rotation into the matrix M.
	 * The parameter specifies which rotation is currently being modified
	 * to signal that the others should have their rotation lumped into 
	 * M and zeroed if not already zero.
	 */
	private void bake( int which ) {
		final Matrix3d T = new Matrix3d();

		if ( which != 0 && X.getValue() != 0 ) {
			T.rotX( X.getValue() * Math.PI / 180 );
			R.mul(T);
			X.setValue(0);
		}
		if ( which != 1 && Y.getValue() != 0 ) {
			T.rotY( Y.getValue() * Math.PI / 180 );
			R.mul(T);
			Y.setValue(0);
		}
		if ( which != 2 && Z.getValue() != 0 ) {
			T.rotZ( Z.getValue() * Math.PI / 180 );
			R.mul(T);
			Z.setValue(0);
		}
		M.getBackingMatrix().set(R);
	}
	
	public void applyTransformation( GL2 gl ) {
		gl.glMultMatrixd( M.asArray(), 0 );
		gl.glRotated( X.getValue(), 1, 0, 0 );
		gl.glRotated( Y.getValue(), 0, 1, 0 );
		gl.glRotated( Z.getValue(), 0, 0, 1 );
	}
}
