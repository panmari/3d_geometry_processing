package meshes;

import java.util.ArrayList;
import java.util.List;

public class MorphHalfEdgeStructure extends HalfEdgeStructure {

	private List<Float> diffs;

	/**
	 * Do not use this.
	 */
	protected MorphHalfEdgeStructure() {
	}

	public MorphHalfEdgeStructure(HalfEdgeStructure hes) {
		super(hes);
		this.diffs = new ArrayList<>(this.getVertices().size());
		for (int i = 0; i < this.getVertices().size(); i++) {
			diffs.add(0f);
		}
	}

	public List<Float> getDiffs() {
		return diffs;
	}
}
