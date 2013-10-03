package assignment2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.PointCloud;


/**
 * This is an implementation of a hashtable based Octree.
 * The Morton Codes used are described in detail in the slides accompanying
 * this exercise, the bitwise operations on them will be implemented in the class
 * {@link MortonCodes}.
 * <p>
 * To navigate through the adjacencies of the octree, the methods getNbr_*2* can be
 * used (once implemented).
 * Their arguments Oxyz encode relative difference vectors, which usually
 * will be 3-bit integers. For example the argument 0b011 in the method
 * getNbr_c2c(cell, 0b011) would encode that the cell at the relative grid position
 * +0x, +1y , +1z is sought (relative to the cell passed as parameter).
 * 
 * @author Alf
 *
 */
public class HashOctree {

	/** The root of the tree*/
	public HashOctreeCell root;
	
	/** Abort criterion for the hashoctree construction: stop refining at depth maxDepth 
	 * MaxDepth should never be  more than 20, as longs have 64 bit, and every level needs 3 bits 
	 * and 4 additional bits are needed to discover overflows.*/
	private int maxDepth;
	/** Abort criterion for the hashoctree refinement: stop refining if a cell has less than
	 * pointsPerCell points. */
	private int pointsPerCell;
	
	/** Number of points stored in the octree */
	private int nrPoints;
	/** depth of this octree */
	private int depth;
	
	/** The bounding box of this tree. */
	private Point3f bboxMax, bboxMin;
	
	/** The hashmap to store the octree cells */
	private HashMap<Long, HashOctreeCell> cellMap;
	/** The hashmap to store the octree vertices */
	private HashMap<Long, HashOctreeVertex> vertexMap;
	/** For convenience, all leafs of the octree (cells with no sub-cells) are stored here. This
	 * allows to enumerate them and access them by an index (useful for matrix construcion)*/
	private ArrayList<HashOctreeCell> leafs;
	/** For convenience, all vertices of the octree are stored here. This
	 * allows to enumerate them and access them by an index (useful for matrix construcion)*/
	private ArrayList<HashOctreeVertex> vertices;

	
	/**
	 * 
	 * <p>Construct a hashbased octree for the PointCloud pc. depth denotes the maximal depth the octree can have,
	 * and pointsPerCell controls at what number of points a cell is not split any further.
	 * </p>
	 * <p>
	 * The factor parameter controls how many times as big as the boundingbox of the pointcloud
	 * the hashtree will be; if factor is < 1 the argument is ignored and 1 is used instead.
	 * </p>
	 * @param pc
	 * @param depth
	 * @param pointsPerCell
	 * @param factor
	 */
	public HashOctree(PointCloud pc, int depth, int pointsPerCell, float factor) {
		
		this.maxDepth= depth;
		this.pointsPerCell = pointsPerCell;
		
		this.cellMap = new HashMap<>();
		this.leafs = new ArrayList<>();
				
		this.nrPoints = 0;
		this.depth = 0;
		
		this.root = HashOctreeCell.Root(pc.points, Math.max(factor,1));
		
		bboxMax = new Point3f();
		bboxMin = new Point3f();
		root.computeVertexPos(0b111, bboxMax);
		root.computeVertexPos(0b000, bboxMin);

		//build the octree
		//this creates all octree cells.
		Stack<HashOctreeCell> stack = new Stack<>();
		stack.push(root);
		buildTree(stack);
		
		//this  creates a hashmap of all octree vertices. This method makes sure that every 
		//vertex is registered  exactly once, even if it is used on multiple levels by multiple cells
		buildVertexMap();
		
		//lastly: enumerate the vertices and leafs.
		// the vertices and leafs are stored a second time in two separate arrays.
		//Leafs and Vertices have an 'index' field, this method assign to every vertex and every leaf its unique integer index.
		//This index will be used during the Reconstruction assignment to build and debug linear systems.
		enumerateVertices();
		enumerateLeafs();
		
		assert(nrPoints == pc.points.size());
	}


	/**
	 * First step int the hashoctree creation. This initializes the 
	 * cells, the hashmap containing the cells and the list containing all Leafs
	 * The method operates in a recursive fashion on the stack.
	 * @param stack
	 */
	private void buildTree(Stack<HashOctreeCell> stack) {
		HashOctreeCell node = null;
		
		//split cells until there is no cell left to split:
		while(!stack.isEmpty()){
			node = stack.pop();
			
			if(!this.cellMap.containsKey(node.code)){
				this.cellMap.put(node.code, node);
			}
			else{
				assert(false); //sanity check no two cells should have the same morton code!!!!
			}
			
			
			if(node.points.size() > pointsPerCell && node.lvl <maxDepth){
				//creates 8 children,
				//the points stored in node are split between the 8 children
				//and  morton codes are assigned to the children
				splitNode(node, stack);
				
			}else{
				leafs.add(node);
				nrPoints += node.points.size();
				depth = (node.lvl > depth? node.lvl: depth);
			}
		}
	}

