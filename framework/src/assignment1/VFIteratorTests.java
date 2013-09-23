package assignment1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class VFIteratorTests {

	HalfEdgeStructure hs;
	
	@Before
	public void setUp() throws IOException {
		WireframeMesh n = ObjReader.read("./objs/oneNeighborhood.obj", true);
		hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(n);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	@Test
	public void vertex0() {
		Vertex v = hs.getVertices().get(0);
		Iterator<Face> iter = v.iteratorVF();
		assertTrue(iter.hasNext());
		assertEquals("f: [4,5,0]", iter.next().toString());
		assertTrue(iter.hasNext());
		assertEquals("f: [3,4,0]", iter.next().toString());
		assertTrue(iter.hasNext());
		assertEquals("f: [2,3,0]", iter.next().toString());
		assertTrue(iter.hasNext());
		assertEquals("f: [1,2,0]", iter.next().toString());
		assertTrue(iter.hasNext());
		assertEquals("f: [5,1,0]", iter.next().toString());
		assertFalse(iter.hasNext());
	}
	
	@Test
	public void vertex3() {
		Vertex v = hs.getVertices().get(3);
		Iterator<Face> iter = v.iteratorVF();
		assertTrue(iter.hasNext());
		assertEquals("f: [2,3,0]", iter.next().toString());
		assertTrue(iter.hasNext());
		assertEquals("f: [3,4,0]", iter.next().toString());
		assertFalse(iter.hasNext());
	}
	
	@Test
	public void vertex5() {
		Vertex v = hs.getVertices().get(5);
		Iterator<Face> iter = v.iteratorVF();
		assertTrue(iter.hasNext());
		assertEquals("f: [4,5,0]", iter.next().toString());
		assertTrue(iter.hasNext());
		assertEquals("f: [5,1,0]", iter.next().toString());
		assertFalse(iter.hasNext());
	}
}
