package assignment6;

import glWrapper.GLUpdatableHEStructure;

import java.util.HashSet;

import javax.vecmath.Matrix3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.picking.PickingProcessor;
import openGL.picking.TransformedBBox;


/**
 * This class handles call backs from the MyPickingDisplay.java
 * 
 * Adapt this class to call the RAPS_modeling methods you implement
 * to get an interactive modeling tool.
 *  
 *
 */
public class DeformationPickingProcessor implements PickingProcessor{

	//The Half-edge structure
	private HalfEdgeStructure hs;
	
	//The gl-wrapper for the half-edge structure
	private GLUpdatableHEStructure hs_visualization;
	
	
	HashSet<Integer> set1, set2;
	
	//colors to highlight selected regions
	HEData3d colors;
	private Tuple3f color1 = new Vector3f(0.7f, 0.7f, 0.2f);
	private Tuple3f color2 = new Vector3f(0.8f, 0.2f, 0.2f);
	private Tuple3f stdColor = new Vector3f(0.8f, 0.8f, 0.8f);
	
	
	//Encapsulation of the RAPS modeling algorithms
	//the interesting work is delegated to the modeler.
	RAPS_modelling modeler;
	
	public DeformationPickingProcessor(HalfEdgeStructure hs, 
			GLUpdatableHEStructure vis){
		this.hs = hs;
		this.hs_visualization = vis;
		this.set1 = new HashSet<>();
		this.set2 = new HashSet<>();
		
		colors = new HEData3d(hs);
		for(Vertex v : hs.getVertices()){
			colors.put(v, new Vector3f(0.7f,0.6f,0.5f));
		}
		hs_visualization.add(colors, "color");
		
		modeler = new RAPS_modelling(hs);
		
	}

	
	/**
	 * Callback, called after the user has finished manipulating the
	 * two sets and switched to deformation mode, but before any move(...)
	 * or rotate(...) call is triggered.
	 */
	@Override
	public void prepareMove() {
		
		//update the sets of constrained vertices,
		modeler.keep(set1);
		modeler.target(set2);
		//update the deformation matrix
		//do the cholesky factorization, etc

		modeler.updateL();
	}

	
	/**
	 * Callback called when the user makes a move operation
	 */
	@Override
	public void move(Vector3f delta, PickTarget target) {
		
		HashSet<Integer> set = (target == PickTarget.SET1 ? set1: set2);
		
		for(Integer v : set){
			hs.getVertices().get(v).getPos().add(delta);
		}
		
		//delegate the work to find the deformed mesh to the modeler..
		
		hs_visualization.updatePosition();
	}

	
	/**
	 * Callback when the user makes a rotate operation
	 */
	@Override
	public void rotate(Matrix3f rot, PickTarget target) {
		HashSet<Integer> set = (target == PickTarget.SET1 ? set1: set2);
		
		for(Integer v : set){
			rot.transform(hs.getVertices().get(v).getPos());
		}
		
		//delegate the work to find the deformed mesh to the modeler..
		
		hs_visualization.updatePosition();
	}
	
	
	
	/**
	 * Callback, called when the mode of the picking operation in the 
	 * Picking display changes.
	 */
	@Override
	public void pick(TransformedBBox t, PickOperation op, PickTarget target) {
		
		HashSet<Integer> set = (target == PickTarget.SET1 ? set1: set2);
		//collect vertices
		for(Vertex v: hs.getVertices()){
			if(t.contains(v.getPos())){
				switch (op) {
				case ADD:
					set.add(v.index);
					break;
				case REMOVE:
					set.remove(v.index);
				}
			}
		}
		
		recomputeColors();
		hs_visualization.update("color");
	}

	
	/**
	 * update colors to highlight the selected regions
	 */
	private void recomputeColors() {
		for(Vertex v: hs.getVertices()){
			if(set1.contains(v.index)){
				colors.put(v, color1);
			}
			else if(set2.contains(v.index)){
				colors.put(v, color2);
			}
			else{
				colors.put(v, stdColor);
			}
		}
	}



}
