package assignment7.remeshing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
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

	/**
	 * @param mesh a Half Edge Structure that needs to be remeshed.
	 */
	public RemeshTexture(HalfEdgeStructure mesh) {
		for (int i = 0; i < CELL_NUMBER; i++) {
			for (int j = 0; j < CELL_NUMBER; j++) {
				textureGrid.put(new Point2i(i,j), new ArrayList<Face>());
			}
		}
		for(Face f: mesh.getFaces()) {
			// assert that the faces are small enough, ie indices are within +/-1 of each other.
			for(Point2f corner: new BoundingBox(f)) {
				Point2i cell = cellCoordinate(corner);
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
	
	/**
	 * Creates a bounding box around the texture coordinates of the given face. 
	 * The corners of the bounding box can be iterated.
	 */
	private class BoundingBox implements Iterable<Point2f>{
		Point2f upperLeft = new Point2f(Float.MIN_VALUE, Float.MIN_VALUE);
		Point2f lowerRight = new Point2f(Float.MAX_VALUE, Float.MAX_VALUE);
		BoundingBox(Face f) {
			Iterator<Vertex> iter = f.iteratorFV();
			while (iter.hasNext()) {
				Point2f tex = iter.next().tex;
				if (upperLeft.x < tex.x) 
					upperLeft.x = tex.x;
				if (upperLeft.y < tex.y) 
					upperLeft.y = tex.y;
				if (lowerRight.x > tex.x) 
					lowerRight.x = tex.x;
				if (lowerRight.y > tex.y) 
					lowerRight.y = tex.y;
			}
		}
		
		/**
		 * Iterate over the corners of this bounding box
		 */
		@Override
		public Iterator<Point2f> iterator() {
			Point2f[] p = new Point2f[]{upperLeft, new Point2f(upperLeft.x, lowerRight.y), 
					lowerRight, new Point2f(lowerRight.x, upperLeft.y)};
			return Arrays.asList(p).iterator();
		}
	}
	
}
