package assignment7.conformalMap;

import glWrapper.GLHalfEdgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import meshes.reader.ObjWriter;
import openGL.MyDisplay;
import openGL.gl.GLDisplayable.Semantic;
import assignment7.conformalMap.delauny.DelaunayTriangulation;
import assignment7.conformalMap.delauny.DelaunayTriangulation.Triangle;

public class ConformalMapDemo {

	private static HalfEdgeStructure hs;
	private static LinkedHashMap<Integer, Point2f> labels;
	private static LinkedHashMap<String, Integer> allLabels;
	private static LinkedHashMap<String, Integer> allLabelsRef;
	private static ArrayList<Point2f> unmorphedTexCoords;
	private static WireframeMesh delaunayWf;
	private static MyDisplay d;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception
	{
		String refName = "aaron";
		String[] names = {"cedric", "gian", "michael", "michele", "stefan", "tiziano"};
		System.out.println("Reading obj...");
		
		d = new MyDisplay();
		/*for(String name: names)
		{
			WireframeMesh m = ObjReader.read("./objs/faces/aligned/" + name + "_disk_aligned.obj", false);
			GLWireframeMesh glwf = new GLWireframeMesh(m);
			glwf.setName(name);
			glwf.configurePreferredShader("shaders/trimesh_flatColor3f.vert", "shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
			d.addToDisplay(glwf);
		}
		WireframeMesh m = ObjReader.read("./objs/faces/aligned/" + refName + "_disk_aligned.obj", false);
		GLWireframeMesh glwf = new GLWireframeMesh(m);
		glwf.setName(refName);
		glwf.configurePreferredShader("shaders/trimesh_flatColor3f.vert", "shaders/trimesh_flatColor3f.frag", "shaders/trimesh_flatColor3f.geom");
		d.addToDisplay(glwf);*/
		
		ArrayList<Point2f> refTexcoords = compute(refName);
		unmorphedTexCoords = new ArrayList<Point2f>(refTexcoords);
		ArrayList<Point2f> refPos = getLabelCoords(refTexcoords);
		writeObj(refName, refTexcoords);
		display("Reference", refTexcoords);
		
		for(String name: names)
		{
			ArrayList<Point2f> texcoords = compute(name);
			unmorphedTexCoords = new ArrayList<Point2f>(texcoords);
			delaunayWf = morph(texcoords, refPos, getLabelCoords(texcoords));
			writeObj(name, texcoords);
			display(name, texcoords);
		}
		
		
	}
	
	public static ArrayList<Point2f> getLabelCoords(ArrayList<Point2f> texcoords)
	{
		ArrayList<Point2f> pos = new ArrayList<>();
		pos.ensureCapacity(allLabels.size());
		for(String label: allLabelsRef.keySet())
		{
			//Need to get the current labels in the order of allLabelsRef
			pos.add(new Point2f(texcoords.get(allLabels.get(label))));
		}
		return pos;
	}
	
