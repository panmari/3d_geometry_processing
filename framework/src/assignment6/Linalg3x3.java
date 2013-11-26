package assignment6;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;


/**
 * <p>Implementation of an SVD, QR and EVD algorithm for
 * 3x3 matrices. Numerically stable in the presence of zero eigenvalues
 * or singular values. The algorithm is based on Givens rotations and
 * allows to sacrifices precision for speed by reducing the givensIterations
 * field.</p>
 * <p>
 * This algorithm uses simple approximations instead of trigonometric operations
 * as proposed in the original paper Aleka McAdams et Al. (Computing the singular
 * value decomposition of 3x3 matrixes with minimal branching and elementary floating
 * point operations) and could be further sped up by using fast squareroots instead of
 * the Math.sqrt(); using more iterations the quality of the results should still be the
 * same.
 * </p>
 * Note that this algorithm should work well on gpus too.
 * @author Alf
 *
 */
public class Linalg3x3 {

	
	//allocate data only once.
	Matrix3f buffer = new Matrix3f();
	Vector3f[] vec = {new Vector3f(), new Vector3f(), new Vector3f()};
	int[] permutation = {0,1,2};
	int[] sign = {1};
	
	
	//the more iterations, the more accurate the svd will be (the iterations
	//influences the JacobiEVD)
	// 3 iterations will give poor quality SVD's, 
	//but the approx seems to be sufficiently good for deformations
	int givensIterations;
	
	
	final static float sqrt_0p5 = 0.70710678118f;
	
	
	/**
	 * the more iterations, the more accurate the svd will be (the iterations
	 * influences the JacobiEVD), but the decomposition will take more time.
	 * three iterations are the minimum, with 10-15 iterations you get precise decompositions
	 * @param iterations
	 */
	public Linalg3x3(int iterations){
		this.givensIterations = iterations;
	}
	
	/**
	 * SVD algorithm for 3x3 matrices, roughly following Aleka McAdams et Al. (Computing the singular
	 * value decomposition of 3x3 matrixes with minimal branching and elementary floating
	 * point operations), without quaternions.
	 * 
	 * U sigma V^T = A
	 * 
	 * @param A
	 * @param u
	 * @param sigma
	 * @param v
	 */
	public void svd(Matrix3f A, Matrix3f u, Matrix3f sigma, Matrix3f v) {
		//first: evd of A'A
		Matrix3f tmp = new Matrix3f();
		tmp.mulTransposeLeft(A, A);
		
		jacobiEVD(tmp, v, sigma);
		
		tmp.mul(A,v);
		
		//sort tmp = 'B' = U sigma =  Av by order of column magnitudes
		//and sort V in the same way.
		
		tmp.getColumn(0, vec[0]);
		tmp.getColumn(1, vec[1]);
		tmp.getColumn(2, vec[2]);
		
		
		sign[0] = 1;
		permutation[0] = 0; permutation[1] = 1; permutation[2] = 2;
		findSortingPermutation(vec[0], vec[1], vec[2], permutation, sign);
		tmp.setColumn(0, vec[permutation[0]]);
		tmp.setColumn(1, vec[permutation[1]]);
		vec[permutation[2]].scale(sign[0]);
		tmp.setColumn(2, vec[permutation[2]]);
		
		
		v.getColumn(0, vec[0]);
		v.getColumn(1, vec[1]);
		v.getColumn(2, vec[2]);
		v.setColumn(0, vec[permutation[0]]);
		v.setColumn(1, vec[permutation[1]]);
		vec[permutation[2]].scale(sign[0]);
		v.setColumn(2, vec[permutation[2]]);
		
		givensQr(tmp, u, sigma);
		
		sigma.m01 = sigma.m02 = sigma.m10 = sigma.m12 = 
				sigma.m20 = sigma.m21 =0;
		
		u.m00 *= (sigma.m00 >0? 1 : -1);
		u.m10 *= (sigma.m00 >0? 1 : -1);
		u.m20 *= (sigma.m00 >0? 1 : -1);
		u.m01 *= (sigma.m11 >0? 1 : -1);
		u.m11 *= (sigma.m11 >0? 1 : -1);
		u.m21 *= (sigma.m11 >0? 1 : -1);
		u.m02 *= (sigma.m22 >0? 1 : -1);
		u.m12 *= (sigma.m22 >0? 1 : -1);
		u.m22 *= (sigma.m22 >0? 1 : -1);
		sigma.m00 = Math.abs(sigma.m00);
		sigma.m11 = Math.abs(sigma.m11);
		sigma.m22 = Math.abs(sigma.m22);
		
			
	}

