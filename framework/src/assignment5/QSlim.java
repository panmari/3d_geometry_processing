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
	private HalfEdgeStructure hs;
	
	/**
	 * Prepares the QSlim algorithm for the given HalfEdgeStructure, namely by
	 * * Creating a HashMap that saves the error quadric for every vertex
	 * * Creates all potential collapses
	 */
	public QSlim(HalfEdgeStructure hs){
		this.hs = hs;
		for(Vertex v: hs.getVertices()) {
			hm.put(v, makeErrorQuadricFor(v));
		}
		for (HalfEdge he: hs.getHalfEdges()) {
			//only add every edge once
			if (!mostRecentCollapse.containsKey(he.getOpposite()))
				new PotentialCollapse(he);
		}
		this.hec = new HalfEdgeCollapse(hs);
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached. Target should be of reasonable size,
	 * or some methods of HalfEdgeCollapse will start throwing errors.
	 * @param target
	 */
	public void simplify(int target){
		while (target < hs.getVertices().size() - hec.deadVertices.size()) {
			collapsCheapestEdge();
		}
		hec.finish();
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. This method can be called
	 * until some target number of vertices is reached.
	 * It polls the cheapest edge from the priority queue, checks if it is 
	 * removable and removes it if it is removable.
	 * @return the number of vertices deleted (1 or 0)
	 */
	public int collapsCheapestEdge(){
		PotentialCollapse pc = collapses.poll();
		//System.out.println(pc);
		if (pc.isDeleted || hec.isEdgeDead(pc.he))
			return 0;
		HalfEdge he = pc.he;
		if (hec.isCollapseMeshInv(he, pc.targetPosition) ||
				!HalfEdgeCollapse.isEdgeCollapsable(he)) {
			new PotentialCollapse(he, (pc.cost + 0.1f)*10);
			return 0;
		}
		hec.collapseEdge(he, pc.targetPosition);
	
		updateEdgesAround(he.end(), pc.qem);
		return 1;
	}
	
	/**
	 * Computes the error quadric of a vertex by summing up
	 * the error quadrics of the surrounding Faces.
	 * @param v
	 * @return
	 */
	private Matrix4f makeErrorQuadricFor(Vertex v) {
		Matrix4f qem = new Matrix4f();
		Iterator<Face> iter = v.iteratorVF();
		while (iter.hasNext())
			qem.add(iter.next().getQuadricErrorMatrix());
		return qem;
	}
	
	/**
	 * Updates the collapses of the edges surrounding v.
	 * Incoming AND outgoing edges are reinserted.
	 * @param v
	 * @param qem
	 */
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
		 * If true, the optimal target position is computed. If false, target position
		 * is the center of the half edge.
		 */
		static final boolean optimal = true;
		
		/**
		 * Constructor also inserts this collapse into priority queue, marks
		 * older collapse of this edge as deleted and puts itself as most
		 * recent collapse into the hashmap.
		 * @param he, HalfEdge this potential collapse is concerned with
		 * @param cost, predefined cost. Will be computed if zero is given.
		 */
		PotentialCollapse(HalfEdge he, float cost) {
			this.he = he;	
			computeCost(cost);
		}
		/**
		 * Sets the center of the half edge as target position.
		 */
		private void computeSimpleTargetPosition() {
			targetPosition =  new Point3f();
			targetPosition.add(he.end().getPos(), he.start().getPos());
			targetPosition.scale(1/2f);
		}
		
		/**
		 * Sets the optimal target position by optimizing for the given error quadric. 
		 * (Constraints the x, y, z derivatives of vQv to zero). 
		 * If the computed matrix is not invertable, the simple target position is set.
		 * @param qem
		 */
		private void computeOptimalTargetPosition(Matrix4f qem) {
			Matrix4f Q = new Matrix4f(qem);
			Q.setRow(3, 0, 0, 0, 1);
			if (optimal && Q.determinant() != 0) {
				Q.invert();
				Point3f optPos = new Point3f();
				Q.transform(optPos); //assumes w=1 automatically 
				targetPosition = optPos;
			} else
				computeSimpleTargetPosition();
		}
		
		PotentialCollapse(HalfEdge he) {
			this(he, 0.f);
		}
		
		/**
		 * Sets cost by doing stuff.
		 * @param cost
		 */
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
