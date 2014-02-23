package comp557.a3;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point2d;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.ControlFrame;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4f;
import mintools.viewer.Interactor;
import mintools.viewer.TrackBallCamera;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Assignment 3 - depth of field blur, and anaglyphys
 * 
 * For additional information, see the following paper, which covers
 * more on quality rendering, but does not cover anaglyphs.
 * 
 * The Accumulation Buffer: Hardware Support for High-Quality Rendering
 * Paul Haeberli and Kurt Akeley
 * SIGGRAPH 1990
 * 
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
 * GPU Gems [2007] has a slightly more recent survey of techniques.
 *
 * @author Celine Bensoussan
 * October, 18th 2013
 */
public class A3App implements GLEventListener, Interactor {

	/** TODO: Put your name in the window title */
	private String name = "Comp 557 Assignment 3 - CELINE BENSOUSSAN";
	
    /** Viewing mode as specified in the assignment */
    int viewingMode = 1;
    
    FastPoissonDisk fpd = new FastPoissonDisk();
    
    // Note that limits are set to try to prevents you from choosing bad values (e.g., a near plane behind the eye,
    // or near plane beyond far plane).  But feel free to change the limits and experiment.
    // Also note that these quantities give the z coordinate in the world coordinate system!
    
    private DoubleParameter eyeZPosition = new DoubleParameter( "eye z position in world", 0.5, 0.25, 3 ); 
    private DoubleParameter near = new DoubleParameter( "near z position in world", 0.25, -0.2, 0.5 ); 
    private DoubleParameter far  = new DoubleParameter( "far z position in world", -0.5, -2, -0.25 ); 
    private DoubleParameter focalPlaneZPosition = new DoubleParameter( "focal z position in world", 0, -1.5, 0.4 );     

    /** samples and aperture size are for drawing depth of field blur */    
    private IntParameter samples = new IntParameter( "samples", 2, 1, 100 );   
    
    /** In the human eye, pupil diameter ranges between approximately 2 and 8 mm */
    private DoubleParameter aperture = new DoubleParameter( "aperture size", 0.003, 0, 0.01 );
    
    /** x eye offsets for testing (see objective 4) */         
    private DoubleParameter eyeXOffset = new DoubleParameter("eye offset in x", 0.0, -0.3, 0.3);
    /** y eye offsets for testing (see objective 4) */
    private DoubleParameter eyeYOffset = new DoubleParameter("eye offset in y", 0.0, -0.3, 0.3);
    
    private BooleanParameter drawCenterEyeFrustum = new BooleanParameter( "Draw center eye frustum", true ); 
    private BooleanParameter drawEyeOffsetFrustum = new BooleanParameter( "Draw eye offset frustum", true );
    private BooleanParameter drawEyeFrustums = new BooleanParameter( "Draw left and right eye frustums", true );
    private BooleanParameter drawEyeFrustumsBonus = new BooleanParameter( "Draw left and right eye offset frustums (BONUS)", false );
    
	/**
	 * The eye disparity should be constant, but can be adjusted to test the
	 * creation of left and right eye frustums or likewise, can be adjusted for
	 * your own eyes!! Note that 63 mm is a good inter occular distance for the
	 * average human, but you may likewise want to lower this to reduce the
	 * depth effect (images may be hard to fuse with cheap 3D colour filter
	 * glasses). Setting the disparity negative should help you check if you
	 * have your left and right eyes reversed!
	 */
    private DoubleParameter eyeDisparity = new DoubleParameter("eye disparity", 0.063, -0.1, 0.1 );

    private GLUT glut = new GLUT();
    
    private Scene scene = new Scene();

    /**
     * Launches the application
     * @param args
     */
    public static void main(String[] args) {
        new A3App();
    }
    
    GLCanvas glCanvas;
    
    /** Main trackball for viewing the world and the two eye frustums */
    TrackBallCamera tbc = new TrackBallCamera();
    /** Second trackball for rotating the scene */
    TrackBallCamera tbc2 = new TrackBallCamera();
    
