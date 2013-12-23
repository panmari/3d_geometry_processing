package assignment7.conformalMap.delauny;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

/**
 * User: nix Date: 03-Feb-2005 Time: 13:54:52 To change this template use Options | File Templates.
 */
public class DelaunayTriangulation {

	public static final float EPSILON = 0.00001f; //EPSILON is defined to compare floats with zero

	public static class Triangle {

		public Triangle(int p1, int p2, int p3) {
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
			completed = false;
		}

		public int p1;
		public int p2;
		public int p3;
		boolean completed;
		
		public Point2f getBarycentricCoordinates(Point2f p, ArrayList<Point2f> vertices)
		{
			Point2f a = vertices.get(p1);
			Point2f b = vertices.get(p2);
			Point2f c = vertices.get(p3);
			
			// Compute vectors        
			Vector2f v0 = new Vector2f(c);
			v0.sub(a);
			Vector2f v1 = new Vector2f(b);
			v1.sub(a);
			Vector2f v2 = new Vector2f(p);
			v2.sub(a);

			// Compute dot products
			float dot00 = v0.dot(v0);
			float dot01 = v0.dot(v1);
			float dot02 = v0.dot(v2);
			float dot11 = v1.dot(v1);
			float dot12 = v1.dot(v2);

			// Compute barycentric coordinates
			float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
			float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
			float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

			// Check if point is in triangle
			return new Point2f(u, v);
		}
		
		public boolean isInside(Point2f p, ArrayList<Point2f> vertices)
		{
			Point2f barycentricCoords = getBarycentricCoordinates(p, vertices);
			return (barycentricCoords.x >= -EPSILON) && (barycentricCoords.y >= -EPSILON) && (barycentricCoords.x + barycentricCoords.y < 1 + EPSILON);
		}
	}

	static class Edge {

		public Edge(int p1, int p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		int p1;
		int p2;
	}

	static class Circle {

		public Circle(float x1, float y1, float x2, float y2, float x3, float y3) {

			// Check for coincident points, three points in a line
			//assert (Math.abs(y1 - y2) < EPSILON && Math.abs(y2 - y3) < EPSILON);

			float m1, m2, mx1, mx2, my1, my2;

			if (Math.abs(y2 - y1) < EPSILON) {
				m2 = -(x3 - x2) / (y3 - y2);
				mx2 = (x2 + x3) / 2.0f;
				my2 = (y2 + y3) / 2.0f;
				xc = (x2 + x1) / 2.0f;
				yc = m2 * (xc - mx2) + my2;
			} else if (Math.abs(y3 - y2) < EPSILON) {
				m1 = -(x2 - x1) / (y2 - y1);
				mx1 = (x1 + x2) / 2.0f;
				my1 = (y1 + y2) / 2.0f;
				xc = (x3 + x2) / 2.0f;
				yc = m1 * (xc - mx1) + my1;
			} else {
				m1 = -(x2 - x1) / (y2 - y1);
				m2 = -(x3 - x2) / (y3 - y2);
				mx1 = (x1 + x2) / 2.0f;
				mx2 = (x2 + x3) / 2.0f;
				my1 = (y1 + y2) / 2.0f;
				my2 = (y2 + y3) / 2.0f;
				xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
				yc = m1 * (xc - mx1) + my1;
			}
			float dx = x2 - xc;
			float dy = y2 - yc;
			rsqr = dx * dx + dy * dy;
			r = (float) Math.sqrt(rsqr);
		}

		public boolean isInside(float xp, float yp) {
			float dx = xp - xc;
			float dy = yp - yc;
			float drsqr = dx * dx + dy * dy;

			return drsqr <= rsqr;

		}

		float xc;
		float yc;
		float r;
		float rsqr;
	}

