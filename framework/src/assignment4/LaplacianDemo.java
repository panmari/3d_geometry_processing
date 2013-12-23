package assignment4;

import glWrapper.GLHalfEdgeStructure;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;

public class LaplacianDemo {
	
	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		WireframeMesh m = ObjReader.read("objs/dragon.obj", false);
		HalfEdgeStructure hs1 = new HalfEdgeStructure();
		hs1.init(m);
		
		m = ObjReader.read("objs/sphere.obj", true);
		HalfEdgeStructure hs2 = new HalfEdgeStructure();
		hs2.init(m);
		HalfEdgeStructure[] hsArray = new HalfEdgeStructure[]{hs1, hs2};
		MyDisplay d = new MyDisplay();
		
		for (HalfEdgeStructure hs: hsArray) {
			CSRMatrix mMixed = LMatrices.mixedCotanLaplacian(hs);
			CSRMatrix mUniform = LMatrices.uniformLaplacian(hs);
			CSRMatrix[] laplacians = new CSRMatrix[]{ mUniform, mMixed };
			for (CSRMatrix laplacian: laplacians) {
				ArrayList<Tuple3f> curvatures = new ArrayList<Tuple3f>();
				LMatrices.mult(laplacian, hs, curvatures);
				
				GLHalfEdgeStructure glHs = new GLHalfEdgeStructure(hs);
				glHs.add(curvatures, "curvature");
				//And show off...
				
				glHs.configurePreferredShader("shaders/curvature_arrows.vert",
						"shaders/curvature_arrows.frag", 
						"shaders/curvature_arrows.geom");
				d.addToDisplay(glHs);
			}
			//And show off...
			GLHalfEdgeStructure glMesh = new GLHalfEdgeStructure(hs);
			glMesh.configurePreferredShader("shaders/trimesh_flat.vert",
					"shaders/trimesh_flat.frag", 
					"shaders/trimesh_flat.geom");
			d.addToDisplay(glMesh);
		}
	}
}
