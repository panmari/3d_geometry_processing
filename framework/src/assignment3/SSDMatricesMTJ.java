package assignment3;

import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

import org.jblas.FloatMatrix;
import org.jblas.Solve;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;


public class SSDMatricesMTJ {
	
	/**
	 * One line per point, One column per vertex,
	 * enforcing that the interpolation of the Octree vertex values
	 * is zero at the point position.
	 *
	 */
	public static FlexCompRowMatrix D0Term(HashOctree tree, PointCloud cloud){
		FlexCompRowMatrix mat2 = new FlexCompRowMatrix(cloud.points.size(), tree.numberOfVertices());
		int pointIdx = 0;
		for (Point3f p: cloud.points) {
			
			HashOctreeCell c = tree.getCell(p);
			MarchableCube v_000 = c.getCornerElement(0b000, tree);
			Vector3f v_d = new Vector3f(p);
			v_d.sub(v_000.getPosition());
			v_d.scale(1/c.side); //scale to unit size.
			for(int i = 0; i < 8; i++) {
				float weight = (0b100 & i) != 0b100 ? 1 - v_d.x : v_d.x;  
				weight *= (0b010 & i) != 0b010 ? 1 - v_d.y : v_d.y; 
				weight *= (0b001 & i) != 0b001 ? 1 - v_d.z : v_d.z; 
				mat2.set(pointIdx, c.getCornerElement(i, tree).getIndex(), weight);
			}
			pointIdx++;
		}		
		return mat2;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 correxponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static FlexCompRowMatrix D1Term(HashOctree tree, PointCloud cloud) {
		FlexCompRowMatrix mat2 = new FlexCompRowMatrix(cloud.points.size()*3, tree.numberOfVertices());
		int pointIdx = 0;
		for (Point3f p: cloud.points) {
			HashOctreeCell c = tree.getCell(p);
			float gradientNormalizationTerm = 1/(4*c.side);
			for (int i = 0; i < 8; i++) {
				float xGrad = (i & 0b100) == 0b100 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float yGrad = (i & 0b010) == 0b010 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float zGrad = (i & 0b001) == 0b001 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				int idx = c.getCornerElement(i, tree).getIndex();
				mat2.set(pointIdx*3 + 0, idx, xGrad);
				mat2.set(pointIdx*3 + 1, idx, yGrad);
				mat2.set(pointIdx*3 + 2, idx, zGrad);
			}
			pointIdx++;
		}		
		return mat2;
	}
	
	
	
	public static FlexCompRowMatrix RTerm(HashOctree tree){
		FlexCompRowMatrix mat2 = new FlexCompRowMatrix(tree.numberOfVertices()*9, tree.numberOfVertices());
		float scaleFactor = 0;
		int idx = 0;
		for (HashOctreeVertex j: tree.getVertices()) {
			for (int shift = 0b100; shift > 0b000; shift >>= 1) {
				HashOctreeVertex i = tree.getNbr_v2vMinus(j, shift); //nbr in minus direction
				if (i == null)
					continue;
				HashOctreeVertex k = tree.getNbr_v2v(j, shift); //nbr in plus direction
				if (k == null)
					continue;
				float dist_ij = i.getPosition().distance(j.getPosition());
				float dist_kj = k.getPosition().distance(j.getPosition());
				float dist_ik = dist_ij + dist_kj;
				mat2.set(idx*3+0, j.getIndex(), 1);
				mat2.set(idx*3+1, k.getIndex(), -dist_ij/(dist_ik));
				mat2.set(idx*3+2, i.getIndex(), -dist_kj/(dist_ik));
				scaleFactor += dist_ij*dist_kj;
			}
			idx++;
		}
		mat2.scale(1/scaleFactor);
		return mat2;
	}

	/**
	 * Set up the linear system for ssd: append the three matrices, 
	 * appropriately scaled. And set up the appropriate right hand side, i.e. the
	 * b in Ax = b
	 * @param tree
	 * @param pc
	 * @param lambda0
	 * @param lambda1
	 * @param lambda2
	 * @return
	 */
	public static Vector ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
		int N = tree.numberOfVertices();
		FlexCompRowMatrix D0 = D0Term(tree, pc);
		D0.scale((float) Math.sqrt(lambda0/N));
		
		FlexCompRowMatrix D1 = D1Term(tree, pc);
		float scaleD1 =  (float) Math.sqrt(lambda1/N);
		D1.scale(scaleD1);

		FlexCompRowMatrix R = RTerm(tree);
		//careful, the 1/sum(..) was already scaled in method
		float scaleR = (float) Math.sqrt(lambda2);
		R.scale(scaleR);
		
		//very stupid filling stuff into a dense matrix
		DenseMatrix A = new  DenseMatrix(D0.numRows() + 
				D1.numRows() + R.numRows(), D0.numColumns());
		Iterator<MatrixEntry> iter = D0.iterator();
		while (iter.hasNext()) {
			MatrixEntry entry = iter.next();
			A.set(entry.row(), entry.column(), entry.get());
		}
		iter = D1.iterator();
		while (iter.hasNext()) {
			MatrixEntry entry = iter.next();
			A.set(entry.row() + D0.numRows(), entry.column(), entry.get());
		}
		iter = R.iterator();
		while (iter.hasNext()) {
			MatrixEntry entry = iter.next();
			A.set(entry.row() + D0.numRows() + D1.numRows(), entry.column(), entry.get());
		}
		Vector b = new DenseVector(D0.numRows() + 
				D1.numRows() + R.numRows());
		
		int idx = 0;
		for (Vector3f n: pc.normals) //actually not needed, bc initialized to 0
			b.set(idx++, 0);
		for (Vector3f n: pc.normals) {
			b.set(idx++, n.x*scaleD1);
			b.set(idx++, n.y*scaleD1);
			b.set(idx++, n.z*scaleD1);
		}
		while (idx < b.size())
			b.set(idx++, 0);
		Vector x = new DenseVector(D0.numColumns());
		A.solve(b, x);
		return x;
	}

}
