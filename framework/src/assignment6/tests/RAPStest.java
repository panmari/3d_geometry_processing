package assignment6.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import glWrapper.GLUpdatableHEStructure;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;

import org.junit.Before;
import org.junit.Test;

import assignment4.generatedMeshes.Cylinder2;
import assignment6.Constraint;
import assignment6.RAPS_modelling;
import static assignment6.Assignment6_examples.*;

public class RAPStest {

	private RAPS_modelling modeler;

	@Before
	public void setUp() throws Exception {
		//WireframeMesh m = new Cylinder(0.3f,2.f).result;
		WireframeMesh m = new Cylinder2(0.3f,2f).result;
		
		//generate he struture
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(m);
		GLUpdatableHEStructure glhs = new GLUpdatableHEStructure(hs);
		//generate he struture
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
		modeler = new RAPS_modelling(hs);
		
		modeler.keep(boundary1);
		modeler.target(boundary2);
		modeler.updateL();
		
		
	}

	@Test
	public void testSimpleTranslation() {
		//Demo 1: a simple deformation
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		t.setTranslation(new Vector3f(-0.8f,1.5f,0));
		
		//where the magic will happen
		modeler.deform(t, 1);
		ArrayList<Point3f> initial = modeler.getOriginalCopy().getVerticesAsPointArray();
		//show some points in the middle of bent area
		for (int i = 500; i < 550; i++)
			System.out.println(initial.get(i) + "\t" + modeler.x.get(i));
	}	
}
