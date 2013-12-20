package assignment7.morphing;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import assignment7.morphing.UI.MorphPanel;
import expr.Expr;
import expr.Parser;
import expr.SyntaxException;
import expr.Variable;

public class Weighting {

	public static final String VAR_DIFF = "d";

	private float weight;
	private String function;
	private Map<Integer, Float> functionCache;

	private Expr expr;

	public Weighting() {
		this.weight = MorphPanel.INIT / MorphPanel.SCALE;
		this.function = VAR_DIFF;
		this.functionCache = new HashMap<>();
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	/**
	 * Subtract values in second structure from first, weighted by this
	 * weighting object.
	 * 
	 * @param structure
	 * @param diff
	 */
	public void subtract(HalfEdgeStructure structure, HalfEdgeStructure diff) {
		applyDiff(structure, diff, false);
	}

	/**
	 * Add values in second structure from first, weighted by this weighting
	 * object.
	 * 
	 * @param structure
	 * @param diff
	 */
	public void add(HalfEdgeStructure structure, HalfEdgeStructure diff) {
		applyDiff(structure, diff, true);
	}

	/**
	 * Apply a weighted diff, either add or subtract.
	 * 
	 * @param structure
	 * @param diff
	 * @param additive
	 */
	private void applyDiff(HalfEdgeStructure structure, HalfEdgeStructure diff,
			boolean additive) {
		if (!function.equals(VAR_DIFF))
			prepareEvaluation();

		for (int i = 0; i < structure.getVertices().size(); i++) {
			Point3f pos = structure.getVertices().get(i).getPos();
			Point3f diffVec = diff.getVertices().get(i).getPos();
			Vector3f weightedDiff = new Vector3f(diffVec);

			if (!function.equals(VAR_DIFF)) {
				weightedDiff.normalize();
				float functionScale;
				if (functionCache.containsKey(i))
					functionScale = functionCache.get(i);
				else {
					functionScale = evaluateFunction(pos, new Vector3f(diffVec));
					functionCache.put(i, functionScale);
				}
				weightedDiff.scale(functionScale);
			}

			weightedDiff.scale(weight);

			if (additive)
				pos.add(weightedDiff);
			else
				pos.sub(weightedDiff);
		}
	}

	private void prepareEvaluation() {
		try {
			expr = Parser.parse(function);
		} catch (SyntaxException e) {
			System.err.println(e.explain());
		}
	}

	private float evaluateFunction(Point3f pos, Vector3f diffVec) {
		if (expr == null)
			return diffVec.length();

		Variable x = Variable.make("x");
		Variable y = Variable.make("y");
		Variable z = Variable.make("z");
		Variable d = Variable.make(VAR_DIFF);
		x.setValue(pos.x);
		y.setValue(pos.y);
		z.setValue(pos.z);
		d.setValue(diffVec.length());

		return (float) expr.value();
	}

	public void setFunction(String function) {
		this.function = function;
		this.functionCache.clear();
	}
}
