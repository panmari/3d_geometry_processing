package assignment5;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;

import glWrapper.GLHalfedgeStructure;
import openGL.MyDisplay;
import meshes.HalfEdge;
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
		HalfEdge h = hs.getHalfEdges().get(0);
		
		//mark the halfedge on untouched object
		ArrayList<Color3f> color = new ArrayList<Color3f>(Collections.nCopies(untouched.getNumberOfVertices(), new Color3f(0,1,0)));
		color.set(h.end().index, new Color3f(1,0,0));
		color.set(h.start().index, new Color3f(1,1,0));
		
		if (!HalfEdgeCollapse.isEdgeCollapsable(h))
			throw new RuntimeException();
		hec.collapseEdgeAndDelete(h);
		
		MyDisplay d = new MyDisplay();
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		glHs.setName("reduced edges");
		d.addToDisplay(glHs);

		untouched.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		untouched.add(color, "color");
		untouched.setName("All edges");
		d.addToDisplay(untouched);
	}
}
