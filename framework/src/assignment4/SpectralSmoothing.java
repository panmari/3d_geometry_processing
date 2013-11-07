package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Point3f;

import org.jblas.FloatFunction;

import com.jogamp.opengl.math.FloatUtil;

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
	public static void smooth(HalfEdgeStructure hs, int nrEigenVectors, FloatFunction ffs) throws IOException {
		CSRMatrix mLaplacian = LMatrices.symmetricCotanLaplacian(hs);
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		SCIPYEVD.doSVD(mLaplacian, "eigenStuff", nrEigenVectors, eigenValues, eigenVectors);
		CSRMatrix mEigen = new CSRMatrix(0, hs.getVertices().size());
		CSRMatrix mFreqBoost = new CSRMatrix(0, nrEigenVectors);
		String freqString = "Frequencies: ";
		for (int i = 0; i < nrEigenVectors; i++) {
			ArrayList<col_val> row = mEigen.addRow();
			Iterator<Float> iter = eigenVectors.get(i).iterator();
			for (int j = 0; j < hs.getVertices().size(); j++) {
				row.add(new col_val(j, iter.next()));
			}
			Collections.sort(row);
			//add boost to diagonal of frequency boosting matrix
			float freq = FloatUtil.sqrt(Math.abs(eigenValues.get(i)));
			float boost = ffs.compute(freq);
			freqString += freq + ", ";
			mFreqBoost.addRow().add(new col_val(i, boost));
		
		}
		System.out.println(freqString);
		CSRMatrix mEigenT = mEigen.transposed();
		CSRMatrix boosted = new CSRMatrix(0, 0);
		mEigenT.multParallel(mFreqBoost, boosted);
		CSRMatrix result = new CSRMatrix(0, 0);
		boosted.multParallel(mEigen, result);
		ArrayList<Point3f> smoothedVertices = new ArrayList<Point3f>();
		result.multTuple(hs.getVerticesAsPointArray(), smoothedVertices);
		hs.setVerticesTo(smoothedVertices);
	}
	
	public static void smooth(HalfEdgeStructure hs, int nrEigenVectors) throws IOException {
		smooth(hs, nrEigenVectors, new FloatFunction() {
			@Override
			public float compute(float x) {
				return 1;
			}
		});
	}
	
	public static void boostLargeFrequencies(HalfEdgeStructure hs, int nrEigenVectors) throws IOException {
		smooth(hs, nrEigenVectors, new FloatFunction() {
			@Override
			public float compute(float x) {
				if (x > 5)
					return 2;
				else
					return 1;
			}
		});
	}
	
	public static void boostMediumFrequencies(HalfEdgeStructure hs, int nrEigenVectors) throws IOException {
		smooth(hs, nrEigenVectors, new FloatFunction() {
			@Override
			public float compute(float x) {
				if (x > 2 && x < 5)
					return 2;
				else
					return 1;
			}
		});
	}

}
