package assignment5;

import glWrapper.GLHalfedgeStructure;
import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class QSlimDemo {

	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/bunny_ear.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		GLHalfedgeStructure untouched = new GLHalfedgeStructure(hs);
		HalfEdgeCollapse hec = new HalfEdgeCollapse(hs);
		hec.collapseEdgeAndDelete(hs.getHalfEdges().get(0));
		
		MyDisplay d = new MyDisplay();
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		glHs.setName("reduced edges");
		d.addToDisplay(glHs);
		
		untouched.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		untouched.setName("All edges");
		d.addToDisplay(untouched);
	}
}
