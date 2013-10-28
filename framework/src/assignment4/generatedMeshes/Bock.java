package assignment4.generatedMeshes;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;

import javax.vecmath.Point3f;

import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import openGL.MyDisplay;
import assignment4.Assignment4_3_minimalSurfaces;

public class Bock {
	
	public WireframeMesh result;
	private float width;
	//public HEData1d boundary;
	private float height;
	private float length;

	
	public Bock(float width, float length, float height){
		this.length = length;
		this.height = height;
		this.width = width;
		this.setUp();
	}
	
	
	public void setUp(){
		this.result = new WireframeMesh();
		
		int num1 = 40;
		
		
		for(int i = 0; i < num1 ; i++){
			for(int j = 0; j < num1 ; j++){
				 this.result.vertices.add(new Point3f(width/num1 * j -width/2, height/num1*i, -length/2));
			}
		}
		for(int i = 0; i < num1 ; i++){
			for(int j = 0; j < num1 ; j++){
				 this.result.vertices.add(new Point3f(width/num1 *j -width/2, height, -length/2 + length/num1 * i));
			}
		}
		
		for(int i = 0; i < num1 +1 ; i++){
			for(int j = 0; j < num1 ; j++){
				 this.result.vertices.add(new Point3f(width/num1*j -width/2, height - height/num1 * i, length/2));
			}
		}
		
		
		for(int i = 0; i < 3*num1; i++){
			for(int j = 0 ; j < num1-1; j++){
				int[] fc1 = {i*num1 +j + 1,i*num1 + j, (i+1)*num1 +j + 1};
				int[] fc2 = {(i+1)*num1 +j+1 , i*num1 + j,  (i+1)*num1 +j};
				this.result.faces.add(fc1);
				this.result.faces.add(fc2);
			}
		}
		
	}
	
	public static void main(String[] arg) throws MeshNotOrientedException, DanglingTriangleException{
		Bock b = new Bock(0.5f,0.5f,0.5f);
		MyDisplay d = new MyDisplay();
		GLWireframeMesh w = new GLWireframeMesh(b.result);
		d.addToDisplay(w);
		
		
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(b.result);
		
		//HEData3d colors = FixedBoundarySmoothing.binaryColorMap(b.boundary, hs);
		HEData3d colors = Assignment4_3_minimalSurfaces.binaryColorMap(Assignment4_3_minimalSurfaces.collectBoundary(hs, 1), hs);
		GLHalfedgeStructure glHE = new GLHalfedgeStructure(hs);
		glHE.configurePreferredShader("shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");
		glHE.add(colors, "color");
		d.addToDisplay(glHE);
	}

}
