package sparse.solver;

import java.io.IOException;
import java.util.ArrayList;

import sparse.CSRMatrix;
import sparse.SCIPY;


/**
 * This solver calls a Python script to solve the given equations.
 * @author bertholet
 *
 */
public class SciPySolver extends Solver{
	
	String filePrefix;
	
	/**
	 * under what prefix to store the files when dumping them in the temp folder
	 * @param prefix
	 */
	public SciPySolver(String prefix){
		filePrefix = prefix;
	}

	@Override
	public void solve(CSRMatrix mat, ArrayList<Float> b, ArrayList<Float> x) {
		System.out.println("writing matrix files...");
		try {
			SCIPY.toPythonFiles(mat, b, filePrefix);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		System.out.println("starting to solve...");
		try {
			SCIPY.runLeastSquaresPy(filePrefix, x);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
}
