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
import sparse.solver.JMTSolver;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;
import assignment3.SSDMatrices;

public class LaplacianSmoother {

	public static GLDisplayable smooth(HalfEdgeStructure hs, CSRMatrix m, float lambda) {
		int nrVertices = hs.getVertices().size();
		
		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(nrVertices);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(nrVertices);
		getVertices(hs, m, lambda, vertices, smoothedVertices);
		
		GLHalfedgeStructure glHsSmooth = new GLHalfedgeStructure(hs);
		glHsSmooth.add(smoothedVertices, "position");
		return glHsSmooth;
	}
	
	/**
	 * Expects two empty lists vertices and smoothedVertices, which are then filled with stuff.
	 * @param hs
	 * @param m
	 * @param lambda
	 * @param vertices
	 * @param smoothedVertices
	 */
	private static void getVertices(HalfEdgeStructure hs, CSRMatrix m, float lambda,
			ArrayList<Tuple3f> vertices, ArrayList<Tuple3f> smoothedVertices) {
		int nrVertices = hs.getVertices().size();
		CSRMatrix I = SSDMatrices.eye(nrVertices, nrVertices);
		m.scale(-lambda);
		CSRMatrix smoothM = new CSRMatrix(0,0);
		smoothM.add(I, m);
		for (Vertex v: hs.getVertices()) {
			vertices.add(v.getPos());
		}
		Solver solver = new JMTSolver();
		solver.solveTuple(smoothM, vertices, smoothedVertices);
	}
		
	public static GLDisplayable unsharpMasking(HalfEdgeStructure hs, CSRMatrix m, float lambda, float s) {
		int nrVertices = hs.getVertices().size();
		
		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(nrVertices);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(nrVertices);
		getVertices(hs, m, lambda, vertices, smoothedVertices);
		
		ArrayList<Tuple3f> sharpenedVertices = new ArrayList<Tuple3f>(nrVertices);
		for (int i = 0; i < nrVertices; i++) {
			Vector3f v = new Vector3f(vertices.get(i));
			v.sub(smoothedVertices.get(i));
			v.scale(s);
			v.add(smoothedVertices.get(i));
			sharpenedVertices.add(v);
		}
		
		GLHalfedgeStructure glHsSharp = new GLHalfedgeStructure(hs);
		glHsSharp.add(sharpenedVertices, "position");
		return glHsSharp;
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
