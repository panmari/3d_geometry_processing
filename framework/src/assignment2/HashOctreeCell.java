package assignment2;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

/**
 * Implementation of a hashoctree cell. A cell stores its Morton code
 * and if it is a leaf cell it has a payload of Point3f's.
 * 
 * Furthermore, for convenience, a cell stores its center, its side length,
 * and its level, even thought in principle all this information could be gained from
 * the morton codes.
 * 
 * @author bertholet
 *
 */
public class HashOctreeCell {
	
	/** the center of this cell*/
	public Point3f center;
	/** the sidelength of this cell*/
	public float side;
	
	/**the morton code of this cell*/
	public long code;
	/**the grid level on which this cell lies*/
	public int lvl;
	
	
	/** If the cell is a leaf, i.e. has no children,
	 * this array stores all points of the pointcloud lying in this cell.
	 */
	public ArrayList<Point3f> points;
	/**
	 * If this cell is a leaf it gets a unique index, as all leafs are enumerated.
	 */
	public int leafIndex;
	
	

	/**
	 * This will create a root node for a hashoctree containing the points.
	 * scale should be a factor >= 1, this will make the octree to cover a volume
	 * larger than the the bounding points of the tree, by a factor of scale.
	 * @param points
	 * @param scale
	 * @return
	 */
	public static HashOctreeCell Root(ArrayList<Point3f> points, float scale){
		return new HashOctreeCell(points, scale);
	}
	
	
	/**
	 * construct a root node
	 * @param points2
	 */
	private HashOctreeCell(ArrayList<Point3f> points2, float scale) {
		leafIndex = -1; //invalid
		lvl = 1;
		code = 0b1000;
		points = new ArrayList<>();
		points.addAll(points2);
		
		
		Point3f max = new Point3f(points.get(0)), 
				min = new Point3f(points.get(0));
		
		for(Point3f p: points){
			max.x = (p.x > max.x? p.x : max.x);
			max.y = (p.y > max.y? p.y : max.y);
			max.z = (p.z > max.z? p.z : max.z);
			
			min.x = (p.x < min.x? p.x : min.x);
			min.y = (p.y < min.y? p.y : min.y);
			min.z = (p.z < min.z? p.z : min.z);
		}
		
		//compute side length
		this.side = scale* Math.max(Math.max(
				max.x - min.x,
				max.y - min.y),
				max.z - min.z); 
		
		//compute center
		this.center = max;
		this.center.add(min);
		this.center.scale(0.5f);
	}
	
	
	/**
	 * Create the childnode 0bxyz of dad.
	 * @param dad
	 */
	public HashOctreeCell(HashOctreeCell dad, int Obxyz) {
		assert(0<=Obxyz && Obxyz <8);
		
		this.lvl = dad.lvl+1;
		this.points = new ArrayList<>();
		this.code = (dad.code << 3) | Obxyz;
		assert(this.code >=0);
		
		this.side = dad.side /2;
		this.center = new Point3f(dad.center);
						//is this cell < dad.center.x?
		this.center.x += ((Obxyz & 0b100) == 0       ? -side/2: side/2);
		this.center.y += ((Obxyz & 0b010) == 0       ? -side/2: side/2);
		this.center.z += ((Obxyz & 0b001) == 0       ? -side/2: side/2);
		
		this.leafIndex = -1; //invalid
		
	}
	
	/**
	 * computes center-position of the child Obxyz and stores it in target
	 * @param Obxyz
	 * @param target
	 */
	public void computeChildPos(int Obxyz, Tuple3f target){
		target.set(this.center);
		target.x += ((Obxyz & 0b100) == 0       ? -side/4: side/4);
		target.y += ((Obxyz & 0b010) == 0       ? -side/4: side/4);
		target.z += ((Obxyz & 0b001) == 0       ? -side/4: side/4);
	}

	/**
	 * Computes the vertex position
	 * @param Obxyz
	 * @param target
	 */
	public void computeVertexPos(int Obxyz, Point3f target) {
		target.set(this.center);
		target.x += ((Obxyz & 0b100) == 0       ? -side/2: side/2);
		target.y += ((Obxyz & 0b010) == 0       ? -side/2: side/2);
		target.z += ((Obxyz & 0b001) == 0       ? -side/2: side/2);
	}


	public boolean isLeaf() {
		return points != null;
	}


	
	
}