    /**
     * Creates the application
     */
    public A3App() {      
        Dimension controlSize = new Dimension(640, 640);
        Dimension size = new Dimension(640, 480);
        ControlFrame controlFrame = new ControlFrame("Controls");
        controlFrame.add("Camera", tbc.getControls());
        controlFrame.add("Scene TrackBall", tbc2.getControls());
        controlFrame.add("Scene", getControls());
        controlFrame.setSelectedTab("Scene");
        controlFrame.setSize(controlSize.width, controlSize.height);
        controlFrame.setLocation(size.width + 20, 0);
        controlFrame.setVisible(true);    
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities glc = new GLCapabilities(glp);
        System.out.println( "R bits = " + glc.getAccumRedBits() );
        System.out.println( "G bits = " + glc.getAccumGreenBits() );
        System.out.println( "B bits = " + glc.getAccumBlueBits() );
        System.out.println( "A bits = " + glc.getAccumAlphaBits() );
        glc.setAccumRedBits(8);
        glc.setAccumGreenBits(8);
        glc.setAccumBlueBits(8);
        glc.setAccumAlphaBits(8);
        glCanvas = new GLCanvas( glc );
        glCanvas.setSize( size.width, size.height );
        glCanvas.setIgnoreRepaint( true );
        glCanvas.addGLEventListener( this );
        glCanvas.requestFocus();
        FPSAnimator animator = new FPSAnimator( glCanvas, 60 );
        animator.start();        
        tbc.attach( glCanvas );
        tbc2.attach( glCanvas );
        // initially disable second trackball, and improve default parameters given our intended use
        tbc2.enable(false);
        tbc2.setFocalDistance( 0 );
        tbc2.panRate.setValue(5e-5);
        tbc2.advanceRate.setValue(0.005);
        this.attach( glCanvas );        
        JFrame frame = new JFrame( name );
        frame.getContentPane().setLayout( new BorderLayout() );
        frame.getContentPane().add( glCanvas, BorderLayout.CENTER );
        frame.setLocation(0,0);        
        frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible( true );        
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// nothing to do
    }
        
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // do nothing
    }
    
    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_7) {
                    viewingMode = e.getKeyCode() - KeyEvent.VK_1 + 1;
                }
                if ( viewingMode == 1 ) {
                	tbc.enable(true);
                	tbc2.enable(false);
	            } else {
                	tbc.enable(false);
                	tbc2.enable(true);
	            }
            }
        });
    }
    
    /**
     * @return a control panel
     */
    public JPanel getControls() {     
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( eyeZPosition.getSliderControls(false));        
        vfp.add (drawCenterEyeFrustum.getControls() );
        vfp.add (drawEyeOffsetFrustum.getControls() );       
        vfp.add( near.getSliderControls(false));
        vfp.add( far.getSliderControls(false));        
        vfp.add( focalPlaneZPosition.getSliderControls(false));        
        vfp.add( eyeXOffset.getSliderControls(false ) );
        vfp.add( eyeYOffset.getSliderControls(false ) );        
        vfp.add ( aperture.getSliderControls(false) );
        vfp.add ( samples.getSliderControls() );        
        vfp.add( eyeDisparity.getSliderControls(false) );        
        vfp.add ( drawEyeFrustums.getControls() ); 
        vfp.add ( drawEyeFrustumsBonus.getControls() );         
        VerticalFlowPanel vfp2 = new VerticalFlowPanel();
        vfp2.setBorder( new TitledBorder("Scene size and position" ));
        vfp2.add( scene.getControls() );
        vfp.add( vfp2.getPanel() );        
        return vfp.getPanel();
    }
             
    public void init( GLAutoDrawable drawable ) {
    	drawable.setGL( new DebugGL2( drawable.getGL().getGL2() ) );
        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_NORMALIZE );
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do 
        gl.glLineWidth( 2 );                        // slightly fatter lines by default!
    }   

	//Screen resolution
	double screenWidthPixels = 1280;
	double screenWidthMeters = 0.3;
	double metersPerPixel = screenWidthMeters / screenWidthPixels;
    
    @Override
    public void display(GLAutoDrawable drawable) {        
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);            

        //I divided the height and width by 2 so the code is easier to read
        double w = drawable.getWidth() * metersPerPixel/2;
        double h = drawable.getHeight() * metersPerPixel/2;
        
        //Declare all variables used in different viewing modes
        FlatMatrix4f m = new FlatMatrix4f();
        double left, right, bottom, top;
        double fpRight, fpLeft, fpTop, fpBottom;
        double zNear = eyeZPosition.getValue() - near.getValue();
		double zFar = eyeZPosition.getValue() - far.getValue();
        double disparity = (eyeDisparity.getValue()/2);
    	double fp = eyeZPosition.getValue() - focalPlaneZPosition.getValue(); //Distance of focal plane w.r.t eye           	
    	double x = eyeXOffset.getValue();
		double y = eyeYOffset.getValue();
		
		//Draw with thin lines
        gl.glLineWidth((float)0.5);

        if ( viewingMode == 1 ) {
        	// We will use a trackball camera, but also apply an 
        	// arbitrary scale to make the scene and frustums a bit easier to see
            tbc.prepareForDisplay(drawable);
            gl.glScaled(15,15,15);        
            
            gl.glPushMatrix();
            	tbc2.applyViewTransformation(drawable); // only the view transformation
            	scene.display( drawable );
            gl.glPopMatrix();  
           
            gl.glDisable(GL2.GL_LIGHTING);         
            gl.glColor3f(1.0f, 1.0f, 1.0f); //WHITE
            
            //QUESTION 1 - White rectangle in the XY plane centered at the origin 
            gl.glBegin(GL.GL_LINE_LOOP); //start drawing a line loop
            	gl.glVertex3d(-w, -h, 0.0f); //bottom-left of window
            	gl.glVertex3d(-w, h, 0.0f); //up-left of window
            	gl.glVertex3d(w, h, 0.0f); //up-right of window
            	gl.glVertex3d(w, -h, 0.0f); //bottom-top of window
            gl.glEnd();
            
            //QUESTION 3 - Focal Plane Rectangle       	           	
        	fpRight = w * fp / eyeZPosition.getValue();      	
        	fpLeft = -fpRight;
        	fpTop = h * fp / eyeZPosition.getValue();
        	fpBottom = -fpTop;
        	           	
        	gl.glPushMatrix();          		
        		gl.glEnable(GL.GL_BLEND); 
        		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);           		
        		gl.glTranslated(0, 0, focalPlaneZPosition.getValue());
        		gl.glColor3f(0.4f, 0.4f, 0.4f);
        		gl.glRectd(fpLeft, fpTop, fpRight, fpBottom);
        		gl.glDisable (GL.GL_BLEND);
        	gl.glPopMatrix();
                    	
        	gl.glEnable(GL2.GL_LIGHTING);
            
            //OBJECTIVE 2 - Draw camera frustum if drawCenterEyeFrustum is true
            if(drawCenterEyeFrustum.getValue()){         	
            	gl.glDisable(GL2.GL_LIGHTING);
            	gl.glColor3f(1.0f, 1.0f, 1.0f);
            	gl.glPushMatrix();
             		gl.glTranslated(x, y, eyeZPosition.getValue());
             		glut.glutSolidSphere(0.0125, 32, 32);
             		
             		//Keeps screen fixed
             		left = (-w-x) * zNear / eyeZPosition.getValue();
             		right = (w-x) * zNear / eyeZPosition.getValue(); 
             		top = (h-y) * zNear / eyeZPosition.getValue();
             		bottom = (-h-y) * zNear / eyeZPosition.getValue();
             		
             		gl.glMatrixMode(GL2.GL_PROJECTION);
             		gl.glPushMatrix();
            	 		gl.glLoadIdentity();
            	 		gl.glFrustum(left, right, bottom, top, zNear, zFar);
            	 		gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.asArray(), 0);         		
            		gl.glPopMatrix();
            		
            		gl.glMatrixMode(GL2.GL_MODELVIEW);
            		
            		m.reconstitute();
            		m.getBackingMatrix().invert();

            		gl.glMultMatrixf(m.asArray(), 0 ); // Applies the transformation to the current matrix stack 
            		glut.glutWireCube(2); // Draw frustum
            	gl.glPopMatrix();
            	gl.glEnable(GL2.GL_LIGHTING);
            }
            	
            //OBJECTIVE 4 - Eye Position Offset frustum if EyeOffsetFrustum is true
            if(drawEyeOffsetFrustum.getValue()){                	
            	gl.glDisable(GL2.GL_LIGHTING); 
            	gl.glColor3f(1.0f, 1.0f, 1.0f); //WHITE
            	gl.glPushMatrix();      		
        			gl.glTranslated(x, y, eyeZPosition.getValue());
        			glut.glutSolidSphere(0.0125, 32, 32);
         	          	
        			
        			left = (fpLeft-x) * zNear / fp; 
        			right = (fpRight-x) * zNear / fp;
        			bottom = (fpBottom-y) * zNear / fp;
        			top = (fpTop-y) * zNear / fp;

        			gl.glMatrixMode(GL2.GL_PROJECTION);
        			gl.glPushMatrix();            	 		
        				gl.glLoadIdentity();
        				gl.glFrustum(left, right, bottom, top, zNear, zFar);
        				gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.asArray(), 0);         		
        			gl.glPopMatrix();
        			gl.glMatrixMode(GL2.GL_MODELVIEW);

        			m.reconstitute();
        			m.getBackingMatrix().invert();

        			gl.glMultMatrixf(m.asArray(), 0 ); // Applies the transformation to the current matrix stack 
        			glut.glutWireCube(2); // Draw frustum
        		gl.glPopMatrix();
            	
        		gl.glEnable(GL2.GL_LIGHTING);
            }
            
            // OBJECTIVE 6 - Draw left and right eye frustums if drawEyeFrustums is true
            if(drawEyeFrustums.getValue()){            	
            	gl.glDisable(GL2.GL_LIGHTING);
            	
            	//-----LEFT EYE-----//
            	gl.glColor3f(1.0f, 0.0f, 1.0f); //MAGENTA
            	gl.glPushMatrix();
            		gl.glTranslated(-disparity+x, y, eyeZPosition.getValue());
            		glut.glutSolidSphere(0.0125, 32, 32);
            	
            		left = (-w+disparity-x) * zNear / eyeZPosition.getValue();  
            		right = (w+disparity-x) * zNear / eyeZPosition.getValue();
            		bottom = (-h-y) * zNear / eyeZPosition.getValue();
            		top = (h-y) * zNear / eyeZPosition.getValue();   

            		gl.glMatrixMode(GL2.GL_PROJECTION);
            		gl.glPushMatrix();
            			gl.glLoadIdentity();
            			gl.glFrustum(right, left, bottom, top, zNear, zFar);
            			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.asArray(), 0);         		
            		gl.glPopMatrix();	  
            		gl.glMatrixMode(GL2.GL_MODELVIEW);
            		
            		m.reconstitute();
            		m.getBackingMatrix().invert();
            	
        			gl.glMultMatrixf(m.asArray(), 0 ); //Applies the transformation to the current matrix stack       	          	
        			glut.glutWireCube(2); //Draw frustum 
        		gl.glPopMatrix();
        		              
        		//-----RIGHT EYE-----//         	    
        		gl.glColor3f(0.0f, 1.0f, 0.0f); //GREEN
        		gl.glPushMatrix();
        			gl.glTranslated(disparity+x, 0, eyeZPosition.getValue());
        			glut.glutSolidSphere(0.0125, 32, 32);

        			left = (-w-disparity-x) * zNear / eyeZPosition.getValue();  
        			right = (w-disparity-x) * zNear / eyeZPosition.getValue();
        			bottom = (-h-y) * zNear / eyeZPosition.getValue();
        			top = (h-y) * zNear / eyeZPosition.getValue();     

        			gl.glMatrixMode(GL2.GL_PROJECTION);
        			gl.glPushMatrix();
        				gl.glLoadIdentity();
        				gl.glFrustum(right, left, bottom, top, zNear, zFar);
        				gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.asArray(), 0);         		
        			gl.glPopMatrix();	  
        			gl.glMatrixMode(GL2.GL_MODELVIEW);
        			
        			m.reconstitute();
        			m.getBackingMatrix().invert();
        	
        			gl.glMultMatrixf(m.asArray(), 0 ); //Applies the transformation to the current matrix stack       	          	
        			glut.glutWireCube(2); //Draw frustum 
        		gl.glPopMatrix();   
        		gl.glEnable(GL2.GL_LIGHTING);
            }
            
            //OBJECTIVE 8 - Draw left eye and right eye frustums with offset
            if(drawEyeFrustumsBonus.getValue()){
            	
            	gl.glDisable(GL2.GL_LIGHTING);
            	
            	//-----LEFT EYE-----//
            	gl.glColor3f(1.0f, 0.0f, 1.0f); //MAGENTA
            	gl.glPushMatrix();
            		gl.glTranslated(-disparity+x, y, eyeZPosition.getValue());
            		glut.glutSolidSphere(0.0125, 32, 32);
            	
            		left = (fpLeft-x+disparity) * zNear / fp;  
            		right = (fpRight-x+disparity) * zNear / fp;
            		bottom = (fpBottom-y) * zNear / fp;
        			top = (fpTop-y) * zNear / fp;      		
            		
            		gl.glMatrixMode(GL2.GL_PROJECTION);
            		gl.glPushMatrix();
            			gl.glLoadIdentity();
            			gl.glFrustum(right, left, bottom, top, zNear, zFar);
            			gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.asArray(), 0);         		
            		gl.glPopMatrix();	  
            		gl.glMatrixMode(GL2.GL_MODELVIEW);
            		
            		m.reconstitute();
            		m.getBackingMatrix().invert();
            	
        			gl.glMultMatrixf(m.asArray(), 0 ); //Applies the transformation to the current matrix stack       	          	
        			glut.glutWireCube(2); //Draw frustum 
        		gl.glPopMatrix();
        		              
        		//-----RIGHT EYE-----//         	    
        		gl.glColor3f(0.0f, 1.0f, 0.0f); //GREEN
        		gl.glPushMatrix();
        			gl.glTranslated(disparity+x, y, eyeZPosition.getValue());
        			glut.glutSolidSphere(0.0125, 32, 32);

        			left = (fpLeft-x-disparity) * zNear / fp;  
            		right = (fpRight-x-disparity) * zNear / fp;
            		bottom = (fpBottom-y) * zNear / fp;
        			top = (fpTop-y) * zNear / fp;          		

        			gl.glMatrixMode(GL2.GL_PROJECTION);
        			gl.glPushMatrix();
        				gl.glLoadIdentity();
        				gl.glFrustum(right, left, bottom, top, zNear, zFar);
        				gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, m.asArray(), 0);         		
        			gl.glPopMatrix();	  
        			gl.glMatrixMode(GL2.GL_MODELVIEW);
        			
        			m.reconstitute();
        			m.getBackingMatrix().invert();
        	
        			gl.glMultMatrixf(m.asArray(), 0 ); //Applies the transformation to the current matrix stack       	          	
        			glut.glutWireCube(2); //Draw frustum 
        		gl.glPopMatrix();   
        		gl.glEnable(GL2.GL_LIGHTING);          	
            }
            
            
        } else if ( viewingMode == 2 ) { //OBJECTIVE 2 - Draw center eye
            
        	//Get parameters to draw frustum
            right = w * zNear / eyeZPosition.getValue();  
            left = -right;            
            top = h * zNear / eyeZPosition.getValue();
            bottom = -top;
            
            //Overwrite projection and model view matrices to be my projection and viewing transformation
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustum(left, right, bottom, top, zNear, zFar);           
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslated(x, y, -eyeZPosition.getValue());
        
            //Draw scene            
            gl.glPushMatrix();
            	tbc2.applyViewTransformation(drawable); // only the view transformation
            	scene.display( drawable );
            gl.glPopMatrix(); 			
        	
        } else if ( viewingMode == 3 ) { //OBJECTIVE 5 - Draw center eye with depth of field blur
        	
        	int n = samples.getValue();  
        	        	
        	for (int i = 0;  i < samples.getValue(); i++){
        		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
        		
        		Point2d p = new Point2d();
        		fpd.get(p, i, n);
        		double fpdX = p.x * aperture.getValue();
        		double fpdY = p.y * aperture.getValue();
        		
        		//Get coordinates of the Focal Plane Rectangle
        		fpRight = w * fp / eyeZPosition.getValue();      	
            	fpLeft = -fpRight;
            	fpTop = h * fp / eyeZPosition.getValue();
            	fpBottom = -fpTop;
            	
            	//Get parameters to draw frustum
    			left = (fpLeft-fpdX-x) * zNear / fp; 
    			right = (fpRight-fpdX-x) * zNear / fp;
    			bottom = (fpBottom-fpdY-y) * zNear / fp;
    			top = (fpTop-fpdY-y) * zNear / fp;    			
    			
    			//Overwrite projection and model view matrices to be my projection and viewing transformation
    			gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustum(left, right, bottom, top, zNear, zFar);               
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();
                gl.glTranslated(-x-fpdX, -y-fpdY, -eyeZPosition.getValue());
                
                //Draw scene               
                gl.glPushMatrix();
            		tbc2.applyViewTransformation(drawable); // only the view transformation
            		scene.display( drawable );
            	gl.glPopMatrix(); 

                if(i==0)
                	gl.glAccum(GL2.GL_LOAD, 1f/n);
                else                
                	gl.glAccum(GL2.GL_ACCUM, 1f/n);
        	}
        	
        	gl.glAccum(GL2.GL_RETURN, 1);          
        	
        } else if ( viewingMode == 4 ) { //OBJECTIVE 6 - Draw the left eye view  
        	
        	left = (-w+disparity-x) * zNear / eyeZPosition.getValue();  
    		right = (w+disparity-x) * zNear / eyeZPosition.getValue();
    		bottom = (-h-y) * zNear / eyeZPosition.getValue();
    		top = (h-y) * zNear / eyeZPosition.getValue(); 
    		
    		//Overwrite projection and model view matrices to be my projection and viewing transformation
    		gl.glMatrixMode(GL2.GL_PROJECTION);
    		gl.glLoadIdentity();
			gl.glFrustum(left, right, bottom, top, zNear, zFar);			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslated(-disparity+x, y, -eyeZPosition.getValue());

    		//Draw scene            
    		gl.glPushMatrix();
    			tbc2.applyViewTransformation(drawable);
    			scene.display( drawable );
    		gl.glPopMatrix(); 
        	
        } else if ( viewingMode == 5 ) {  //OBJECTIVE 6 - Draw the right eye view
        	
        	left = (-w-disparity-x) * zNear / eyeZPosition.getValue();  
			right = (w-disparity-x) * zNear / eyeZPosition.getValue();
			bottom = (-h-y) * zNear / eyeZPosition.getValue();
			top = (h-y) * zNear / eyeZPosition.getValue();     			
    		
			//Overwrite projection and model view matrices to be my projection and viewing transformation
			gl.glMatrixMode(GL2.GL_PROJECTION);
    		gl.glLoadIdentity();
			gl.glFrustum(left, right, bottom, top, zNear, zFar);			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslated(disparity+x, y, -eyeZPosition.getValue());

    		//Draw scene            
    		gl.glPushMatrix();
    		tbc2.applyViewTransformation(drawable);
    		scene.display( drawable );
    		gl.glPopMatrix();
        	                               
        } else if ( viewingMode == 6 ) { //OBJECTIVE 7 - Draw the anaglyph view using glColouMask       
        		
        	//-----LEFT EYE MAGENTA-----//       	
        	gl.glColorMask(true, false, true, true); //MAGENTA       	
 
    		left = (-w+disparity) * zNear / eyeZPosition.getValue();  
    		right = (w+disparity) * zNear / eyeZPosition.getValue();
    		top = h * zNear / eyeZPosition.getValue();  
    		bottom = -top;
    		
    		//Overwrite projection and model view matrices to be my projection and viewing transformation
    		gl.glMatrixMode(GL2.GL_PROJECTION);
    		gl.glLoadIdentity();
			gl.glFrustum(left, right, bottom, top, zNear, zFar);			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslated(-disparity+x, y, -eyeZPosition.getValue());

    		//Draw scene    
    		gl.glPushMatrix();
    			tbc2.applyViewTransformation(drawable); // only the view transformation
    			scene.display( drawable );
    		gl.glPopMatrix();		
    		
    		//-----RIGHT EYE GREEN-----//
        	gl.glColorMask(false, true, false, true); //GREEN

			left = (-w-disparity) * zNear / eyeZPosition.getValue();  
			right = (w-disparity) * zNear / eyeZPosition.getValue();
			top = h * zNear / eyeZPosition.getValue();  
			bottom = -top;
    		
			//Overwrite projection and model view matrices to be my projection and viewing transformation
			gl.glMatrixMode(GL2.GL_PROJECTION);
    		gl.glLoadIdentity();
			gl.glFrustum(left, right, bottom, top, zNear, zFar);			
			gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            gl.glTranslated(disparity+x, y, -eyeZPosition.getValue());

    		//Draw scene  
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
    		gl.glPushMatrix();
    			tbc2.applyViewTransformation(drawable); // only the view transformation
    			scene.display( drawable );
    		gl.glPopMatrix();
    		
    		gl.glColorMask(true, true, true, true);

        } else if ( viewingMode == 7 ) { //OBJECTIVE 8 - Draw the anaglyph view with depth of field blur
        	
        	int n = samples.getValue();  
        	
        	//Get coordinates of the Focal Plane Rectangle
    		fpRight = w * fp / eyeZPosition.getValue();      	
        	fpLeft = -fpRight;
        	fpTop = h * fp / eyeZPosition.getValue();
        	fpBottom = -fpTop;
        	
        	//LEFT EYE
        	for (int i = 0;  i < samples.getValue(); i++){      		
        		Point2d p = new Point2d();
        		fpd.get(p, i, n);
        		double fpdX = p.x * aperture.getValue();
        		double fpdY = p.y * aperture.getValue();       	
            	
        		left = (fpLeft-fpdX-x+disparity) * zNear / fp;  
        		right = (fpRight-fpdX-x+disparity) * zNear / fp;
        		bottom = (fpBottom-fpdY-y) * zNear / fp;
    			top = (fpTop-fpdY-y) * zNear / fp;     
        		
    			//Overwrite projection and model view matrices to be my projection and viewing transformation
    			gl.glMatrixMode(GL2.GL_PROJECTION);
        		gl.glLoadIdentity();
    			gl.glFrustum(left, right, bottom, top, zNear, zFar);		
    			gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();
                gl.glTranslated(-disparity+x, y, -eyeZPosition.getValue());

        		//Draw scene 
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT );
        		gl.glColorMask(true, false, true, true); //MAGENTA 
        		gl.glPushMatrix();
        			tbc2.applyViewTransformation(drawable); // only the view transformation
        			scene.display( drawable );
        		gl.glPopMatrix();
        		
        		if(i==0)
                	gl.glAccum(GL2.GL_LOAD, 1f/n);
                else                
                	gl.glAccum(GL2.GL_ACCUM, 1f/n);
        	}
        	gl.glAccum(GL2.GL_RETURN, 1);
        	
        	// (I thought I was supposed to clear the color buffer but it seems to work fine without it.
        	//gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        	
        	//RIGHT EYE
        	for (int i = 0;  i < samples.getValue(); i++){	
        		Point2d p = new Point2d();
        		fpd.get(p, i, n);
        		double fpdX = p.x * aperture.getValue();
        		double fpdY = p.y * aperture.getValue();
        		
    			left = (fpLeft-fpdX-x-disparity) * zNear / fp;  
        		right = (fpRight-fpdX-x-disparity) * zNear / fp;
        		bottom = (fpBottom-fpdY-y) * zNear / fp;
    			top = (fpTop-fpdY-y) * zNear / fp;   
        		
    			//Overwrite projection and model view matrices to be my projection and viewing transformation
    			gl.glMatrixMode(GL2.GL_PROJECTION);
        		gl.glLoadIdentity();
    			gl.glFrustum(left, right, bottom, top, zNear, zFar);   			
    			gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glLoadIdentity();
                gl.glTranslated(disparity+x, y, -eyeZPosition.getValue());
                
                //Draw scene  
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT );
        		gl.glColorMask(false, true, false, true); //GREEN
        		gl.glPushMatrix();
            		tbc2.applyViewTransformation(drawable); // only the view transformation
            		scene.display( drawable );
            	gl.glPopMatrix(); 

                if(i==0)
                	gl.glAccum(GL2.GL_LOAD, 1f/n);
                else               
                	gl.glAccum(GL2.GL_ACCUM, 1f/n);
        	}        	
        	gl.glAccum(GL2.GL_RETURN, 1);     
        	
        	gl.glColorMask(true, true, true, true);        	
        }        
    }
    
}
