package assignment1;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

/**
 * 
 * @author smoser
 *
 */
public class CurvatureShaderDemo {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", false);
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
		
		MyDisplay disp = new MyDisplay();
		teapot.configurePreferredShader("shaders/valence.vert", "shaders/curvature.frag", null);
		disp.addToDisplay(teapot);
	}
}