	private void findSortingPermutation(Vector3f vec0, Vector3f vec1, Vector3f vec2,
			int[] abc, int[] sign) {
		float tempf;
		int tempi;
		float sig0_sqr = vec0.lengthSquared();
		float sig1_sqr = vec1.lengthSquared();
		float sig2_sqr = vec2.lengthSquared();
		boolean c;
		
		//swap 0,1
		tempi = abc[0];
		c = sig0_sqr < sig1_sqr;
		abc[0] = (c? abc[1]: abc[0]);
		abc[1] = (c? tempi: abc[1]);
		tempf = sig0_sqr;
		sig0_sqr = (c? sig1_sqr: sig0_sqr);
		sig1_sqr = (c? tempf: sig1_sqr);
		sign[0] *= (c? -1 : 1);
		
		//swap 0,2
		c = sig0_sqr < sig2_sqr;
		tempi = abc[0];
		abc[0] = (c? abc[2]: abc[0]);
		abc[2] = (c? tempi: abc[2]);
		tempf = sig0_sqr;
		sig0_sqr = (c? sig2_sqr: sig0_sqr);
		sig2_sqr = (c? tempf: sig2_sqr);
		sign[0] *= (c? -1 : 1);
		
		//swap 1,2
		c = sig1_sqr < sig2_sqr;
		tempi = abc[1];
		abc[1] = (c? abc[2]: abc[1]);
		abc[2] = (c? tempi: abc[2]);
		tempf = sig1_sqr;
		sig1_sqr = (c? sig2_sqr: sig1_sqr);
		sig2_sqr = (c? tempf: sig2_sqr);
		sign[0] *= (c? -1 : 1);
		
	}

	/**
	 * QR factorization of m. 		
	 * R is now an upper triangle matrix, Q^T*M = R, QR = M
	 * @param R 
	 * @param Q 
	 * @param tmp
	 */
	public void givensQr(Matrix3f m, Matrix3f Q, Matrix3f R) {
		R.set(m);
		Q.setIdentity();

		givens_qr(R, Q, 1,0, buffer);
		givens_qr(R, Q, 2,0, buffer);
		givens_qr(R, Q, 2,1, buffer);
		
		//R is now an upper triangle matrix,
		//Q^T*M = R
		//QR = M


	}

	/**
	 * Anihilating one entry in the lower diag matrix, using a 2d rotation
	 * @param R
	 * @param Q
	 * @param p
	 * @param q
	 * @param buff
	 */
	private void givens_qr(Matrix3f R, Matrix3f Q, int p, int q, Matrix3f buff) {
		
		float c = R.getElement(q, q)/ (float) Math.sqrt(
				R.getElement(q, q)*R.getElement(q, q) + R.getElement(p, q)*R.getElement(p, q) );
		float s = R.getElement(p, q)/ (float) Math.sqrt(
				R.getElement(q, q)*R.getElement(q, q) + R.getElement(p, q)*R.getElement(p, q) );

		buff.setIdentity();
		buff.setElement(p,p,c);
		buff.setElement(p,q,-s);
		buff.setElement(q,p,s);
		buff.setElement(q,q,c);
		
		
		Q.mulTransposeRight(Q,buff);
		R.mul(buff, R);
		
	}

	/**
	 * Jacobi iteration to solve make an eigenvalue decomposition. U sigma U^T = A
	 * @param ata
	 * @param q
	 * @param sigma
	 */
	public void jacobiEVD(Matrix3f ata, Matrix3f q, Matrix3f sigma) {
		q.setIdentity();
		
		sigma.set(ata);
		for(int i = 0; i < givensIterations; i++){
			givens(sigma, 0,1,q,buffer);
			givens(sigma, 0,2,q, buffer);
			givens(sigma, 1,2,q, buffer);
		}
	}

	private void givens(Matrix3f ata, int p, int q, Matrix3f q_mat, Matrix3f buff) {
		float c,s;
	
		//correct givens rotation
		//but atan, cos and sin are slow...
		/*float theta;
		if(Math.abs(ata.getElement(p, p) - ata.getElement(q, q)) < 1e-8){
			theta = (float) Math.PI/4;
		}
		else{
			theta = (float) Math.atan(2*ata.getElement(p, q)/
					(ata.getElement(p, p) - ata.getElement(q, q)))/2.f;
			
		}
		c= (float)Math.cos(theta); s= (float)Math.sin(theta);*/
		
		//approximate givens rotation:
		boolean b = ata.getElement(p,q)*ata.getElement(p,q) <
				Math.pow((ata.getElement(p,p) -
						ata.getElement(q,q)),2);
		float omega = 1.f/(float)Math.sqrt(ata.getElement(p,q)*ata.getElement(p,q) +
				Math.pow((ata.getElement(p,p) -
						ata.getElement(q,q)),2));
		
		s= (b? omega* ata.getElement(p,q) : sqrt_0p5);
		c= (b? omega* (ata.getElement(p,p) -ata.getElement(q,q)) : sqrt_0p5);
		
		buff.setIdentity();
		buff.setElement(p,p,c);
		buff.setElement(p,q,-s);
		buff.setElement(q,p,s);
		buff.setElement(q,q,c);
		

		q_mat.mul(buff);
		ata.mul(ata, buff);
		buff.transpose();
		ata.mul(buff, ata);
		
	}
}
