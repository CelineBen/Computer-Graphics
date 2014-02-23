package comp557.a5;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import mintools.swing.FileSelect;

import org.w3c.dom.Document;

public class A5App {
    /**
     * Entry point for the application.
     * 
     * @param args The first argument should be the filename of the scene XML file.
     */
    public static void main(String[] args) {
        
    	if ( args.length == 0 ) {
    		File f = FileSelect.select( "xml", "Scene Description", "Load", "a5data", false);
    		if( f != null ) {
    			args = new String[] { f.getAbsolutePath() }; 
    		}
    		else {
    			System.exit(0);
    		}
    	}
        try {
            InputStream inputStream = new FileInputStream( new File(args[0]) );
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            Scene scene = Parser.createScene( document.getDocumentElement() );
            
            scene.render(true);
            
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("Failed to load simulation input file.", e);
        }
        
        System.exit(0);
    }
}
