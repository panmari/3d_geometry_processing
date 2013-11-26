package assignment6;

import glWrapper.GLUpdatableHEStructure;

import java.io.IOException;

import javax.vecmath.Vector3f;

import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyPickingDisplay;
import assignment4.Assignment4_2_smoothing;
import assignment4.generatedMeshes.Cylinder2;



/**
 * In this class, the usage of the picking display is demonstrated.
 * 
 *  To get something like an interactive deformation experience, don't use meshes with more than 
 *  5000 vertices. QSlim is great to reduce arbitrary meshes to such a size.
 * @author bertholet
 *
 */
public class Assignment6_interactive {

	public static void main(String[] args) throws Exception{
		
		//if your setup is correct a psychedelic bunny should be displayed
		//interactivityDemo();
		
		//start the picking display. If your setup is correct you should be able
		// to pick and move mesh parts
		pickingDemo();
	}

	
	/**
	 * Open a picking display with a mesh loaded.
	 * @throws Exception
	 */
	private static void pickingDemo() throws Exception{
		//WireframeMesh wf = new Cylinder2(0.3f,1.8f).result;
		WireframeMesh wf = ObjReader.read("./objs/head.obj", true);
		//WireframeMesh wf = ObjReader.read("./objs/armadillo_1000.obj", true);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		

		GLUpdatableHEStructure glHE = new GLUpdatableHEStructure(hs);
		MyPickingDisplay disp = new MyPickingDisplay();
		DeformationPickingProcessor pr = new DeformationPickingProcessor(hs, glHE);
		disp.addAsPickable(glHE, pr);
	}

	
	/**
	 * demonstrates the usage of an updateable GlHalfEdgeStructure
	 * @throws Exception
	 */
	private static void interactivityDemo() throws Exception {
		
		
		//read mesh
		WireframeMesh wf = ObjReader.read("objs/bunny.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);

		//create a picking display
		MyPickingDisplay disp = new MyPickingDisplay();

		//new wrapper that allows updates of data buffers on the gpu
		GLUpdatableHEStructure glHE = new GLUpdatableHEStructure(hs);
		glHE.configurePreferredShader("shaders/trimesh_flatColor3f.vert",
				"shaders/trimesh_flatColor3f.frag", 
				"shaders/trimesh_flatColor3f.geom");

		//associate color data
		HEData3d color = colors(hs);
		glHE.add(color, "color");
		disp.addToDisplay(glHE);
		
		int i = 0;
		while(true && disp.isVisible()){
			i++;
			
			//change data
			for(Vertex v: hs.getVertices()){
				color.get(v).x = (float) ( 2+Math.abs(Math.sin(i/4f)))/4;
				color.get(v).y = (float) ( 2+Math.abs(Math.sin(i/13f + Math.PI/3)))/4;
				color.get(v).z = (float) ( 2+Math.abs(Math.sin(i/17f)))/4;
				v.getPos().x+= Math.sin((i+ v.index)/6f)*0.005;
				v.getPos().y+= Math.sin((i+ v.index)/7f)*0.005;
				v.getPos().z+= Math.sin((i+ v.index)/5f)*0.005;
			}
	
			//update the gpu buffers
			glHE.updatePosition();
			glHE.update("color");
			
			//update the display
			disp.updateDisplay();
			
			//Zzzzz
			Thread.sleep(30);
			
		}
	}

	private static HEData3d colors(HalfEdgeStructure hs) {
		HEData3d colors = new HEData3d(hs);
		for(Vertex v: hs.getVertices()){
			colors.put(v, new Vector3f(0,1,0));
		}
		return colors;
	}
}
