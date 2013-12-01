package assignment6;

import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3f;

public class LinalgVecmath implements SVDProvider {

	@Override
	public void svd(Matrix3f A, Matrix3f u, Matrix3f sigma, Matrix3f v) {
		GMatrix g = new GMatrix(3, 3);
		GMatrix U = new GMatrix(g);
		GMatrix W = new GMatrix(g);
		GMatrix V = new GMatrix(g);
		g.set(A);
		g.SVD(U, W, V);
		U.get(u);
		W.get(sigma);
		V.get(v);
	}

}
