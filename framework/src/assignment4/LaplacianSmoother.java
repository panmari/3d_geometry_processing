package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Tuple3f;

import openGL.gl.GLDisplayable;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import sparse.CSRMatrix;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment3.SSDMatrices;

public class LaplacianSmoother {

	public static GLDisplayable smoothMixedCotan(HalfEdgeStructure hs) {
		CSRMatrix mMixed = LMatrices.mixedCotanLaplacian(hs);
		return smooth(hs, mMixed);
	}
	
	public static GLDisplayable smoothUniform(HalfEdgeStructure hs) {
		CSRMatrix mUniform = LMatrices.uniformLaplacian(hs);
		return smooth(hs, mUniform);
	}
	
	private static GLDisplayable smooth(HalfEdgeStructure hs, CSRMatrix m) {
		float lambda = 0.01f;
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
}
