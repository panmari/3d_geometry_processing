package assignment6;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseCholesky;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.UpperTriangDenseMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SparseTools;

/**
 * Implementation/ wrapper of a Cholesky Solver.
 * The Cholesky decomposition method for dense matrices provided in the
 * Java matrix toolkit libraries is used for decomposition; the
 * forward and backward substitution is reimplemented for sparse matrices
 * to speed up the solves. 
 * @author Alf
 *
 */
public class Cholesky {

	
	private DenseCholesky chl;
	DenseMatrix b;
	private CSRMatrix sparse_u;
	private CSRMatrix sparse_uT;
	
	ArrayList<Float> z;

	
	/**
	 * Compute the cholesky decomposition of m.
	 * 
	 * This class and the contained cholesky decomposition can not be recycled
	 * if the matrix m changes.
	 * @param m
	 */
	public Cholesky(CSRMatrix m){
		assert(m.nCols == m.nRows);
		CompRowMatrix mat = SparseTools.createCRMatrix(m);
		
		z = new ArrayList<>();
		for(int i = 0; i < m.nCols; i++){
			z.add(0.f);
		}
		

		System.out.println(mat.isSquare());
		
		
		System.out.println("starting cholesky...");
		chl = DenseCholesky.factorize(mat);
		
		if(!chl.isSPD()){
			System.out.println("Cholesky Decomposition is not possible: Matrix not symmetric or positive definit");
			throw new RuntimeException("Cholesky Decomposition is not possible: Matrix not symmetric or positive definit");
			
			//if this Exception is thrown, test if your matrix really is symmetric. If your matrix is not positive definite thought it should be
			//maybe you have a switched sign naking your matrix negative definite...
		}
		
		UpperTriangDenseMatrix u = chl.getU();
		
		//the cholesky factorization should be quite sparse, chl does not make use of this!
		sparse_u = toCsr(u);
		sparse_uT = sparse_u.transposed();
		
		System.out.println("pos semidef : " + chl.isSPD());
		System.out.println("condition : " + chl.rcond(mat));
		System.out.println("Did cholesky :)");
	}
	
	private CSRMatrix toCsr(Matrix u) {
		CSRMatrix result = new CSRMatrix(0,0);
		
		for(int i = 0; i < u.numRows(); i++){
			result.addRow();
			ArrayList<col_val> row = result.lastRow();
			for(int j = 0; j < u.numColumns(); j++){
				if(u.get(i, j) != 0){
					row.add(new col_val(j, (float) u.get(i,j)));
				}
			}
		}
		result.nCols = u.numColumns();
		result.nRows = u.numRows();
		return result;
	}


	/**
	 * forward substitution
	 * @param x
	 * @param b
	 */
	private void forwardSubst(ArrayList<Float> x, ArrayList<Float> b){
		ArrayList<col_val> row ;
		
		col_val diag_element;
		for(int i = 0; i < sparse_uT.nRows; i++){
			float val = b.get(i);
			
			diag_element = null;
			row = sparse_uT.rows.get(i);
			for(col_val c : row){
				assert(c.col <= i);
				if(c.col < i){
					val -= c.val * x.get(c.col); 
				}
				else if(c.col == i){
					diag_element = c;
				}
			}
			
			x.set(i, val/diag_element.val);
		}
	}
	
	/**
	 * backward substitution
	 * @param x
	 * @param b
	 */
	private void backwardSubst(ArrayList<Float> x, ArrayList<Float> b){
		ArrayList<col_val> row ;
		
		col_val diag_element;
		for(int i = sparse_u.nRows -1; i >=0 ; i--){
			float val = b.get(i);
			diag_element = null;
			row = sparse_u.rows.get(i);
			for(col_val c : row){
				assert(c.col >= i);
				if(c.col > i){
					val -= c.val * x.get(c.col); 
				}
				else if(c.col == i){
					diag_element = c;
				}
			}
			
			x.set(i, val/diag_element.val);
		}
	}

	/**
	 *  solve mx = b
	 * @param b
	 * @param x
	 */
	public void solve(ArrayList<Float> b, ArrayList<Float> x){
		forwardSubst(z, b);
		backwardSubst(x, z);
	}
}
