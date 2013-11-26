package glWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Tuple3f;

import meshes.Face;
import meshes.HEData;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLRenderer;
import openGL.gl.interactive.GLUpdateable;
import openGL.objects.Transformation;


public class GLUpdatableHEStructure extends GLUpdateable{

	private HalfEdgeStructure myHE;
	HashMap<String,HEData> name2Data;
	//HEData3d colors;
	
	public GLUpdatableHEStructure(HalfEdgeStructure e) {
		super(e.getVertices().size());
		myHE = e;	
		name2Data = new HashMap<String, HEData>();
		
		//add vertices
		float[] verts = new float[myHE.getVertices().size() *3];
		int[] ind = new int[myHE.getFaces().size()*3];
		
		copyToArray(myHE.getVertices(), verts);
		copyToArray(myHE.getFaces(), ind);
		this.addElement(verts, Semantic.POSITION , 3, "position");
		
		this.addIndices(ind);
		this.configurePreferredShader(
				"shaders/trimesh_flatColor3f.vert", 
				"shaders/trimesh_flatColor3f.frag",
				"shaders/trimesh_flatColor3f.geom");
	}


	/**
	 * The position buffer will be updated in the next pass
	 */
	public void updatePosition() {
		float[] data = getDataBuffer("position");
		copyToArray(myHE.getVertices(), data);
		scheduleUpdate("position");/***/
	}
	
	/**
	 * The gpu buffer associated to the name glName will be updated in
	 * the next render pass.
	 * @param glName
	 */
	public void update(String glName){
		float[] data = getDataBuffer(glName);
		copyToArray(name2Data.get(glName), data);
		scheduleUpdate(glName);/***/
	}
	
	/**
	 * For user specified objects, the specified object will tied to the shader attribute name.
	 * If the oneDData is changed outside of the class, calling the
	 * update(name) method will cause the associated gpu buffers to be updated.
	 */
	public void add(HEData1d oneDData, String name) {
		name2Data.put(name,oneDData);
		this.sendElement(oneDData, name);
	}
	
	/**
	 * For user specified objects, the specified object will tied to the shader attribute name.
	 * If the data is changed outside of the class, calling the
	 * update(name) method will cause the associated gpu buffers to be updated.
	 * 
	 * @param threeDData
	 * @param name
	 */
	public void add(HEData3d threeDData, String name) {
		name2Data.put(name,threeDData);
		this.sendElement(threeDData, name);
	}
	
	
	
	

	private void sendElement(HEData1d d, String name) {
		float[] vals = new float[d.size()];
		copyToArray(d, vals);
		this.addElement(vals, Semantic.USERSPECIFIED, 1,name);
	
	}




	private void sendElement(HEData3d d, String name) {
		float[] vals = new float[d.size()*3];
		copyToArray(d, vals);
		
		
		this.addElement(vals, Semantic.USERSPECIFIED, 3,name);
	}


	private void copyToArray(HEData d, float[] vals) {
		if(d instanceof HEData1d){
			int i=0;
			for(Number n: (HEData1d) d){
				vals[i++] = n.floatValue();
			}
		}
		else if(d instanceof HEData3d){
			int i=0;
			for(Tuple3f v : (HEData3d) d){
				vals[i++] = v.x;
				vals[i++] = v.y;
				vals[i++] = v.z;
			}
		}
		else{
			throw new UnsupportedOperationException();
		}
	}


	private void copyToArray(ArrayList<Face> faces, int[] ind) {
		Iterator<Vertex> it;
		int i=0;
		for(Face f: faces){
			it = f.iteratorFV();
			while(it.hasNext()){
				ind[i++] = it.next().index;
			}
		}
	}

	private void copyToArray(ArrayList<Vertex> vertices, float[] verts) {
		int i = 0;
		for(Vertex v: vertices){
			v.index = i/3;
			verts[i++]= v.getPos().x;
			verts[i++]= v.getPos().y;
			verts[i++]= v.getPos().z;
			
		}
	}


	

	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}


	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// no additional uniforms		
	}



}
