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
public class GLHashtreeVertices extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtreeVertices(HashOctree tree) {
		
		super(6*tree.numberofVertices());
		this.myTree = tree;
		//Add Vertices
		//float[] verts = new float[myTree.getNumberOfPoints()*3];
		float[] verts = new float[6*myTree.numberofVertices()*3];
		float[] adjVerts = new float[6*myTree.numberofVertices()*3];
		
		
		int idx = 0;
		Collection<HashOctreeVertex> temp = tree.getVertices();
		for(HashOctreeVertex v : temp) {
			for (int mask = 0b100; mask > 0; mask >>= 1) {
				HashOctreeVertex adjv = myTree.getNbr_v2v(v, mask);
				if (adjv != null) {
					verts[3*idx] = v.position.x;
					verts[3*idx + 1] = v.position.y;
					verts[3*idx + 2] = v.position.z;
					adjVerts[3*idx] = adjv.position.x;
					adjVerts[3*idx + 1] = adjv.position.y;
					adjVerts[3*idx + 2] = adjv.position.z;
					idx++;
				}
			}
			
		}
		
		int[] ind = new int[6*myTree.numberofVertices()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		this.addElement(adjVerts, Semantic.USERSPECIFIED , 3, "parent");

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
