package glWrapper;

import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HEData3d;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLHalfedgeStructure extends GLDisplayable {

	HalfEdgeStructure m;
	float maxValence = Float.MIN_VALUE, minValence = Float.MAX_VALUE; //is actually int, but who cares
	public GLHalfedgeStructure(HalfEdgeStructure m) {
		super(m.getVertices().size());
		this.m = m;
		
		//Add Vertices
		setPositionVertices(m.iteratorV());
		computeNormals();
		
		int[] ind = new int[m.getFaces().size()*3];
		
		//Add faces
		int idx = 0;
		for (Face f: m.getFaces()) {
			Iterator<Vertex> iter = f.iteratorFV();
			while (iter.hasNext())
				ind[idx++] = iter.next().index;
		}
		
		//Here the position coordinates are passed a second time to the shader as color
		this.addIndices(ind);
	}
	
	private void setPositionVertices(Iterator<Vertex> vertices) {
		float[] verts = new float[m.getVertices().size()*3];
		float[] valence = new float[m.getVertices().size()];

		int idx = 0;
		for (Vertex v: m.getVertices()) {
			Point3f p = v.getPos();
			valence[idx] = v.getValence();
			if (valence[idx] > maxValence)
				maxValence = valence[idx];
			if (valence[idx] < minValence)
				minValence = valence[idx];
			verts[idx*3] = p.x;
			verts[idx*3 + 1] = p.y;
			verts[idx*3 + 2] = p.z;
			idx++;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
		this.addElement(valence, Semantic.USERSPECIFIED, 1, "valence");
	}
	
	public void computeNormals() {
		float[] normals = new float[m.getVertices().size()*3];
		int idx = 0;
		for (Vertex v: m.getVertices()) {
			Vector3f normal = new Vector3f();
			Iterator<HalfEdge> iter = v.iteratorVE();
			Vector3f first = iter.next().getOpposite().asVector();
			while (iter.hasNext()) {
				Vector3f second = iter.next().getOpposite().asVector();
				Vector3f partialNormal = new Vector3f();
				partialNormal.cross(first, second);
				float angle = first.angle(second);
				partialNormal.scale(angle);
				normal.add(partialNormal);
				first = second;
			}
			normal.normalize();
			normals[idx++] = normal.x;
			normals[idx++] = normal.y;
			normals[idx++] = normal.z;
		}
		this.addElement(normals, Semantic.USERSPECIFIED , 3, "normal");
	}

	public void smooth(int iterations) {
		HEData3d inputVerts = new HEData3d(m);
		for (Vertex v: m.getVertices()){
			inputVerts.put(v, v.getPos());
		}
		for (int i = iterations; i > 0; i--) {
			HEData3d smoothedVerts = new HEData3d(m);
			for(Vertex v: m.getVertices()) {
				Vector3f smoothed = new Vector3f();
				float count = 0;
				for (Iterator<Vertex> iter = v.iteratorVV(); iter.hasNext();) {
					smoothed.add(inputVerts.get(iter.next()));
					count++;
				}
				smoothed.scale(1/count);
				smoothedVerts.put(v, smoothed);
			}
			inputVerts = smoothedVerts;
		}
		for (Vertex v: m.getVertices()) {
			Point3f pos = v.getPos();
			pos.set(inputVerts.get(v));
		}
		setPositionVertices(m.iteratorV());
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
		glRenderContext.setUniform("max_valence", maxValence);
		glRenderContext.setUniform("min_valence", minValence);
		//Such uniforms can be accessed in the shader by declaring them as
		// uniform <type> name;
		//where type is the appropriate type, e.g. float / vec3 / mat4 etc.
		//this method is called at every rendering pass.
	}


}
