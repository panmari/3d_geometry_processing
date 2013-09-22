package assignment1;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Iterator;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class VVIteratorTests {

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
		Iterator<Vertex> iter = v.iteratorVV();
		assertTrue(iter.hasNext());
		assertEquals(5, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(4, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(3, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(2, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(1, iter.next().index);
		assertFalse(iter.hasNext());
	}
	
	@Test
	public void vertex3() {
		Vertex v = hs.getVertices().get(3);
		Iterator<Vertex> iter = v.iteratorVV();
		assertTrue(iter.hasNext());
		assertEquals(4, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(2, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(0, iter.next().index);
		assertFalse(iter.hasNext());
	}
	
	@Test
	public void vertex5() {
		Vertex v = hs.getVertices().get(5);
		Iterator<Vertex> iter = v.iteratorVV();
		assertTrue(iter.hasNext());
		assertEquals(1, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(4, iter.next().index);
		assertTrue(iter.hasNext());
		assertEquals(0, iter.next().index);
		
		assertFalse(iter.hasNext());
	}
}
