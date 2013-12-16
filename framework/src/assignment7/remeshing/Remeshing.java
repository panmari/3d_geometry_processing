package assignment7.remeshing;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;

import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.Face;
import meshes.HalfEdge;
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
			this.results.add(hsRemeshed);
		}

		handleDeathNote();
		
	}
	
	private void handleDeathNote() {
		System.out.println("Going to remove " + deathNote.size() + " vertices");
		ArrayList<WireframeMesh> wmResults = new ArrayList<>();
		for (HalfEdgeStructure hs: this.results) {
			HashSet<HalfEdge> deadEdges = new HashSet<>();
			HashSet<Face> deathFaces = new HashSet<>();
			for (int idx: deathNote) { 
				Vertex v = hs.getVertices().get(idx);
				Iterator<HalfEdge> iter = v.iteratorVE();
				while (iter.hasNext()) {
					HalfEdge e = iter.next();
					deadEdges.add(e);
					deadEdges.add(e.getOpposite());
					deathFaces.add(e.getFace());
					deathFaces.add(e.getOpposite().getFace());
				}
			}
			hs.getVertices().removeAll(deathNote);
			hs.getFaces().removeAll(deathFaces);
			hs.getHalfEdges().removeAll(deadEdges);
			for (HalfEdge he: hs.getHalfEdges()) {
				if (deathFaces.contains(he.getFace()))
					he.setFace(null);
			}
			// remove dangling triangles, can not iterate over vertices around a vertex as long exist.
			List<List<Face>> components = new ArrayList<>();
			HashSet<Face> uninspectedFaces = new HashSet<>(hs.getFaces());
			// find all fully connected (common edge) components in mesh
			while(!uninspectedFaces.isEmpty()) {
				ArrayList<Face> component = new ArrayList<Face>();
				Stack<Face> inspectFaces = new Stack<>();
				inspectFaces.push(uninspectedFaces.iterator().next());
				while (!inspectFaces.isEmpty()) {
					Face f = inspectFaces.pop();
					Iterator<HalfEdge> faceIter = f.iteratorFE();
					while(faceIter.hasNext()) {
						Face nbrFace = faceIter.next().getOpposite().getFace();
						if (nbrFace != null && uninspectedFaces.contains(nbrFace)) {
							inspectFaces.push(nbrFace);
						}
					}
					component.add(f);
					uninspectedFaces.remove(f);
				}
				components.add(component);
			}
			
			// choose maximal component as main mesh
			List<Face> maxComponent = components.get(0);
			for (List<Face> c: components) {
				if (maxComponent.size() < c.size())
					maxComponent = c;
			}
			components.remove(maxComponent);
			HashSet<Vertex> livingVertices = new HashSet<>();
			// find all vertices belonging to maximum component
			for(Face livingFace: maxComponent) {
				Iterator<Vertex> livingVertexIter = livingFace.iteratorFV();
				while(livingVertexIter.hasNext())
					livingVertices.add(livingVertexIter.next());
			}
			hs.getVertices().retainAll(livingVertices);
			hs.getFaces().retainAll(maxComponent);
			wmResults.add(new WireframeMesh(hs));
		}
		results.clear();
		for (WireframeMesh wm: wmResults) {
			HalfEdgeStructure hs = new HalfEdgeStructure();
			try {
				hs.init(wm);
			} catch (MeshNotOrientedException | DanglingTriangleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			results.add(hs);
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
