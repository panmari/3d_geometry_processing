package assignment4;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;
import sparse.CSRMatrix;


/**
 * Smoothing
 * @author Alf
 *
 */
public class LsmoothingUmaskDemo {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException {
		WireframeMesh mesh = ObjReader.read("objs/bunny5k.obj", true);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(mesh);
		CSRMatrix m = LMatrices.mixedCotanLaplacian(hs);
		LaplacianSmoother.smooth(hs, m, 0.010f);
		GLDisplayable glHsSmooth = new GLHalfedgeStructure(hs);
		glHsSmooth.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		HalfEdgeStructure hs2 = new HalfEdgeStructure();
		hs2.init(mesh);
		m = LMatrices.mixedCotanLaplacian(hs2);
		LaplacianSmoother.unsharpMasking(hs2, m, 0.010f, 2.5f);
		GLDisplayable glHsSharp = new GLHalfedgeStructure(hs2);
		glHsSharp.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		
		
		//add initial mesh
		HalfEdgeStructure hs3 = new HalfEdgeStructure();
		hs3.init(mesh);
		GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs3);
		glHs.configurePreferredShader("shaders/trimesh_flat.vert",
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
				
		MyDisplay d = new MyDisplay();		
		d.addToDisplay(glHs);
		
		d.addToDisplay(glHsSmooth);
		d.addToDisplay(glHsSharp);
	}
}
