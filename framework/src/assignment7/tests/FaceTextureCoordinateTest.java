package assignment7.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class FaceTextureCoordinateTest {

	HalfEdgeStructure hs;
	private Face face;
	
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
		this.face = hs.getFaces().get(0); 
	}
	

	@Test
	public void testVertexItself() {
		assertTrue(face.contains(new Point2f(0,0)));
	}	

	@Test
	public void testOutOfEverythingEver() {
		assertFalse(face.contains(new Point2f(-1000,-1000)));
	}
	
	@Test
	public void testOutOfEverythingEverPositive() {
		assertFalse(face.contains(new Point2f(1000,1000)));
	}
	
	@Test
	public void testOutOf() {
		assertFalse(face.contains(new Point2f(1,1)));
		assertFalse(face.contains(new Point2f(.1f,.1f)));
	}

	@Test
	public void testInside() {
		assertTrue(face.contains(new Point2f(.1f,-.5f)));
		assertTrue(face.contains(new Point2f(.2f,-.5f)));
		assertTrue(face.contains(new Point2f(-.1f,-.5f)));
	}
	
	@Test
	public void testSumBilinearWeights() {
		assertEquals(1 ,
				face.bilinearInterpolationWeights(new Point2f(.1f,-.5f)).dot(new Vector3f(1,1,1)), 0.001f);
		assertEquals(1 ,
				face.bilinearInterpolationWeights(new Point2f(.2f,-.5f)).dot(new Vector3f(1,1,1)), 0.001f);
		assertEquals(1 ,
				face.bilinearInterpolationWeights(new Point2f(-.1f,-.5f)).dot(new Vector3f(1,1,1)), 0.001f);
	}
}
