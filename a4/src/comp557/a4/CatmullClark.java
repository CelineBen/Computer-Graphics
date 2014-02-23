package comp557.a4;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class implementing the Catmull-Clark subdivision scheme
 * 
 * @author Celine Bensoussan
 * Assignment 3 - Meshes subdivision
 * November 8th, 2013
 */
public class CatmullClark {

	/**
	 * Subdivides the provided half edge data structure
	 * @param heds
	 * @return the subdivided mesh
	 */
	public static HEDS subdivide( HEDS heds ) {
		HEDS heds2 = new HEDS();     
		List<Face> faces = heds.faces;

		for (int i = 0; i < faces.size(); i++){    

			//---QUESTION 2 : Even Vertex---//
			HalfEdge loop = faces.get(i).he;      	
			do {   
				if (loop.head.child == null){
					Vertex childVertex = new Vertex();
					childVertex.p = getEvenVertex(loop);
					loop.head.child = childVertex;	
				}				
				loop = loop.next;
			} while ( loop != faces.get(i).he );
		}

		//---QUESTION 3 : Odd Vertices---//

		//Face Child Vertices
		for (int i = 0; i < faces.size(); i++){   
			Vertex faceChild = new Vertex();
			faceChild.p = getFaceChild(faces.get(i));
			faces.get(i).child = faceChild;

		}

		//Edge Child Vertices
		for (int i = 0; i < faces.size(); i++){   
			HalfEdge loop = faces.get(i).he;
			do {
				if(loop.child1 == null && loop.child2 == null){     			
					//Middle Vertex
					Vertex halfEdge = new Vertex();
					halfEdge.p = getEdgeVertex(loop);

					//Children Half Edge
					HalfEdge c1 = new HalfEdge();
					HalfEdge c2 = new HalfEdge();
					loop.child1 = c1;
					loop.child2 = c2;
					c1.head = halfEdge;
					//c1.next = c2;
					c1.parent = loop;
					//c1.leftFace = faces.get(i);
					c2.head = loop.head.child;
					c2.parent = loop;
					//c2.leftFace = faces.get(i);

					loop.child1 = c1;
					loop.child2 = c2;

					//Children Twin Half Edge
					if(loop.twin != null){
						HalfEdge ctwin1 = new HalfEdge();
						HalfEdge ctwin2 = new HalfEdge();
						ctwin1.head = halfEdge;
						ctwin1.parent = loop.twin;
						ctwin2.head = loop.twin.head.child;
						ctwin2.parent = loop.twin;					
						loop.twin.child1 = ctwin1;
						loop.twin.child2 = ctwin2;

						//Hook up twins with each other
						loop.child1.twin = loop.twin.child2;
						loop.child2.twin = loop.twin.child1;
						loop.twin.child1.twin = loop.child2;
						loop.twin.child2.twin = loop.child1; 
					}   			    			
				}           		
				loop = loop.next;
			} while ( loop != faces.get(i).he );
		}


		//---QUESTION 4 : Connectivity---//
		for (int i = 0; i < faces.size(); i++){   

			//Create the 8 child half edges that fall into the middle of each face		
			HalfEdge loop = faces.get(i).he;
			do {
				loop.child2.next = loop.next.child1;
				HalfEdge in = new HalfEdge();
				HalfEdge out = new HalfEdge();
				in.head = faces.get(i).child;
				out.head = loop.child1.head;
				out.next = loop.child2;
				in.twin = out;
				out.twin = in;
				loop.child1.next = in;
				loop = loop.next;
			} while ( loop != faces.get(i).he );

			//Set next of half edges pointing in the middle of face
			loop = faces.get(i).he;
			do {
				loop.child1.next.next = loop.prev().child1.next.twin;
				loop = loop.next;
			} while ( loop != faces.get(i).he );   

			//Add sub-faces to faceList of heds2
			loop = faces.get(i).he;
			do {	
				Face f = new Face(loop.child1);
				heds2.faces.add(f); 
				loop = loop.next;
			} while ( loop != faces.get(i).he ); 
	
		}
		
		//QUESTION 5 : Normals
		if(faces.get(0).he.parent == null){
			for (int i = 0; i < faces.size(); i++){
				HalfEdge loop = faces.get(i).he;
				do {	
					if(loop.head.n == null){
						loop.head.n = getNormal(loop);
					}
					loop = loop.next;
				} while ( loop != faces.get(i).he );		
			}
		}
		
		List<Face> faces2 = heds2.faces;		
		for (int i = 0; i < faces2.size(); i++){
			HalfEdge loop = faces2.get(i).he;
			do {	
				if(loop.head.n == null){
					loop.head.n = getNormal(loop);
				}
				loop = loop.next;
			} while ( loop != faces2.get(i).he );		
		}
		return heds2;      		
	}

	private static int getValence(HalfEdge he){
		int k = 0;		
		if(isBorder(he)){		
			he = getStart(he);
		}
		HalfEdge loop = he;
		do{
			k++;
			if (loop.next.twin == null){
				k++;
				break;
			}
			loop = loop.next.twin;
		} while ( loop != he );

		return k;   	
	}

