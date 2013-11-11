package assignment5.tests;

import static myutils.MyJunitAsserts.*;
import static org.junit.Assert.*;

import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class HalfEdgeCollapseTests {

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
	public void normalOfFaceShouldBeUp() {
		for(Face f: hs.getFaces())
			assertEquals(new Vector3f(0,0,1), f.normal());
	}

}
