package meshes;

import java.util.ArrayList;

import javax.vecmath.Point3f;

/**
 * A Wireframe Mesh represents a mesh as a list of vertices and a list of faces.
 * Very lightweight representation.
 * @author bertholet
 *
 */
public class WireframeMesh {

	public ArrayList<Point3f> vertices;
	public ArrayList<int[]> faces;
	
	public WireframeMesh(){
		vertices = new ArrayList<Point3f>();
		faces = new ArrayList<>();
	}

	private int[] currentFace = new int[3];
	private int currentFaceIdx = 0;
	
	/**
	 * Zomfg this is way to complicated for what I'm trying to do... I could just go on gl-level...
	 * @param idx
	 */
	public void addIndex(Integer idx) {
		currentFace[currentFaceIdx++] = idx;
		if (currentFaceIdx == 3) {
			faces.add(currentFace);
			currentFace = new int[3];
			currentFaceIdx = 0;
		}
	}
}