	public static WireframeMesh morph(ArrayList<Point2f> texcoords, ArrayList<Point2f> refpos, ArrayList<Point2f> featurepos)
	{
		ArrayList<Point2f> refposM = new ArrayList<Point2f>(refpos);
		refposM.add(new Point2f(0, 0));
		refposM.add(new Point2f(0, 1));
		refposM.add(new Point2f(1, 0));
		refposM.add(new Point2f(1, 1));
		System.out.println("[");
		for(Point2f p: refposM)
		{
			System.out.println("" + p.x + "," + p.y + ";");
			
		}
		System.out.println("]");

		featurepos.add(new Point2f(0, 0));
		featurepos.add(new Point2f(0, 1));
		featurepos.add(new Point2f(1, 0));
		featurepos.add(new Point2f(1, 1));
		
//		Triangle[] delaunay = DelaunayTriangulation.triangulate(featurepos);
		WireframeMesh wf = null;
		try {
			wf = ObjReader.read("objs/faces/aaron_features_delaunay.obj", false);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		Triangle[] delaunay = new Triangle[wf.faces.size()];
		int in = 0;
		for(int[] face: wf.faces)
		{
			delaunay[in++] = new Triangle(face[0], face[1], face[2]);
		}
		in = 0;
		for(Point2f p: featurepos)
		{
			wf.vertices.set(in++, new Point3f(p.x, p.y, 0));
		}
//		GLWireframeMesh glwf = new GLWireframeMesh(wf);
//		glwf.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
//		PointCloud pc = new PointCloud();
//		pc.points = new ArrayList<Point3f>(wf.vertices);
//		GLPointCloud glpc = new GLPointCloud(pc);
//		MyDisplay d = new MyDisplay();
//		d.addToDisplay(glwf);
//		d.addToDisplay(glpc);
		
		int i = 0;
		for(Point2f p: texcoords)
		{
			Triangle t = DelaunayTriangulation.getTriangle(p, delaunay, featurepos);
			assert(t != null);
			Point2f barycentricCoordinates = t.getBarycentricCoordinates(p, featurepos);
			Point2f newPos1 = new Point2f(refposM.get(t.p1));
			Point2f newPos2 = new Point2f(refposM.get(t.p2));
			Point2f newPos3 = new Point2f(refposM.get(t.p3));
			newPos1.scale(1 - barycentricCoordinates.x - barycentricCoordinates.y);
			newPos2.scale(barycentricCoordinates.y);
			newPos3.scale(barycentricCoordinates.x);
			Point2f newPos = new Point2f();
			newPos.add(newPos1);
			newPos.add(newPos2);
			newPos.add(newPos3);
			texcoords.set(i++, newPos);
		}
		
		return wf;
	}
	
	public static ArrayList<Point2f> compute(String name) throws Exception
	{
		WireframeMesh wf = ObjReader.read("./objs/faces/aligned/" + name + "_disk_aligned.obj", false);
		hs = new HalfEdgeStructure();
		hs.init(wf);
//		GLWireframeMesh glwf = new GLWireframeMesh(wf);
//		glwf.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		
		System.out.println("Reading labels");
		LabelReader l = new LabelReader("labels/faces/" + name + "_disk_remeshed.lab", "labels/faces/faces.txc");
		labels = l.read();
		
//		LabelReader boundaryLabels = new LabelReader("out/" + name + "_boundary.lbl", "out/" + name + "_boundary.txc");
//		labels.putAll(boundaryLabels.read());
		
		ConformalMapper mapper = new ConformalMapper(hs, labels);
		mapper.compute();
		System.out.println("Done, writing OBJ");
		
		LabelReader lAll = new LabelReader("labels/faces/" + name + "_disk_remeshed.lab", null);
		allLabels = lAll.readIndices();
		if(allLabelsRef == null) allLabelsRef = allLabels;
		
		return mapper.get();
	}
	
	public static void writeObj(String name, ArrayList<Point2f> texcoords) throws Exception
	{
		ObjWriter writer = new ObjWriter(name + "_tex.obj");
		writer.writeTexcoord(texcoords);
		writer.write(hs);
		writer.close();
	}
	
	public static void display(String name, ArrayList<Point2f> texcoords)
	{
		GLConstraints glc = new GLConstraints(hs, getLabelCoords(texcoords));
		
		GLHalfEdgeStructure glhs = new GLHalfEdgeStructure(hs);
		glhs.addElement2D(texcoords, "position");
		glhs.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		glhs.setName(name + ": texture map");
		
		GLHalfEdgeStructure glhs2 = new GLHalfEdgeStructure(hs);
		glhs2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
		glhs2.setName(name + ": mesh");
		
		if(delaunayWf != null)
		{
			GLWireframeMesh glwf2 = new GLWireframeMesh(delaunayWf);
			glwf2.configurePreferredShader("shaders/wiremesh.vert", "shaders/wiremesh.frag", "shaders/wiremesh.geom");
			glwf2.setName(name + ": delaunay triangulation");
			d.addToDisplay(glwf2);
		}
		
		
		
		d.addToDisplay(glhs);
		d.addToDisplay(glhs2);
		d.addToDisplay(glc);
		
	}

}
