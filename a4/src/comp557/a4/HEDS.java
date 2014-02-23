package comp557.a4;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow for easy display of geometry.
 * 
 * @author Celine Bensoussan
 * Assignment 3 - Meshes subdivision
 * November 8th, 2013
 */
public class HEDS {

	/**
	 * List of faces 
	 */
	List<Face> faces = new ArrayList<Face>();

	/**
	 * Constructs an empty mesh (used when building a mesh with subdivision)
	 */
	public HEDS() {
		// do nothing
	}

	/**
	 * Builds a half edge data structure from the polygon soup   
	 * @param soup
	 */
	public HEDS( PolygonSoup soup ) {

		List<Vertex> vertexList = soup.vertexList;
		List<int[]> faceList = soup.faceList;
		
		Map<String, HalfEdge> M = new HashMap();

		for ( int[] faceVertex : faceList ) {        	
			
			int i = 0;
			int j = 0;

			for ( int k = 0; k < faceVertex.length; k++ ) {
				
				HalfEdge he = new HalfEdge();
				
				if(k < faceVertex.length-1){
					i = faceVertex[k];
					j = faceVertex[k+1];
				}else{
					i = faceVertex[k];
					j = faceVertex[0];
				}         	
				he.head = vertexList.get(j);

				M.put(""+ i + "," + j, he);
				
				//TWIN
				if(M.get(""+ j + "," + i) != null){
					he.twin = M.get(""+ j + "," + i);
					M.get(""+ j + "," + i).twin = he;
				}
			}
			
			//NEXT
			for ( int k = 0; k < faceVertex.length; k++ ) {				
				HalfEdge current = new HalfEdge();
				
				if(k < faceVertex.length-2){
					i = faceVertex[k];
					j = faceVertex[k+1];
					current = M.get(""+ i + "," + j);					
					current.next = M.get(""+ j + "," + faceVertex[k+2]);
				}else if(k < faceVertex.length-1){
					i = faceVertex[k];
					j = faceVertex[k+1];
					current = M.get(""+ i + "," + j);
					current.next = M.get(""+ j + "," + faceVertex[0]);
				}else{
					i = faceVertex[k];
					j = faceVertex[0];
					current = M.get(""+ i + "," + j);
					current.next = M.get(""+ j + "," + faceVertex[1]);					
				}
			}			
			//Add face to List<Face> faces
			Face f = new Face(M.get(""+ faceVertex[0] + "," + faceVertex[1]));
			faces.add(f);
		}


	} 

	/**
	 * Draws the half edge data structure by drawing each of its faces.
	 * Per vertex normals are used to draw the smooth surface when available,
	 * otherwise a face normal is computed. 
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		// note that we do not assume triangular or quad faces, so this method is slow! :(     
		Point3d p;
		Vector3d n;        
		for ( Face face : faces ) {
			HalfEdge he = face.he;
			if ( he.head.n == null ) { // don't have per vertex normals? use the face
				gl.glBegin( GL2.GL_POLYGON );
				n = he.leftFace.n;
				gl.glNormal3d( n.x, n.y, n.z );
				HalfEdge e = he;
				do {
					p = e.head.p;
					gl.glVertex3d( p.x, p.y, p.z );
					e = e.next;
				} while ( e != he );
				gl.glEnd();
			} else {
				gl.glBegin( GL2.GL_POLYGON );                
				HalfEdge e = he;
				do {
					p = e.head.p;
					n = e.head.n;
					gl.glNormal3d( n.x, n.y, n.z );
					gl.glVertex3d( p.x, p.y, p.z );
					e = e.next;
				} while ( e != he );
				gl.glEnd();
			}
		}
	}

	/** 
	 * Draws all child vertices to help with debugging and evaluation.
	 * (this will draw each points multiple times)
	 * @param drawable
	 */
	public void drawChildVertices( GLAutoDrawable drawable ) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glDisable( GL2.GL_LIGHTING );
		gl.glPointSize(8);
		gl.glBegin( GL.GL_POINTS );
		for ( Face face : faces ) {
			if ( face.child != null ) {
				Point3d p = face.child.p;
				gl.glColor3f(0,0,1);
				gl.glVertex3d( p.x, p.y, p.z );
			}
			HalfEdge loop = face.he;
			do {
				if ( loop.head.child != null ) {
					Point3d p = loop.head.child.p;
					gl.glColor3f(1,0,0);
					gl.glVertex3d( p.x, p.y, p.z );
				}
				if ( loop.child1 != null && loop.child1.head != null ) {
					Point3d p = loop.child1.head.p;
					gl.glColor3f(0,1,0);
					gl.glVertex3d( p.x, p.y, p.z );
				}
				loop = loop.next;
			} while ( loop != face.he );
		}
		gl.glEnd();
		gl.glEnable( GL2.GL_LIGHTING );
	}
}
