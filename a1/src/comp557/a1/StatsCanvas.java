package comp557.a1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import mintools.parameters.BooleanParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import comp557.a1.rotationControllers.RotationController;

public class StatsCanvas implements GLEventListener {

	GLCanvas glCanvas;
	
	/** number of samples collected for each interaction type */
    int[] count; 
    
    RotationController[] rotationController;
    
    int selectedIndex = 0;

    /**
     * Creates a canvas that shows statistics.
     * The class needs the rotation controllers to query their names and to 
     * know how many there are.  The log file is loaded at creation.
     * 
     * @param rotationController
     * @param logFile
     */
	public StatsCanvas( RotationController[] rotationController, String logFile ) {
		this.rotationController = rotationController;
		int N = rotationController.length;
		count = new int[N];
        load(logFile);

		GLProfile glp = GLProfile.getDefault();
        GLCapabilities glcap = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glcap );
        glCanvas.setSize( 300, 300 );
        glCanvas.addGLEventListener(this);
        final FPSAnimator animator; 
        animator = new FPSAnimator(glCanvas, 60);
        animator.start();
	}
	
	static class Record {
		int type;
		double error;
		double time;
	}
	
	private ArrayList<Record> records = new ArrayList<Record>();
	
	private BufferedWriter writer;
	
	@Override
	protected void finalize() throws Throwable {
		writer.close();
		super.finalize();
	}
	
	public void load( String logFile ) {
		File f = new File( logFile );
		if ( f.exists() ) {
			try {
				BufferedReader reader = new BufferedReader( new FileReader( logFile ) );
				String line = reader.readLine();
				while ( line != null ) {
					Scanner s = new Scanner(line);
					s.useLocale(Locale.US);
					Record r = new Record();
					r.type = s.nextInt();
					r.error = s.nextDouble();
					r.time = s.nextDouble();
					records.add( r );
					s.close();
					line = reader.readLine();
				}
				reader.close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		} 
		try {
			writer = new BufferedWriter( new FileWriter( logFile , true ) );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		for ( Record r : records ) {
			count[r.type]++;
		}
	}
	
	public void add( int type, double error, double time ) {
		try {
			Record r = new Record();
			r.type = type;
			r.error = error;
			r.time = time;
			records.add( r );
			writer.write( type + " " + error + " " + time + "\n" );
			writer.flush();
			count[r.type]++;
			haveNewData = true;
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void init(GLAutoDrawable drawable) {
        drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0f); // Black Background
        gl.glClearDepth(1.0f); // Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST); // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL); // The Type Of Depth Testing To Do
        gl.glEnable( GL2.GL_NORMALIZE ); // normals stay normal length under scale
		gl.glDisable( GL2.GL_LIGHTING );

		// different blending properties for overlapping curves
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL2.GL_POINT_SMOOTH );
        gl.glEnable( GL2.GL_MULTISAMPLE );
        gl.glLineWidth( 2 );
	}
	
	private int lastN = -1;
	private boolean haveNewData = false;
	private double[][] hist;
	private double maxHistBin = 0;
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		int w = drawable.getWidth();
		int h = drawable.getHeight();
        gl.glViewport( 0, 0, w, h );

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        final GLU glu = new GLU();
        glu.gluOrtho2D(0, w, 0, h);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();        
        gl.glTranslated(20,20,0);
        
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1f);
        gl.glBegin( GL2.GL_LINE_STRIP );
        gl.glVertex2d( 0, h-40 );
        gl.glVertex2d( 0, 0 );
        gl.glVertex2d( w-40, 0 );
        gl.glEnd();
        
        int N = bins.getValue(); // # of bins
        if ( N != lastN ) {
        	lastN = N;
        	hist = new double[rotationController.length][N];
        	haveNewData = true; // force recompute
        } 
        if ( haveNewData ) {
    		haveNewData = false;        	
            for (int j = 0; j < rotationController.length; j++ ) {
            	for ( int i = 0; i < N; i++ ) {
            		hist[j][i] = 0;
            	}
            }
            double secPerBin = 60.0/N;
            for ( Record r : records ) {
            	int i = (int) (r.time/secPerBin);
            	if ( i >= N ) continue;
            	hist[r.type][i]++;        	
            }
            // normalize for the number of samples
            for (int j = 0; j < rotationController.length; j++ ) {
            	if ( count[j] != 0 ) {
            		for ( int i = 0; i < N; i++ ) {
            			hist[j][i] /= count[j];
            		}
            	}
            }
            // find the max across all normalized bins
            maxHistBin = 0;
            for (int j = 0; j < rotationController.length; j++ ) {
            	for ( int i = 0; i < N; i++ ) {
            		if ( hist[j][i] > maxHistBin ) maxHistBin = hist[j][i];
            	}
            }
            if ( maxHistBin == 0 ) maxHistBin = 1;
        }
                   
        gl.glScaled( (w-40.0)/N, (h*0.8)/maxHistBin, 1 ); //10, 1);
        
        // a line drawing of the PDFs
        for ( int k = 0; k < rotationController.length; k++ ) {
        	if ( ! showAll.getValue() && k != selectedIndex ) continue;
        	switch ( k ) {
        	case 0: gl.glColor4f(1,0,0,1.0f); break;
        	case 1: gl.glColor4f(0,1,0,1.0f); break;
        	case 2: gl.glColor4f(0,0,1,1.0f); break;
        	case 3: gl.glColor4f(1,0,1,1.0f); break;
        	case 4: gl.glColor4f(0,1,1,1.0f); break;
        	case 5: gl.glColor4f(1,1,0,1.0f); break;
        	}
            gl.glBegin( GL2.GL_LINE_STRIP );
	        for ( int i = 0 ; i < N; i++ ) {
	        	gl.glVertex2d( i+0.5, hist[k][i] );
	        }
		    gl.glEnd();
        }
        
        // now draw a text overlay with the names
        EasyViewer.beginOverlay(drawable);
        int ycoord = 14;
        for ( int k = 0; k < rotationController.length; k++ ) {
        	if ( ! showAll.getValue() && k != selectedIndex ) continue;
        	switch ( k % 6 ) {
        	case 0: gl.glColor4f(1,0,0,1.0f); break;
        	case 1: gl.glColor4f(0,1,0,1.0f); break;
        	case 2: gl.glColor4f(0,0,1,1.0f); break;
        	case 3: gl.glColor4f(1,0,1,1.0f); break;
        	case 4: gl.glColor4f(0,1,1,1.0f); break;
        	case 5: gl.glColor4f(1,1,0,1.0f); break;
        	}
        	EasyViewer.printTextLines(drawable, rotationController[k].getName() + " " + count[k], w - 100, ycoord, h, GLUT.BITMAP_HELVETICA_10);
        	ycoord += 12;
        }
        EasyViewer.endOverlay(drawable);
	}
	
	private IntParameter bins = new IntParameter( "bins", 20, 5, 50 );
	private BooleanParameter showAll = new BooleanParameter("show all", true);
	
	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.setBorder( new TitledBorder("Stats controls"));
		IntParameter.DEFAULT_SLIDER_LABEL_WIDTH = 30;
		IntParameter.DEFAULT_SLIDER_TEXT_WIDTH = 80;

		vfp.add( bins.getSliderControls() );
		vfp.add( showAll.getControls() );
		return vfp.getPanel();
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
