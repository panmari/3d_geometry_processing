package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import sparse.CSRMatrix;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment3.SSDMatrices;

public class LaplacianSmoother {

	public static GLDisplayable smoothMixedCotan(HalfEdgeStructure hs, float lambda) {
		CSRMatrix mMixed = LMatrices.mixedCotanLaplacian(hs);
		return smooth(hs, mMixed, lambda);
	}
	
	public static GLDisplayable smoothUniform(HalfEdgeStructure hs, float lambda) {
		CSRMatrix mUniform = LMatrices.uniformLaplacian(hs);
		return smooth(hs, mUniform, lambda);
	}
	
	private static GLDisplayable smooth(HalfEdgeStructure hs, CSRMatrix m, float lambda) {
		int nrVertices = hs.getVertices().size();
		CSRMatrix I = SSDMatrices.eye(nrVertices, nrVertices);
		m.scale(-lambda);
		CSRMatrix smoothM = new CSRMatrix(0,0);
		smoothM.add(I, m);
		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(nrVertices);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(nrVertices);
		for (Vertex v: hs.getVertices()) {
			vertices.add(v.getPos());
		}
		Solver solver = new SciPySolver("smooth_demo");
		solver.solveTuple(smoothM, vertices, smoothedVertices);
		
		GLHalfedgeStructure glHsSmooth = new GLHalfedgeStructure(hs);
		glHsSmooth.add(smoothedVertices, "position");
		return glHsSmooth;
	}
	
	/**
	 * TODO: rescale smoothed stuff to original volume
	 * @param hs
	 * @return
	 */
	private static float getVolume(HalfEdgeStructure hs) {
		float sum = 0;
		for (Face f: hs.getFaces()) {
			Iterator<Vertex> iter = f.iteratorFV();
			Vector3f p1 = new Vector3f(iter.next().getPos());
			Vector3f p2 = new Vector3f(iter.next().getPos());
			Vector3f p3 = new Vector3f(iter.next().getPos());
			Vector3f cross = new Vector3f();
			cross.cross(p2, p3);
			sum += p1.dot(cross);
		}
		return sum/6;
	}
}
