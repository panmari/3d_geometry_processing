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
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", true);
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
		GLHalfedgeStructure smoothed = new GLHalfedgeStructure(hs);
		GLHalfedgeStructure smoothedMore = new GLHalfedgeStructure(hs);
		GLHalfedgeStructure unsmoothed = new GLHalfedgeStructure(hs);

		GLHalfedgeStructure teapot2 = new GLHalfedgeStructure(hs);
		// you might want to change this constant:
		smoothed.smooth(1);
		smoothedMore.smooth(40);
		
		MyDisplay disp = new MyDisplay();
		smoothed.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		unsmoothed.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		smoothedMore.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		teapot2.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		disp.addToDisplay(unsmoothed);
		disp.addToDisplay(smoothed);
		disp.addToDisplay(smoothedMore);
		disp.addToDisplay(teapot2);
	}
}
