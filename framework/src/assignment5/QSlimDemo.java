package assignment5;


import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;

import glWrapper.GLHalfedgeStructure;
import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class QSlimDemo {

	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		GLHalfedgeStructure untouched = new GLHalfedgeStructure(hs);
		QSlim qs = new QSlim(hs);
		qs.simplify(48500);
		
		MyDisplay d = new MyDisplay();
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		glHs.setName("Reduced edges");
		d.addToDisplay(glHs);

		ArrayList<Color3f> color = new ArrayList<Color3f>(Collections.nCopies(untouched.getNumberOfVertices(), new Color3f(1,0,0)));
		untouched.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		untouched.add(color, "color");
		d.addToDisplay(untouched);
	}
}
