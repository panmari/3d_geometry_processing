package assignment7.conformalMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import sparse.CSRMatrix;
import sparse.MATLAB;
import sparse.solver.SciPySolver;
import sparse.solver.Solver;

public class ConformalMapper {
	private ArrayList<Point2f> texcoords;
	private HashMap<Integer, Point2f> labels;
	private HalfEdgeStructure hs;
	
	private static float userweight = 100;
	
	public ConformalMapper(HalfEdgeStructure hs, HashMap<Integer, Point2f> labels)
	{
		this.hs = hs;
		this.labels = labels;
	}
	
	public ArrayList<Point2f> get()
	{
		return texcoords;
	}
	
	public void compute()
	{
		CSRMatrix m = new CSRMatrix(0, 2 * hs.getVertices().size());
		for(Face f: hs.getFaces())
		{
			m.append(getMatrix(f), (float) (1.f / Math.max(Math.sqrt(f.getArea()), 0.00000)));
//			System.out.println(f.area());
		}
		ArrayList<Float> rhs = new ArrayList<Float>(Collections.nCopies(m.nRows, 0.f));
		
		for (Integer index : labels.keySet()){
			Point2f coords = labels.get(index);
			m.addRow();
			m.createLastRowEntry(index, userweight);
			rhs.add(userweight * coords.x);
			m.addRow();
			m.createLastRowEntry(index + hs.getVertices().size(), userweight);
			rhs.add(userweight * coords.y);
		}
		System.out.println("Matrix built, starting to solve");

		Solver s = new MATLAB("confmap");
		ArrayList<Float> uv = new ArrayList<Float>();
		s.solve(m, rhs, uv);
		
		texcoords = new ArrayList<Point2f>();
		texcoords.ensureCapacity(hs.getVertices().size());
		for(int i = 0; i < hs.getVertices().size(); i++)
		{
			Point2f p = new Point2f(uv.get(i), uv.get(i + hs.getVertices().size()));
			texcoords.add(p);
		}
		
		Point2f max = null, min = null;
		for(Point2f p: texcoords)
		{
			if(max == null) max = new Point2f(p);
			if(min == null) min = new Point2f(p);
			
			if(p.x > max.x) max.x = p.x;
			if(p.y > max.y) max.y = p.y;
			
			if(p.x < min.x) min.x = p.x;
			if(p.y < min.y) min.y = p.y;
		}
		
		for(Point2f p: texcoords)
		{
			float maxDiff = Math.max(max.x - min.x, max.y - min.y);
			if(max.x != min.x) p.x = (p.x - min.x) / maxDiff;
			if(max.y != min.y) p.y = (p.y - min.y) / maxDiff;
		}
	}
	
	/**
	 * TODO: do this more efficient than with CSRMatrix :-/
	 * @param f
	 * @return
	 */
	public CSRMatrix getMatrix(Face f)
	{
		CSRMatrix out = new CSRMatrix(2, 2 * hs.getVertices().size());
		HalfEdge e_ij = f.getHalfEdge();
		HalfEdge e_ik = e_ij.getPrev().getOpposite();
		Vector3f x = e_ij.asVector();
		Vector3f n = new Vector3f();
		n.cross(x, e_ik.asVector());
		Vector3f y = new Vector3f();
		y.cross(n, x);
		
		/*if(x.lengthSquared() == 0 || y.lengthSquared() == 0 || n.lengthSquared() == 0)
		{
			CSRMatrix m = new CSRMatrix(0, 2 * hs.getVertices().size());
			if(x.lengthSquared() == 0)
			{
				m.addRow();
				m.setLastRow(e_ij.start().index, 1);
				m.setLastRow(e_ij.end().index, -1);
				m.addRow();
				m.setLastRow(e_ij.start().index + hs.getVertices().size(), 1);
				m.setLastRow(e_ij.end().index + hs.getVertices().size(), -1);
			}
			if(y.lengthSquared() == 0)
			{
				m.addRow();
				m.setLastRow(e_ik.start().index, 1);
				m.setLastRow(e_ik.end().index, -1);
				m.addRow();
				m.setLastRow(e_ik.start().index + hs.getVertices().size(), 1);
				m.setLastRow(e_ik.end().index + hs.getVertices().size(), -1);
			}
			return m;
		}*/
		x.normalize();
		y.normalize();
		n.normalize();
		
		Vector3f x_i = new Vector3f(0, 0, 0);
		Vector3f x_j = new Vector3f(e_ij.asVector().length(), 0, 0);
		Vector3f x_k = new Vector3f(e_ik.asVector().dot(x), e_ik.asVector().dot(y), 0);
		
		CSRMatrix m_t = new CSRMatrix(2, 3);
		m_t.set(0, 0, x_j.y - x_k.y);
		m_t.set(0, 1, x_k.y - x_i.y);
		m_t.set(0, 2, x_i.y - x_j.y);
		m_t.set(1, 0, x_k.x - x_j.x);
		m_t.set(1, 1, x_i.x - x_k.x);
		m_t.set(1, 2, x_j.x - x_i.x);
		m_t.scale(1.f / 2.f);
		
		CSRMatrix m_t_rot = new CSRMatrix(2, 3);
		CSRMatrix rot = new CSRMatrix(2, 2);
		rot.set(0, 1, 1);
		rot.set(1, 0, -1);
		rot.mult(m_t, m_t_rot);
		
		int pos_i = e_ij.start().index;
		int pos_j = e_ij.end().index;
		int pos_k = e_ik.end().index;
		
		out.set(0, pos_i, m_t_rot.get(0, 0));
		out.set(1, pos_i, m_t_rot.get(1, 0));
		out.set(0, pos_j, m_t_rot.get(0, 1));
		out.set(1, pos_j, m_t_rot.get(1, 1));
		out.set(0, pos_k, m_t_rot.get(0, 2));
		out.set(1, pos_k, m_t_rot.get(1, 2));

		out.set(0, pos_i + hs.getVertices().size(), m_t.get(0, 0));
		out.set(1, pos_i + hs.getVertices().size(), m_t.get(1, 0));
		out.set(0, pos_j + hs.getVertices().size(), m_t.get(0, 1));
		out.set(1, pos_j + hs.getVertices().size(), m_t.get(1, 1));
		out.set(0, pos_k + hs.getVertices().size(), m_t.get(0, 2));
		out.set(1, pos_k + hs.getVertices().size(), m_t.get(1, 2));
		
		return out;
	}
}
