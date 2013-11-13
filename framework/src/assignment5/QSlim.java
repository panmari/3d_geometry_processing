package assignment5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


/** 
 * Implement the QSlim algorithm here
 * 
 * @author Alf
 *
 */
public class QSlim {
	
	HashMap<Vertex, Matrix4f> hm = new HashMap<Vertex, Matrix4f>();
	HashMap<HalfEdge, PotentialCollapse> mostRecentCollapse = new HashMap<HalfEdge, PotentialCollapse>();
	PriorityQueue<PotentialCollapse> collapses = new PriorityQueue<PotentialCollapse>();
	private HalfEdgeCollapse hec;
	
	/**
	 * Compute per vertex matrices
	 * Compute edge collapse costs,
	 * Fill up the Priority queue/heap or similar
	 */
	public QSlim(HalfEdgeStructure hs){
		
		for(Vertex v: hs.getVertices()) {
			Matrix4f qem = new Matrix4f();
			Iterator<Face> iter = v.iteratorVF();
			while (iter.hasNext())
				qem.add(iter.next().getQuadricErrorMatrix());
			hm.put(v, qem);
		}
		for (HalfEdge he: hs.getHalfEdges()) {
			new PotentialCollapse(he);
		}
		this.hec = new HalfEdgeCollapse(hs);
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached.
	 * @param target
	 */
	public void simplify(int target){
		while (target > 0) {
			target -= collapsCheapestEdge();
		}
		
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public int collapsCheapestEdge(){
		PotentialCollapse pc = collapses.poll();
		if (pc.isDeleted)
			return 0;
		HalfEdge he = pc.he;
		if (hec.isCollapseMeshInv(he, he.end().getPos()) ||
				!HalfEdgeCollapse.isEdgeCollapsable(he)) {
			new PotentialCollapse(he, (pc.cost + 0.1f)*10);
			return 0;
		}
		hec.collapseEdge(he);
		for (HalfEdge deadHE: hec.deadEdges)
			mostRecentCollapse.get(deadHE).isDeleted = true;
		return 1;
	}

	/**
	 * Represent a potential collapse
	 * @author Alf
	 *
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse>{

		float cost;
		HalfEdge he;
		boolean isDeleted;
		Vector3f targetPosition;
		
		PotentialCollapse(HalfEdge he, float cost) {
			this.he = he;	
			this.targetPosition = new Vector3f();
			targetPosition.add(he.end().getPos(), he.start().getPos());
			targetPosition.scale(1/2f);
			computeCost(cost);
		}
		
		PotentialCollapse(HalfEdge he) {
			this(he, 0.f);
		}
		
		private void computeCost(float cost) {
			if (cost == 0.f) {
				Matrix4f qem = new Matrix4f();
				qem.add(hm.get(he.start()), hm.get(he.end()));
				Vector3f Qp = new Vector3f(targetPosition);
				qem.transform(Qp);
				this.cost = Qp.dot(targetPosition);
			}
			else this.cost = cost;
			collapses.add(this);
			mostRecentCollapse.put(he, this);
		}
		
		@Override
		public int compareTo(PotentialCollapse other) {
			return (int) Math.signum(this.cost - other.cost);
		}
	}

}
