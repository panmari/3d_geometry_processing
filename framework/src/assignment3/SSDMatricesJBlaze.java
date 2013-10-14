package assignment3;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.jblas.FloatMatrix;
import org.jblas.Solve;

import meshes.PointCloud;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;


public class SSDMatricesJBlaze {
	
	/**
	 * One line per point, One column per vertex,
	 * enforcing that the interpolation of the Octree vertex values
	 * is zero at the point position.
	 *
	 */
	public static FloatMatrix D0Term(HashOctree tree, PointCloud cloud){
		FloatMatrix mat2 = new FloatMatrix(cloud.points.size(), tree.numberOfVertices());
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
				mat2.put(pointIdx, c.getCornerElement(i, tree).getIndex(), weight);
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
	public static FloatMatrix D1Term(HashOctree tree, PointCloud cloud) {
		FloatMatrix mat2 = new FloatMatrix(cloud.points.size()*3, tree.numberOfVertices());
		int pointIdx = 0;
		for (Point3f p: cloud.points) {
			HashOctreeCell c = tree.getCell(p);
			float gradientNormalizationTerm = 1/(4*c.side);
			for (int i = 0; i < 8; i++) {
				float xGrad = (i & 0b100) == 0b100 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float yGrad = (i & 0b010) == 0b010 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float zGrad = (i & 0b001) == 0b001 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				int idx = c.getCornerElement(i, tree).getIndex();
				mat2.put(pointIdx*3 + 0, idx, xGrad);
				mat2.put(pointIdx*3 + 1, idx, yGrad);
				mat2.put(pointIdx*3 + 2, idx, zGrad);
			}
			pointIdx++;
		}		
		return mat2;
	}
	
	
	
	public static FloatMatrix RTerm(HashOctree tree){
		FloatMatrix mat2 = new FloatMatrix(0, tree.numberOfVertices());
		float scaleFactor = 0;
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
				FloatMatrix newRows = new FloatMatrix(3, tree.numberOfVertices());
				newRows.put(0, j.getIndex(), 1);
				newRows.put(1, k.getIndex(), -dist_ij/(dist_ik));
				newRows.put(2, i.getIndex(), -dist_kj/(dist_ik));
				mat2 = FloatMatrix.concatVertically(mat2, newRows);
				scaleFactor += dist_ij*dist_kj;
			}
		}
		mat2.mul(1/scaleFactor);
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
	public static float[] ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
		int N = tree.numberOfVertices();
		FloatMatrix D0 = D0Term(tree, pc);
		D0.muli((float) Math.sqrt(lambda0/N));

		FloatMatrix b = new FloatMatrix(D0.rows, 1);
		for (int i = 0; i < D0.rows; i++)
			b.put(i, 0, 0f);
		FloatMatrix D1 = D1Term(tree, pc);
		float scaleD1 =  (float) Math.sqrt(lambda1/N);
		D1.muli(scaleD1);
		FloatMatrix A = FloatMatrix.concatVertically(D0, D1);
		FloatMatrix b_D1 = new FloatMatrix(D1.rows, 1);
		int idx = 0;
		for (Vector3f n: pc.normals) {
			b_D1.put(idx++, 0, n.x*scaleD1);
			b_D1.put(idx++, 0, n.y*scaleD1);
			b_D1.put(idx++, 0, n.z*scaleD1);
		}
		b = FloatMatrix.concatVertically(b, b_D1);
		FloatMatrix R = RTerm(tree);
		//careful, the 1/sum(..) was already scaled in method
		float scaleR = (float) Math.sqrt(lambda2);
		R.muli(scaleR);
		A = FloatMatrix.concatVertically(A, R);
		FloatMatrix b_R = new FloatMatrix(R.rows, 1);
		for (int i = 0; i < D0.rows; i++)
			b_R.put(i, 0, 0f);
		b = FloatMatrix.concatVertically(b, b_R);
		FloatMatrix X = Solve.solve(A, b); //lulz not least squares

		return X.toArray();
	}

}
