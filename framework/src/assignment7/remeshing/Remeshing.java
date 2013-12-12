package assignment7.remeshing;

import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Point2i;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

public class Remeshing {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException {
		// TODO Auto-generated method stub
		
		WireframeMesh face1 = ObjReader.read("objs/texface.obj", false);
		WireframeMesh face2 = ObjReader.read("objs/texface2.obj", false);
		List<WireframeMesh> faces = Arrays.asList(new WireframeMesh[]{face1, face2});
		
		new Remeshing(faces);
	}
	
	private HalfEdgeStructure reference;
	private RemeshTexture referenceTG;
	
	public Remeshing(List<WireframeMesh> meshes) throws MeshNotOrientedException, DanglingTriangleException {
		MyDisplay d = new MyDisplay();
		// take one as reference mesh (eg least degenerated triangles in parametrization (min angle largest))
		List<HalfEdgeStructure> facesHs = new ArrayList<>();
		for(WireframeMesh m: meshes) {
			HalfEdgeStructure hs = new HalfEdgeStructure();
			hs.init(m);		
			facesHs.add(hs);
		}
		
		determineReferenceMesh(facesHs);
		
		
		for (WireframeMesh m: meshes) { 
			GLDisplayable glhs = new GLWireframeMesh(m);
			//glhs.addElement2D(mapper.get(), Semantic.POSITION, "pos");
			glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
			d.addToDisplay(glhs);
		}
	}

	/**
	 * TODO: don't just take first as reference, find 'best' mesh
	 * @param facesHs
	 */
	private void determineReferenceMesh(List<HalfEdgeStructure> facesHs) {
		this.reference = facesHs.get(0);
		this.referenceTG = new RemeshTexture(this.reference);
	}
}
