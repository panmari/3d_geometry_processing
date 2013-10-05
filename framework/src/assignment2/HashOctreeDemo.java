package assignment2;

import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;

import java.io.IOException;

import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable;

public class HashOctreeDemo {
	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		//PointCloud pc = ObjReader.readAsPointCloud("./objs/dragon.obj", true);
		PointCloud pc = PlyReader.readPointCloud("./objs/octreeTest.ply", true);
		HashOctree ho = new HashOctree(pc, 4, 1, 1);
		GLDisplayable pcGL = new GLHashtree(ho);
		GLDisplayable pcGL2 = new GLHashtree(ho);
		//... do something with it, display it ....
		
		MyDisplay disp = new MyDisplay();
		pcGL.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree.geom");
		disp.addToDisplay(pcGL);
		pcGL2.configurePreferredShader("shaders/octree.vert", "shaders/octree.frag", "shaders/octree_parents.geom");
		disp.addToDisplay(pcGL2);
	}
}