	/**
	 * Second step in the hashoctree creation: Initialize the vertex hashmap and
	 * computes all vertex codes.
	 */
	private void buildVertexMap() {
		this.vertexMap = new HashMap<Long, HashOctreeVertex>();
		
		long vertKey;
		//every vertex is the corner of some leaf. This method makes sure that every vertex is registered once only.
		HashOctreeVertex v;
 
		for(HashOctreeCell c : leafs){
			for(int i = 0b000; i <= 0b111; i++){
				//compute the hash corresponding to the vertex 0bxyz: this is the nbr code + padding
				vertKey = MortonCodes.nbrCode(c.code, c.lvl, i) << (3*(depth - c.lvl)); //getVertexHash(c, i);
				if(vertKey >= 0){
					//retrieve the vertex associated to the hash if existing
					if(vertexMap.containsKey(vertKey)){
						v= vertexMap.get(vertKey);
						
						//update
						v.maxLvl = (v.maxLvl < c.lvl ? c.lvl : v.maxLvl);
						v.minLvl = (v.minLvl > c.lvl ? c.lvl : v.minLvl);
						
						assert(v.code == vertKey);
					}
					//or create a new vertex for the given hash
					else{
						v = new HashOctreeVertex();
						v.maxLvl = c.lvl;
						v.minLvl = c.lvl;
						v.code = vertKey;
						c.computeVertexPos(i, v.position);
						vertexMap.put(v.code,v);
					}
				}
				else{
					//sanity check.
					assert(false);
				}
			}

		}
	}


	/**
	 * This helper method creates the child octree cells, computes their morton codes and
	 * splits the points contained in node between the children,   
	 * @param node
	 * @param stack
	 */
	private void splitNode(HashOctreeCell node, Stack<HashOctreeCell> stack) {
		HashOctreeCell[] children = new HashOctreeCell[8];
		for(int i = 0b0; i <= 0b111; i++){
			//Constructor takes care of the following code generation
			//children[i].code = node.code << 3;
			//children[i].code = children[i].code | i;
			children[i] = new HashOctreeCell(node, i);
		}
		
		int pointCode;
		//reassign all points
		for(Point3f p : node.points){
			//to which child should this point be assigned?
			pointCode = 0;
			if(p.x > node.center.x){
				pointCode = pointCode | 0b100;
			}
			if(p.y > node.center.y){
				pointCode = pointCode | 0b010;
			}
			if(p.z > node.center.z){
				pointCode = pointCode | 0b001;
			}
			
			//...to the one computed.
			children[pointCode].points.add(p);
		}
		node.points = null;
		for(HashOctreeCell n: children){
			stack.push(n);
		}
	}

	/** Enumerate all vertices */
	private void enumerateVertices(){
		int i = 0;
		this.vertices = new ArrayList<HashOctreeVertex>(this.vertexMap.values().size());
		for( HashOctreeVertex v: vertexMap.values()){
			this.vertices.add(v);
			v.index = i;
			i++;
		}
		
	}
	
	/** Enumerate all Leafs */
	private void enumerateLeafs() {
		int idx = 0;
		for(HashOctreeCell n : leafs){
			n.leafIndex = idx++;
		}
	}


	public ArrayList<HashOctreeCell> getLeafs() {
		return leafs;
	}

	public ArrayList<HashOctreeVertex> getVertices() {
		return this.vertices;
	}

	public Collection<HashOctreeCell> getCells() {
		return cellMap.values();
	}
	

	public HashOctreeVertex getVertexbyIndex(int index){
		return vertices.get(index);
	}
	
	
	public int getDepth() {
		return this.depth;
	}
	

	public int numberofVertices() {
		return vertexMap.size();
	}

	public int numberOfPoints() {
		return nrPoints;
	}
	
	public int numberOfCells() {
		return this.cellMap.size();
	}

	public int numberOfLeafs() {
		return leafs.size();
	}
	

	/**
	 * Retrieve the cell which corresponds to the morton code 
	 * in the argument. This will return null if no such cell exists.
	 * @param hash
	 * @return
	 */
	public HashOctreeCell getCell(long hash) {
		return this.cellMap.get(hash);
	}

	/**
	 * Retrieve the vertex which corresponds to the morton code 
	 * in the argument. This will return null if no such cell exists.
	 * @param hash
	 * @return
	 */
	public HashOctreeVertex getVertex(long hash) {
		return this.vertexMap.get(hash);
	}
	
	
	
