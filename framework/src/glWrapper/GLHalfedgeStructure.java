package glWrapper;

import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

public class GLHalfedgeStructure extends GLDisplayable {

	HalfEdgeStructure m;
	public GLHalfedgeStructure(HalfEdgeStructure m) {
		super(m.getVertices().size());
		this.m = m;
		
		//Add Vertices
		float[] verts = new float[m.getVertices().size()*3];
		int[] ind = new int[m.getFaces().size()*3];
		
		int idx = 0;
		for (Vertex v: m.getVertices()) {
			Point3f p = v.getPos();
			verts[idx++] = p.x;
			verts[idx++] = p.y;
			verts[idx++] = p.z;
		}
		idx = 0;
		for (Face f: m.getFaces()) {
			Iterator<Vertex> iter = f.iteratorFV();
			while (iter.hasNext())
				ind[idx++] = iter.next().index;
		}
		
		this.addElement(verts, Semantic.POSITION , 3);
		//Here the position coordinates are passed a second time to the shader as color
		this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
		this.addIndices(ind);
	}

	
	/**
	 * Return the gl render flag to inform opengl that the indices/positions describe
	 * triangles
	 */
	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}


	/**
	 * No additional uniform variabes are passed to the shader.
	 */
	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
		//additional uniforms can be loaded using the function
		//glRenderContext.setUniform(name, val);
		
		//Such uniforms can be accessed in the shader by declaring them as
		// uniform <type> name;
		//where type is the appropriate type, e.g. float / vec3 / mat4 etc.
		//this method is called at every rendering pass.
	}


}
