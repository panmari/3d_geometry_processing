package assignment6;

import glWrapper.GLUpdatableHEStructure;

import java.util.HashSet;
import java.util.Iterator;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import openGL.MyDisplay;
import assignment4.generatedMeshes.Cylinder2;


/**
 * This class describes four example deformations and can be used to
 * test the deformation algorithm
 * @author bertholet
 *
 */
public class Assignment6_examples {

	public static void main(String[] args) throws Exception{
		
		//WireframeMesh m = new Cylinder(0.3f,2.f).result;
		WireframeMesh m = new Cylinder2(0.3f,2f).result;
		
		//generate he struture
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);
		
		//mask of the boundary vertices
		HashSet<Integer> boundary1 = collectBoundary(hs, 3, new Constraint() {
			public boolean isEligible(Vertex v) {
				return v.getPos().x < 0.5f;
			}
		});
		
		//mask of the vertices to transform
		HashSet<Integer> boundary2 = collectBoundary(hs, 3, new Constraint() {
			public boolean isEligible(Vertex v) {
				return v.getPos().x > 0;
			}
		});
		
		
		//Demo 1: a simple deformation
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		t.setTranslation(new Vector3f(-0.8f,1.5f,0));
		//deformDemo(hs,boundary1, boundary2, t, 2);
		
		
		//Demo 2: a continuous deformation
		int mode = 3;// 1 = bend, 2 = translate, 3 = twist
		int nRefinements = 3;
		continuousDeformationDemo(hs, boundary1, boundary2, nRefinements,1f,mode);
		
	}

	


	/**
	 * Demonstration of a simple deformation. the set1 is kept fixed, the set2
	 * is deformed according to t.
	 * @param hs
	 * @param set1
	 * @param set2
	 * @param t
	 * @param nRefinements
	 */
	private static void deformDemo(HalfEdgeStructure hs, HashSet<Integer> set1,
			HashSet<Integer> set2, Matrix4f t, int nRefinements) {
		MyDisplay disp = new MyDisplay();
		GLUpdatableHEStructure glhs = new GLUpdatableHEStructure(hs);
		
		disp.addToDisplay(glhs);
		
		RAPS_modelling modeler = new RAPS_modelling(hs);
		
		modeler.keep(set1);
		modeler.target(set2);
		modeler.updateL();
		
		//where the magic will happen
		modeler.deform(t, nRefinements);


		glhs.updatePosition();
		disp.updateDisplay();
		
	}




	/**
	 * Demo where the halfedge structure is deformed continuously.
	 * @param hs
	 * @param boundary1
	 * @param boundary2
	 * @param nRefinements
	 * @param speed
	 * @param mode
	 */
	private static void continuousDeformationDemo(HalfEdgeStructure hs,
			HashSet<Integer> boundary1, HashSet<Integer> boundary2, 
			int nRefinements, float speed,
			int mode) {
		
		MyDisplay disp = new MyDisplay();
		GLUpdatableHEStructure glhs = new GLUpdatableHEStructure(hs);
		disp.addToDisplay(glhs);
		
		RAPS_modelling modelMonkey = new RAPS_modelling(hs);
		modelMonkey.keep(boundary1);
		modelMonkey.target(boundary2);
		
		//trigger recomputation of the linear system
		modelMonkey.updateL();
	
		for(int i = 0; i < 50; i++){

			Matrix4f transf = new Matrix4f();
			transf.setIdentity();
			//motion 1: bend
			if(mode == 1){
				transf.setRotation(
					new AxisAngle4f(new Vector3f(0,0,1), 
							(float) Math.PI * (float) /*Math.sin(i/10.f)**/0.012f * speed) );
			} 
			//motion 2: translate
			else if (mode == 2){
				transf.setTranslation(new Vector3f(0.f
						,(float) /*Math.sin(i/10.f)*/0.04f*speed ,0));
			}
			//motion 3 twist
			else if (mode == 3){
				transf.setRotation(
						new AxisAngle4f(new Vector3f(1,0,0), 
								(float) Math.PI * 0.007f * speed) );
			}

			//where the magic will happen
			modelMonkey.deform(transf, nRefinements);
			
			glhs.updatePosition();
			disp.updateDisplay();
			
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
					
			
	}

	/**
	 * Returns the set of boundary vertex indices which is up to dist
	 * edges away from the boundary and fulfills the constraint c.
	 * @param hs
	 * @param dist
	 * @param c
	 * @return
	 */
	private static HashSet<Integer> collectBoundary(HalfEdgeStructure hs,
			int dist, Constraint c) {
		HashSet<Integer> has_jm1_dist = new HashSet<>();
		for(Vertex v : hs.getVertices()){
			if(v.isOnBoundary() && c.isEligible(v)){
				has_jm1_dist.add(v.index);
			}
		}
		
		Vertex temp;
		HashSet<Integer> has_j_dist = new HashSet<>();
		for(int j = 0; j <dist; j++){
			for(Vertex v : hs.getVertices()){
				Iterator<Vertex> it = v.iteratorVV();
				while(it.hasNext()){
					temp = it.next();
					if(has_jm1_dist.contains(temp.index)){
						has_j_dist.add(v.index);
					}
				}
			}
			
			HashSet<Integer> tmp = has_jm1_dist;
			has_jm1_dist = has_j_dist;
			has_j_dist = tmp;
			
		}
		return has_jm1_dist;
	}
	

}
