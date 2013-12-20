package assignment1;

import glWrapper.GLHalfedgeStructure;

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
public class SmoothingDemo {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		HalfEdgeStructure smoothed = new HalfEdgeStructure();
		HalfEdgeStructure moreSmoothed = new HalfEdgeStructure();
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(m);
			smoothed.init(m);
			moreSmoothed.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}

		GLHalfedgeStructure unsmoothed = new GLHalfedgeStructure(hs);
		SimpleSmoother.smooth(smoothed,1);
		SimpleSmoother.smooth(moreSmoothed, 40);
		GLHalfedgeStructure glSmoothed = new GLHalfedgeStructure(smoothed);
		GLHalfedgeStructure glmoreSmoothed = new GLHalfedgeStructure(moreSmoothed);
		MyDisplay disp = new MyDisplay();
		glSmoothed.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		unsmoothed.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		glmoreSmoothed.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		disp.addToDisplay(unsmoothed);
		disp.addToDisplay(glSmoothed);
		disp.addToDisplay(glmoreSmoothed);
	}
}
