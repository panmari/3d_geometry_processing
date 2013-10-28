package assignment4;


import static org.junit.Assert.fail;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;

public class Assignment4_1_Tests {
	
	// A sphere of radius 2.
	private HalfEdgeStructure hs; 
	// An ugly sphere of radius 1, don't expect the Laplacians 
	//to perform accurately on this mesh.
	private HalfEdgeStructure hs2; 
	@Before
	public void setUp(){
		try {
			WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
			
			m = ObjReader.read("objs/uglySphere.obj", false);
			hs2 = new HalfEdgeStructure();
			hs2.init(m);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	
	//Test everything and anything...

}
