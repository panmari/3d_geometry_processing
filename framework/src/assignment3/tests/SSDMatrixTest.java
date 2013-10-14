package assignment3.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;

import meshes.PointCloud;
import meshes.reader.PlyReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;
import assignment3.MarchableCube;
import assignment3.SSDMatrices;

public class SSDMatrixTest {

	private PointCloud pc;
	private HashOctree tree;
	private CSRMatrix D_0, D_1, R;
	//for linear functions
	float a = 1, b = 2, c = 3;

	@Before
	public void setUp() throws IOException{
		pc = PlyReader.readPointCloud("./objs/octreeTest2.ply", true);
		tree = new HashOctree(pc,4,1,1f);
		D_0 = SSDMatrices.D0Term(tree, pc);
		D_1 = SSDMatrices.D1Term(tree, pc);
		R = SSDMatrices.RTerm(tree);
	}
	
	@Test
	public void D0RowShouldSumUpToOne() {
		for (ArrayList<col_val> row: D_0.rows) {
			float sum = 0;
			for (col_val entry: row) {
				sum += entry.val;
			}
			assertEquals(1, sum, 0.0001);
		}
	}
	
	@Test
	public void D0multipliedByPoints()  {	
		ArrayList<Point3f> vertexPos = new ArrayList<Point3f>();
		for (HashOctreeVertex v: tree.getVertices()){
			vertexPos.add(v.getPosition());
		}
		ArrayList<Point3f> result = new ArrayList<Point3f>();
		assertEquals(tree.getVertices().size(), D_0.nCols);
		D_0.multPoints(vertexPos, result);		

		for (int i = 0; i < result.size(); i++) {
			Point3f computed = result.get(i);
			Point3f original = pc.points.get(i);
			assertTrue(original.distance(computed) < 0.00001f);
		}
	}
	
	@Test
	public void D1shouldHaveRightSize() {
		assertEquals(D_1.nCols, tree.getVertices().size());
		assertEquals(D_1.nRows, pc.points.size()*3);
	}
	
	/**
	 * Simple test: If f is a sampled linear function a*x + b*y + c*z,
	 *	D1*f = (a,b,c) on each cell.  
	 */
	@Test
	public void petersD1Test() {
		ArrayList<Float> f = getLinearFunctionOfVertices();
		ArrayList<Float> result = new ArrayList<Float>();
		D_1.mult(f, result);
		Iterator<Float> iter = result.iterator();
		//check if linear function is reproduced
		while(iter.hasNext()) {
			assertEquals(a, iter.next(), 0.00001f);
			assertEquals(b, iter.next(), 0.00001f);
			assertEquals(c, iter.next(), 0.00001f);
		}
	}
	
	@Test
	public void basicRTest() {
		assertEquals(R.nCols, tree.getVertices().size());
	}
	
	/**
	 * Simple test: R * f should be 0 for any linear function f
	 */
	@Test
	public void petersRTest() {
		ArrayList<Float> f = getLinearFunctionOfVertices();
		ArrayList<Float> result = new ArrayList<Float>();
		R.mult(f, result);
		System.out.println(f);
	}
	
	@Test
	public void marchableStuffTest() {
		//TODO: is 
		//actuall, just look at the output...
	}

	private ArrayList<Float> getLinearFunctionOfVertices() {
		ArrayList<Float> f = new ArrayList<Float>();
		//my awesome linear function
		for (MarchableCube v: tree.getVertices()) {
			Point3f p = v.getPosition();
			f.add(a*p.x + b*p.y + c*p.z);
		}
		return f;
	}
}
