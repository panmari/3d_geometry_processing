package assignment4;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;

public class MinimalSurface {
	
	private static final boolean JMT = true;

	/**
	 * Threshold should be something between 0 and 1, usually 0.99 for a reasonable solution.
	 * @param hs, the halfedgeStructure you want to solve for.
	 * @param threshold, dictates, when we are close enough to the real solution.
	 */
	public static void solve(HalfEdgeStructure hs, float threshold) {
		float surfaceAreaBefore;
		float surfaceArea = hs.getSurfaceArea();
		Solver solver;
		if (JMT)
			solver = new JMTSolver();
		else 
			solver = new SciPySolver("laplacian_stuff");

		do {
			surfaceAreaBefore = surfaceArea;
			ArrayList<Vector3f> zeroCurvature = new ArrayList<Vector3f>();
			CSRMatrix mat = LMatrices.mixedCotanLaplacian(hs);
		
			for(Vertex v: hs.getVertices()) {
				if (v.isOnBoundary()) {
					zeroCurvature.add(new Vector3f(v.getPos()));
					//add identity constraint on row
					mat.rows.get(v.index).add(new col_val(v.index, 1f));
				}
				else
					zeroCurvature.add(new Vector3f()); // vector filled with 0
			}
			
			ArrayList<Vector3f> minifiedVertices = new ArrayList<Vector3f>();
			solver.solveTuple(mat, zeroCurvature, minifiedVertices);
			Iterator<Vertex> hsViter = hs.iteratorV();
			Iterator<Vector3f> minifiedVerticesIter = minifiedVertices.iterator();
			while (hsViter.hasNext())
				hsViter.next().getPos().set(minifiedVerticesIter.next());
			surfaceArea = hs.getSurfaceArea(); 
			System.out.println(surfaceArea/surfaceAreaBefore);
			//TODO: break if solver does not converge
		} while (surfaceArea/surfaceAreaBefore < threshold);
}
}
