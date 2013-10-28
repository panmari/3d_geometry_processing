package sparse.solver;

import java.util.ArrayList;


import sparse.CSRMatrix;
import sparse.SparseTools;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.BiCGstab;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.DiagonalPreconditioner;
import no.uib.cipr.matrix.sparse.ICC;
import no.uib.cipr.matrix.sparse.ILU;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;

/**
 * Wrapper for the conjugent gradient and stabilized biconjugate gradient
 * Java matrix toolkit algorithms. in Ax =  b, b is taken as an initial guess.
 * @author Alf
 *
 */
public class JMTSolver extends Solver{
	
	final int cg = 0, bcstab = 1;  
	int type;
	
	public JMTSolver(){
		this.type = bcstab;
	}
	
	public JMTSolver(int type){
		this.type = type;
	}
	
	/**
	 * solves A x = b and takes b as an initial guess.
	 * @param m
	 * @param x
	 */
	public void solve(CSRMatrix m, ArrayList<Float> b, ArrayList<Float> x){
		System.out.println("Starting the solver...");
		DenseVector b_ = SparseTools.denseVector(b);
		DenseVector x_ = b_.copy();
		
		CompRowMatrix mat = SparseTools.createCRMatrix(m);
		
		//construct the specified JMT solver
		IterativeSolver solver = (type == bcstab? new BiCGstab(b_): new CG(b_) );
		
		//this preconditioner works best/ leads most consistently to convergence.
		Preconditioner M = new DiagonalPreconditioner(mat.numRows());///*new ICC(mat.copy());	// new ILU(mat.copy());
		M.setMatrix(mat);
		solver.setPreconditioner(M);
		solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());
		((DefaultIterationMonitor) solver.getIterationMonitor()).setRelativeTolerance(1e-3);
		((DefaultIterationMonitor) solver.getIterationMonitor()).setMaxIterations(2000);
		
		//try to solve the equation
		try {
			solver.solve(mat, b_, x_);
		} catch (IterativeSolverNotConvergedException e) {
			System.err.println("Iterative Solver did not converge");
		}
		
		//copy the result back
		for(int i = 0; i < x_.size(); i++){
			x.set(i, (float) x_.get(i));
		}
	}

}
