package assignment4;

import glWrapper.GLWireframeMesh;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class Assignment4_2_uMasking {

	public static void main(String[] arg) throws IOException{
		headDemo();
					
	}

	private static void headDemo() throws IOException {
		WireframeMesh m = ObjReader.read("./objs/head.obj", true);//*/
		
		MyDisplay disp = new MyDisplay();
		GLWireframeMesh glwf = new GLWireframeMesh(m);
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
			try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		//do your unsharp masking thing...
	}

}
