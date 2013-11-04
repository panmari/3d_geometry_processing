package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.math.FloatUtil;

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

	public static final boolean JMT = true;
	
	public static void smooth(HalfEdgeStructure hs, CSRMatrix m, float lambda) {
		int nrVertices = hs.getVertices().size();
		float volumeBefore = getVolume(hs);
		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(nrVertices);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(nrVertices);
		getVertices(hs, m, lambda, vertices, smoothedVertices);	
		Iterator<Vertex> hsViter = hs.iteratorV();
		Iterator<Tuple3f> smoothedViter = smoothedVertices.iterator();
		while (hsViter.hasNext())
			hsViter.next().getPos().set(smoothedViter.next());
		
		rescale(hs, volumeBefore);
	}
	
	private static void rescale(HalfEdgeStructure hs, float volumeBefore) {
		float volumeAfter = getVolume(hs);
		float volumeRatio = FloatUtil.pow(volumeBefore/volumeAfter, 1/3f);
		Iterator<Vertex> hsViter = hs.iteratorV();
		while (hsViter.hasNext())
			hsViter.next().getPos().scale(volumeRatio);
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
			vertices.add(new Point3f(v.getPos()));
		}
		Solver solver;
		if (JMT)
			solver = new JMTSolver();
		else 
			solver = new SciPySolver("laplacian_stuff");
		solver.solveTuple(smoothM, vertices, smoothedVertices);
	}
		
	public static void unsharpMasking(HalfEdgeStructure hs, CSRMatrix m, float lambda, float s) {
		int nrVertices = hs.getVertices().size();
		float volumeBefore = getVolume(hs);
		
		ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>(nrVertices);
		ArrayList<Tuple3f> vertices = new ArrayList<Tuple3f>(nrVertices);
		getVertices(hs, m, lambda, vertices, smoothedVertices);
		
		for (int i = 0; i < nrVertices; i++) {
			Vector3f v = new Vector3f(vertices.get(i));
			v.sub(smoothedVertices.get(i));
			v.scale(s);
			v.add(smoothedVertices.get(i));
			hs.getVertices().get(i).getPos().set(v);
		}
		rescale(hs, volumeBefore);
	}
	
	/**
	 * @param hs
	 * @return
	 */
	static float getVolume(HalfEdgeStructure hs) {
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
