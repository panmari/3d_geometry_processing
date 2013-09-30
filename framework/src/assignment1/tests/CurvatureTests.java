package assignment1.tests;

import static org.junit.Assert.*;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class CurvatureTests {
	private HalfEdgeStructure hs;
	private float sphereRadius = 2f;
	
	@Before
	public void startUp() {
		hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			WireframeMesh m = ObjReader.read("./objs/sphere.obj", false);
			hs.init(m);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		
	}
	@Test
	public void testMixedVoroiAreaOfCircle() {
		float summedCellArea = 0;
		for(Vertex v: hs.getVertices()){
			summedCellArea += v.getAMixed();
		}
		assertEquals(4*Math.PI*sphereRadius*sphereRadius, summedCellArea, 0.3f);
	}

}
