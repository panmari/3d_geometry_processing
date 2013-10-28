package glWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Tuple3f;

import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

import meshes.Face;
import meshes.HEData;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;


public class GLHalfedgeStructure extends GLDisplayable {

	private HalfEdgeStructure myHE;
	HashMap<Object,RenderConfig> glNames;
	
	public GLHalfedgeStructure(HalfEdgeStructure e) {
		super(e.getVertices().size());
		myHE = e;
		
		glNames = new HashMap<Object,RenderConfig>();
		
		//add vertices
		float[] verts = new float[myHE.getVertices().size() *3];
		int[] ind = new int[myHE.getFaces().size()*3];
		
		copyToArray(myHE.getVertices(), verts);
		copyToArray(myHE.getFaces(), ind);
		this.addElement(verts, Semantic.POSITION , 3);
		this.addIndices(ind);
	}


	
	/**
	 * For user specified objects, the specified object will tied to the shader attribute name.
	 * @param objectToRender
	 * @param s
	 * @param name
	 */
	public void add(HEData1d oneDData, String name) {
		RenderConfig c = new RenderConfig();
		c.s = Semantic.USERSPECIFIED;
		c.name = name;
		glNames.put(oneDData, c);
		this.sendElement(oneDData);
	}
	
	/**
	 * For user specified objects, the specified object will tied to the shader attribute name.
	 * @param objectToRender
	 * @param s
	 * @param name
	 */
	public void add(HEData3d threeDData, String name) {
		RenderConfig c = new RenderConfig();
		c.s = Semantic.USERSPECIFIED;
		c.name = name;
		glNames.put(threeDData, c);
		this.sendElement(threeDData);
	}
	
	

	private void sendElement(HEData1d d) {
		float[] vals = new float[d.size()];
		int i=0;
		for(Number n: d){
			vals[i++] = n.floatValue();
		}
		
		RenderConfig c = glNames.get(d);
		if( c!= null)
				this.addElement(vals, Semantic.USERSPECIFIED, 1, c.name);
		else{
			System.out.println("not configured what to map : " + d 
					+ " to, use configureGLSL and createAllConfiguredBuffers");
		}
	}
	
	private void sendElement(HEData3d d) {
		float[] vals = new float[d.size()*3];
		int i=0;
		for(Tuple3f v : d){
			vals[i++] = v.x;
			vals[i++] = v.y;
			vals[i++] = v.z;
		}
		
		RenderConfig c = glNames.get(d);
		if( c!= null)
				this.addElement(vals, Semantic.USERSPECIFIED, 3, c.name);
		else{
			System.out.println("not configured what to map : " + d 
					+ " to, use configureGLSL and createAllConfiguredBuffers");
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


	

	private class RenderConfig{
		Semantic s;
		String name;
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
