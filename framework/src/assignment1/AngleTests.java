package assignment1;

import static org.junit.Assert.assertEquals;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class AngleTests {

	HalfEdgeStructure hs;
	static final float epsilon = 0.01f;

	@Before
	public void setUp() {
		WireframeMesh m;
		hs = new HalfEdgeStructure();
		try {
			m = ObjReader.read("./objs/oneNeighborhood.obj", true);	
			hs.init(m);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Test
	public void testSumOverTriangle() {	
		Vertex center = hs.getVertices().get(0);
		HalfEdge towardsCenter = center.getHalfEdge().getOpposite();
		
		float angleSum = 0;
		angleSum += towardsCenter.getIncidentAngle();
		angleSum += towardsCenter.getNext().getIncidentAngle();
		angleSum += towardsCenter.getNext().getNext().getIncidentAngle();
		assertEquals(Math.PI, angleSum, epsilon);
	}
	
	@Test
	public void testMoreAngles() {
		// quite a bit hard, oneNeighborhood is not regular -.-
	}

}
