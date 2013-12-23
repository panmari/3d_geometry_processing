package assignment1;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.HEData1d;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author smoser
 *
 */
public class NormalsDemo {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		//... do something with it, display it ....
		GLHalfEdgeStructure teapot = new GLHalfEdgeStructure(hs);
		teapot.configurePreferredShader("shaders/default.vert", "shaders/default.frag", null);
		// you might want to change this constant:
		GLHalfEdgeStructure visualized_normals = new GLHalfEdgeStructure(hs);
		visualized_normals.configurePreferredShader("shaders/visualize_normals.vert", "shaders/visualize_normals.frag", null);
		
		GLHalfEdgeStructure diffuse_normals = new GLHalfEdgeStructure(hs);
		diffuse_normals.configurePreferredShader("shaders/visualize_normals.vert", "shaders/diffuse_normals.frag", null);

		GLHalfEdgeStructure geometry_shader_normals = new GLHalfEdgeStructure(hs);
		geometry_shader_normals.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");

		MyDisplay disp = new MyDisplay();
		disp.addToDisplay(teapot);
		disp.addToDisplay(visualized_normals);
		disp.addToDisplay(diffuse_normals);
		disp.addToDisplay(geometry_shader_normals);
	}
}
