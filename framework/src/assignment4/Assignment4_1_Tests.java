package assignment4;


import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

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
	
	@Test
	public void testSphereMeanCurvatureCotan() {
		CSRMatrix m = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<Vector3f> res = new ArrayList<Vector3f>();
		LMatrices.mult(m, hs, res);
		for (Vector3f v: res)
			assertEquals(0.5f, v.length()/2f, 0.0001);
	}
	
	@Test
	public void testSphereCurvatureDirectionCotan() {
		CSRMatrix m = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<Vector3f> res = new ArrayList<Vector3f>();
		LMatrices.mult(m, hs, res);
		for (int i = 0; i < res.size(); i++) {
			Vector3f curv = new Vector3f(res.get(i));
			curv.normalize();
			curv.scale(-1f);
			//asserts, that origin is the center of sphere
			Vector3f normal = new Vector3f(hs.getVertices().get(i).getPos());
			normal.normalize();
			assertTrue("expected " + normal + ", but was" + curv, curv.epsilonEquals(normal, 0.01f));
		}
	}
	
	/**
	 * This does not work very well, but according to @alf,
	 * the uniform curvature can not be used to approximate mean curvature anyway.
	 */
	@Test
	public void testSphereCurvatureDirectionUniform() {
		CSRMatrix m = LMatrices.uniformLaplacian(hs);
		ArrayList<Vector3f> res = new ArrayList<Vector3f>();
		LMatrices.mult(m, hs, res);
		int failures = 0;
		for (int i = 0; i < res.size(); i++) {
			Vector3f curv = new Vector3f(res.get(i));
			curv.normalize();
			//curv.scale(-1f);
			//asserts, that origin is the center of sphere
			Vector3f normal = new Vector3f(hs.getVertices().get(i).getPos());
			normal.normalize();
			if (!curv.epsilonEquals(normal, 0.01f))
				failures++;
			//assertTrue("expected " + normal + ", but was" + curv, curv.epsilonEquals(normal, 0.01f));
		}
		assertEquals("Failed " + failures + " out of " + res.size(), 0, failures);
	}
	
	@Test
	public void rowSumShouldBeZeroCotan() {
		CSRMatrix m = LMatrices.mixedCotanLaplacian(hs);
		for (ArrayList<col_val> row: m.rows) {
			float sum = 0;
			for (col_val entry: row) {
				sum += entry.val;
			}
			assertEquals(0f, sum, 0.0001);
		}
	}
	
	@Test
	public void rowSumShouldBeZeroUniform() {
		CSRMatrix m = LMatrices.uniformLaplacian(hs);
		for (ArrayList<col_val> row: m.rows) {
			float sum = 0;
			for (col_val entry: row) {
				sum += entry.val;
			}
			assertEquals(0f, sum, 0.0001);
		}
	}
	
	@Test
	public void sphereVolumeShouldAddUp() {
		float vol = LaplacianSmoother.getVolume(hs);
		assertEquals(4/3f*Math.PI*2*2*2, vol, 0.3);
	}

}
