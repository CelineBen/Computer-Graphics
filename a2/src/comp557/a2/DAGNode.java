package comp557.a2;

import java.util.Collection;
import java.util.LinkedList;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.swing.CollapsiblePanel;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FancyAxis;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Base class for scene graph nodes.  
 * You should not need to edit this class, but you should be aware of 
 * its methods and members.
 * @author kry
 */
public abstract class DAGNode {
    
	String name = "";
	
    LinkedList<DAGNode> children = new LinkedList<DAGNode>();

    Collection<DoubleParameter> dofs = new LinkedList<DoubleParameter>();
    
    static final BooleanParameter debugFrames = new BooleanParameter( "debug frames", false );
        
    /**
     * This static GLUT instance is included here for your convenience 
     * in case you wish to call glutWireCube, glutSolidSphere, etc.
     * Feel free to access this glut instance in a static manner from 
     * any of your other classes!
     */
    static final public GLUT glut = new GLUT();
    
    static final public FancyAxis fa = new FancyAxis();

    public static void drawLabel( GL2 gl, String msg ) {
    	gl.glDisable( GL2.GL_LIGHTING );
    	gl.glColor4f(1,1,1,1);
    	gl.glRasterPos3f( .3f,.3f,.3f );    	
    	glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, msg );
    	gl.glEnable( GL2.GL_LIGHTING );
    }
    
    public void add( DAGNode n ) {
    	children.add( n );
    }
    
    /**
     * Recursively creates the controls for the DOFs of the nodes.
     * Note that if instancing occurs then the controls will appear
     * multiple times in the returned JPanel.
     * @return
     */
    public JPanel getControls() {
    	if ( dofs.isEmpty() && children.isEmpty() ) return null;
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
    	vfp.setBorder( new TitledBorder(name) );
    	for ( DoubleParameter p : dofs ) {
    		vfp.add( p.getSliderControls(false) );
    	}
    	for ( DAGNode n : children ) {
    		JPanel p = n.getControls();
    		if ( p != null ) {
    			vfp.add( p );
    		}
    	}
    	CollapsiblePanel cp = new CollapsiblePanel( vfp.getPanel() );
    	return cp;
    }
    
    /**
     * Recursively collects all the DOFs for use in creating key poses.
     * @param dofs
     */
    public void getDOFs( Collection<DoubleParameter> dofs ) {
    	dofs.addAll( this.dofs );
    	for ( DAGNode n : children ) {
			n.getDOFs(dofs);
		}
    }
    
    /**
     * Draws the node and all its children.  
     * 
     * You will need to override this method, and you will likewise want to call 
     * super.display(drawable) at some point in your implementation!
     * 
     * Note that we do not pass the transform to this method as was seen in the class notes
     * because the transform is stored on the OpenGL modelview matrix stack.  Instead we 
     * pass the OpenGL drawable as the context for drawing.
     * 
     * @param drawable 
     */
    public void display( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();
    	if ( debugFrames.getValue() ) {
    		fa.draw(gl);
    		drawLabel(gl, name);
    	}    	
		for ( DAGNode n : children ) {
			n.display(drawable);
		}
    }
    
}