	public static Triangle[] triangulate(List<Point2f> points) {

		// sort the list
		Collections.sort(points, new Comparator<Point2f>() {
			// returns -ve when (object1 < object2)
			// returns 0 when (object1 == object2)
			// returns +ve when (object1 > object2)
			public int compare(Point2f point1, Point2f point2) {

				if (Math.abs(point1.x - point2.x) <= EPSILON) {
					return 0;
				} else if (point1.x < point2.x) {
					return -1;
				} else if (point1.x > point2.x) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		ArrayList<Triangle> triangles = new ArrayList<Triangle>();

		// Find the maximum and minimum vertex bounds.
		// This is to allow calculation of the bounding triangle
		float xmin = points.get(0).x;
		float ymin = points.get(0).y;
		float xmax = xmin;
		float ymax = ymin;
		int number_of_points = points.size();
		for (int i = 1; i < number_of_points; i++) {
			if (points.get(i).x < xmin) {
				xmin = points.get(i).x;
			}
			if (points.get(i).x > xmax) {
				xmax = points.get(i).x;
			}
			if (points.get(i).y < ymin) {
				ymin = points.get(i).y;
			}
			if (points.get(i).y > ymax) {
				ymax = points.get(i).y;
			}
		}


		// Set up the supertriangle
		// This is a triangle which encompasses all the sample points.
		// The supertriangle coordinates are added to the end of the
		// vertex list. The supertriangle is the first triangle in
		// the triangle list.
		float dx = xmax - xmin;
		float dy = ymax - ymin;
		float dmax = (dx > dy) ? dx : dy;
		float xmid = (xmax + xmin) / 2.0f;
		float ymid = (ymax + ymin) / 2.0f;
		points.add(new Point2f(xmid - 20 * dmax, ymid - dmax));
		points.add(new Point2f(xmid, ymid + 20 * dmax));
		points.add(new Point2f(xmid + 20 * dmax, ymid - dmax));

		Triangle supertriangle =
			new Triangle(number_of_points, number_of_points + 1, number_of_points + 2);
		triangles.add(supertriangle);

		//Include each point one at a time into the existing mesh
		//   for (i=0;i<number_of_points;i++) {
		for (int i = 0; i < number_of_points; i++) {

			float xp = points.get(i).x;
			float yp = points.get(i).y;

			// Set up the edge buffer.
			// If the point (xp,yp) lies inside the circumcircle then the
			// three edges of that triangle are added to the edge buffer
			// and that triangle is removed.
			ArrayList<Edge> edges = new ArrayList<Edge>();
			for (int j = 0; j < triangles.size(); j++) {
				Triangle triangle = triangles.get(j);

				if (!triangle.completed) {

					Circle circle = new Circle(points.get(triangle.p1).x, points.get(triangle.p1).y,
																		 points.get(triangle.p2).x, points.get(triangle.p2).y,
																		 points.get(triangle.p3).x, points.get(triangle.p3).y);

					if (circle.xc + circle.r < xp) {
						triangle.completed = true;
					}

					if (circle.isInside(xp, yp)) {
						Edge edge1 = new Edge(triangle.p1, triangle.p2);
						Edge edge2 = new Edge(triangle.p2, triangle.p3);
						Edge edge3 = new Edge(triangle.p3, triangle.p1);
						edges.add(edge1);
						edges.add(edge2);
						edges.add(edge3);
						triangles.remove(triangle);
						j--;
					}
				}
			}

			// Tag multiple edges
			// Note: if all triangles are specified anticlockwise then all
			//       interior edges are opposite pointing in direction.
			Edge[] edges_array = edges.toArray(new Edge[0]);
			for (int j = 0; j < edges_array.length - 1; j++) {
				Edge edgej = edges_array[j];
				for (int k = j + 1; k < edges_array.length - 1; k++) {
					Edge edgek = edges_array[k];
					if ((edgej.p1 == edgek.p2) && (edgej.p2 == edgek.p1)) {
						edges.remove(edgej);
						edges.remove(edgek);
					}
					//  Shouldn't need the following, see note above
					if ((edgej.p1 == edgek.p1) && (edgej.p2 == edgek.p2)) {
						edges.remove(edgej);
						edges.remove(edgek);
					}
				}
			}

			// Form new triangles for the current point
			// Skipping over any tagged edges.
			// All edges are arranged in clockwise order.
			for (int j = 0; j < edges.size(); j++) {
				Edge edgej = edges.get(j);
				Triangle new_triangle = new Triangle(edgej.p1, edgej.p2, i);
				triangles.add(new_triangle);
			}
		}

		// Remove triangles with supertriangle vertices
		// These are triangles which have a vertex number greater than number_of_points
		for (int i = 0; i < triangles.size(); i++) {
			Triangle triangle = triangles.get(i);
			if (triangle.p1 >= number_of_points ||
					triangle.p2 >= number_of_points ||
					triangle.p3 >= number_of_points) {
				triangles.remove(triangle);
				i--;
			}
		}

		return triangles.toArray(new Triangle[0]);
	}
	
	public static Triangle getTriangle(Point2f p, Triangle[] triangles, ArrayList<Point2f> positions)
	{
		for(DelaunayTriangulation.Triangle currentT: triangles)
		{
			if(currentT.isInside(p, positions))
			{
				return currentT;
			}
		}
		return null;
	}
}