	private static Point3d getEvenVertex(HalfEdge he){		
		Point3d childPosition = new Point3d(he.head.p);
		double k = getValence(he);

		System.out.println("Valence : " + k);

		if(!isBorder(he)){
			double beta = 3/(2*k);
			double gamma = 1/(4*k);
			childPosition.scale(1-beta-gamma);
			if(he.twin != null){
				HalfEdge loop = he;
				do{		
					//(beta/k)
					Point3d p1 = new Point3d(loop.twin.head.p);
					p1.scale(beta/k);
					childPosition.add(p1);

					if (!loop.twin.next.head.p.equals(loop.twin.prev().twin.head.p)){
						Point3d p2 = new Point3d(loop.twin.next.head.p);

						if(!loop.twin.next.next.head.p.equals(loop.twin.prev().twin.head.p)){
							//number of edges > 4, average vertices 
							int count = 1;
							HalfEdge temp = loop.twin.next.next;
							while(!temp.head.p.equals(loop.twin.prev().twin.head.p)){
								p2.add(temp.head.p);
								count++;
								temp = temp.next;
							}						
							p2.scale((double)1/count);	
						}
						else{
							//number of edges exactly equal to 4, do nothing
						}
						p2.scale(gamma/k);
						childPosition.add(p2);		
					}
					else{
						//triangle, do nothing
					}
					//Should not go in here but prevent it from crashing
					if (loop.next.twin == null){
						break;
					}
					loop = loop.next.twin;
				} while ( loop != he );
			}

		}else{
			HalfEdge loop = getStart(he);
			childPosition.scale(6);
			childPosition.add(loop.prev().head.p);				
			while(loop.next.twin != null){
				loop = loop.next.twin;
			}
			childPosition.add(loop.next.head.p);
			childPosition.scale((double)1/8);
		}
		return childPosition;
	}

	//Calculate the vertex child of a face
	//Counts the number of edges composing the face since child vertex is the average of all vertices
	private static Point3d getFaceChild(Face f){
		Point3d faceChild = new Point3d();
		double n = 0;
		HalfEdge loop = f.he;
		do{
			faceChild.add(loop.head.p); //add all the vertices together
			n++;
			loop = loop.next;
		}while ( loop != f.he );

		faceChild.scale((double)1/n); //divide by the total number of vertices

		return faceChild;
	}

	//Get Odd Vertex of given HalfEdge
	private static Point3d getEdgeVertex(HalfEdge he){ 	
		Point3d edgeVertex = new Point3d(he.head.p); 
		if(he.twin != null){   	
			edgeVertex.scale(6);   		
			Point3d v1 = new Point3d(he.twin.head.p);
			v1.scale(6);
			edgeVertex.add(v1);
			edgeVertex.add(he.next.head.p);
			edgeVertex.add(he.next.next.head.p);
			edgeVertex.add(he.twin.next.head.p);
			edgeVertex.add(he.twin.next.next.head.p);   		
			edgeVertex.scale((double)1/16);
		}
		else{
			edgeVertex.add(he.prev().head.p);
			edgeVertex.scale((double)1/2);
		}
		return edgeVertex;
	}

	//Return true if the head of a given HalfEdge is at the border of mesh
	private static boolean isBorder(HalfEdge he){		
		HalfEdge loop = he;
		do{
			if(loop.next.twin == null){
				return true;
			}
			loop = loop.next.twin;
		}while(loop != he);
		return false;
	}

	//Goes counterclockwise around the head of the given HalfEdge and returns the HalfEdge at the border
	private static HalfEdge getStart(HalfEdge he){
		HalfEdge loop = he;
		while(loop.twin != null){
			loop = loop.twin.prev();
		}
		return loop;
	}

	private static Vector3d getNormal(HalfEdge he){
		Vector3d t1 = new Vector3d();
		Vector3d t2 = new Vector3d();
		int k = getValence(he);
		
		if(!isBorder(he)){
			HalfEdge loop = he;
			int i = 0;
			do{
				Point3d temp1 = new Point3d(loop.twin.head.p);
				temp1.scale(Math.cos(2*Math.PI*i / k));
				t1.add(temp1);

				Point3d temp2 = new Point3d(loop.twin.head.p);
				temp2.scale(Math.sin(2*Math.PI*i / k));
				t2.add(temp2);

				if(loop.twin == null){
					return null;
				}
				loop = loop.twin.prev();
				i++;
			}while(loop != he);
		}
		// Calculating normals for edges
		// It does not work for 
		else{
			//T-ALONG [p(0,1) - p(k-1, 1)]
			HalfEdge loop = getStart(he);
			t1.sub(loop.prev().head.p); //p(k-1, 1)
			while(loop.next.twin != null){
				loop = loop.next.twin;
			}
			t1.add(loop.next.head.p); //p(0,1)

			
			//T-ACROSS
			if(k==2){ //[p(0,1) + p(1,1) - 2p(0)] for k=2
				t2.add(he.next.head.p); //p(0,1)
				t2.add(he.prev().head.p); //p(1,1)
				t2.sub(he.head.p); //p(0)
				t2.sub(he.head.p); //p(0)
			}else if(k==3){ //[p(1,1) - p(0)] for k=3
				he = getStart(he);
				t2.add(he.next.head.p);
				t2.sub(he.head.p);
			}else{//sin(PI/(k-1))[p(0,1)+p(k-1,1)] + (2 *cos(PI/(k-1))-2) .... for k>=4
				// TODO Calculate t-across when valence >= 4
				double theta = Math.PI / (k-1);
				he = getStart(he);
				t2.add(loop.prev().head.p); //p(k-1,1)
				while(loop.next.twin != null){
					loop = loop.next.twin;
				}
				t2.add(loop.next.head.p); //p(0,1)
				
				t2.scale(Math.sin(theta));
				
				Vector3d average = new Vector3d();
				int i = 1;
				do{
					Vector3d temp = new Vector3d(loop.twin.head.p);
					temp.sub(loop.head.p);
					temp.scale(Math.sin(i*theta));
					average.add(temp);
					loop = loop.twin.prev();
					i++;
				}while(loop != getStart(he));
				average.scale(2*Math.cos(theta) - 2);				
				t2.add(average);
			}
		}
		t1.cross(t1, t2);
		return t1;
	}

}


