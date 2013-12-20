package assignment1;

import java.util.Iterator;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;

public class SimpleSmoother {
	public static void smooth(HalfEdgeStructure m, int iterations) {
		HEData3d inputVerts = new HEData3d(m);
		for (Vertex v : m.getVertices()) {
			inputVerts.put(v, v.getPos());
		}
		for (int i = iterations; i > 0; i--) {
			HEData3d smoothedVerts = new HEData3d(m);
			for (Vertex v : m.getVertices()) {
				Vector3f smoothed = new Vector3f();
				float count = 0;
				for (Iterator<Vertex> iter = v.iteratorVV(); iter.hasNext();) {
					smoothed.add(inputVerts.get(iter.next()));
					count++;
				}
				smoothed.scale(1 / count);
				smoothedVerts.put(v, smoothed);
			}
			inputVerts = smoothedVerts;
		}
		for (Vertex v : m.getVertices()) {
			Point3f pos = v.getPos();
			pos.set(inputVerts.get(v));
		}
	}
}
