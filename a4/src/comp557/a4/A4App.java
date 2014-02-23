package comp557.a4;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.BooleanParameter;
import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.Interactor;
import mintools.viewer.SceneGraphNode;

/**
 * COMP557 - Assignment 3 class.
 */
public class A4App implements SceneGraphNode, Interactor {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new A4App();
    }
    
    private PolygonSoup soup;
    
    private HEDS[] heds;
    
    private HalfEdge currentHE;
    
    private int whichSoup = 0;
    
    private String[] soupFiles = {            
            "a4data/torus3.obj",  // all regular vertices, no boundary, all quads
            "a4data/torus4.obj",
            "a4data/torus6.obj",
            "a4data/cylinder.obj",
            "a4data/cube.obj",    // some irregular vertices, no boundary, all quads
            //"a4data/soccerBall.obj",
            //"a4data/badSphere.obj",
            "a4data/human.obj",         
            "a4data/werewolf.obj", // no boundaries, mixed faces
            "a4data/quad.obj",    // boundaries, all quads
            "a4data/plane5x5.obj", 
            "a4data/cutcube.obj",       
            "a4data/monkey.obj",   // boundaries, mixed faces            
            "a4data/350zrelease.obj",               
        };
    
    /**
     * COMP557 - Assignment 3 class.
     */
    public A4App() {    
        loadSoupBuildAndSubdivide( soupFiles[0], 3 );
        EasyViewer ev = new EasyViewer("Comp 557 Assignment 4 - Celine Bensoussan 260350861", this, new Dimension(600, 600), new Dimension(600, 600) );
        ev.addInteractor(this);
    }
    
    /**
     * Loads the currently 
     */
    private void loadSoupBuildAndSubdivide( String filename, int levels ) {          
        soup = new PolygonSoup( filename );
        heds = new HEDS[levels];
        heds[0] = new HEDS(soup);
        for ( int i = 1; i < levels; i++ ) {
            heds[i] = CatmullClark.subdivide( heds[i-1] );
        }
        if ( heds[0].faces.size() > 0 ) {
            // can only display a half edge and use the keyboard interface
            // to walk around the mesh if we actually have a half edge data
            // structure with faces!
            currentHE = heds[0].faces.get(0).he;
        }
        drawLevel = 0;        
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
    
        if ( !drawWireFrame.getValue()) {
            // if drawing with lighting, we'll set the material
            // properties for the font and back surfaces, and set
            // polygons to render filled.
            gl.glEnable(GL2.GL_LIGHTING);
            final float frontColour[] = {.7f,.7f,0,1};
            final float backColour[] = {0,.7f,.7f,1};
            final float[] shinyColour = new float[] {1f, 1f, 1f, 1};            
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glDisable( GL.GL_CULL_FACE );
            gl.glMaterialfv( GL.GL_FRONT,GL2.GL_AMBIENT_AND_DIFFUSE, frontColour, 0 );
            gl.glMaterialfv( GL.GL_BACK,GL2.GL_AMBIENT_AND_DIFFUSE, backColour, 0 );
            gl.glMaterialfv( GL.GL_FRONT_AND_BACK,GL2.GL_SPECULAR, shinyColour, 0 );
            gl.glMateriali( GL.GL_FRONT_AND_BACK,GL2.GL_SHININESS, 50 );
            gl.glLightModelf(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1);
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_FILL );            
        } else {
            // if drawing without lighting, we'll set the colour to white
            // and set polygons to render in wire frame
            gl.glDisable( GL2.GL_LIGHTING );
            gl.glColor4f(.7f,.7f,0.0f,1);
            gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL2.GL_LINE );
        }
        
        if ( drawCoarse.getValue() ) {
            soup.display( drawable );
        }
        if ( drawChildVerts.getValue() ) {
            heds[drawLevel].drawChildVertices( drawable );
        }
        if ( drawHalfEdge.getValue() && currentHE != null ) {
            currentHE.display( drawable );
        }
        heds[drawLevel].display( drawable );
        
        EasyViewer.beginOverlay(drawable);
        gl.glDisable( GL2.GL_LIGHTING );
        gl.glColor3f(1,1,1);
        EasyViewer.printTextLines(drawable, soupFiles[whichSoup] + "\nlevel " + drawLevel, 40,40,20, GLUT.BITMAP_HELVETICA_18 );
        EasyViewer.endOverlay(drawable);
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL( new DebugGL2(drawable.getGL().getGL2()) );
        GL gl = drawable.getGL();        
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_NORMALIZE );
    }

    /** Level of subdivision of the mesh to draw */
    private int drawLevel = 0;
    
    private BooleanParameter drawHalfEdge = new BooleanParameter( "draw test half edge", true );
    private BooleanParameter drawChildVerts = new BooleanParameter( "draw child vertices", false );
    private BooleanParameter drawWireFrame = new BooleanParameter( "draw wire frame", false );
    // TODO: Objective 1: on the line below, set the drawCoarse default value to false once you've correctly created your half edge data structure
    private BooleanParameter drawCoarse = new BooleanParameter( "draw coarse soup mesh", false );
    private IntParameter subdivisionLevels = new IntParameter("maximum subdivisions", 3, 3, Integer.MAX_VALUE );
    
    @Override
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add( drawWireFrame.getControls() );
        vfp.add( drawCoarse.getControls() );        
        vfp.add( drawChildVerts.getControls() );
        vfp.add( drawHalfEdge.getControls() );
        vfp.add( subdivisionLevels.getControls() );
        vfp.add( new JLabel("increase maximum subdivision level carefully!" ) );        
        JButton reload = new JButton("Reload and resubdivide");
        vfp.add( reload );
        reload.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSoupBuildAndSubdivide( soupFiles[whichSoup], subdivisionLevels.getValue() );
            }
        });
        JTextArea ta = new JTextArea(
        		"   space - half edge twin\n" +
        		"   n - half edge next\n" +
        		"   left - half edge child 1\n" +
        		"   right - half edge child 2\n" +
        		"   up - half edge parent\n" +
        		"   home - display mesh subdivision level - 1\n" +
        		"   end - display subdivision level + 1\n" +
        		"   page up - previous model\n" +
        		"   page down - next model");
        ta.setEditable(false);
        ta.setBorder( new TitledBorder("Keyboard controls") );
        vfp.add( ta );
        return vfp.getPanel();
    }

    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if ( currentHE.twin != null ) currentHE = currentHE.twin;                    
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if ( currentHE.parent != null ) currentHE = currentHE.parent;
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    if ( currentHE.next != null ) currentHE = currentHE.next;
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if ( currentHE.child1 != null ) currentHE = currentHE.child1;                    
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if ( currentHE.child2 != null ) currentHE = currentHE.child2;
                } else if (e.getKeyCode() == KeyEvent.VK_S) {
                    // walks along the mesh one quadrilateral at a time
                    if ( currentHE.next != null ) currentHE = currentHE.next;
                    if ( currentHE.next != null ) currentHE = currentHE.next;
                    if ( currentHE.twin != null ) currentHE = currentHE.twin;
                } else if ( e.getKeyCode() == KeyEvent.VK_PAGE_UP ) {
                    if ( whichSoup > 0 ) whichSoup--;                    
                    loadSoupBuildAndSubdivide( soupFiles[whichSoup], subdivisionLevels.getValue() );
                } else if ( e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ) {
                    if ( whichSoup < soupFiles.length -1 ) whichSoup++;                    
                    loadSoupBuildAndSubdivide( soupFiles[whichSoup], subdivisionLevels.getValue() );
                } else if ( e.getKeyCode() == KeyEvent.VK_HOME ) {
                    drawLevel--;
                    if ( drawLevel < 0 ) drawLevel = 0;
                } else if ( e.getKeyCode() == KeyEvent.VK_END ) {
                    drawLevel++;
                    if ( drawLevel >= subdivisionLevels.getValue() ) drawLevel = subdivisionLevels.getValue() - 1;
                }                
            }
        });
    }

}
