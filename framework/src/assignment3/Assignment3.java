package assignment3;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLHashtree;
import glWrapper.GLHashtreeVertices;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import assignment2.HashOctree;
import assignment2.HashOctreeVertex;

public class Assignment3 {
	
	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		
		
		marchingCubesDemo();
		

			
	}
	
	
	public static void marchingCubesDemo() throws MeshNotOrientedException, DanglingTriangleException{
		
		//Test Data: create an octree
		HashOctree tree = new HashOctree( 
				nonUniformPointCloud(15),
				6,7,1.2f);
		//and sample per vertex function values.
		ArrayList<Float> x = sphericalFunction(tree);
		
		//Do your magic here...
		MarchingCubes mc = new MarchingCubes(tree);
		mc.primaryMC(x);
		GLDisplayable primaryMarch = new GLWireframeMesh(mc.result);
		
		mc.dualMC(x);
		WireframeMesh dualMarch = mc.result;
		GLDisplayable glDualMarch = new GLWireframeMesh(dualMarch);
		
		HalfEdgeStructure dualMarchSmooth = new HalfEdgeStructure();
		dualMarchSmooth.init(dualMarch);
		GLHalfedgeStructure glDualMarchSmooth = new GLHalfedgeStructure(dualMarchSmooth);
		glDualMarchSmooth.smooth(1);

		//And show off...
		
		//visualization of the per vertex values (blue = negative, 
		//red = positive, green = 0);
		MyDisplay d = new MyDisplay();
		
		primaryMarch.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		d.addToDisplay(primaryMarch);
		
		glDualMarch.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		d.addToDisplay(glDualMarch);
		
		glDualMarchSmooth.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		d.addToDisplay(glDualMarchSmooth);
		
		GLHashtreeVertices gl_v = new GLHashtreeVertices(tree);
		gl_v.addFunctionValues(x);
		gl_v.configurePreferredShader("shaders/func.vert", 
				"shaders/func.frag", null);
		d.addToDisplay(gl_v);
		
		//discrete approximation of the zero level set: render all
		//tree cubes that have negative values.
		GLHashtree gltree = new GLHashtree(tree);
		gltree.addFunctionValues(x);
		gltree.configurePreferredShader("shaders/octree_zro.vert", 
				"shaders/octree_zro.frag", "shaders/octree_zro.geom");
		d.addToDisplay(gltree);
	}
	
	
	
	
	/**
	 * Samples the implicit function of a sphere at the tree's vertex positions.
	 * @param tree
	 * @return
	 */
	private static ArrayList<Float> sphericalFunction(HashOctree tree){
		
		//initialize the array
		ArrayList<Float> primaryValues = new ArrayList<>(tree.numberOfVertices());
		for(int i = 0; i <tree.numberOfVertices(); i++){
			primaryValues.add(new Float(0));
		}
		
		//compute the implicit function
		Point3f c = new Point3f(0.f,0.f,0.f);
		for(HashOctreeVertex v : tree.getVertices()){
			primaryValues.set(v.index, (float)
					v.position.distance(c) - 1f); 
		}
		return primaryValues;
	}
	
	/**
	 * generating a pointcloud
	 * @param max
	 * @return
	 */
	private static PointCloud nonUniformPointCloud(int max){
		PointCloud pc = new PointCloud();
		float delta = 1.f/max;
		for(int i = -max; i < max; i++){
			for(int j = -max; j < max; j++){
				for(int k = -max; k < max; k++){
					if(k>0){
						k+=3;
						if(j%3 !=0 || i%3 !=0){
							continue;
						}
					}
					pc.points.add(new Point3f(
							delta*i,
							delta*j,
							delta*k));
					pc.normals.add(new Vector3f(1,0,0));
				}
			}

		}
		
		return pc;
	}
	
}
