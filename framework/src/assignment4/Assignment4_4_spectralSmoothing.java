package assignment4;

import static org.junit.Assert.fail;
import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import openGL.MyDisplay;

import com.jogamp.opengl.math.FloatUtil;

import sparse.CSRMatrix;
import sparse.SCIPYEVD;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;



/**
 * You can implement the spectral smoothing application here....
 * @author Alf
 *
 */
public class Assignment4_4_spectralSmoothing {

	public static void main(String[] args) throws IOException, MeshNotOrientedException, DanglingTriangleException{
		HalfEdgeStructure hs = new HalfEdgeStructure();
		WireframeMesh mesh = ObjReader.read("objs/bunny.obj", false);
		hs.init(mesh);
		
		CSRMatrix m = LMatrices.symmetricCotanLaplacian(hs);
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		SCIPYEVD.doSVD(m, "eigenStuff", 20, eigenValues, eigenVectors);
		MyDisplay d = new MyDisplay();
		int eigenVectorCount = 0;
		for (ArrayList<Float> eigenVector: eigenVectors) {
			GLHalfedgeStructure glHs = new GLHalfedgeStructure(hs);
			ArrayList<Color3f> eigenVectorColor = new ArrayList<Color3f>();
			float minEV = Collections.min(eigenVector);
			float maxEV = Collections.max(eigenVector);
			// coloring as described by @alf
			for (Float v: eigenVector) {
				float vTilde = (v - minEV)/(Math.max(maxEV - minEV, 0.001f));
				Color3f c = new Color3f();
				c.x = Math.min(2*Math.max(vTilde, 0.1f), 0.8f);
				c.z = Math.min(2*Math.max(1 - vTilde, 0.1f), 0.8f);
				c.y = Math.min(c.x, c.z);
				eigenVectorColor.add(c);
			}
			glHs.add(eigenVectorColor, "color");
			glHs.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
					"shaders/trimesh_flatColor3f.frag", 
					"shaders/trimesh_flatColor3f.geom");
			glHs.setName("Eigenvector nr. " + ++eigenVectorCount);
			d.addToDisplay(glHs);
		}
		//System.out.println(eigenVectors);
	}
}
