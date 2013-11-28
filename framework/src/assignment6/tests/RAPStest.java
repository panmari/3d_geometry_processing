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
import static myutils.MyJunitAsserts.*;
import assignment4.generatedMeshes.Cylinder2;
import assignment6.Constraint;
import assignment6.RAPS_modelling;
import static assignment6.Assignment6_examples.*;

public class RAPStest {

	private RAPS_modelling modeler;
	private HashSet<Integer> boundary1;
	private HashSet<Integer> boundary2;

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
		boundary1 = collectBoundary(hs, 3, new Constraint() {
			public boolean isEligible(Vertex v) {
				return v.getPos().x < 0.5f;
			}
		});
		
		//mask of the vertices to transform
		boundary2 = collectBoundary(hs, 3, new Constraint() {
			public boolean isEligible(Vertex v) {
				return v.getPos().x > 0;
			}
		});		
		modeler = new RAPS_modelling(hs);
		
		
	}

	@Test
	public void testSimpleTranslation() {
		modeler.keep(boundary1);
		modeler.target(boundary2);
		modeler.updateL();
		
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
	
	@Test
	public void testNoDeformation() {
		modeler.keep(new HashSet<Integer>());
		modeler.target(new HashSet<Integer>());
		modeler.updateL();
		//Demo 1: a simple deformation
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		//t.setTranslation(new Vector3f(-0.8f,1.5f,0));
		
		//where the magic will happen
		modeler.deform(t, 1);
		ArrayList<Point3f> p = modeler.getOriginalCopy().getVerticesAsPointArray();
		ArrayList<Point3f> Lp = new ArrayList<Point3f>();
		modeler.L_cotan.multTuple(p, Lp);
		for (int i = 500; i < 550; i++)
			assertEquals(Lp.get(i), modeler.b.get(i));
	}	
}
