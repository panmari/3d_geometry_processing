package assignment1;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class AngleTests {

	HalfEdgeStructure hs;
	Vertex center;
	static final float epsilon = 0.01f;

	@Before
	public void setUp() {
		WireframeMesh m;
		hs = new HalfEdgeStructure();
		try {
			m = ObjReader.read("./objs/oneNeighborhood.obj", false);	
			hs.init(m);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		center = hs.getVertices().get(0);
	}
	
	@Test
	public void testSumOverTriangle() {	
		HalfEdge towardsCenter = center.getHalfEdge().getOpposite();
		
		float angleSum = 0;
		angleSum += towardsCenter.getIncidentAngle();
		angleSum += towardsCenter.getNext().getIncidentAngle();
		angleSum += towardsCenter.getNext().getNext().getIncidentAngle();
		assertEquals(Math.PI, angleSum, epsilon);
	}
	
	@Test
	public void testMoreAngles() {
		// TODO: quite a bit hard, oneNeighborhood is not regular -.-
	}
	
	@Test
	public void testIsObtuse() {
		ArrayList<Face> faces = hs.getFaces();
		assertFalse(faces.get(0).isObtuse());
		assertFalse(faces.get(1).isObtuse());
		assertFalse(faces.get(2).isObtuse());
		assertFalse(faces.get(3).isObtuse());
		assertTrue(faces.get(4).isObtuse()); //the one at the bottom
	}
	
	@Test
	public void testVoronoiCellAreaOneNeighborhood() {
		Iterator<Face> iter = center.iteratorVF();
		float sum = 0;
		while(iter.hasNext()) {
			float a = iter.next().getMixedVoronoiCellArea(center);
			sum += a;
		}
		assertEquals(sum, center.getAMixed(), epsilon);
	}
	
	@Test
	public void testCurvatureOneNeighborhoodCenter() {
		assertEquals(0f, center.getCurvature(), epsilon);
	}

}
