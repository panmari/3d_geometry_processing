package assignment7.conformalMap;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.vecmath.Point2f;

import meshes.HalfEdgeStructure;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.objects.Transformation;

public class GLConstraints extends GLDisplayable {

	public GLConstraints(HalfEdgeStructure hs, ArrayList<Point2f> labels) {
		super(labels.size());
		
		
		int[] ind = new int[labels.size()];
		
		for(int i = 0; i < ind.length; i++){
			ind[i] = i;
		}
		
		this.addIndices(ind);
		this.addElement2D(labels, "texcoords");
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
