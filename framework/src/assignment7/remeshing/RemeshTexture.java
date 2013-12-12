package assignment7.remeshing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.Point2i;
import meshes.Vertex;

public class RemeshTexture {
	private HashMap<Point2i, List<Face>> textureGrid = new HashMap<>();
	private static final int CELL_NUMBER = 10;

	public RemeshTexture(HalfEdgeStructure mesh) {
		for (int i = 0; i < CELL_NUMBER; i++) {
			for (int j = 0; j < CELL_NUMBER; j++) {
				textureGrid.put(new Point2i(i,j), new ArrayList<Face>());
			}
		}
		for(Face f: mesh.getFaces()) {
			// assert that indices are within +/-1 of each other.
			Iterator<Vertex> iter = f.iteratorFV();
			while (iter.hasNext()) {
				Vertex v = iter.next();
				Point2i cell = cellCoordinate(v.tex);
				textureGrid.get(cell).add(f);
			}
		}
	}
	
	private Point2i cellCoordinate(Point2f texCoord) {
		return new Point2i((int) (texCoord.x*CELL_NUMBER), (int) (texCoord.y * CELL_NUMBER));
	}
	
	private Face findFaceAround(Point2f texCoord) {
		for (Face f: textureGrid.get(cellCoordinate(texCoord))){
			if (f.contains(texCoord)) {
				return f;
			}
		}
		return null;
	}
	
	/**
	 * @param v Vertex from reference mesh that needs to be interpolated on this parametrization.
	 */
	public Point3f interpolateBilinear(Vertex v) {
		Face f = findFaceAround(v.tex);
		if (f == null)
			return null;
		Vector3f weights = f.bilinearInterpolationWeights(v.tex);
		Point3f interpolatedPosition = new Point3f();
		Iterator<Vertex> iterV = f.iteratorFV();
		interpolatedPosition.scaleAdd(weights.x, new Point3f(iterV.next().getPos()), interpolatedPosition);
		interpolatedPosition.scaleAdd(weights.y, new Point3f(iterV.next().getPos()), interpolatedPosition);
		interpolatedPosition.scaleAdd(weights.z, new Point3f(iterV.next().getPos()), interpolatedPosition);
		return interpolatedPosition;
	}
}
