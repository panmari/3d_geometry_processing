package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import assignment3.SSDMatrices;


/**
 * Smoothing
 * @author Alf
 *
 */
public class Assignment4_2_smoothing {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException {
		WireframeMesh mesh = ObjReader.read("objs/bunny.obj", false);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(mesh);
		
		MyDisplay d = new MyDisplay();
		CSRMatrix mMixed = LMatrices.mixedCotanLaplacian(hs);
		CSRMatrix mUniform = LMatrices.uniformLaplacian(hs);
		CSRMatrix[] laplacians = new CSRMatrix[]{ mUniform, mMixed };
		for (CSRMatrix m: laplacians) {
			//CSRMatrix[] laplacians = new CSRMatrix[]{ mUniform, mMixed };
			float lambda = 0.1f;
			CSRMatrix I = SSDMatrices.eye(hs.getVertices().size(), hs.getVertices().size());
			m.scale(-lambda);
			CSRMatrix smoothM = new CSRMatrix(0,0);
			smoothM.add(I, m);
			ArrayList<Tuple3f> smoothedVertices = new ArrayList<Tuple3f>();
			LMatrices.mult(smoothM, hs, smoothedVertices);
			GLHalfedgeStructure glHsSmooth = new GLHalfedgeStructure(hs);
			glHsSmooth.add(smoothedVertices, "position");
			glHsSmooth.configurePreferredShader("shaders/trimesh_flat.vert",
					"shaders/trimesh_flat.frag", 
					"shaders/trimesh_flat.geom");
			d.addToDisplay(glHsSmooth);
		}
		
		//add initial mesh
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		d.addToDisplay(glHs);
	}
}
