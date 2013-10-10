package assignment3;

import meshes.PointCloud;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;


public class SSDMatrices {
	
	
	/**
	 * Example Matrix creation:
	 * Create an identity matrix, clamped to the provided format.
	 */
	public static CSRMatrix eye(int nRows, int nCols){
		CSRMatrix eye = new CSRMatrix(0, nCols);
		
		//initialize the identiti matrix part
		for(int i = 0; i< Math.min(nRows, nCols); i++){
			eye.addRow();
			eye.lastRow().add(
						//column i, vlue 1
					new col_val(i,1));
		}
		//fill up the matrix with empt rows.
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
		
		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());
				
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
		
		// Do your stuff
		
		return null;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 correxponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {
		
		//Do your stuff
		
		return null;
	}
	
	
	
	public static CSRMatrix RTerm(HashOctree tree){
		
		//Do your stuff
		
		return null;
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
		system.mat = null;
		system.b = null;
		return system;
	}

}
