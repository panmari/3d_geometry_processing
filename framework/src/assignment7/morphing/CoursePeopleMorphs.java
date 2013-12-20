package assignment7.morphing;

import meshes.HalfEdgeStructure;
import myutils.HESUtil;
import assignment7.morphing.UI.MorphDisplay;

public class CoursePeopleMorphs {

	private static String[] names = { "Aaron", "Cedric", "Gian", "Michael",
			"Michelle", "Stefan", "Tiziano", };
	private static String[] files = { "remeshed/aaron_remeshed.obj",
			"remeshed/cedric_remeshed.obj",
			"remeshed/gian_remeshed.obj",
			"remeshed/michael_remeshed.obj",
			"remeshed/michele_remeshed.obj",
			"remeshed/stefan_remeshed.obj",
			"remeshed/tiziano_remeshed.obj" };

	public static void main(String[] args) {
		Morpher morpher = new Morpher(Morpher.AverageMode.MEAN);
		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			String name = names[i];
			System.out.println("Loading " + name + "...");
			HalfEdgeStructure hs = HESUtil.createStructure(file);
			morpher.add(hs, name);
		}

		new MorphDisplay(morpher);
	}
}
