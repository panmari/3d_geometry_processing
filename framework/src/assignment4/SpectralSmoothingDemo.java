package assignment4;

import java.io.IOException;
import java.util.ArrayList;

import org.jblas.FloatFunction;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import glWrapper.GLHalfedgeStructure;
import openGL.MyDisplay;

public class SpectralSmoothingDemo {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		HalfEdgeStructure hs = new HalfEdgeStructure();
		WireframeMesh mesh = ObjReader.read("objs/dragon_5000.obj", false);
		hs.init(mesh);	
		
		MyDisplay d = new MyDisplay();
		SpectralSmoothing.boostMediumFrequencies(hs, 40);
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		d.addToDisplay(glHs);
	}

}
