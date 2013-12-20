package glWrapper;

import java.util.List;

import meshes.MorphHalfEdgeStructure;

public class GLMorphHEStructure extends GLUpdatableHEStructure {

	private MorphHalfEdgeStructure mHES;

	public GLMorphHEStructure(MorphHalfEdgeStructure e) {
		super(e);

		this.mHES = e;

		List<Float> diffList = e.getDiffs();
		float[] diffs = new float[diffList.size()];

		copyToArray(diffList, diffs);
		this.addElement(diffs, Semantic.USERSPECIFIED, 1, "diff");
	}

	@Override
	public void updatePosition() {
		super.updatePosition();

		float[] data = getDataBuffer("diff");
		copyToArray(mHES.getDiffs(), data);
		scheduleUpdate("diff");
	}

	private void copyToArray(List<Float> diffList, float[] diffs) {
		for (int i = 0; i < diffs.length; i++) {
			diffs[i] = diffList.get(i);
		}
	}
}
