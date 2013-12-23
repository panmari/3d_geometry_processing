package assignment5;


import glWrapper.GLHalfEdgeStructure;
import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

public class SmallEdgeCollapserDemo {

	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/buddha.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		GLHalfEdgeStructure untouched = SmallEdgeCollapser.collapse(hs, 0.5f);
		
		MyDisplay d = new MyDisplay();
		GLHalfEdgeStructure glHs = new GLHalfEdgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		glHs.setName("Reduced edges");
		d.addToDisplay(glHs);

		d.addToDisplay(untouched);
	}
}
	