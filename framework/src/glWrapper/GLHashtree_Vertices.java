package glWrapper;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;

import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

import assignment2.HashOctree;
import assignment2.HashOctreeVertex;

/**
 * GLWrapper which will send the HashOctree vertex positions to the GPU
 * @author Alf
 *
 */
public class GLHashtree_Vertices extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtree_Vertices(HashOctree tree) {
		
		super(tree.numberofVertices());
		this.myTree = tree;
		//Add Vertices
		//float[] verts = new float[myTree.getNumberOfPoints()*3];
		float[] verts = new float[myTree.numberofVertices()*3];
		
		
		int idx = 0;
		Collection<HashOctreeVertex> temp = tree.getVertices();
		for(HashOctreeVertex v : temp){
			verts[idx++] = v.position.x;
			verts[idx++] = v.position.y;
			verts[idx++] = v.position.z;
		}
		
		int[] ind = new int[myTree.numberofVertices()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		this.addIndices(ind);
		
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberofVertices()];
		
		for(HashOctreeVertex v: myTree.getVertices()){
			vals[v.index] = values.get(v.index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}

	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		// TODO Auto-generated method stub
		
	}
}
