package assignment4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

/**
 * Methods to create different flavours of the cotangent and uniform laplacian.
 * @author Alf
 *
 */
public class LMatrices {
	
	/**
	 * The uniform Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs){
		return null;
	}
	
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
		return null;
	}
	
	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * @param hs
	 * @return
	 */
	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs){
		return null;
	}
	
	
	/**
	 * helper method to multiply x,y and z coordinates of the halfedge structure at once
	 * @param m
	 * @param s
	 * @param res
	 */
	public static void mult(CSRMatrix m, HalfEdgeStructure s, ArrayList<Vector3f> res){
		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s.getVertices().size());
		x.ensureCapacity(s.getVertices().size());
		
		res.clear();
		res.ensureCapacity(s.getVertices().size());
		for(Vertex v : s.getVertices()){
			x.add(0.f);
			res.add(new Vector3f());
		}
		
		for(int i = 0; i < 3; i++){
			
			//setup x
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					x.set(v.index, v.getPos().x);	
					break;
				case 1:
					x.set(v.index, v.getPos().y);	
					break;
				case 2:
					x.set(v.index, v.getPos().z);	
					break;
				}
				
			}
			
			m.mult(x, b);
			
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					res.get(v.index).x = b.get(v.index);	
					break;
				case 1:
					res.get(v.index).y = b.get(v.index);	
					break;
				case 2:
					res.get(v.index).z = b.get(v.index);	
					break;
				}
				
			}
		}
	}
}
