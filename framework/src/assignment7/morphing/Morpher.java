package assignment7.morphing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.MorphHalfEdgeStructure;
import meshes.Vertex;

public class Morpher {

	private List<HalfEdgeStructure> structures, diffs;
	private List<String> names;
	private HalfEdgeStructure average;
	private MorphHalfEdgeStructure current;
	private AverageMode averageMode;

	private List<Weighting> weightings;
	/**
	 * Enables/disables color highlighting of diff
	 */
	private boolean diffEneabled;

	public Morpher(AverageMode avgMode) {
		this.structures = new ArrayList<>();
		this.diffs = new ArrayList<>();
		this.names = new ArrayList<>();
		this.averageMode = avgMode;
		this.weightings = new ArrayList<>();
		this.diffEneabled = true;
	}

	public List<String> getFigureNames() {
		return names;
	}

	public MorphHalfEdgeStructure getCurrent() {
		return current;
	}

	public void setWeighting(int index, float weight) {
		Weighting weighting = weightings.get(index);
		weighting.subtract(current, diffs.get(index));
		weighting.setWeight(weight);
		weighting.add(current, diffs.get(index));
		updateCurrentDiff();
	}

	public void setWeightingFunction(int index, String function) {
		Weighting weighting = weightings.get(index);
		weighting.subtract(current, diffs.get(index));
		weighting.setFunction(function);
		weighting.add(current, diffs.get(index));
		updateCurrentDiff();
	}

	public void setDiffEnabled(boolean enabled) {
		this.diffEneabled = enabled;
		this.updateCurrentDiff();
	}

	private void updateCurrentDiff() {
		ArrayList<Vertex> avgVerts = average.getVertices();
		List<Vertex> currentVerts = current.getVertices();
		List<Float> currentDiffs = current.getDiffs();
		for (int i = 0; i < currentVerts.size(); i++) {
			if (!diffEneabled) { // XXX: Hack...
				currentDiffs.set(i, 0f);
				continue;
			}

			currentDiffs.set(
					i,
					currentVerts.get(i).getPos()
							.distance(avgVerts.get(i).getPos()));
		}
	}

	public void add(HalfEdgeStructure hs, String name) {
		this.names.add(name);
		this.structures.add(hs);
		this.diffs.add(new HalfEdgeStructure(hs));
		this.weightings.add(new Weighting());
		updateAverage();
	}

	private void updateAverage() {
		assert !structures.isEmpty();
		this.average = new HalfEdgeStructure(structures.get(0));
		for (int i = 0; i < structures.get(0).getVertices().size(); i++) {
			Point3f avg = averagePosition(i);
			this.average.getVertices().get(i).getPos().set(avg);

			// update diffs
			for (int j = 0; j < structures.size(); j++) {
				HalfEdgeStructure structure = structures.get(j);
				HalfEdgeStructure diff = diffs.get(j);

				Vector3f diffVec = new Vector3f(structure.getVertices().get(i)
						.getPos());
				diffVec.sub(avg);
				diff.getVertices().get(i).getPos().set(diffVec);
			}
		}
		this.current = new MorphHalfEdgeStructure(average);
	}

	private Point3f averagePosition(int i) {
		if (averageMode == AverageMode.MEAN)
			return meanPosition(i);
		else if (averageMode == AverageMode.MEDIAN)
			return medianPosition(i);
		throw new RuntimeException("Invalid AverageMode");
	}

	private Point3f meanPosition(int i) {
		int count = 0;
		Point3f avg = new Point3f();
		for (HalfEdgeStructure structure : structures) {
			avg.add(structure.getVertices().get(i).getPos());
			count++;
		}
		avg.scale(1f / count);
		return avg;
	}

	private Point3f medianPosition(int i) {
		List<Float> xVals = new ArrayList<>();
		List<Float> yVals = new ArrayList<>();
		List<Float> zVals = new ArrayList<>();
		for (HalfEdgeStructure structure : structures) {
			Point3f pos = structure.getVertices().get(i).getPos();
			xVals.add(pos.x);
			yVals.add(pos.y);
			zVals.add(pos.z);
		}
		Collections.sort(xVals);
		Collections.sort(yVals);
		Collections.sort(zVals);
		int quantile = (xVals.size() - 1) / 2;
		return new Point3f(xVals.get(quantile), yVals.get(quantile),
				zVals.get(quantile));
	}

	public static class AverageMode {
		public static final AverageMode MEAN = new AverageMode();
		public static final AverageMode MEDIAN = new AverageMode();
	}

}
