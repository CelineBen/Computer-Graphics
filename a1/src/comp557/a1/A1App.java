package comp557.a1;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import comp557.a1.rotationControllers.LocalRotationControls;
import comp557.a1.rotationControllers.RotationController;
import comp557.a1.rotationControllers.TrackBall;
import comp557.a1.rotationControllers.WorldRotationControls;
import comp557.a1.rotationControllers.XYBall;
import comp557.a1.rotationControllers.XYXRotationControls;
import comp557.a1.rotationControllers.XYZRotationControls;

import mintools.swing.VerticalFlowPanel;

/**
 * Assignment 1 - rotation interface and evaluation sample solution
 *  
 * @author kry
 */
public class A1App extends KeyAdapter {

    /**
     * Creates a Basic GL Window and links it to a GLEventListener
     * @param args
     */
    public static void main(String[] args) {
    	new A1App();
    }
    
    JPanel controls = new JPanel();
    
    JTabbedPane tabs = new JTabbedPane();

    private final RotationController rotationController[] = {
    		new XYZRotationControls(),
    		new XYXRotationControls(),
    		new LocalRotationControls(),
    		new WorldRotationControls(),
    		new XYBall(),
    		new TrackBall(),
    };

    private MainCanvas mainCanvas = new MainCanvas();

    private TargetCanvas targetCanvas = new TargetCanvas();
    
    /** The stats canvas keeps track of data on each of the control types, 
     * and needs to have the array of controllers 
     */
    private StatsCanvas statsCanvas = new StatsCanvas( rotationController, "a1data/log.txt" );

    public A1App() {
    	for ( RotationController r : rotationController ) {
    		r.attach( mainCanvas.glCanvas);
    	}
        controls.setPreferredSize( new Dimension(400,400) );
        createPanels();
        // the main canvas needs to know about the target rotation
        mainCanvas.targetRotation = targetCanvas.targetRotation;
        
        final JFrame jframe; 
        String windowName = "Assignment 1 - Rotations";
        jframe = new JFrame(windowName);
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                jframe.dispose();
                System.exit( 0 );
            }
        });
        jframe.getContentPane().setLayout( new GridLayout( 1, 4, 2, 2 ) );
        jframe.getContentPane().add( controls );
        jframe.getContentPane().add( mainCanvas.glCanvas );
        jframe.getContentPane().add( targetCanvas.glCanvas );
        jframe.getContentPane().add( statsCanvas.glCanvas );
        jframe.setSize( 1200, 320 );
        jframe.setVisible( true );
    }
    
    public void createPanels() {
    	final JButton next = new JButton("NEXT");
    	final JButton start = new JButton("RESTART / SKIP");
    	// the main canvas needs to enable/disable the next button
    	// based on the current error.
        mainCanvas.next = next;
    	start.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainCanvas.timer.resetAndStart();
				targetCanvas.setNewTargetRotation();
			}
		});
    	next.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				double elapsed = mainCanvas.timer.getElapsed();
				double error = mainCanvas.error;
				statsCanvas.add( tabs.getSelectedIndex(), error, elapsed );
				// log the elapsed time, error, and method
				mainCanvas.timer.resetAndStart();
				targetCanvas.setNewTargetRotation();
		    	next.setEnabled(false);
			}
		});
    	next.setEnabled(false);
    	tabs.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				mainCanvas.rotationController = rotationController[tabs.getSelectedIndex()];
				statsCanvas.selectedIndex = tabs.getSelectedIndex();
			}
		});
    	
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
    	JPanel p = new JPanel();
    	p.setLayout( new GridLayout(1,2) );
    	p.add( start );
    	p.add( next );
    	vfp.add( p );
    	
        tabs.setTabPlacement( SwingConstants.TOP );
        vfp.add( tabs );
        
        for ( RotationController r : rotationController ) {
        	tabs.add( r.getControls(), r.getName() );
        }
        
        vfp.add( statsCanvas.getControls() );
        controls = vfp.getPanel();
    }

}
