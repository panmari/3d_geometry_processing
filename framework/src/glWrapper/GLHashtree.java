package glWrapper;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;
import assignment2.HashOctree;
import assignment2.HashOctreeCell;

/**
 * Simple GLWrapper for the {@link HashOctree}.
 * The octree is sent to the Gpu as set of cell-center points and
 * side lengths. 
 * @author Alf
 *
 */
public class GLHashtree extends GLDisplayable {

	private HashOctree myTree;
	public GLHashtree(HashOctree tree) {
		
		super(tree.numberOfLeafs());
		this.myTree = tree;
		//Add Vertices
		float[] verts = new float[myTree.numberOfLeafs()*3];
		float[] parents = new float[myTree.numberOfLeafs()*3];
		float[] sides = new float[myTree.numberOfLeafs()];
		
		
		int idx = 0;
		for(HashOctreeCell n : tree.getLeafs()){
			verts[idx*3 + 0] = n.center.x;
			verts[idx*3 + 1] = n.center.y;
			verts[idx*3 + 2] = n.center.z;
			Point3f parentCenter = tree.getParent(n).center;
			parents[idx*3 + 0] = parentCenter.x;
			parents[idx*3 + 1] = parentCenter.y;
			parents[idx*3 + 2] = parentCenter.z;
			sides[idx] = n.side;
			idx++;
		}
		
		int[] ind = new int[myTree.numberOfLeafs()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		this.addElement(verts, Semantic.POSITION , 3);
		this.addElement(sides, Semantic.USERSPECIFIED , 1, "side");
		this.addElement(parents, Semantic.USERSPECIFIED , 3, "parent");
		
		this.addIndices(ind);
		
	}
	
	/**
	 * values are given by OctreeVertex
	 * @param values
	 */
	public void addFunctionValues(ArrayList<Float> values){
		float[] vals = new float[myTree.numberOfLeafs()];
		
		for(HashOctreeCell n: myTree.getLeafs()){
			for(int i = 0; i <=0b111; i++){
				vals[n.leafIndex] += values.get(myTree.getNbr_c2v(n, i).index);//*/Math.signum(values.get(myTree.getVertex(n, i).index));
			}
			vals[n.leafIndex] /=8;
			//vals[n.leafIndex] = Math.abs(vals[n.leafIndex]) < 5.99 ? -1: 1;
		}
		
		this.addElement(vals, Semantic.USERSPECIFIED , 1, "func");
	}

	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
	}
}
