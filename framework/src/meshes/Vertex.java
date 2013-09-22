package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

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
	
	

	public boolean isAdjascent(Vertex w) {
		boolean isAdj = false;
		Vertex v = null;
		Iterator<Vertex> it = iteratorVV();
		for( v = it.next() ; it.hasNext(); v = it.next()){
			if( v==w){
				isAdj=true;
			}
		}
		return isAdj;
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
	
	//TODO
	public final class IteratorVF extends IteratorV implements Iterator<Face> {

		public IteratorVF(HalfEdge anEdge) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Face next() {
			// TODO Auto-generated method stub
			return null;
		}	
		
	}
}
