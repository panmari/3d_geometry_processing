package assignment3;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;


public class SSDMatrices {
	
	
	/**
	 * Example Matrix creation:
	 * Create an identity matrix, clamped to the provided format.
	 */
	public static CSRMatrix eye(int nRows, int nCols){
		CSRMatrix eye = new CSRMatrix(0, nCols);
		
		//initialize the identity matrix part
		for(int i = 0; i< Math.min(nRows, nCols); i++){
			eye.addRow();
			eye.lastRow().add(
						//column i, value 1
					new col_val(i,1));
		}
		//fill up the matrix with empty rows.
		for(int i = Math.min(nRows, nCols); i < nRows; i++){
			eye.addRow();
		}
		
		return eye;
	}
	
	
	/**
	 * Example matrix creation:
	 * Identity matrix restricted to boundary per vertex values.
	 */
	public static CSRMatrix Eye_octree_boundary(HashOctree tree){
		
		CSRMatrix result = new CSRMatrix(0, tree.numberOfVertices());
				
		for(HashOctreeVertex v : tree.getVertices()){
			if(MortonCodes.isVertexOnBoundary(v.code, tree.getDepth())){
				result.addRow();
				result.lastRow().add(new col_val(v.index,1));
			}
		}
		
		return result;
	}
	
	/**
	 * One line per point, One column per vertex,
	 * enforcing that the interpolation of the Octree vertex values
	 * is zero at the point position.
	 *
	 */
	public static CSRMatrix D0Term(HashOctree tree, PointCloud cloud){
		CSRMatrix mat = new CSRMatrix(0, tree.numberOfVertices());
		
		for (Point3f p: cloud.points) {
			
			HashOctreeCell c = tree.getCell(p);
			MarchableCube v_000 = c.getCornerElement(0b000, tree);
			Vector3f v_d = new Vector3f(p);
			v_d.sub(v_000.getPosition());
			v_d.scale(1/c.side); //scale to unit size.
			
			mat.addRow();
			ArrayList<col_val> currentRow = mat.lastRow();
			for(int i = 0; i < 8; i++) {
				float weight = (0b100 & i) != 0b100 ? 1 - v_d.x : v_d.x;  
				weight *= (0b010 & i) != 0b010 ? 1 - v_d.y : v_d.y; 
				weight *= (0b001 & i) != 0b001 ? 1 - v_d.z : v_d.z; 
				
				currentRow.add(new col_val(c.getCornerElement(i, tree).getIndex(), weight));
			}
		}		
		return mat;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 correxponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {
		CSRMatrix mat = new CSRMatrix(0, tree.numberOfVertices());
		
		for (Point3f p: cloud.points) {
			HashOctreeCell c = tree.getCell(p);
			float gradientNormalizationTerm = 1/(4*c.side);
			//add 3 rows, for x, y and z derivative
			ArrayList<col_val> xRow = mat.addRow();
			ArrayList<col_val> yRow = mat.addRow();
			ArrayList<col_val> zRow = mat.addRow();
			for (int i = 0; i < 8; i++) {
				float xGrad = (i & 0b100) == 0b100 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float yGrad = (i & 0b010) == 0b010 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				float zGrad = (i & 0b001) == 0b001 ? gradientNormalizationTerm : -gradientNormalizationTerm;
				int idx = c.getCornerElement(i, tree).getIndex();
				xRow.add(new col_val(idx, xGrad));
				yRow.add(new col_val(idx, yGrad));
				zRow.add(new col_val(idx, zGrad));
			}
		}		
		return mat;
	}
	
	
	
	public static CSRMatrix RTerm(HashOctree tree){
		CSRMatrix mat = new CSRMatrix(0, tree.numberOfVertices());
		float scaleFactor = 0;
		for (HashOctreeVertex j: tree.getVertices()) {
			for (int shift = 0b100; shift > 0b000; shift >>= 1) {
				HashOctreeVertex i = tree.getNbr_v2vMinus(j, shift); //nbr in minus direction
				if (i == null)
					continue;
				HashOctreeVertex k = tree.getNbr_v2v(j, shift); //nbr in plus direction
				if (k == null)
					continue;
				ArrayList<col_val> currentRow = mat.addRow();
				float dist_ij = i.getPosition().distance(j.getPosition());
				float dist_kj = k.getPosition().distance(j.getPosition());
				float dist_ik = dist_ij + dist_kj;
				currentRow.add(new col_val(j.getIndex(), 1));
				currentRow.add(new col_val(k.getIndex(), -dist_ij/(dist_ik)));
				currentRow.add(new col_val(i.getIndex(), -dist_kj/(dist_ik)));
				scaleFactor += dist_ij*dist_kj;
			}
		}
		mat.scale(1/scaleFactor);
		return mat;
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
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
				
		LinearSystem system = new LinearSystem();
		system.mat = new CSRMatrix(0, tree.numberOfVertices());
		system.b = new ArrayList<Float>();

		int N = tree.numberOfVertices();
		CSRMatrix D0 = D0Term(tree, pc);
		system.mat.append(D0, (float)Math.sqrt(lambda0/N));
		system.b.addAll(new ArrayList<Float>(Collections.nCopies(D0.nRows, 0f)));
		
		CSRMatrix D1 = D1Term(tree, pc);
		float scaleD1 =  (float) Math.sqrt(lambda1/N);
		system.mat.append(D1, scaleD1);
		
		for (Vector3f n: pc.normals) {
			system.b.add(n.x*scaleD1);
			system.b.add(n.y*scaleD1);
			system.b.add(n.z*scaleD1);
		}
		
		CSRMatrix R = RTerm(tree);
		//careful, the 1/sum(..) was already scaled in method
		float scaleR = (float) Math.sqrt(lambda2);
		system.mat.append(R, scaleR);
		system.b.addAll(new ArrayList<Float>(Collections.nCopies(R.nRows, 0f)));
		
		return system;
	}

}
