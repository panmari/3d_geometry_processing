package assignment3;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import meshes.Point2i;
import meshes.WireframeMesh;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;


/**
 * Implwmwnr your Marching cubes algorithms here.
 * @author bertholet
 *
 */
public class MarchingCubes {
	
	//the reconstructed surface
	public WireframeMesh result;
	

	//the tree to march
	private HashOctree tree;
	//per marchable cube values
	private ArrayList<Float> val;
	
	
	
	
		
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;
		
		
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 * Assumption: byVertex at position i holds the function value of vertex with index i
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();
		
		for (HashOctreeCell c: tree.getLeafs()) {
			pushCube(c);
		}
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {
		
		// TODO: do your stuff
	}
	
	/**
	 * March a single cube: compute the triangles and add them to the wireframe model
	 * @param n
	 */
	private void pushCube(MarchableCube n){
		float[] values = new float[8];
		Point2i[] points = new Point2i[15];
		for (int i = 0; i < points.length; i++)
			points[i] = new Point2i();
		for (int i = 0; i < 8; i++) {
			MarchableCube corner = n.getCornerElement(i, tree);
			values[i] = val.get(corner.getIndex());
			MCTable.resolve(values, points);
		}
		for (Point2i p: points) {
			if (p.x == -1) //no more triangles to generate
				break;
			MarchableCube marchable_a = n.getCornerElement(p.x, tree);
			MarchableCube marchable_b = n.getCornerElement(p.y, tree);
			float a = val.get(marchable_a.getIndex());
			float b = val.get(marchable_b.getIndex());
			//formula as on assignement sheet:
			Point3f pos = new Point3f(marchable_a.getPosition()); //pos_a 
			pos.scale(1 - a/(a-b));
			Point3f pos_b = new Point3f(marchable_b.getPosition());
			pos_b.scale(a/(a-b));
			pos.add(pos_b);
			result.vertices.add(pos);
		}
		for (int i = 0; i < result.vertices.size(); i+=3) {
			int[] idxs = {i, i + 1, i+2};
			result.faces.add(idxs);
		}
	}

	
	/**
	 * Get a nicely marched wireframe mesh...
	 * @return
	 */
	public WireframeMesh getResult() {
		return this.result;
	}


	/**
	 * compute a key from the edge description e, that can be used to
	 * uniquely identify the edge e of the cube n. See Assignment 3 Exerise 1-5
	 * @param n
	 * @param e
	 * @return
	 */
	private Point2i key(MarchableCube n, Point2i e) {
		Point2i p = new Point2i(n.getCornerElement(e.x, tree).getIndex(),
				n.getCornerElement(e.y, tree).getIndex());
		if(p.x > p.y) {
			int temp = p.x;
			p.x= p.y; p.y = temp;
		}
		return p;
	}
	

}
