package assignment7.remeshing;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.vecmath.Point3f;

import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Point2i;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

public class Remeshing {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException {
		// TODO Auto-generated method stub
		
		WireframeMesh face1 = ObjReader.read("objs/texface.obj", false);
		WireframeMesh face2 = ObjReader.read("objs/texface2.obj", false);
		List<WireframeMesh> meshes = Arrays.asList(new WireframeMesh[]{face1, face2});
		
		Remeshing r = new Remeshing(meshes);
		MyDisplay d = new MyDisplay();
		for (WireframeMesh m: meshes) { 
			GLDisplayable glhs = new GLWireframeMesh(m);
			glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
			glhs.setName("Input");
			d.addToDisplay(glhs);
		}
		for (WireframeMesh m: meshes) { 
			GLDisplayable glhs = new GLWireframeMesh(m);
			glhs.add2D(m.texCoords, "position");
			glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
			glhs.setName("Texture coordinates");
			d.addToDisplay(glhs);
		}
		
		for (HalfEdgeStructure m: r.results) { 
			GLDisplayable glhs = new GLHalfedgeStructure(m);
			glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
			glhs.setName("Result");
			d.addToDisplay(glhs);
		}
	}
	
	private HalfEdgeStructure reference;
	private HashSet<Integer> deathNote = new HashSet<>();
	List<HalfEdgeStructure> results = new ArrayList<>();
	
	public Remeshing(List<WireframeMesh> meshes) throws MeshNotOrientedException, DanglingTriangleException {
		// take one as reference mesh (eg least degenerated triangles in parametrization (min angle largest))
		List<HalfEdgeStructure> meshHs = new ArrayList<>();
		for(WireframeMesh m: meshes) {
			HalfEdgeStructure hs = new HalfEdgeStructure();
			hs.init(m);		
			meshHs.add(hs);
		}
		
		determineReferenceMesh(meshHs);
		
		for (HalfEdgeStructure hs: meshHs) {
			HalfEdgeStructure hsRemeshed = remesh(hs);
			results.add(hsRemeshed);
		}
	}

	private HalfEdgeStructure remesh(HalfEdgeStructure hs) {
		// Copy topology from reference mesh.
		HalfEdgeStructure remeshed = new HalfEdgeStructure(this.reference);
		RemeshTexture rt = new RemeshTexture(hs);
		
		// Set positions to interpolated coordinates of hs.
		for(Vertex v: remeshed.getVertices()) {
			Point3f p = v.getPos();
			Point3f newPos = rt.interpolateBilinear(v);
			if (newPos == null) {
				// Vertex would not work, it's parameters don't lie on new map.
				deathNote.add(v.index);
			}
			else {
				p.set(newPos);
			}
		}
		
		// Delete every vertex that wasn't mappable.
		//remeshed.removeVertices(deathNote);
		return remeshed;
	}

	/**
	 * TODO: don't just take first as reference, find 'best' mesh
	 * @param facesHs
	 */
	private void determineReferenceMesh(List<HalfEdgeStructure> facesHs) {
		this.reference = facesHs.get(0);
	}
}
