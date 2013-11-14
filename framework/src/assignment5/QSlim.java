package assignment5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

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
			hm.put(v, makeErrorQuadricFor(v));
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
		hec.finish();
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public int collapsCheapestEdge(){
		PotentialCollapse pc = collapses.poll();
		if (pc.isDeleted || hec.isEdgeDead(pc.he))
			return 0;
		HalfEdge he = pc.he;
		if (hec.isCollapseMeshInv(he, pc.targetPosition) ||
				!HalfEdgeCollapse.isEdgeCollapsable(he)) {
			new PotentialCollapse(he, (pc.cost + 0.1f)*10);
			return 0;
		}
		
		//TODO: Update some error quadrics
		hec.collapseEdge(he, pc.targetPosition);
	
		updateEdgesAround(he.end(), pc.qem);
		return 1;
	}
	
	private Matrix4f makeErrorQuadricFor(Vertex v) {
		Matrix4f qem = new Matrix4f();
		Iterator<Face> iter = v.iteratorVF();
		while (iter.hasNext())
			qem.add(iter.next().getQuadricErrorMatrix());
		return qem;
	}
	
	private void updateEdgesAround(Vertex v, Matrix4f qem) {
		hm.put(v, qem);
		Iterator<HalfEdge> iter = v.iteratorVE();
		while (iter.hasNext()) {
			HalfEdge he = iter.next();
			new PotentialCollapse(he);
			new PotentialCollapse(he.getOpposite());
		}
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
		Point3f targetPosition;
		Matrix4f qem;
		
		/**
		 * Constructor also inserts this collapse into priority queue, marks
		 * older collapse of this edge as deleted and puts itself as most
		 * recent collapse into hashmap.
		 * @param he
		 * @param cost
		 */
		PotentialCollapse(HalfEdge he, float cost) {
			this.he = he;	
			computeCost(cost);
		}
		
		private void computeSimpleTargetPosition() {
			targetPosition =  new Point3f();
			targetPosition.add(he.end().getPos(), he.start().getPos());
			targetPosition.scale(1/2f);
		}
		
		private void computeOptimalTargetPosition(Matrix4f qem) {
			Matrix4f qOpt = new Matrix4f(qem);
			qOpt.setRow(3, 0, 0, 0, 1);
			if (qOpt.determinant() != 0) {
				qOpt.invert();
				Point3f optPos = new Point3f();
				qOpt.transform(optPos); //assumes w=1 automatically 
				targetPosition = optPos;
			} else
				computeSimpleTargetPosition();
		}
		
		PotentialCollapse(HalfEdge he) {
			this(he, 0.f);
		}
		
		private void computeCost(float cost) {
			if (cost == 0.f) {
				qem = new Matrix4f();
				qem.add(hm.get(he.start()), hm.get(he.end()));
				computeOptimalTargetPosition(qem);
				Vector4f Qp = new Vector4f(targetPosition);
				Qp.w = 1;
				qem.transform(Qp);
				Vector4f t = new Vector4f(targetPosition);
				t.w = 1;
				this.cost = Qp.dot(t);
			}
			else this.cost = cost;
			if(mostRecentCollapse.containsKey(he))
				mostRecentCollapse.get(he).isDeleted = true;
			collapses.add(this);
			mostRecentCollapse.put(he, this);
		}
		
		@Override
		public int compareTo(PotentialCollapse other) {
			return (int) Math.signum(this.cost - other.cost);
		}
		
		@Override
		public String toString() {
			return he.toString() + ": " + cost;
		}
	}

}
