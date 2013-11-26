package assignment6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;



/**
 * As rigid as possible deformations.
 * @author Alf
 *
 */
public class RAPS_modelling {

	//ArrayList containing all optimized rotations,
	//keyed by vertex.index
	ArrayList<Matrix3f> rotations;
	
	//A copy of the original half-edge structure. This is needed  to compute the correct
	//rotation matrices.
	private HalfEdgeStructure hs_originl;
	//The halfedge structure being deformed
	private HalfEdgeStructure hs_deformed;
	
	//The unnormalized cotan weight matrix, with zero rows for
	//boundary vertices.
	//It can be computed once at setup time and then be reused
	//to compute the matrix needed for position optimization
	CSRMatrix L_cotan;
	//The matrix used when solving for optimal positions
	CSRMatrix L_deform;
	
	//allocate righthand sides and x only once.
	ArrayList<Float>[] b;
	ArrayList<Float> x;

	//sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;

		
	
	
	
	/**
	 * The mesh to be deformed
	 * @param hs
	 */
	public RAPS_modelling(HalfEdgeStructure hs){
		this.hs_originl = new HalfEdgeStructure(hs); //deep copy of the original mesh
		this.hs_deformed = hs;
		
		this.keepFixed = new HashSet<>();
		this.deform = new HashSet<>();
		
		
		init_b_x(hs);
		
	}
	
	/**
	 * Set which vertices should be kept fixed. 
	 * @param verts_idx
	 */
	public void keep(Collection<Integer> verts_idx) {
		this.keepFixed.clear();
		this.keepFixed.addAll(verts_idx);

	}
	
	/**
	 * constrain these vertices to the new target position
	 */
	public void target(Collection<Integer> vert_idx){
		this.deform.clear();
		this.deform.addAll(vert_idx);
	}
	
	
	/**
	 * update the linear system used to find optimal positions
	 * for the currently constrained vertices.
	 * Good place to do the cholesky decompositoin
	 */
	public void updateL() {
		//do your stuff
	}
	
	/**
	 * The RAPS modelling algorithm.
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements){
		this.transformTarget(t);
		
		//RAPS algorithm,,
	}
	

	/**
	 * Method to transform the target positions and do nothing else.
	 * @param t
	 */
	public void transformTarget(Matrix4f t) {
		for(Vertex v : hs_deformed.getVertices()){
			if(deform.contains(v.index)){
				t.transform(v.getPos());
			}
		}
	}
	
	
	/**
	 * ArrayList keyed with the vertex indices.
	 * @return
	 */
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}

	/**
	 * Getter for undeformed version of the mesh
	 * @return
	 */
	public HalfEdgeStructure getOriginalCopy() {
		return hs_originl;
	}
	
	

	/**
	 * initialize b and x
	 * @param hs
	 */
	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList[3];
		for(int i = 0; i < 3; i++){
			b[i] = new ArrayList<>(hs.getVertices().size());
			for(int j = 0; j < hs.getVertices().size(); j++){
				b[i].add(0.f);
			}
		}
		x = new ArrayList<>(hs.getVertices().size());
		for(int j = 0; j < hs.getVertices().size(); j++){
			x.add(0.f);
		}
	}
	
	
	
	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions(){
	
		//do your stuff...
	}
	

	/**
	 * compute the righthand side for the position optimization
	 */
	private void compute_b() {
		reset_b();
		//do your stuff...
		
		
	}



	/**
	 * helper method
	 */
	private void reset_b() {
		for(int i = 0 ; i < 3; i++){
			for(int j = 0; j < b[i].size(); j++){
				b[i].set(j,0.f);
			}
		}
	}


	/**
	 * Compute the optimal rotations for 1-neighborhoods, given
	 * the original and deformed positions.
	 */
	public void optimalRotations() {
		//for the svd.
		Linalg3x3 l = new Linalg3x3(10);// argument controls number of iterations for ed/svd decompositions 
										//3 = very low precision but high speed. 3 seems to be good enough
			
		//Note: slightly better results are achieved when the absolute of cotangent
		//weights w_ij are used instead of plain cotangent weights.		
			
		//do your stuff..
		
	}

	


	
	

	private void compute_ppT(Vector3f p, Vector3f p2, Matrix3f pp2T) {
		assert(p.x*0==0);
		assert(p.y*0==0);
		assert(p.z*0==0);

		pp2T.m00 = p.x*p2.x; pp2T.m01 = p.x*p2.y; pp2T.m02 = p.x*p2.z; 
		pp2T.m10 = p.y*p2.x; pp2T.m11 = p.y*p2.y; pp2T.m12 = p.y*p2.z; 
		pp2T.m20 = p.z*p2.x; pp2T.m21 = p.z*p2.y; pp2T.m22 = p.z*p2.z; 

	}


	
	




}
