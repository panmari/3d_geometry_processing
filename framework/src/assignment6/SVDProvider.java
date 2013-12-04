package assignment6;

import javax.vecmath.Matrix3f;

public interface SVDProvider {
	void svd(Matrix3f A, Matrix3f u, Matrix3f sigma, Matrix3f v);
}