	/**
	 * Retrieve a vertex neighboring this cell i.e. a corner vertex of the cell
	 * 
	 *    011------111
	 *   /         /|
	 *  /         / |
	 * 010------101 |
	 * |         |  |
	 * |  001----|-101
	 * | /       | /
	 * |/        |/
	 * 000------100
	 */
	public HashOctreeVertex getNbr_c2v(HashOctreeCell cell, int vertex_Obxyz) {
		
		//get the neighbor code
		long code = MortonCodes.nbrCode(cell.code, cell.lvl, vertex_Obxyz);
		
		//and padd it to get the vertex code.
		code = code << 3*(depth - cell.lvl);
		return getVertex(code);
	}
	
	/**
	 * Find and return a leaf cell that is adjacent to v.
	 * 
	 * 
	 * @param v
	 * @param nbr_Obxyz
	 * @return
	 */
	public HashOctreeCell getNbr_v2c(HashOctreeVertex v, int nbr_Obxyz) {
		long code = v.code;
		
		//first compute the code from the top left cell ($$), 
		// which is simply the unpadded morton code.
		// -------
		// |  |$$|
		// ---v---
		// |  |  |
		// -------
		code = code >> 3* (depth - v.maxLvl);
	
		// as $$ corresponds to the parameter nbr_0bxyz = 0b111
		// the seeked cell can be computed by nbrCodeMinus.
		//0b111 & (~ nbr_Obxyz) computes the appropriate 'difference vector'
		code = MortonCodes.nbrCodeMinus(code, v.maxLvl, 0b111 & (~ nbr_Obxyz));
		
		//finally, iterate through the multigrid layers 
		//to find the smallest existing cell. 
		while(getCell(code) == null && code > 0){
			code = code >>3;
		}
		
		return getCell(code);
		
	}
	
	
	/**
	 * Return the parent cell.
	 * @param hash
	 * @return
	 */
	public HashOctreeCell getParent(HashOctreeCell cell){
		
		//TODO implement this...
		return null;
	}
	
	/**
	 * Return an adjacent cell on the same or on a lower level, which lies in the
	 * direction Obxyz. If there is no such cell, null is returned
	 * 
	 * @param cell
	 * @param Obxyz
	 * @return
	 */
	public HashOctreeCell getNbr_c2c(HashOctreeCell cell, int Obxyz){
		
		//TODO implement this...
		
		return null;
	}
	
	/**
	 * Return the cell on the same or on a lower level, which lies in the
	 * direction Obxyz. If there is no such cell, null is returned
	 * @param cell
	 * @param Obxyz
	 * @return
	 */
	public HashOctreeCell getNbr_c2cMinus(HashOctreeCell cell, int Obxyz){

		//TODO implement this
		return null;
	}
	
	
	
	/** find and return a vertex on the finest grid possible, that shares an edge of some octreecell
	 * with the vertex v and lies in direction nbr_0bxyz (0b100 = +x direction, 0b010 = +y direction
	 * 0b001 = +z direction). If no neighbor exists, null is returned.
	 * @param v
	 * @param nbr_0bxyz
	 * @return
	 */
	public HashOctreeVertex getNbr_v2v(HashOctreeVertex v, int nbr_0bxyz){

		//TODO implement this
		
		return null;
	}
	
	/** find and return maximal depth vertex, that shares an edge of some octreecell
	 * with vertex and lies in direction nbr_0bxyz (0b100 = +x direction, 0b010 = +y direction
	 * 0b001 = +z direction). If no neighbor exists, null is returned.
	 * @param vertex
	 * @param nbr_0bxyz
	 * @return
	 */
	public HashOctreeVertex getNbr_v2vMinus(HashOctreeVertex v, int nbr_0bxyz){
		
		//TODO implement this
		
		return null;
	}
	
	


	
	
	
	/**
	 * This method checks if the vertex code lies on the bounday of the
	 * hashoctree
	 */
	public boolean isOnBoundary(HashOctreeVertex v) {
		return MortonCodes.isVertexOnBoundary(v.code, this.depth);
	}

	/**
	 * Find and return the Leaf cell in which p lies. This method
	 * will return null if the point is outside of the volume
	 * covered by this octree.
	 * @param p
	 * @return
	 */
	public HashOctreeCell getCell(Point3f p){
		
		if(p.x >bboxMax.x + 10e-5 || 
				p.y > bboxMax.y+ 10e-5 || 
				p.z > bboxMax.z + 10e-5 ||
				p.x < bboxMin.x - 10e-5 ||
				p.y < bboxMin.y - 10e-5 ||
				p.z < bboxMin.z - 10e-5){
			return null;
		}
		long code = root.code;
		
		HashOctreeCell child, cell;
		cell = child = root;
		
		do{
			cell = child;
			
			//compute the code of the correct child
			code = code << 3;
			if(p.x > cell.center.x){
				code = code | 0b100;
			}
			if(p.y > cell.center.y){
				code = code | 0b010;
			}
			if(p.z > cell.center.z){
				code = code | 0b001;
			}
			
			child = getCell(code);
			
		}while (child!= null);//iterate while there is a child.
		
		return cell;
		
	}


}

