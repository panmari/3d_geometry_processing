package myutils;

import java.io.IOException;

import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

public class HESUtil {
	public static HalfEdgeStructure createStructure(String filename) {
		try {
			WireframeMesh mesh = ObjReader.read("objs/" + filename, true);
			HalfEdgeStructure hs = new HalfEdgeStructure();
			hs.init(mesh);
			return hs;
		} catch (MeshNotOrientedException | DanglingTriangleException
				| IOException e) {
			throw new RuntimeException(e);
		}
	}
}
