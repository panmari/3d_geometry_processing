package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement{

	/**position*/
	Point3f pos;
	/**adjacent edge: this vertex is startVertex of anEdge*/
	HalfEdge anEdge;
	
	/**The index of the vertex, mainly used for toString()*/
	public int index;

	public Vertex(Point3f v) {
		pos = v;
		anEdge = null;
	}
	
	
	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		anEdge = he;
	}
	/**
	 * @return an adjacend HalfEdge, that uses this vertex as starting point
	 */
	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	/**
	 * Get an iterator which iterates over the 1-neighbourhood
	 * @return
	 */
	public Iterator<Vertex> iteratorVV(){
		return new IteratorVV(anEdge);
	}
	
	/**
	 * Iterate over the incident edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorVE(){
		return new IteratorVE(anEdge);
	}
	
	/**
	 * Iterate over the neighboring faces
	 * @return
	 */
	public Iterator<Face> iteratorVF(){
		return new IteratorVF(anEdge);
	}
	
	
	public String toString(){
		return "" + index;
	}
	
	public int getValence() {
		Iterator<HalfEdge> i = new IteratorVE(anEdge);
		int valence = 0;
		while(i.hasNext()) {
			i.next();
			valence++;
		}
		return valence;
	}
	
	public float getCurvature() {
		float aMixed = 0;
		Iterator<HalfEdge> iter = iteratorVE();
		Vector3f first = iter.next().asVector();
		while(iter.hasNext()) {
			Vector3f second = iter.next().asVector();
			float angle = first.angle(second);
			Vector3f cross = new Vector3f();
			cross.cross(first, second);
			float area = cross.length()/2;
			if (angle < Math.PI/4) { // non-obtuse
				aMixed += 1/8*first.lengthSquared() + second.lengthSquared(); //TODO cot stuff
			} else if (angle > Math.PI/2) { // not non-obtuse nor obtuse
				aMixed += area/2;
			} else { // obtuse
				aMixed += area/4;
			}
		}
		
		return 1/(aMixed*4); //TODO
	}

	public boolean isAdjascent(Vertex w) {
		Iterator<Vertex> it = iteratorVV();
		while(it.hasNext()) {
			if(it.next() == w)
				return true;
		}
		return false;
	}
	
	/**
	 * Abstract iterator class for internal iterators. 
	 * @panmari
	 */
	private abstract class IteratorV {
		HalfEdge start, current;
	
		public void remove() {
			//we don't support removing through the iterator.
			throw new UnsupportedOperationException();
		}
	}

	public final class IteratorVE extends IteratorV implements Iterator<HalfEdge> {	
		public IteratorVE(HalfEdge anEdge) {
			start = anEdge.opposite;
			current = null;
		}

		@Override
		public boolean hasNext() {
			return current == null || current.next.opposite != start;
		}

		@Override
		public HalfEdge next() {
			//make sure eternam iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			//update what edge was returned last
			current = (current == null?
						start:
						current.next.opposite);
			return current;
		}
	}
	public final class IteratorVV extends IteratorV implements Iterator<Vertex> {

		private IteratorVE iter;

		public IteratorVV(HalfEdge anEdge) {
			iter = new IteratorVE(anEdge);
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public Vertex next() {
			return iter.next().start();
		}

	}
	
	public final class IteratorVF extends IteratorV implements Iterator<Face> {

		private IteratorVE iter;
		private Face next;

		public IteratorVF(HalfEdge anEdge) {
			iter = new IteratorVE(anEdge);
		}

		@Override
		public boolean hasNext() {
			while (next == null){
				if (!iter.hasNext())
					return false;
				next = iter.next().getFace();
			}
			return true;
		}

		@Override
		public Face next() {
			//make sure eternam iteration is impossible
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			Face returnFace = next;
			next = null;
			return returnFace;
		}	
	}
}
