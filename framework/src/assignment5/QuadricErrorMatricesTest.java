package assignment5;

import static org.junit.Assert.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;

import org.junit.Before;
import org.junit.Test;

public class QuadricErrorMatricesTest {

	private HalfEdgeStructure hs;

	@Before
	public void setUp() throws Exception {
		try {
			WireframeMesh m = ObjReader.read("objs/buddha.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void qemShouldNotContainNans() {
		for(Face f: hs.getFaces()) {
			Vector4f v = new Vector4f(1,1,1,1);
			Matrix4f m = f.getQuadricErrorMatrix();//.transform(v);
			//TODO: do some better testing
			assertFalse(m.toString(), m.epsilonEquals(new Matrix4f(), 0.01f));
		}
	}

}
