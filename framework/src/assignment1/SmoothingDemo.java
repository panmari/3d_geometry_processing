package assignment1;

import glWrapper.GLHalfedgeStructure;
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
public class SmoothingDemo {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/bunny5k.obj", true);
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
		GLHalfedgeStructure teapot = new GLHalfedgeStructure(hs);
		teapot.smooth(100);
		
		MyDisplay disp = new MyDisplay();
		teapot.configurePreferredShader("shaders/default.vert", "shaders/default.frag", null);
		//teapot.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		disp.addToDisplay(teapot);
	}
}
