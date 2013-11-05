package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Point3f;

import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.HalfEdgeStructure;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SCIPYEVD;

public class SpectralSmoothing {

	/**
	 * Given HS will have smoothed vertices. More eigenvectors leads to less smoothing.
	 * @param hs
	 * @param nrEigenVectors
	 * @throws IOException
	 */
	public static void smooth(HalfEdgeStructure hs, int nrEigenVectors) throws IOException {
		CSRMatrix mLaplacian = LMatrices.symmetricCotanLaplacian(hs);
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		SCIPYEVD.doSVD(mLaplacian, "eigenStuff", nrEigenVectors, eigenValues, eigenVectors);
		CSRMatrix mEigen = new CSRMatrix(0, hs.getVertices().size());
		for (int i = 0; i < nrEigenVectors; i++) {
			ArrayList<col_val> row = mEigen.addRow();
			Iterator<Float> iter = eigenVectors.get(i).iterator();
			for (int j = 0; j < hs.getVertices().size(); j++) {
				row.add(new col_val(j, iter.next()));
			}
			Collections.sort(row);
		}
		CSRMatrix mEigenT = mEigen.transposed();
		CSRMatrix result = new CSRMatrix(0, 0);
		mEigenT.multParallel(mEigen, result);
		ArrayList<Point3f> smoothedVertices = new ArrayList<Point3f>();
		result.multTuple(hs.getVerticesAsPointArray(), smoothedVertices);
		hs.setVerticesTo(smoothedVertices);
	}
}
