package assignment4;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import sparse.solver.Solver;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


/**
 * A couple of helper methods to copy data from and to Halfedge structures.
 * @author Alf
 *
 */
public class HSCopyTools {

		
	/**
	 * Copy the x (what= 0), y (what = 1) or z (what = 2) coordinates to the target.
	 * @param hs
	 * @param target
	 * @param what
	 */
	public static void copy(ArrayList<Float> pos, HalfEdgeStructure target, int what) {
		for(Vertex v : target.getVertices()){
			switch (what) {
			case 0:
				v.getPos().x = pos.get(v.index);
				break;
			case 1:
				v.getPos().y = pos.get(v.index);
				break;
			case 2:
				v.getPos().z = pos.get(v.index);
				break;
			}
		}
	}


	/**
	 * Copy the x (what= 0), y (what = 1) or z (what = 2) coordinates to target.
	 * @param hs
	 * @param target
	 * @param what
	 */
	public static void copy(HalfEdgeStructure hs, ArrayList<Float> target, int what) {
		target.clear();
		for(Vertex v : hs.getVertices()){
			target.add(0.f);
		}
		for(Vertex v : hs.getVertices()){
			switch (what) {
			case 0:
				target.set(v.index, v.getPos().x);
				break;
			case 1:
				target.set(v.index, v.getPos().y);
				break;
			case 2:
				target.set(v.index, v.getPos().z);
				break;
			}
		}
	}
	
	
	/**
	 * Copy the positions to the target vertex positions
	 * @param pos
	 * @param target
	 */
	public static void copy(ArrayList<Point3f> pos, HalfEdgeStructure target) {
		for(Vertex v : target.getVertices()){
			v.getPos().set(pos.get(v.index));
		}
	}


	/**
	 * copy the vertex positions to the target list
	 * @param hs
	 * @param target
	 */
	public static void copy(HalfEdgeStructure hs, ArrayList<Point3f> target) {
		target.clear();
		for(Vertex v : hs.getVertices()){
			target.add(new Point3f(v.getPos()));
		}
	}

}
