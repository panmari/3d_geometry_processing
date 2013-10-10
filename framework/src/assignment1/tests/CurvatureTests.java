package assignment1.tests;

import static org.junit.Assert.*;

import java.util.Iterator;

import meshes.Face;
import meshes.HalfEdge;
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
	public void testMixedVoronoiAreaOfCircle() {
		float summedCellArea = 0;
		for(Vertex v: hs.getVertices()){
			summedCellArea += v.getAMixed();
		}
		assertEquals(4*Math.PI*sphereRadius*sphereRadius, summedCellArea, 0.3f);
	}
	
	@Test
	public void testCurvatureOfCircle() {
		for(Vertex v: hs.getVertices()){
			assertEquals(1/2f, v.getCurvature(), 0.0001);
		}
	}
	
	@Test
	public void testBenchmark() {
		long start = System.currentTimeMillis();
		for(Face f: hs.getFaces()){
			f.isObtuse();
		}
		long end = System.currentTimeMillis();
		long timeStupid = end - start;
		for(Face f: hs.getFaces()){
			f.isObtuse();
		}
		
		start = System.currentTimeMillis();
		for(Face f: hs.getFaces()) {
			isObtuseUnrolled(f);
		}
		end = System.currentTimeMillis();
		long timeUnrolled = end - start;
		//System.out.println("" + timeUnrolled + " vs. " + timeStupid);
		//System.out.println("ratio: " + timeStupid / (float) timeUnrolled);

	}
	
	private boolean isObtuseUnrolled(Face f){

		HalfEdge he = f.getHalfEdge();
		float angle = he.getIncidentAngle();
		if (angle > Math.PI/2)
			return true;
		else {
			float angle2 = he.getNext().getIncidentAngle();
			if (angle > Math.PI/2 || angle + angle2 < Math.PI/2)
				return true;
			else
				return false;
		}
	}
	


}
