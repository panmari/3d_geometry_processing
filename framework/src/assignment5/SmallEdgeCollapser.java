package assignment5;

import glWrapper.GLHalfedgeStructure;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;

public class SmallEdgeCollapser {

 	public static GLHalfedgeStructure collapse(HalfEdgeStructure hs, float threshold) {
 		GLHalfedgeStructure untouched = new GLHalfedgeStructure(hs);
		HalfEdgeCollapse hec = new HalfEdgeCollapse(hs);
		ArrayList<Color3f> color = new ArrayList<Color3f>(Collections.nCopies(untouched.getNumberOfVertices(), new Color3f(0,1,0)));
		int deadCount = 0;
		int deadCountIter;
		do {
			deadCountIter = 0;
			for(HalfEdge h: hs.getHalfEdges()) {
				if (h.asVector().length() > threshold ||
						hec.isEdgeDead(h) ||
						hec.isCollapseMeshInv(h, h.end().getPos()) ||
						!HalfEdgeCollapse.isEdgeCollapsable(h))
					continue;
				// mark the halfedge on untouched object
				color.set(h.start().index, new Color3f(1,1,0));
				color.set(h.end().index, new Color3f(1,0,0));
				hec.collapseEdge(h);
				deadCountIter++;
			}
			deadCount += deadCountIter;
			System.out.println("Deleted " + deadCountIter + " edges in this iteration, altogether " + deadCount);
		} while(deadCountIter > 0);
		
		hec.finish();		
		hs.enumerateVertices();

		untouched.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		untouched.add(color, "color");
		untouched.setName("Yellow -> Red");
		return untouched;
 	}
}
