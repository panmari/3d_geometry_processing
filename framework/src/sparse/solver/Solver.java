package sparse.solver;

import java.util.ArrayList;

import sparse.CSRMatrix;
import sparse.LinearSystem;

public abstract class Solver {
	
	/**
	 * x will be used as an initial guess, the result will be stored in x
	 * @param mat
	 * @param b
	 * @param x
	 */
	public abstract void solve(CSRMatrix mat, ArrayList<Float> b,ArrayList<Float> x);
	
	
	public void solve(LinearSystem l, ArrayList<Float> x){
		if(l.mat.nCols == l.mat.nRows){
			solve(l.mat, l.b, x);
		}
		else{
			throw new UnsupportedOperationException("can solve only square mats");
		}
	}
	
	

}
