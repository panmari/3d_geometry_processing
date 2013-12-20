package assignment7.morphing;

import meshes.HalfEdgeStructure;
import myutils.HESUtil;
import assignment7.morphing.UI.MorphDisplay;

public class SampleMorphs {

	private static String[] sampleFiles = { "faces/face01.obj",
			// "faces/face02.obj", "faces/face03.obj", "faces/face04.obj",
			// "faces/face05.obj", "faces/face06.obj", "faces/face07.obj",
			// "faces/face08.obj", "faces/face09.obj", "faces/face10.obj",
			"faces/face11.obj" };

	public static void main(String[] args) {
		Morpher morpher = new Morpher(Morpher.AverageMode.MEAN);
		for (String file : sampleFiles) {
			HalfEdgeStructure hs = HESUtil.createStructure(file);
			morpher.add(hs, file);
		}

		new MorphDisplay(morpher);
	}

}
