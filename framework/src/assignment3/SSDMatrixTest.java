package assignment3;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import meshes.PointCloud;
import meshes.reader.PlyReader;

import org.junit.Before;
import org.junit.Test;

import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;

public class SSDMatrixTest {

	private PointCloud pc;
	private HashOctree tree;
	private CSRMatrix mat;

	@Before
	public void setUp() throws IOException{
		pc = PlyReader.readPointCloud("./objs/octreeTest2.ply", true);
		tree = new HashOctree(pc,4,1,1f);
		mat = SSDMatrices.D0Term(tree, pc);
	}
	
	@Test
	public void D0RowShouldSumUpToOne() {
		for (ArrayList<col_val> row: mat.rows) {
			float sum = 0;
			for (col_val entry: row) {
				sum += entry.val;
			}
			assertEquals(1, sum, 0.0001);
		}
	}
	@Test
	public void test()  {	
		System.out.println(mat);
		ArrayList<Point3f> vertexPos = new ArrayList<Point3f>();
		for (HashOctreeVertex v: tree.getVertices()){
			vertexPos.add(v.getPosition());
		}
		ArrayList<Point3f> result = new ArrayList<Point3f>();
		assertEquals(tree.getVertices().size(), mat.nCols);
		mat.multPoints(vertexPos, result);
		
		System.out.println(result);
		System.out.println("vs");
		System.out.println(pc.points);
	}
	
	@Test
	public void marchableStuffTest() {
		//TODO: is 
		//c.getCornerElement(i, tree).getIndex()
		//same as
		//c.getIndex() + i
		//??
	}

}
